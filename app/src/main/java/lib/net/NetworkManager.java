// lib/net/NetworkManager.java
package lib.net;

import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import lib.net.command.ACommand;
import lib.net.connection.HttpUrlConnectionFactory;
import lib.net.connection.IHttpConnection;
import lib.net.connection.IHttpConnectionFactory;
import lib.net.interceptor.Interceptor;
import lib.net.parser.GsonResponseParser;
import lib.net.parser.IResponseParser;
import lib.net.util.NetworkConfig;
import lib.net.util.UrlBuilder;

/**
 * Future-only NetworkManager
 * - enqueueFuture(...): CancellableFuture<T>
 * - enqueueFutureResult(...): CancellableFuture<NetResult<T>>
 * Sonuçlar main thread'de tamamlanır.
 */
public class NetworkManager {

    private final ExecutorService executor;
    private final BlockingQueue<RequestTask<?>> requestQueue;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final IHttpConnectionFactory connectionFactory;
    private final ResponseHandler responseHandler;
    private final ConcurrentHashMap<ACommand, RequestTask<?>> activeTasks = new ConcurrentHashMap<>();
    private final AtomicBoolean shuttingDown = new AtomicBoolean(false);
    private final List<Interceptor> globalInterceptors;

    private NetworkManager(IHttpConnectionFactory factory,
                           ResponseHandler handler,
                           int threadPoolSize,
                           int queueCapacity,
                           List<Interceptor> interceptors) {
        this.connectionFactory = (factory != null) ? factory : new HttpUrlConnectionFactory();
        this.responseHandler = (handler != null) ? handler : new ResponseHandler(new GsonResponseParser());
        this.executor = Executors.newFixedThreadPool(threadPoolSize);
        this.requestQueue = new LinkedBlockingQueue<>(queueCapacity);
        this.globalInterceptors = (interceptors != null) ? new ArrayList<>(interceptors) : new ArrayList<>();
        startWorkers(threadPoolSize);
    }

    /* ======================= PUBLIC API (FUTURE) ======================= */

    public <T> CancellableFuture<NetResult<T>> enqueueFutureResult(String baseUrl,
                                                                   ACommand command,
                                                                   Type responseType) {
        CancellableFuture<NetResult<T>> f = new CancellableFuture<>();
        f.attach(command); // cancel() -> command.cancel()
        // Global interceptor'ları komuta tak
        if (globalInterceptors != null) {
            for (Interceptor it : globalInterceptors) {
                if (it != null) command.addInterceptor(it);
            }
        }
        RequestTask<T> task = RequestTask.forResult(baseUrl, command, responseType, f);
        if (!requestQueue.offer(task)) {
            completeOnMain(f, new NetResult.Error<T>(
                    new IOException("Queue is full"), 429, "Too Many Requests"));
            return f;
        }
        activeTasks.put(command, task);
        return f;
    }

    public <T> CancellableFuture<T> enqueueFuture(String baseUrl,
                                                  ACommand command,
                                                  Type responseType) {
        CancellableFuture<T> f = new CancellableFuture<>();
        f.attach(command);
        if (globalInterceptors != null) {
            for (Interceptor it : globalInterceptors) {
                if (it != null) command.addInterceptor(it);
            }
        }
        RequestTask<T> task = RequestTask.forValue(baseUrl, command, responseType, f);
        if (!requestQueue.offer(task)) {
            completeExceptionOnMain(f, new HttpException(
                    new IOException("Queue is full"), 429, "Too Many Requests"));
            return f;
        }
        activeTasks.put(command, task);
        return f;
    }

    /* ======================= WORKERS ======================= */

    private void startWorkers(int workerCount) {
        for (int i = 0; i < workerCount; i++) {
            executor.execute(this::workerLoop);
        }
    }

    private void workerLoop() {
        while (!shuttingDown.get()) {
            RequestTask<?> task = null;
            try {
                task = requestQueue.take();

                try {
                    // Her attempt'te yeni connection
                    final String fullUrl = UrlBuilder.build(task.baseUrl, task.command);
                    java.util.function.Supplier<IHttpConnection> supplier = new java.util.function.Supplier<IHttpConnection>() {
                        @Override
                        public IHttpConnection get() {
                            try {
                                return connectionFactory.createConnection(fullUrl);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    };

                    // Düşük seviye (String) sonuç
                    NetResult<String> raw = task.command.execute(supplier);

                    // Tipli sonuca çevir
                    NetResult<?> finalResult = responseHandler.handle(raw, task.responseType);

                    // Tür güvenli teslim
                    deliver(task, finalResult);

                } catch (Exception procEx) {
                    // İşlem/parse/execute sırasında hata -> error sonucu post et
                    if (task != null) {
                        NetResult<?> err = new NetResult.Error<Object>(
                                procEx, -1, msg(procEx));
                        deliver(task, err);
                    }
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            } catch (Exception ignore) {
                // take() öncesi/sırası — elde task yok, sadece loglamak isteyebilirsiniz
            } finally {
                if (task != null) {
                    activeTasks.remove(task.command);
                }
            }
        }
    }

    private static String msg(Throwable t) {
        return (t == null || t.getMessage() == null) ? "Unexpected error" : t.getMessage();
    }

    @SuppressWarnings("unchecked")
    private <T> void deliver(RequestTask<?> rawTask, NetResult<?> rawResult) {
        RequestTask<T> task = (RequestTask<T>) rawTask;

        if (task.kind == RequestTask.Kind.RESULT) {
            // Future tipi: CancellableFuture<NetResult<T>>
            NetResult<T> typed = (NetResult<T>) rawResult;
            completeOnMain(task.resultFuture, typed);
        } else {
            // Future tipi: CancellableFuture<T>
            if (rawResult.isSuccess()) {
                T val = ((NetResult.Success<T>) rawResult).Data();
                completeOnMain(task.valueFuture, val);
            } else {
                NetResult.Error<T> e = (NetResult.Error<T>) rawResult;
                completeExceptionOnMain(task.valueFuture,
                        new HttpException(e.getException(), e.getResponseCode(), e.getErrorBody()));
            }
        }
    }

    /* ======================= MAIN-THREAD HELPERS ======================= */

    private <T> void completeOnMain(final CancellableFuture<T> f, final T value) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!f.isDone()) f.complete(value);
            }
        });
    }

    private <T> void completeExceptionOnMain(final CancellableFuture<T> f, final Throwable ex) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!f.isDone()) f.completeExceptionally(ex);
            }
        });
    }

    /* ======================= TASK MODEL ======================= */

    private static final class RequestTask<T> {
        enum Kind {RESULT, VALUE}

        final String baseUrl;
        final ACommand command;
        final Type responseType;
        final Kind kind;
        final CancellableFuture<NetResult<T>> resultFuture;
        final CancellableFuture<T> valueFuture;

        private RequestTask(String baseUrl,
                            ACommand command,
                            Type responseType,
                            Kind kind,
                            CancellableFuture<NetResult<T>> rf,
                            CancellableFuture<T> vf) {
            this.baseUrl = baseUrl;
            this.command = command;
            this.responseType = responseType;
            this.kind = kind;
            this.resultFuture = rf;
            this.valueFuture = vf;
        }

        static <T> RequestTask<T> forResult(String b, ACommand c, Type t, CancellableFuture<NetResult<T>> f) {
            return new RequestTask<>(b, c, t, Kind.RESULT, f, null);
        }

        static <T> RequestTask<T> forValue(String b, ACommand c, Type t, CancellableFuture<T> f) {
            return new RequestTask<>(b, c, t, Kind.VALUE, null, f);
        }
    }

    /* ======================= BUILDER ======================= */

    public static class Builder {
        private IHttpConnectionFactory factory = new HttpUrlConnectionFactory();
        private IResponseParser parser = new GsonResponseParser();
        private int threadPoolSize = NetworkConfig.THREAD_POOL_SIZE;
        private int queueCapacity = NetworkConfig.QUEUE_CAPACITY;
        private final List<Interceptor> interceptors = new ArrayList<>();

        public Builder factory(IHttpConnectionFactory f) {
            if (f != null) this.factory = f;
            return this;
        }

        public Builder parser(IResponseParser p) {
            if (p != null) this.parser = p;
            return this;
        }

        public Builder threadPoolSize(int size) {
            if (size > 0) this.threadPoolSize = size;
            return this;
        }

        public Builder queueCapacity(int cap) {
            if (cap > 0) this.queueCapacity = cap;
            return this;
        }

        public Builder addInterceptor(Interceptor i) {
            if (i != null) this.interceptors.add(i);
            return this;
        }

        public Builder addInterceptors(List<Interceptor> list) {
            if (list != null) for (Interceptor i : list) if (i != null) this.interceptors.add(i);
            return this;
        }

        public NetworkManager build() {
            ResponseHandler rh = new ResponseHandler(parser);
            return new NetworkManager(factory, rh, threadPoolSize, queueCapacity, interceptors);
        }
    }
}

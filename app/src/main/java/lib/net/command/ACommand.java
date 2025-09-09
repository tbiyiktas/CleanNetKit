// lib/net/command/ACommand.java
package lib.net.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import lib.net.NetResult;
import lib.net.RequestHandle;
import lib.net.connection.IHttpConnection;
import lib.net.interceptor.Interceptor;
import lib.net.strategy.BackoffStrategy;
import lib.net.strategy.BodyWriter;
import lib.net.strategy.ErrorHandler;
import lib.net.strategy.MethodStrategy;
import lib.net.strategy.RequestConfigurator;
import lib.net.strategy.ResponseReader;
import lib.net.strategy.RetryPolicy;
import lib.net.strategy.backoff.FullJitterBackoff;
import lib.net.strategy.backoff.RetryAfterFirstBackoff;
import lib.net.strategy.retry.StatusCodeWhitelistRetryPolicy;
import lib.net.util.NetworkConfig;
import lib.net.util.RequestCancelledException;

public abstract class ACommand implements RequestHandle {
    protected final String relativeUrl;
    protected HashMap<String, String> queryParams = new HashMap<>();
    protected HashMap<String, String> headers = new HashMap<>();
    protected final boolean isIdempotent;
    protected NetResult<String> result;
    protected final AtomicBoolean cancelled = new AtomicBoolean(false);
    protected int customConnectTimeout = -1, customReadTimeout = -1;

    // Strategies
    protected RetryPolicy retryPolicy;
    protected BackoffStrategy backoffStrategy;
    protected RequestConfigurator requestConfigurator;
    protected BodyWriter bodyWriter;
    protected ResponseReader responseReader;
    protected ErrorHandler errorHandler;
    protected MethodStrategy methodStrategy;

    protected final List<Interceptor> interceptors = new ArrayList<>();

    protected ACommand(String relativeUrl) {
        this(relativeUrl, null, null);
    }

    protected ACommand(String relativeUrl, HashMap<String, String> parameters) {
        this(relativeUrl, parameters, null);
    }

    protected ACommand(String relativeUrl, HashMap<String, String> parameters, HashMap<String, String> headers) {
        this.relativeUrl = relativeUrl;
        this.queryParams = (parameters != null) ? parameters : new HashMap<>();
        this.headers = (headers != null) ? headers : new HashMap<>();
        String method = getMethodName();
        this.isIdempotent = "GET".equals(method) || "PUT".equals(method) || "DELETE".equals(method);
    }

    public ACommand withRetryPolicy(RetryPolicy p) {
        this.retryPolicy = p;
        return this;
    }

    public ACommand withBackoff(BackoffStrategy b) {
        this.backoffStrategy = b;
        return this;
    }

    public ACommand withRequestConfigurator(RequestConfigurator rc) {
        this.requestConfigurator = rc;
        return this;
    }

    public ACommand withBodyWriter(BodyWriter w) {
        this.bodyWriter = w;
        return this;
    }

    public ACommand withResponseReader(ResponseReader rr) {
        this.responseReader = rr;
        return this;
    }

    public ACommand withErrorHandler(ErrorHandler eh) {
        this.errorHandler = eh;
        return this;
    }

    public ACommand withMethodStrategy(MethodStrategy ms) {
        this.methodStrategy = ms;
        return this;
    }

    public ACommand addInterceptor(Interceptor i) {
        if (i != null) this.interceptors.add(i);
        return this;
    }

    public String getRelativeUrl() {
        return relativeUrl;
    }

    public HashMap<String, String> getQueryParams() {
        return queryParams;
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public void addHeader(String k, String v) {
        if (k != null && v != null) headers.put(k, v);
    }

    public boolean hasHeader(String name) {
        return name != null && headers.containsKey(name);
    }

    public boolean isIdempotent() {
        return isIdempotent;
    }

    public String getContentType() {
        return "application/json; charset=utf-8";
    }

    public abstract String getMethodName();

    protected void initDefaultsIfNull() {
        if (retryPolicy == null)
            retryPolicy = new StatusCodeWhitelistRetryPolicy(
                    new java.util.HashSet<>(java.util.Arrays.asList(408, 429, 500, 502, 503, 504)), true);
        if (backoffStrategy == null)
            backoffStrategy = new RetryAfterFirstBackoff(new FullJitterBackoff(300, 8000));
        if (requestConfigurator == null)
            requestConfigurator = new lib.net.strategy.impl.DefaultRequestConfigurator();
        if (bodyWriter == null) bodyWriter = new lib.net.strategy.impl.DefaultBodyWriter();
        if (responseReader == null)
            responseReader = new lib.net.strategy.impl.DefaultResponseReader();
        if (errorHandler == null) errorHandler = new lib.net.strategy.impl.DefaultErrorHandler();
        if (methodStrategy == null)
            methodStrategy = new lib.net.strategy.impl.DefaultMethodStrategy();
    }

    @Override
    public void cancel() {
        cancelled.set(true);
    }

    @Override
    public boolean isCancelled() {
        return cancelled.get();
    }

    public void setConnectTimeout(int ms) {
        this.customConnectTimeout = ms;
    }

    public void setReadTimeout(int ms) {
        this.customReadTimeout = ms;
    }

    public NetResult<String> execute(Supplier<IHttpConnection> connectionSupplier) {
        initDefaultsIfNull();
        int attempts = 0;
        final int maxAttempts = NetworkConfig.RETRY_LIMIT + 1;
        long delay = backoffStrategy.initialDelayMs();

        while (attempts < maxAttempts) {
            if (isCancelled()) {
                handleException(new RequestCancelledException("İstek iptal edildi."));
                return result;
            }
            IHttpConnection conn = null;
            try {
                conn = connectionSupplier.get();
                conn.setConnectTimeout(customConnectTimeout > 0 ? customConnectTimeout : NetworkConfig.CONNECT_TIMEOUT_MS);
                conn.setReadTimeout(customReadTimeout > 0 ? customReadTimeout : NetworkConfig.READ_TIMEOUT_MS);
                methodStrategy.apply(conn, getMethodName());
                requestConfigurator.configure(conn, this);
                for (Interceptor it : interceptors) it.onRequest(conn, this);
                bodyWriter.writeIfNeeded(conn, this);

                int code = conn.getResponseCode();
                for (Interceptor it : interceptors) it.onResponseHeaders(conn, this, code);

                if ((code >= 200 && code < 300) || code == java.net.HttpURLConnection.HTTP_NO_CONTENT) {
                    String payload = (code == java.net.HttpURLConnection.HTTP_NO_CONTENT)
                            ? "" : responseReader.readSuccess(conn);
                    for (Interceptor it : interceptors) it.onSuccess(conn, this, payload);
                    result = new NetResult.Success<>(payload);
                    return result;
                }

                if (retryPolicy.shouldRetryOnStatus(code, isIdempotent)) {
                    String retryAfter = conn.getHeaderField("Retry-After");
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    delay = backoffStrategy.nextDelayMs(delay, retryAfter);
                } else {
                    NetResult.Error<String> e = errorHandler.handleError(conn, code);
                    for (Interceptor it : interceptors)
                        it.onError(conn, this, code, e.getErrorBody());
                    result = e;
                    return result;
                }

            } catch (Exception e) {
                if (retryPolicy.shouldRetryOnException(e, isIdempotent)) {
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    delay = backoffStrategy.nextDelayMs(delay, null);
                } else {
                    for (Interceptor it : interceptors) {
                        try {
                            it.onFailure(this, e);
                        } catch (Exception ignore) {
                        }
                    }
                    handleException(e);
                    return result;
                }
            } finally {
                if (conn != null) conn.disconnect();
            }
            attempts++;
        }
        handleException(new IOException("İstek, tekrar deneme limitini aştı."));
        return result;
    }

    @Deprecated
    public NetResult<String> execute(IHttpConnection single) {
        return execute(() -> single);
    }

    protected void handleException(Exception e) {
        result = new NetResult.Error<>(e, -1, e.getMessage() == null ? "Unknown error" : e.getMessage());
    }
}

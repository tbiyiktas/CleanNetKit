// lib/net/CancellableFuture.java
package lib.net;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import lib.net.command.ACommand;

public class CancellableFuture<T> extends CompletableFuture<T> {
    private volatile ACommand command;

    void attach(ACommand c) { this.command = c; }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (command != null) command.cancel();
        return super.cancel(mayInterruptIfRunning);
    }

    /** CancellableFuture olarak map'ler (CompletableFuture değil). */
    public <U> CancellableFuture<U> thenApplyC(Function<? super T, ? extends U> fn) {
        CancellableFuture<U> next = new CancellableFuture<>();
        if (this.command != null) next.attach(this.command);
        this.whenComplete((val, ex) -> {
            if (ex != null) {
                next.completeExceptionally(ex);
            } else {
                try {
                    U mapped = fn.apply(val);
                    next.complete(mapped);
                } catch (Throwable t) {
                    next.completeExceptionally(t);
                }
            }
        });
        return next;
    }

    /** CancellableFuture<U> döndüren fonksiyonlarla compose yapar. */
    public <U> CancellableFuture<U> thenComposeC(Function<? super T, ? extends CancellableFuture<U>> fn) {
        CancellableFuture<U> next = new CancellableFuture<>();
        if (this.command != null) next.attach(this.command);
        this.whenComplete((val, ex) -> {
            if (ex != null) {
                next.completeExceptionally(ex);
            } else {
                try {
                    CancellableFuture<U> inner = fn.apply(val);
                    if (inner != null) {
                        // Aynı komut üzerinden iptal yayılımı
                        if (inner.command != null) next.attach(inner.command);
                        inner.whenComplete((u, ex2) -> {
                            if (ex2 != null) next.completeExceptionally(ex2);
                            else next.complete(u);
                        });
                    } else {
                        next.complete(null);
                    }
                } catch (Throwable t) {
                    next.completeExceptionally(t);
                }
            }
        });
        return next;
    }
}

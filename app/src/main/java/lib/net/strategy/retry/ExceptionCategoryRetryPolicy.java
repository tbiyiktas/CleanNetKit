package lib.net.strategy.retry;

import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import javax.net.ssl.SSLException;

import lib.net.strategy.RetryPolicy;

// 2) ExceptionCategoryRetryPolicy (sadece transient I/O)
public class ExceptionCategoryRetryPolicy implements RetryPolicy {
    @Override
    public boolean shouldRetryOnStatus(int code, boolean idem) {
        return idem && (code == 408 || code == 429 || code == 500 || code == 502 || code == 503 || code == 504);
    }

    @Override
    public boolean shouldRetryOnException(Exception e, boolean idem) {
        if (!idem) return false;
        return (e instanceof SocketTimeoutException) ||
                (e instanceof ConnectException) ||
                (e instanceof NoRouteToHostException) ||
                (e instanceof UnknownHostException) ||
                (e instanceof SSLException) ||
                (e instanceof IOException);
    }
}
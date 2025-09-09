package lib.net.strategy.retry;

import java.io.IOException;

import lib.net.strategy.RetryPolicy;

// 4) AuthAwareRetryPolicy (401 için tek seferlik yenileme denemesi)
public class AuthAwareRetryPolicy implements RetryPolicy {
    private boolean retriedAfterRefresh = false;
    private final boolean requireIdem;

    public AuthAwareRetryPolicy(boolean requireIdem) {
        this.requireIdem = requireIdem;
    }

    @Override
    public boolean shouldRetryOnStatus(int code, boolean idem) {
        if (code == 401 && !retriedAfterRefresh) {
            retriedAfterRefresh = true;
            return true;
        }
        return (!requireIdem || idem) && (code == 408 || code == 429 || code == 500 || code == 502 || code == 503 || code == 504);
    }

    @Override
    public boolean shouldRetryOnException(Exception e, boolean idem) {
        return (!requireIdem || idem) && (e instanceof IOException);
    }
}
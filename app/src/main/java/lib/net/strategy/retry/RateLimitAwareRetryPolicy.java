package lib.net.strategy.retry;

import java.io.IOException;

import lib.net.strategy.RetryPolicy;

// 7) RateLimitAwareRetryPolicy (429 işbirlikçi)
public class RateLimitAwareRetryPolicy implements RetryPolicy {
    @Override
    public boolean shouldRetryOnStatus(int code, boolean idem) {
        return idem && (code == 429 || code == 503 || code == 408);
    }

    @Override
    public boolean shouldRetryOnException(Exception e, boolean idem) {
        return idem && (e instanceof IOException);
    }
}
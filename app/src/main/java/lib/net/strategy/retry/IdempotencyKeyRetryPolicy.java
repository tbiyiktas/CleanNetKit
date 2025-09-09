package lib.net.strategy.retry;

import java.io.IOException;

import lib.net.strategy.RetryPolicy;

// 5) IdempotencyKeyRetryPolicy (POST da güvenli olabilir)
public class IdempotencyKeyRetryPolicy implements RetryPolicy {
    private final boolean hasIdempotencyKey;

    public IdempotencyKeyRetryPolicy(boolean hasKey) {
        this.hasIdempotencyKey = hasKey;
    }

    @Override
    public boolean shouldRetryOnStatus(int code, boolean idem) {
        boolean safe = idem || hasIdempotencyKey;
        return safe && (code == 408 || code == 429 || code == 500 || code == 502 || code == 503 || code == 504);
    }

    @Override
    public boolean shouldRetryOnException(Exception e, boolean idem) {
        boolean safe = idem || hasIdempotencyKey;
        return safe && (e instanceof IOException);
    }
}

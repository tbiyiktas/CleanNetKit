package lib.net.strategy.retry;

import java.io.IOException;

import lib.net.strategy.RetryPolicy;

// 8) PayloadSensitiveRetryPolicy (büyük gövdede tutumlu)
public class PayloadSensitiveRetryPolicy implements RetryPolicy {
    private final long payloadBytes;
    private final long maxRetriesForLarge;
    private long tries = 0;

    public PayloadSensitiveRetryPolicy(long payloadBytes, long maxRetriesForLarge) {
        this.payloadBytes = payloadBytes;
        this.maxRetriesForLarge = maxRetriesForLarge;
    }

    @Override
    public boolean shouldRetryOnStatus(int code, boolean idem) {
        if (!idem && payloadBytes > 256 * 1024) return false; // 256KB üstü POST/PUT: tutumlu
        return (code == 408 || code == 429 || code == 500 || code == 502 || code == 503 || code == 504) && (++tries <= maxRetriesForLarge);
    }

    @Override
    public boolean shouldRetryOnException(Exception e, boolean idem) {
        if (!idem && payloadBytes > 256 * 1024) return false;
        return (e instanceof IOException) && (++tries <= maxRetriesForLarge);
    }
}
package lib.net.strategy.backoff;

import lib.net.strategy.BackoffStrategy;
import lib.net.strategy.util.RetryAfterUtil;

// 7) RetryAfterFirstBackoff (header öncelikli)
public class RetryAfterFirstBackoff implements BackoffStrategy {
    private final BackoffStrategy fallback;

    public RetryAfterFirstBackoff(BackoffStrategy fallback) {
        this.fallback = fallback;
    }

    @Override
    public long initialDelayMs() {
        return fallback.initialDelayMs();
    }

    @Override
    public long nextDelayMs(long current, String h) {
        long via = RetryAfterUtil.parseRetryAfterMs(h);
        return (via > 0) ? via : fallback.nextDelayMs(current, null);
    }
}
package lib.net.strategy.backoff;

import lib.net.strategy.BackoffStrategy;
import lib.net.strategy.util.RetryAfterUtil;

// 2) ExponentialBackoff (tavanlı)
public class ExponentialBackoff implements BackoffStrategy {
    private final long base;
    private final long cap;

    public ExponentialBackoff(long baseMs, long capMs) {
        this.base = baseMs;
        this.cap = capMs;
    }

    @Override
    public long initialDelayMs() {
        return base;
    }

    @Override
    public long nextDelayMs(long current, String h) {
        long via = RetryAfterUtil.parseRetryAfterMs(h);
        if (via > 0) return Math.min(via, cap);
        long next = (current <= 0 ? base : Math.min(current * 2, cap));
        return next;
    }
}
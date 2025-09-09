package lib.net.strategy.backoff;


import java.util.Random;

import lib.net.strategy.BackoffStrategy;
import lib.net.strategy.util.RetryAfterUtil;

// 5) DecorrelatedJitterBackoff (Exponential + decorrelated)
public class DecorrelatedJitterBackoff implements BackoffStrategy {
    private final long base;
    private final long cap;
    private final Random rnd = new Random();

    public DecorrelatedJitterBackoff(long baseMs, long capMs) {
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
        long prev = current <= 0 ? base : current;
        long next = Math.min(cap, Math.max(base, (long) (rnd.nextDouble() * prev * 3)));
        return next;
    }
}

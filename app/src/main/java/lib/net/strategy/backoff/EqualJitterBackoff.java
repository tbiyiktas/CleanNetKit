package lib.net.strategy.backoff;


import java.util.Random;

import lib.net.strategy.BackoffStrategy;
import lib.net.strategy.util.RetryAfterUtil;

// 4) EqualJitterBackoff
public class EqualJitterBackoff implements BackoffStrategy {
    private final long base;
    private final long cap;
    private final Random rnd = new Random();

    public EqualJitterBackoff(long baseMs, long capMs) {
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
        long exp = current <= 0 ? base : Math.min(current * 2, cap);
        return exp / 2 + (long) (rnd.nextDouble() * (exp / 2));
    }
}
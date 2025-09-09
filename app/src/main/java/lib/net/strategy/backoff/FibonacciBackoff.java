package lib.net.strategy.backoff;

import lib.net.strategy.BackoffStrategy;
import lib.net.strategy.util.RetryAfterUtil;

// 6) FibonacciBackoff
public class FibonacciBackoff implements BackoffStrategy {
    private final long unit;
    private final long cap;
    private long a = 0, b = 1;

    public FibonacciBackoff(long unitMs, long capMs) {
        this.unit = unitMs;
        this.cap = capMs;
    }

    @Override
    public long initialDelayMs() {
        return unit;
    }

    @Override
    public long nextDelayMs(long current, String h) {
        long via = RetryAfterUtil.parseRetryAfterMs(h);
        if (via > 0) return Math.min(via, cap);
        long next = (a + b);
        a = b;
        b = next;
        return Math.min(next * unit, cap);
    }
}
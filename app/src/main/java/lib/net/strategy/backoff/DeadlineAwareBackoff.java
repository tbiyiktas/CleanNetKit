package lib.net.strategy.backoff;

import lib.net.strategy.BackoffStrategy;

// 8) DeadlineAwareBackoff (kalan bütçeye göre kısalt)
public class DeadlineAwareBackoff implements BackoffStrategy {
    private final long deadlineMs;
    private long start = -1;
    private final BackoffStrategy inner;

    public DeadlineAwareBackoff(long deadlineMs, BackoffStrategy inner) {
        this.deadlineMs = deadlineMs;
        this.inner = inner;
    }

    private long remaining() {
        if (start < 0) start = System.currentTimeMillis();
        return Math.max(0, deadlineMs - (System.currentTimeMillis() - start));
    }

    @Override
    public long initialDelayMs() {
        return Math.min(inner.initialDelayMs(), Math.max(0, remaining() / 4));
    }

    @Override
    public long nextDelayMs(long current, String h) {
        long innerNext = inner.nextDelayMs(current, h);
        return Math.min(innerNext, Math.max(0, remaining() / 2));
    }
}
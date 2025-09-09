package lib.net.strategy.retry;

import java.io.IOException;

import lib.net.strategy.RetryPolicy;

// 3) DeadlineRetryPolicy (toplam süre bütçesi)
public class DeadlineRetryPolicy implements RetryPolicy {
    private final long deadlineMs;
    private long startNs = -1;

    public DeadlineRetryPolicy(long deadlineMs) {
        this.deadlineMs = deadlineMs;
    }

    private boolean withinBudget() {
        if (startNs < 0) startNs = System.nanoTime();
        long elapsedMs = (System.nanoTime() - startNs) / 1_000_000L;
        return elapsedMs < deadlineMs;
    }

    @Override
    public boolean shouldRetryOnStatus(int c, boolean idem) {
        return idem && withinBudget() && (c == 408 || c == 429 || c == 500 || c == 502 || c == 503 || c == 504);
    }

    @Override
    public boolean shouldRetryOnException(Exception e, boolean idem) {
        return idem && withinBudget() && (e instanceof IOException);
    }
}
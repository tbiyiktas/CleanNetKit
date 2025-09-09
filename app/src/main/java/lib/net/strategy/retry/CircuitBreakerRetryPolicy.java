package lib.net.strategy.retry;


import java.io.IOException;

import lib.net.strategy.RetryPolicy;

// 6) CircuitBreakerRetryPolicy (çok basit gözetmen)
public class CircuitBreakerRetryPolicy implements RetryPolicy {
    private enum State {CLOSED, OPEN, HALF_OPEN}

    private State state = State.CLOSED;
    private final int failureThreshold;
    private final long openMs;
    private int failures = 0;
    private long openedAt = 0;

    public CircuitBreakerRetryPolicy(int failureThreshold, long openMs) {
        this.failureThreshold = failureThreshold;
        this.openMs = openMs;
    }

    private boolean allowTrial() {
        if (state == State.OPEN && System.currentTimeMillis() - openedAt >= openMs) {
            state = State.HALF_OPEN;
            return true;
        }
        return state != State.OPEN;
    }

    private void recordFailure() {
        failures++;
        if (failures >= failureThreshold) {
            state = State.OPEN;
            openedAt = System.currentTimeMillis();
        }
    }

    private void recordSuccess() {
        failures = 0;
        state = State.CLOSED;
    }

    @Override
    public boolean shouldRetryOnStatus(int code, boolean idem) {
        if (!allowTrial()) return false;
        boolean retry = idem && (code == 408 || code == 429 || code == 500 || code == 502 || code == 503 || code == 504);
        if (retry) recordFailure();
        else recordSuccess();
        return retry;
    }

    @Override
    public boolean shouldRetryOnException(Exception e, boolean idem) {
        if (!allowTrial()) return false;
        boolean retry = idem && (e instanceof IOException);
        if (retry) recordFailure();
        else recordSuccess();
        return retry;
    }
}

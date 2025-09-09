package lib.net.strategy.retry;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

import lib.net.strategy.RetryPolicy;

// 10) AdaptiveErrorRateRetryPolicy (basit kayan pencere)
public class AdaptiveErrorRateRetryPolicy implements RetryPolicy {
    private final Deque<Long> window = new ArrayDeque<>();
    private final int maxEvents;
    private final double maxErrorRate;
    private final long windowMs;

    public AdaptiveErrorRateRetryPolicy(int maxEvents, double maxErrorRate, long windowMs) {
        this.maxEvents = maxEvents;
        this.maxErrorRate = maxErrorRate;
        this.windowMs = windowMs;
    }

    private void record(boolean error) {
        long now = System.currentTimeMillis();
        window.addLast(error ? now : -now);
        while (window.size() > maxEvents) window.removeFirst();
        while (!window.isEmpty() && Math.abs(window.peekFirst()) < now - windowMs)
            window.removeFirst();
    }

    private double errorRate() {
        int err = 0, tot = 0;
        for (long v : window) {
            tot++;
            if (v > 0) err++;
        }
        return tot == 0 ? 0 : (err * 1.0 / tot);
    }

    @Override
    public boolean shouldRetryOnStatus(int code, boolean idem) {
        boolean retry = idem && (code == 408 || code == 429 || code == 500 || code == 502 || code == 503 || code == 504) && (errorRate() < maxErrorRate);
        record(!retry); // retry edilebilir görmediysek "başarısız girişim" say
        return retry;
    }

    @Override
    public boolean shouldRetryOnException(Exception e, boolean idem) {
        boolean retry = idem && (e instanceof IOException) && (errorRate() < maxErrorRate);
        record(!retry);
        return retry;
    }
}
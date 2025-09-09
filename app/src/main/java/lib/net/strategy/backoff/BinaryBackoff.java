package lib.net.strategy.backoff;


import lib.net.strategy.BackoffStrategy;
import lib.net.strategy.util.RetryAfterUtil;

// 10) BinaryBackoff (iki kademeli)
public class BinaryBackoff implements BackoffStrategy {
    private final long shortMs, longMs;
    private final int switchAfter;
    private int attempts = 0;

    public BinaryBackoff(long shortMs, long longMs, int switchAfter) {
        this.shortMs = shortMs;
        this.longMs = longMs;
        this.switchAfter = switchAfter;
    }

    @Override
    public long initialDelayMs() {
        return shortMs;
    }

    @Override
    public long nextDelayMs(long current, String h) {
        long via = RetryAfterUtil.parseRetryAfterMs(h);
        if (via > 0) return via;
        attempts++;
        return attempts <= switchAfter ? shortMs : longMs;
    }
}
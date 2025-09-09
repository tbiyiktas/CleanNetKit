package lib.net.strategy.backoff;


import lib.net.strategy.BackoffStrategy;
import lib.net.strategy.util.RetryAfterUtil;

// 1) FixedBackoff
public class FixedBackoff implements BackoffStrategy {
    private final long fixedMs;

    public FixedBackoff(long fixedMs) {
        this.fixedMs = fixedMs;
    }

    @Override
    public long initialDelayMs() {
        return fixedMs;
    }

    @Override
    public long nextDelayMs(long current, String h) {
        long via = RetryAfterUtil.parseRetryAfterMs(h);
        return via > 0 ? via : fixedMs;
    }
}













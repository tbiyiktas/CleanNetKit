package lib.net.strategy.backoff;


import lib.net.strategy.BackoffStrategy;

// 9) QueueAwareBackoff (sistem yüküne duyarlı)
public class QueueAwareBackoff implements BackoffStrategy {
    public interface LoadProbe {
        double loadFactor();
    } // 0.0 boş, 1.0 dolu

    private final BackoffStrategy base;
    private final LoadProbe probe;
    private final long extraCapMs;

    public QueueAwareBackoff(BackoffStrategy base, LoadProbe probe, long extraCapMs) {
        this.base = base;
        this.probe = probe;
        this.extraCapMs = extraCapMs;
    }

    @Override
    public long initialDelayMs() {
        return base.initialDelayMs();
    }

    @Override
    public long nextDelayMs(long current, String h) {
        long d = base.nextDelayMs(current, h);
        double lf = Math.max(0, Math.min(1, probe.loadFactor()));
        return Math.min(d + (long) (lf * extraCapMs), d + extraCapMs);
    }
}
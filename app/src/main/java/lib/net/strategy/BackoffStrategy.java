package lib.net.strategy;

public interface BackoffStrategy {
    long initialDelayMs();

    long nextDelayMs(long currentDelayMs, String retryAfterHeader);
}
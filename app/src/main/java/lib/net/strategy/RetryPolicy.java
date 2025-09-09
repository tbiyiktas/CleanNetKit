package lib.net.strategy;

public interface RetryPolicy {
    boolean shouldRetryOnStatus(int code, boolean isIdempotent);

    boolean shouldRetryOnException(Exception e, boolean isIdempotent);
}
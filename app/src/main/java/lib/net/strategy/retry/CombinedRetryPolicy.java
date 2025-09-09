// lib/net/strategy/retry/CombinedRetryPolicy.java
package lib.net.strategy.retry;

import java.util.Arrays;
import java.util.List;

import lib.net.strategy.RetryPolicy;

public class CombinedRetryPolicy implements RetryPolicy {
    private final List<RetryPolicy> items;

    public CombinedRetryPolicy(RetryPolicy... items) {
        this.items = Arrays.asList(items);
    }

    @Override
    public boolean shouldRetryOnStatus(int code, boolean idem) {
        for (RetryPolicy rp : items) if (!rp.shouldRetryOnStatus(code, idem)) return false;
        return true;
    }

    @Override
    public boolean shouldRetryOnException(Exception e, boolean idem) {
        for (RetryPolicy rp : items) if (!rp.shouldRetryOnException(e, idem)) return false;
        return true;
    }
}

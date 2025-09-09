package lib.net.strategy.retry;


import java.io.IOException;
import java.util.Set;

import lib.net.strategy.RetryPolicy;

// 1) StatusCodeWhitelistRetryPolicy
public class StatusCodeWhitelistRetryPolicy implements RetryPolicy {
    private final Set<Integer> allow;
    private final boolean requireIdem;

    public StatusCodeWhitelistRetryPolicy(Set<Integer> allow, boolean requireIdem) {
        this.allow = allow;
        this.requireIdem = requireIdem;
    }

    @Override
    public boolean shouldRetryOnStatus(int code, boolean idem) {
        return (!requireIdem || idem) && allow.contains(code);
    }

    @Override
    public boolean shouldRetryOnException(Exception e, boolean idem) {
        return (!requireIdem || idem) && (e instanceof IOException);
    }
}

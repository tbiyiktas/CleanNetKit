package lib.net.strategy.retry;


import java.io.IOException;

import lib.net.strategy.RetryPolicy;

// 9) CacheFriendlyGetRetryPolicy (GET için hızlı-tek tekrar)
public class CacheFriendlyGetRetryPolicy implements RetryPolicy {
    private boolean fastRetryDone = false;

    @Override
    public boolean shouldRetryOnStatus(int code, boolean idem) {
        if (!idem) return false;
        if (!fastRetryDone) {
            fastRetryDone = true;
            return (code == 500 || code == 502 || code == 503 || code == 504 || code == 408);
        }
        return (code == 429); // ikinci aşamada sadece 429
    }

    @Override
    public boolean shouldRetryOnException(Exception e, boolean idem) {
        if (!idem) return false;
        if (!fastRetryDone) {
            fastRetryDone = true;
            return (e instanceof IOException);
        }
        return false;
    }
}
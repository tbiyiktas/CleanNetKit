package lib.net.strategy.util;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public final class RetryAfterUtil {
    private RetryAfterUtil() {
    }

    public static long parseRetryAfterMs(String header) {
        if (header == null || header.isEmpty()) return -1;
        try {
            // seconds
            return Long.parseLong(header.trim()) * 1000L;
        } catch (NumberFormatException ignore) {
        }
        // HTTP-date
        List<String> fmts = Arrays.asList(
                "EEE, dd MMM yyyy HH:mm:ss zzz",    // RFC1123
                "EEEE, dd-MMM-yy HH:mm:ss zzz",     // RFC850
                "EEE MMM d HH:mm:ss yyyy"           // ANSI C asctime()
        );
        for (String f : fmts) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(f, Locale.US);
                sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
                long when = sdf.parse(header).getTime();
                long delta = when - System.currentTimeMillis();
                return Math.max(delta, 0);
            } catch (Exception ignore) {
            }
        }
        return -1;
    }
}

package lib.net.util;

public class MyURLEncoder {

    public static String encode(String s) {
        if (s == null || s.isEmpty()) return "";
        StringBuilder out = new StringBuilder();
        byte[] bytes = s.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        for (byte b : bytes) {
            int v = b & 0xFF;
            // RFC 3986 unreserved
            if ((v >= 'a' && v <= 'z') || (v >= 'A' && v <= 'Z') ||
                    (v >= '0' && v <= '9') || v == '-' || v == '_' || v == '.' || v == '~') {
                out.append((char) v);
            } else if (v == ' ') {
                out.append('+');
            } else {
                out.append('%').append(String.format("%02X", v));
            }
        }
        return out.toString();
    }

    private static boolean isUnreserved(char c) {
        // RFC 3986'ya göre ayrılmış olmayan karakterler
        // Alfabetik, sayısal ve birkaç özel karakter
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                (c >= '0' && c <= '9') ||
                c == '-' || c == '_' || c == '.' || c == '~';
    }
}
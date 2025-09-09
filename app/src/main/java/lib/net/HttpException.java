package lib.net;

public class HttpException extends RuntimeException {
    public final int code;
    public final String body;

    public HttpException(Throwable cause, int code, String body) {
        super(cause == null ? ("HTTP " + code) : cause.getMessage(), cause);
        this.code = code;
        this.body = body;
    }
}
package lib.net.strategy;

import lib.net.NetResult;
import lib.net.connection.IHttpConnection;

public interface ErrorHandler {
    NetResult.Error<String> handleError(IHttpConnection conn, int statusCode);
}
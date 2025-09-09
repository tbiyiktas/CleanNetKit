// lib/net/interceptor/Interceptor.java
package lib.net.interceptor;

import lib.net.command.ACommand;
import lib.net.connection.IHttpConnection;

public interface Interceptor {
    default void onRequest(IHttpConnection conn, ACommand cmd) throws Exception {
    }

    default void onResponseHeaders(IHttpConnection conn, ACommand cmd, int status) throws Exception {
    }

    default void onSuccess(IHttpConnection conn, ACommand cmd, String body) throws Exception {
    }

    default void onError(IHttpConnection conn, ACommand cmd, int status, String errorBody) throws Exception {
    }

    default void onFailure(ACommand cmd, Exception e) throws Exception {
    }
}

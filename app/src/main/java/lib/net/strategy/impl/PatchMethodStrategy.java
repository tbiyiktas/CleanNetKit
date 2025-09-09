package lib.net.strategy.impl;

import java.io.IOException;

import lib.net.connection.IHttpConnection;
import lib.net.strategy.MethodStrategy;

/**
 * Bazı ortamlar PATCH'i doğrudan desteklemezse X-HTTP-Method-Override uygular.
 */
public class PatchMethodStrategy implements MethodStrategy {
    @Override
    public void apply(IHttpConnection conn, String method) throws IOException {
        if ("PATCH".equalsIgnoreCase(method)) {
            conn.setRequestMethod("POST");
            conn.setRequestProperty("X-HTTP-Method-Override", "PATCH");
        } else {
            conn.setRequestMethod(method);
        }
    }
}

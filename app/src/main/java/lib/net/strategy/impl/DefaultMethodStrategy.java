package lib.net.strategy.impl;

import java.io.IOException;

import lib.net.connection.IHttpConnection;
import lib.net.strategy.MethodStrategy;

public class DefaultMethodStrategy implements MethodStrategy {
    @Override
    public void apply(IHttpConnection conn, String method) throws IOException {
        conn.setRequestMethod(method);
    }
}

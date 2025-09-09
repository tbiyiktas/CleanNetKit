package lib.net.strategy;

import java.io.IOException;

import lib.net.connection.IHttpConnection;

public interface MethodStrategy {
    void apply(IHttpConnection conn, String method) throws IOException;
}
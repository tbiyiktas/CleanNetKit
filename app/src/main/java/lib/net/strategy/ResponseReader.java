package lib.net.strategy;

import java.io.IOException;

import lib.net.connection.IHttpConnection;

public interface ResponseReader {
    String readSuccess(IHttpConnection conn) throws IOException;
}
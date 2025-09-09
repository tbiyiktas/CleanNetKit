package lib.net.strategy.impl;

import java.io.IOException;

import lib.net.command.ACommand;
import lib.net.connection.IHttpConnection;
import lib.net.strategy.BodyWriter;

public class DefaultBodyWriter implements BodyWriter {
    @Override
    public void writeIfNeeded(IHttpConnection conn, ACommand cmd) throws IOException {
        // GET gibi durumlarda varsayılan: hiçbir şey yapma (no-op)
    }
}

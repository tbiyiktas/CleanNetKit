package lib.net.strategy;


import java.io.IOException;

import lib.net.command.ACommand;
import lib.net.connection.IHttpConnection;

public interface BodyWriter {
    void writeIfNeeded(IHttpConnection conn, ACommand cmd) throws IOException; // GET gibi durumlarda no-op
}
package lib.net.strategy;


import java.io.IOException;

import lib.net.command.ACommand;
import lib.net.connection.IHttpConnection;

public interface RequestConfigurator {
    void configure(IHttpConnection conn, ACommand cmd) throws IOException;
}
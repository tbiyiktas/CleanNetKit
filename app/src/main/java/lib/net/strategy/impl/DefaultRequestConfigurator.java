package lib.net.strategy.impl;

import java.io.IOException;
import java.util.Map;

import lib.net.command.ACommand;
import lib.net.connection.IHttpConnection;
import lib.net.strategy.RequestConfigurator;

public class DefaultRequestConfigurator implements RequestConfigurator {
    @Override
    public void configure(IHttpConnection c, ACommand cmd) throws IOException {
        c.setRequestProperty("Content-Type", cmd.getContentType());
        c.setRequestProperty("Accept", "application/json");
        c.setRequestProperty("Accept-Encoding", "gzip");
        if (cmd.getHeaders() != null) {
            for (Map.Entry<String, String> e : cmd.getHeaders().entrySet()) {
                c.setRequestProperty(e.getKey(), e.getValue());
            }
        }
    }
}

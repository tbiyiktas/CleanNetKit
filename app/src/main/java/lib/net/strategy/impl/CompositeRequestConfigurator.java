// lib/net/strategy/impl/CompositeRequestConfigurator.java
package lib.net.strategy.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import lib.net.command.ACommand;
import lib.net.connection.IHttpConnection;
import lib.net.strategy.RequestConfigurator;

public class CompositeRequestConfigurator implements RequestConfigurator {
    private final List<RequestConfigurator> items;

    public CompositeRequestConfigurator(RequestConfigurator... items) {
        this.items = Arrays.asList(items);
    }

    @Override
    public void configure(IHttpConnection conn, ACommand cmd) throws IOException {
        for (RequestConfigurator rc : items) rc.configure(conn, cmd);
    }
}

// lib/net/strategy/impl/AuthRequestConfigurator.java
package lib.net.strategy.impl;

import java.io.IOException;
import java.util.function.Supplier;

import lib.net.command.ACommand;
import lib.net.connection.IHttpConnection;
import lib.net.strategy.RequestConfigurator;

public class AuthRequestConfigurator implements RequestConfigurator {
    private final Supplier<String> tokenSupplier;

    public AuthRequestConfigurator(Supplier<String> tokenSupplier) {
        this.tokenSupplier = tokenSupplier;
    }

    @Override
    public void configure(IHttpConnection conn, ACommand cmd) throws IOException {
        // Default header propagation zaten mevcutsa önce onu çalıştırmak istiyorsanız
        // buraya bir "delegate" ekleyebilirsiniz (örn. new DefaultRequestConfigurator().configure(...))
        String token = tokenSupplier != null ? tokenSupplier.get() : null;
        if (token != null && !token.isEmpty()) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }
        // Örn: ortak User-Agent
        conn.setRequestProperty("User-Agent", "CleanNetKit/1.0");
    }
}

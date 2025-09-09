// lib/net/strategy/impl/IdempotencyRequestConfigurator.java
package lib.net.strategy.impl;

import java.io.IOException;
import java.util.UUID;

import lib.net.command.ACommand;
import lib.net.connection.IHttpConnection;
import lib.net.strategy.RequestConfigurator;

public class IdempotencyRequestConfigurator implements RequestConfigurator {

    private final boolean enabled;

    public IdempotencyRequestConfigurator() {
        this(true);
    }

    public IdempotencyRequestConfigurator(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void configure(IHttpConnection conn, ACommand cmd) throws IOException {
        if (!enabled) return;

        // Sadece idempotent OLMAYAN isteklerde uygula (örn. POST, PATCH)
        if (cmd.isIdempotent()) return;

        final String headerName = "Idempotency-Key";
        if (!cmd.hasHeader(headerName)) {
            String key = UUID.randomUUID().toString();
            // Komutun kendi header haritasına ekle
            cmd.addHeader(headerName, key);
            // Ve bağlantıya da yaz (DefaultRequestConfigurator yoksa da çalışsın)
            conn.setRequestProperty(headerName, key);
        }
    }
}

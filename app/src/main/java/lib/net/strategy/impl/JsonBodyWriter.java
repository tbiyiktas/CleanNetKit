package lib.net.strategy.impl;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

import lib.net.command.ACommand;
import lib.net.connection.IHttpConnection;
import lib.net.strategy.BodyWriter;

public class JsonBodyWriter implements BodyWriter {
    private final Supplier<String> jsonSupplier;

    public JsonBodyWriter(Supplier<String> supplier) {
        this.jsonSupplier = supplier;
    }

    @Override
    public void writeIfNeeded(IHttpConnection conn, ACommand cmd) throws IOException {
        String json = jsonSupplier.get();
        if (json == null) return;
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        try (DataOutputStream out = new DataOutputStream(conn.getOutputStream())) {
            out.write(json.getBytes(StandardCharsets.UTF_8));
            out.flush();
        }
    }
}

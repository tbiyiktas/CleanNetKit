// lib/net/strategy/impl/BytesBodyWriter.java
package lib.net.strategy.impl;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.function.Supplier;

import lib.net.command.ACommand;
import lib.net.connection.IHttpConnection;
import lib.net.strategy.BodyWriter;

public class BytesBodyWriter implements BodyWriter {
    private final Supplier<byte[]> dataSupplier;
    private final String contentType;

    public BytesBodyWriter(Supplier<byte[]> supplier, String contentType) {
        this.dataSupplier = supplier;
        this.contentType = contentType;
    }

    @Override
    public void writeIfNeeded(IHttpConnection conn, ACommand cmd) throws IOException {
        byte[] bytes = dataSupplier.get();
        if (bytes == null) return;
        conn.setDoOutput(true);
        if (contentType != null) conn.setRequestProperty("Content-Type", contentType);
        try (DataOutputStream out = new DataOutputStream(conn.getOutputStream())) {
            out.write(bytes);
            out.flush();
        }
    }
}

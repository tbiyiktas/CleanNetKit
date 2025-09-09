package lib.net.strategy.impl;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import lib.net.command.ACommand;
import lib.net.connection.IHttpConnection;
import lib.net.strategy.BodyWriter;

public class MultipartBodyWriter implements BodyWriter {

    private final String boundary;
    private final Map<String, String> formFields;
    private final Map<String, File> files;

    public MultipartBodyWriter(String boundary, Map<String, String> formFields, Map<String, File> files) {
        this.boundary = boundary;
        this.formFields = formFields;
        this.files = files;
    }

    @Override
    public void writeIfNeeded(IHttpConnection connection, ACommand cmd) throws IOException {
        connection.setDoOutput(true);
        try (DataOutputStream out = new DataOutputStream(connection.getOutputStream())) {
            // fields
            if (formFields != null) {
                for (Map.Entry<String, String> e : formFields.entrySet()) {
                    out.writeBytes("--" + boundary + "\r\n");
                    out.writeBytes("Content-Disposition: form-data; name=\"" + e.getKey() + "\"\r\n");
                    out.writeBytes("Content-Type: text/plain; charset=utf-8\r\n\r\n");
                    out.write(e.getValue().getBytes(StandardCharsets.UTF_8));
                    out.writeBytes("\r\n");
                }
            }
            // files
            if (files != null) {
                byte[] buf = new byte[4096];
                for (Map.Entry<String, File> f : files.entrySet()) {
                    out.writeBytes("--" + boundary + "\r\n");
                    out.writeBytes("Content-Disposition: form-data; name=\"" + f.getKey() + "\"; filename=\"" + f.getValue().getName() + "\"\r\n");
                    out.writeBytes("Content-Type: application/octet-stream\r\n");
                    out.writeBytes("Content-Transfer-Encoding: binary\r\n\r\n");
                    try (FileInputStream fis = new FileInputStream(f.getValue())) {
                        int n;
                        while ((n = fis.read(buf)) != -1) {
                            out.write(buf, 0, n);
                        }
                    }
                    out.writeBytes("\r\n");
                }
            }
            out.writeBytes("--" + boundary + "--\r\n");
            out.flush();
        }
    }
}

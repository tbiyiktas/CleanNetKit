package lib.net.command;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import lib.net.strategy.impl.MultipartBodyWriter;

public class MultipartCommand extends ACommand {
    private static final String BOUNDARY_PREFIX = "----WebKitFormBoundary" + UUID.randomUUID().toString();
    private static final String CRLF = "\r\n";
    private final String boundary;
    private final HashMap<String, String> formFields;
    private final HashMap<String, File> files;
    private final long estimatedPayloadBytes; // ~yaklaşık toplam

    public MultipartCommand(String relativeUrl,
                            HashMap<String, String> headers,
                            HashMap<String, String> formFields,
                            HashMap<String, File> files) {
        super(relativeUrl, null, headers);
        this.boundary = BOUNDARY_PREFIX + System.currentTimeMillis();
        this.formFields = formFields != null ? formFields : new HashMap<>();
        this.files = files != null ? files : new HashMap<>();
        withBodyWriter(new MultipartBodyWriter(boundary, this.formFields, this.files));
        this.estimatedPayloadBytes = estimateBytes(boundary, this.formFields, this.files);
    }

    @Override
    public String getMethodName() {
        return "POST";
    }

    @Override
    public String getContentType() {
        return "multipart/form-data; boundary=" + boundary;
    }

    public long estimatePayloadBytes() {
        return estimatedPayloadBytes;
    }

    private static long estimateBytes(String boundary,
                                      Map<String, String> fields,
                                      Map<String, File> files) {
        long total = 0L;
        final byte[] boundaryLine = ("--" + boundary + CRLF).getBytes(StandardCharsets.UTF_8);
        final byte[] finalBoundary = ("--" + boundary + "--" + CRLF).getBytes(StandardCharsets.UTF_8);

        // text fields
        if (fields != null) {
            for (Map.Entry<String, String> e : fields.entrySet()) {
                String name = e.getKey() == null ? "" : e.getKey();
                String val = e.getValue() == null ? "" : e.getValue();

                String headers =
                        "Content-Disposition: form-data; name=\"" + name + "\"" + CRLF +
                                CRLF; // boş satırdan sonra body
                total += boundaryLine.length;
                total += headers.getBytes(StandardCharsets.UTF_8).length;
                total += val.getBytes(StandardCharsets.UTF_8).length;
                total += CRLF.getBytes(StandardCharsets.UTF_8).length;
            }
        }

        // files
        if (files != null) {
            for (Map.Entry<String, File> e : files.entrySet()) {
                String name = e.getKey() == null ? "file" : e.getKey();
                File f = e.getValue();
                long fileLen = (f != null && f.exists()) ? f.length() : 0L;

                String headers =
                        "Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" +
                                (f != null ? f.getName() : "blob") + "\"" + CRLF +
                                "Content-Type: application/octet-stream" + CRLF +
                                CRLF;
                total += boundaryLine.length;
                total += headers.getBytes(StandardCharsets.UTF_8).length;
                total += fileLen;
                total += CRLF.getBytes(StandardCharsets.UTF_8).length;
            }
        }

        // closing
        total += finalBoundary.length;
        return total;
    }
}

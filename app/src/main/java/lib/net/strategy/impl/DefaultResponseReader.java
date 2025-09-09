package lib.net.strategy.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

import lib.net.connection.IHttpConnection;
import lib.net.strategy.ResponseReader;

public class DefaultResponseReader implements ResponseReader {

//    @Override
//    public String readSuccess(IHttpConnection conn) throws IOException {
//        InputStream stream = conn.getInputStream();
//        String ce = conn.getHeaderField("Content-Encoding");
//        boolean gzip = ce != null && "gzip".equalsIgnoreCase(ce);
//        try (InputStream in = gzip ? new GZIPInputStream(stream) : stream;
//             InputStreamReader r = new InputStreamReader(in, StandardCharsets.UTF_8);
//             BufferedReader br = new BufferedReader(r)) {
//            StringBuilder sb = new StringBuilder(); String line;
//            while ((line = br.readLine()) != null) sb.append(line);
//            return sb.toString();
//        }
//    }

    public String readSuccess(IHttpConnection conn) throws IOException {
        InputStream stream = conn.getInputStream();
        String ce = conn.getHeaderField("Content-Encoding");
        boolean gzip = ce != null && "gzip".equalsIgnoreCase(ce);
        try (InputStream in = gzip ? new GZIPInputStream(stream) : stream;
             InputStreamReader r = new InputStreamReader(in, StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(r)) {
            StringBuilder sb = new StringBuilder();
            char[] buf = new char[8192];
            int n;
            int ticks = 0;
            while ((n = br.read(buf)) != -1) {
                sb.append(buf, 0, n);
                if ((++ticks & 0x0F) == 0 && Thread.currentThread().isInterrupted()) break;
            }
            return sb.toString();
        }
    }
}

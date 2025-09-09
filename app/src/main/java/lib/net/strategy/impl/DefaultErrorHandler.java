package lib.net.strategy.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import lib.net.NetResult;
import lib.net.connection.IHttpConnection;
import lib.net.strategy.ErrorHandler;

public class DefaultErrorHandler implements ErrorHandler {
    @Override
    public NetResult.Error<String> handleError(IHttpConnection conn, int status) {
        String body = "";
        InputStream es = conn.getErrorStream();
        InputStream s = es;
        if (s == null) {
            try {
                s = conn.getInputStream();
            } catch (IOException ignore) {
            }
        }
        if (s != null) {
            try (InputStream in = s;
                 InputStreamReader r = new InputStreamReader(in, StandardCharsets.UTF_8);
                 BufferedReader br = new BufferedReader(r)) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                body = sb.toString();
            } catch (IOException ignore) {
            }
        }
        return new NetResult.Error<>(new Exception("Response code: " + status),
                status,
                body.isEmpty() ? "No error body provided." : body);
    }
}

// com/example/cleannetkit/application/api/UploadApi.java
package com.example.cleannetkit.application.api;

import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import lib.net.ABaseApi;
import lib.net.CancellableFuture;
import lib.net.NetworkManager;
import lib.net.command.MultipartCommand;
import lib.net.strategy.RequestConfigurator;
import lib.net.strategy.retry.PayloadSensitiveRetryPolicy;

public class UploadApi extends ABaseApi {

    private static final String BASE_URL = "https://example.com"; // kendi endpoint’in

    public UploadApi(NetworkManager nm) { super(BASE_URL, nm); }
    public UploadApi(NetworkManager nm, RequestConfigurator rc) { super(BASE_URL, nm, rc); }

    public static class UploadResponse {
        public String id;
        public String url;
    }

    public CancellableFuture<UploadResponse> uploadProfile(File file,
                                                           Map<String,String> fields,
                                                           Map<String,String> headers) {
        Type t = new TypeToken<UploadResponse>() {}.getType();
        MultipartCommand cmd = new MultipartCommand(
                "/upload/profile",
                toHash(headers),
                toHash(fields),
                toHashFile("file", file)
        );

        long bytes = cmd.estimatePayloadBytes();
        // KURAL: büyük upload’larda retry yok
        cmd.withRetryPolicy(new PayloadSensitiveRetryPolicy(bytes, /*maxRetriesForLarge*/ 0));

        return send(cmd, t);
    }

    private static HashMap<String,String> toHash(Map<String,String> m){
        return (m == null || m instanceof HashMap) ? (HashMap<String,String>) m : new HashMap<>(m);
    }
    private static HashMap<String,File> toHashFile(String name, File f){
        HashMap<String,File> map = new HashMap<>();
        if (f != null) map.put(name, f);
        return map;
    }
}

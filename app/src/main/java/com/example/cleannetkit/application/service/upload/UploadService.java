// com/example/cleannetkit/application/UploadService.java
package com.example.cleannetkit.application.service.upload;

import com.example.cleannetkit.application.api.UploadApi;
import com.example.cleannetkit.application.api.UploadApi.UploadResponse;
import com.example.cleannetkit.domain.upload.UploadUseCase;

import lib.concurrent.CancellableFuture;
import lib.net.NetworkManager;
public class UploadService implements UploadUseCase {
    private final UploadApi api;
    public UploadService(NetworkManager nm) {
        // İstersen composite RC (Default + Auth + Idempotency) ile kur
        this.api = new UploadApi(nm);
    }

    @Override
    public CancellableFuture<UploadResponse> handle(Command c) {
        return api.uploadProfile(c.getFile(), c.getFields(), c.getHeaders());
    }
}
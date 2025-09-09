// com/example/cleannetkit/application/service/posts/DeletePostService.java
package com.example.cleannetkit.application.service.posts;

import com.example.cleannetkit.application.api.PostsApi;
import com.example.cleannetkit.domain.posts.DeletePostUseCase;

import lib.net.CancellableFuture;
import lib.net.NetworkManager;

public class DeletePostService implements DeletePostUseCase {
    private final PostsApi api;
    public DeletePostService(NetworkManager nm) { this.api = new PostsApi(nm); }

    @Override
    public CancellableFuture<Void> handle(Command command) {
        return api.deletePostF(command.getId());
    }
}

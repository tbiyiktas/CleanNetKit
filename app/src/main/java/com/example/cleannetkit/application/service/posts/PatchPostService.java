// com/example/cleannetkit/application/service/posts/PatchPostService.java
package com.example.cleannetkit.application.service.posts;

import com.example.cleannetkit.application.api.PostsApi;
import com.example.cleannetkit.domain.model.Post;
import com.example.cleannetkit.domain.posts.PatchPostUseCase;

import java.util.Map;
import lib.net.CancellableFuture;
import lib.net.NetworkManager;

public class PatchPostService implements PatchPostUseCase {
    private final PostsApi api;
    public PatchPostService(NetworkManager nm) { this.api = new PostsApi(nm); }

    @Override
    public CancellableFuture<Post> handle(Command c) {
        Map<String,Object> patch = c.getPatch(); // doğrudan patch map’ini gönderiyoruz
        return api.patchPostDtoF(c.getId(), patch)
                .thenApplyC(com.example.cleannetkit.data.remote.dto.PostMapper::toDomain);
    }
}

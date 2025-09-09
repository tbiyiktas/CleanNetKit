// com/example/cleannetkit/application/service/posts/GetPostService.java
package com.example.cleannetkit.application.service.posts;

import com.example.cleannetkit.application.api.PostsApi;
import com.example.cleannetkit.data.remote.dto.PostMapper;
import com.example.cleannetkit.domain.model.Post;
import com.example.cleannetkit.domain.posts.GetPostUseCase;

import lib.net.CancellableFuture;
import lib.net.NetworkManager;

public class GetPostService implements GetPostUseCase {
    private final PostsApi api;
    public GetPostService(NetworkManager nm) { this.api = new PostsApi(nm); }

    @Override
    public CancellableFuture<Post> handle(Command command) {
        return api.getPostDtoF(command.getId())
                .thenApplyC(PostMapper::toDomain);
    }
}

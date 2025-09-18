// com/example/cleannetkit/application/service/posts/GetPostsService.java
package com.example.cleannetkit.application.service.posts;

import com.example.cleannetkit.application.api.PostsApi;
import com.example.cleannetkit.data.remote.dto.PostMapper;
import com.example.cleannetkit.domain.model.Post;
import com.example.cleannetkit.domain.posts.GetPostsUseCase;

import java.util.List;
import lib.concurrent.CancellableFuture;
import lib.net.NetworkManager;
public class GetPostsService implements GetPostsUseCase {
    private final PostsApi api;
    public GetPostsService(NetworkManager nm) { this.api = new PostsApi(nm); }

    @Override
    public CancellableFuture<List<Post>> handle(Command command) {
        if (command != null && command.getUserId() != null) {
            return api.getPostsByUserDtoF(command.getUserId())
                    .thenApplyC(PostMapper::toDomainList);
        }
        return api.getPostsDtoF()
                .thenApplyC(PostMapper::toDomainList);
    }
}
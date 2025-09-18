// com/example/cleannetkit/application/service/posts/UpdatePostService.java
package com.example.cleannetkit.application.service.posts;

import com.example.cleannetkit.application.api.PostsApi;
import com.example.cleannetkit.data.remote.dto.PostDto;
import com.example.cleannetkit.data.remote.dto.PostMapper;
import com.example.cleannetkit.domain.model.Post;
import com.example.cleannetkit.domain.posts.UpdatePostUseCase;

import lib.concurrent.CancellableFuture;
import lib.net.NetworkManager;
public class UpdatePostService implements UpdatePostUseCase {
    private final PostsApi api;
    public UpdatePostService(NetworkManager nm) { this.api = new PostsApi(nm); }

    @Override
    public CancellableFuture<Post> handle(Command c) {
        PostDto dto = PostMapper.toDto(c.getPost());
        return api.updatePostPutDtoF(c.getId(), dto)
                .thenApplyC(PostMapper::toDomain);
    }
}

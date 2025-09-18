// com/example/cleannetkit/application/service/posts/CreatePostService.java
package com.example.cleannetkit.application.service.posts;

import com.example.cleannetkit.application.api.PostsApi;
import com.example.cleannetkit.data.remote.dto.PostDto;
import com.example.cleannetkit.data.remote.dto.PostMapper;
import com.example.cleannetkit.domain.model.Post;
import com.example.cleannetkit.domain.posts.CreatePostUseCase;

import lib.concurrent.CancellableFuture;
import lib.net.NetworkManager;

public class CreatePostService implements CreatePostUseCase {
    private final PostsApi api;
    public CreatePostService(NetworkManager nm) { this.api = new PostsApi(nm); }

    @Override
    public CancellableFuture<Post> handle(Command command) {
        PostDto dto = PostMapper.toDto(command.getPost());
        return api.createPostDtoF(dto)
                .thenApplyC(PostMapper::toDomain);
    }
}
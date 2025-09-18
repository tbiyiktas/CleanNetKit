// com/example/cleannetkit/application/service/posts/GetCommentsForPostService.java
package com.example.cleannetkit.application.service.posts;

import com.example.cleannetkit.application.api.PostsApi;
import com.example.cleannetkit.data.remote.dto.CommentMapper;
import com.example.cleannetkit.domain.model.Comment;
import com.example.cleannetkit.domain.posts.GetCommentsForPostUseCase;

import java.util.List;
import lib.concurrent.CancellableFuture;
import lib.net.NetworkManager;
public class GetCommentsForPostService implements GetCommentsForPostUseCase {
    private final PostsApi api;
    public GetCommentsForPostService(NetworkManager nm) { this.api = new PostsApi(nm); }

    @Override
    public CancellableFuture<List<Comment>> handle(Command command) {
        return api.getCommentsForPostDtoF(command.getPostId())
                .thenApplyC(CommentMapper::toDomainList);
    }
}

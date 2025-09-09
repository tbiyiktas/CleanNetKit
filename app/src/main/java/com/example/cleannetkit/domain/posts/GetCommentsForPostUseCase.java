// com/example/cleannetkit/domain/posts/GetCommentsForPostUseCase.java
package com.example.cleannetkit.domain.posts;

import com.example.cleannetkit.domain.model.Comment;
import java.util.List;
import lib.net.CancellableFuture;

public interface GetCommentsForPostUseCase {
    CancellableFuture<List<Comment>> handle(Command command);

    class Command {
        private int postId;
        public Command() {}
        public Command(int postId) { this.postId = postId; }
        public int getPostId() { return postId; }
        public void setPostId(int postId) { this.postId = postId; }
    }
}

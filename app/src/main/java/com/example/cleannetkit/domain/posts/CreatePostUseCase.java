// com/example/cleannetkit/domain/posts/CreatePostUseCase.java
package com.example.cleannetkit.domain.posts;

import com.example.cleannetkit.domain.model.Post;
import lib.net.CancellableFuture;

public interface CreatePostUseCase {
    CancellableFuture<Post> handle(Command command);

    class Command {
        private Post post;
        public Command() {}
        public Command(Post post) { this.post = post; }
        public Post getPost() { return post; }
        public void setPost(Post post) { this.post = post; }
    }
}

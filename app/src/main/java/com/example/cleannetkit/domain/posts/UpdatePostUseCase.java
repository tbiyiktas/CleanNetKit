// com/example/cleannetkit/domain/posts/UpdatePostUseCase.java
package com.example.cleannetkit.domain.posts;

import com.example.cleannetkit.domain.model.Post;
import lib.net.CancellableFuture;

public interface UpdatePostUseCase {
    CancellableFuture<Post> handle(Command command);

    class Command {
        private int id;
        private Post post;

        public Command() {}
        public Command(int id, Post post) { this.id = id; this.post = post; }

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public Post getPost() { return post; }
        public void setPost(Post post) { this.post = post; }
    }
}

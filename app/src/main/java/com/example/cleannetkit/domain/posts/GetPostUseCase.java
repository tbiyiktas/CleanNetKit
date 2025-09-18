// com/example/cleannetkit/domain/posts/GetPostUseCase.java
package com.example.cleannetkit.domain.posts;

import com.example.cleannetkit.domain.model.Post;
import lib.concurrent.CancellableFuture;

public interface GetPostUseCase {
    CancellableFuture<Post> handle(Command command);

    class Command {
        private int id;
        public Command() {}
        public Command(int id) { this.id = id; }
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
    }
}

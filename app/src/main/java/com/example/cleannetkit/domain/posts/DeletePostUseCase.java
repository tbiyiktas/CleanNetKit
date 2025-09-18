// com/example/cleannetkit/domain/posts/DeletePostUseCase.java
package com.example.cleannetkit.domain.posts;

import lib.concurrent.CancellableFuture;

public interface DeletePostUseCase {
    CancellableFuture<Void> handle(Command command);

    class Command {
        private int id;
        public Command() {}
        public Command(int id) { this.id = id; }
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
    }
}

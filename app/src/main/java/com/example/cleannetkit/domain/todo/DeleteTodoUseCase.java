// com/example/cleannetkit/application/DeleteTodoUseCase.java
package com.example.cleannetkit.domain.todo;

import lib.concurrent.CancellableFuture;

public interface DeleteTodoUseCase {
    CancellableFuture<Void> handle(Command command);

    class Command {
        private int id;
        public Command() {}
        public Command(int id) { this.id = id; }
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
    }
}

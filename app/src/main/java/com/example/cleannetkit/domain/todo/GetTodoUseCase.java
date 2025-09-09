// com/example/cleannetkit/domain/todo/GetTodoUseCase.java
package com.example.cleannetkit.domain.todo;

import com.example.cleannetkit.domain.model.Todo;
import lib.net.CancellableFuture;

public interface GetTodoUseCase {
    CancellableFuture<Todo> handle(Command command);

    class Command {
        private int id;
        public Command() {}
        public Command(int id) { this.id = id; }
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
    }
}

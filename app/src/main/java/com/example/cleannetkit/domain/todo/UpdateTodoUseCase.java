// com/example/cleannetkit/application/UpdateTodoUseCase.java
package com.example.cleannetkit.domain.todo;

import com.example.cleannetkit.domain.model.Todo;
import lib.net.CancellableFuture;

public interface UpdateTodoUseCase {
    CancellableFuture<Todo> handle(Command command);

    class Command {
        private int id;
        private Todo todo;
        private boolean partial; // true => PATCH, false => PUT

        public Command() {}
        public Command(int id, Todo todo, boolean partial) {
            this.id = id; this.todo = todo; this.partial = partial;
        }

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public Todo getTodo() { return todo; }
        public void setTodo(Todo todo) { this.todo = todo; }

        public boolean isPartial() { return partial; }
        public void setPartial(boolean partial) { this.partial = partial; }
    }
}


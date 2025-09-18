package com.example.cleannetkit.domain.todo;

import com.example.cleannetkit.domain.model.Todo;
import lib.concurrent.CancellableFuture;

public interface CreateTodoUseCase {
    CancellableFuture<Todo> handle(Command command);

    class Command {
        private Todo todo;

        public Command() {}
        public Command(Todo todo) { this.todo = todo; }

        public Todo getTodo() { return todo; }
        public void setTodo(Todo todo) { this.todo = todo; }
    }
}

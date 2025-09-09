// com/example/cleannetkit/application/service/todo/GetTodoService.java
package com.example.cleannetkit.application.service.todo;

import com.example.cleannetkit.application.api.TodoApi;
import com.example.cleannetkit.data.remote.dto.TodoMapper;
import com.example.cleannetkit.domain.model.Todo;
import com.example.cleannetkit.domain.todo.GetTodoUseCase;

import lib.net.CancellableFuture;
import lib.net.NetworkManager;

public class GetTodoService implements GetTodoUseCase {
    private final TodoApi api;
    public GetTodoService(NetworkManager nm) { this.api = new TodoApi(nm); }

    @Override
    public CancellableFuture<Todo> handle(Command command) {
        return api.getTodoByIdDtoF(command.getId())
                .thenApplyC(TodoMapper::toDomain);
    }
}

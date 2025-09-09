// com/example/cleannetkit/application/service/todo/CreateTodoService.java
package com.example.cleannetkit.application.service.todo;

import com.example.cleannetkit.application.api.TodoApi;
import com.example.cleannetkit.data.remote.dto.TodoDto;
import com.example.cleannetkit.data.remote.dto.TodoMapper;
import com.example.cleannetkit.domain.model.Todo;
import com.example.cleannetkit.domain.todo.CreateTodoUseCase;

import lib.net.CancellableFuture;
import lib.net.NetworkManager;

public class CreateTodoService implements CreateTodoUseCase {
    private final TodoApi api;
    public CreateTodoService(NetworkManager nm) { this.api = new TodoApi(nm); }

    @Override
    public CancellableFuture<Todo> handle(Command command) {
        TodoDto dto = TodoMapper.toDto(command.getTodo());
        return api.createTodoDtoF(dto)
                .thenApplyC(TodoMapper::toDomain);
    }
}

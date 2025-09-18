// com/example/cleannetkit/application/service/todo/UpdateTodoService.java
package com.example.cleannetkit.application.service.todo;

import com.example.cleannetkit.application.api.TodoApi;
import com.example.cleannetkit.data.remote.dto.TodoDto;
import com.example.cleannetkit.data.remote.dto.TodoMapper;
import com.example.cleannetkit.domain.model.Todo;
import com.example.cleannetkit.domain.todo.UpdateTodoUseCase;

import java.util.HashMap;
import java.util.Map;

import lib.concurrent.CancellableFuture;
import lib.net.NetworkManager;

public class UpdateTodoService implements UpdateTodoUseCase {
    private final TodoApi api;
    public UpdateTodoService(NetworkManager nm){ this.api = new TodoApi(nm); }

    @Override
    public CancellableFuture<Todo> handle(Command c){
        if (c.isPartial()) {
            // PATCH — minimal gövde
            Map<String,Object> patch = new HashMap<>();
            Todo t = c.getTodo();
            if (t.getTitle() != null) patch.put("title", t.getTitle());
            patch.put("completed", t.isCompleted());
            patch.put("userId", t.getUserId());
            return api.patchTodoDtoF(c.getId(), patch)
                    .thenApplyC(TodoMapper::toDomain);
        } else {
            // PUT — tam DTO
            TodoDto dto = TodoMapper.toDto(c.getTodo());
            return api.updateTodoPutDtoF(c.getId(), dto)
                    .thenApplyC(TodoMapper::toDomain);
        }
    }
}
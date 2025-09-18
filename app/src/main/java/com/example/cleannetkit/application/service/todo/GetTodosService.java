// com/example/cleannetkit/application/service/todo/GetTodosService.java
package com.example.cleannetkit.application.service.todo;

import com.example.cleannetkit.application.api.TodoApi;
import com.example.cleannetkit.data.remote.dto.TodoMapper;
import com.example.cleannetkit.domain.model.Todo;
import com.example.cleannetkit.domain.todo.GetTodosUseCase;

import java.util.List;
import lib.concurrent.CancellableFuture;
import lib.net.NetworkManager;
public class GetTodosService implements GetTodosUseCase {
    private final TodoApi api;
    public GetTodosService(NetworkManager nm) { this.api = new TodoApi(nm); }

    @Override
    public CancellableFuture<List<Todo>> handle(Command command) {
//        if (command != null && command.getUserId() != null) {
//            return api.getTodosByUserDtoF(command.getUserId())
//                    .thenApplyC(TodoMapper::toDomainList);
//        }
        return api.getTodosDtoF()
                .thenApplyC(TodoMapper::toDomainList);
    }
}

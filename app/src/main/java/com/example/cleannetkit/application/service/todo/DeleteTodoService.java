// com/example/cleannetkit/application/service/todo/DeleteTodoService.java
package com.example.cleannetkit.application.service.todo;

import com.example.cleannetkit.application.api.TodoApi;
import com.example.cleannetkit.domain.todo.DeleteTodoUseCase;

import lib.concurrent.CancellableFuture;
import lib.net.NetworkManager;
public class DeleteTodoService implements DeleteTodoUseCase {
    private final TodoApi api;
    public DeleteTodoService(NetworkManager nm){ this.api = new TodoApi(nm);
    }

    @Override
    public CancellableFuture<Void> handle(Command c){
        return api.deleteTodoF(c.getId());
    }
}
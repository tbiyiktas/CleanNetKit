package com.example.cleannetkit.domain.todo;

import com.example.cleannetkit.domain.model.Todo;

import java.util.List;

import lib.concurrent.CancellableFuture;

public interface GetTodosUseCase {

    CancellableFuture<List<Todo>> handle(Command command);

    class Command {
        private Integer userId;
        public static Command all() {
            return new Command();
        }

        public static Command byUser(Integer userId) {
            Command command = new Command();
            command.userId = userId;
            return  command;
        }

        public Integer getUserId() {
            return userId;
        }
    }
}

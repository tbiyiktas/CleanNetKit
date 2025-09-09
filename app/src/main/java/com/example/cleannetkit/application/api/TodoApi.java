// com/example/cleannetkit/application/api/TodoApi.java
package com.example.cleannetkit.application.api;

import com.example.cleannetkit.data.remote.dto.TodoDto;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lib.net.ABaseApi;
import lib.net.CancellableFuture;
import lib.net.NetworkManager;
import lib.net.command.ACommand;
import lib.net.command.DeleteCommand;
import lib.net.command.GetCommand;
import lib.net.command.PatchCommand;
import lib.net.command.PostCommand;
import lib.net.command.PutCommand;
import lib.net.strategy.RequestConfigurator;
import lib.net.strategy.retry.PayloadSensitiveRetryPolicy;

public class TodoApi extends ABaseApi {

    private static final String BASE_URL = "https://jsonplaceholder.typicode.com";
    private static final Gson GSON = new Gson();

    public TodoApi(NetworkManager nm) { super(BASE_URL, nm); }
    public TodoApi(NetworkManager nm, RequestConfigurator rc) { super(BASE_URL, nm, rc); }

    /* READ */
    public CancellableFuture<List<TodoDto>> getTodosDtoF() {
        Type t = new TypeToken<List<TodoDto>>() {}.getType();
        ACommand cmd = new GetCommand("/todos", null, null);
        return send(cmd, t);
    }

    public CancellableFuture<List<TodoDto>> getTodosByUserDtoF(int userId) {
        Type t = new TypeToken<List<TodoDto>>() {}.getType();
        HashMap<String,String> q = new HashMap<>();
        q.put("userId", String.valueOf(userId));
        ACommand cmd = new GetCommand("/todos", q, null);
        return send(cmd, t);
    }

    /* CREATE */
    public CancellableFuture<TodoDto> createTodoDtoF(TodoDto dto) {
        Type t = new TypeToken<TodoDto>() {}.getType();
        String body = GSON.toJson(dto);
        long bytes = body.getBytes(StandardCharsets.UTF_8).length;
        ACommand cmd = new PostCommand("/todos", body, null)
                .withRetryPolicy(new lib.net.strategy.retry.PayloadSensitiveRetryPolicy(bytes, 3));
        return send(cmd, t);
    }

    /* UPDATE (PUT) */
    public CancellableFuture<TodoDto> updateTodoPutDtoF(int id, TodoDto dto) {
        Type t = new TypeToken<TodoDto>() {}.getType();
        String body = GSON.toJson(dto);
        long bytes = body.getBytes(StandardCharsets.UTF_8).length;
        ACommand cmd = new PutCommand("/todos/" + id, body, null)
                .withRetryPolicy(new lib.net.strategy.retry.PayloadSensitiveRetryPolicy(bytes, 2));
        return send(cmd, t);
    }

    /* PARTIAL UPDATE (PATCH) */
    public CancellableFuture<TodoDto> patchTodoDtoF(int id, Map<String,Object> patch) {
        Type t = new TypeToken<TodoDto>() {}.getType();
        String body = GSON.toJson(patch);
        long bytes = body.getBytes(StandardCharsets.UTF_8).length;
        ACommand cmd = new PatchCommand("/todos/" + id, body, null)
                .withRetryPolicy(new lib.net.strategy.retry.PayloadSensitiveRetryPolicy(bytes, 2));
        return send(cmd, t);
    }

    /* DELETE */
    public CancellableFuture<Void> deleteTodoF(int id) {
        Type t = new TypeToken<Void>() {}.getType();
        ACommand cmd = new DeleteCommand("/todos/" + id, null, null);
        return send(cmd, t);
    }

    public CancellableFuture<TodoDto> getTodoByIdDtoF(int id) {
        Type t = new TypeToken<TodoDto>() {}.getType();
        ACommand cmd = new GetCommand("/todos/" + id, null, null);
        return send(cmd, t);
    }
}

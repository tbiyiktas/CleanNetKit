// com/example/cleannetkit/application/api/PostsApi.java
package com.example.cleannetkit.application.api;

import com.example.cleannetkit.data.remote.dto.CommentDto;
import com.example.cleannetkit.data.remote.dto.PostDto;
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

public class PostsApi extends ABaseApi {

    private static final String BASE_URL = "https://jsonplaceholder.typicode.com";
    private static final Gson GSON = new Gson();

    public PostsApi(NetworkManager nm) { super(BASE_URL, nm); }
    public PostsApi(NetworkManager nm, RequestConfigurator rc) { super(BASE_URL, nm, rc); }

    /* READ */
    public CancellableFuture<List<PostDto>> getPostsDtoF() {
        Type t = new TypeToken<List<PostDto>>() {}.getType();
        ACommand cmd = new GetCommand("/posts", null, null);
        return send(cmd, t);
    }

    public CancellableFuture<List<PostDto>> getPostsByUserDtoF(int userId) {
        Type t = new TypeToken<List<PostDto>>() {}.getType();
        HashMap<String,String> q = new HashMap<>();
        q.put("userId", String.valueOf(userId));
        ACommand cmd = new GetCommand("/posts", q, null);
        return send(cmd, t);
    }

    public CancellableFuture<PostDto> getPostDtoF(int id) {
        Type t = new TypeToken<PostDto>() {}.getType();
        ACommand cmd = new GetCommand("/posts/" + id, null, null);
        return send(cmd, t);
    }

    public CancellableFuture<List<CommentDto>> getCommentsForPostDtoF(int postId) {
        Type t = new TypeToken<List<CommentDto>>() {}.getType();
        HashMap<String,String> q = new HashMap<>();
        q.put("postId", String.valueOf(postId));
        ACommand cmd = new GetCommand("/comments", q, null);
        return send(cmd, t);
    }

    /* CREATE */
    public CancellableFuture<PostDto> createPostDtoF(PostDto dto) {
        Type t = new TypeToken<PostDto>() {}.getType();
        String body = GSON.toJson(dto);
        long bytes = body.getBytes(StandardCharsets.UTF_8).length;
        ACommand cmd = new PostCommand("/posts", body, null)
                .withRetryPolicy(new PayloadSensitiveRetryPolicy(bytes, 3));
        return send(cmd, t);
    }

    /* UPDATE (PUT) */
    public CancellableFuture<PostDto> updatePostPutDtoF(int id, PostDto dto) {
        Type t = new TypeToken<PostDto>() {}.getType();
        String body = GSON.toJson(dto);
        long bytes = body.getBytes(StandardCharsets.UTF_8).length;
        ACommand cmd = new PutCommand("/posts/" + id, body, null)
                .withRetryPolicy(new PayloadSensitiveRetryPolicy(bytes, 2));
        return send(cmd, t);
    }

    /* PARTIAL UPDATE (PATCH) */
    public CancellableFuture<PostDto> patchPostDtoF(int id, Map<String,Object> patch) {
        Type t = new TypeToken<PostDto>() {}.getType();
        String body = GSON.toJson(patch);
        long bytes = body.getBytes(StandardCharsets.UTF_8).length;
        ACommand cmd = new PatchCommand("/posts/" + id, body, null)
                .withRetryPolicy(new PayloadSensitiveRetryPolicy(bytes, 2));
        return send(cmd, t);
    }

    /* DELETE */
    public CancellableFuture<Void> deletePostF(int id) {
        Type t = new TypeToken<Void>() {}.getType();
        ACommand cmd = new DeleteCommand("/posts/" + id, null, null);
        return send(cmd, t);
    }
}

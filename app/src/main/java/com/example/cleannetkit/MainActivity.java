package com.example.cleannetkit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cleannetkit.application.service.todo.GetTodosService;
import com.example.cleannetkit.domain.todo.GetTodosUseCase;

import com.example.cleannetkit.application.service.posts.CreatePostService;
import com.example.cleannetkit.application.service.posts.GetPostsService;
import com.example.cleannetkit.application.service.posts.GetPostService;
import com.example.cleannetkit.application.service.posts.GetCommentsForPostService;
import com.example.cleannetkit.application.service.posts.UpdatePostService;
import com.example.cleannetkit.application.service.posts.PatchPostService;
import com.example.cleannetkit.application.service.posts.DeletePostService;

import com.example.cleannetkit.domain.posts.CreatePostUseCase;
import com.example.cleannetkit.domain.posts.GetPostsUseCase;
import com.example.cleannetkit.domain.posts.GetPostUseCase;
import com.example.cleannetkit.domain.posts.GetCommentsForPostUseCase;
import com.example.cleannetkit.domain.posts.UpdatePostUseCase;
import com.example.cleannetkit.domain.posts.PatchPostUseCase;
import com.example.cleannetkit.domain.posts.DeletePostUseCase;

import com.example.cleannetkit.domain.model.Comment;
import com.example.cleannetkit.domain.model.Post;
import com.example.cleannetkit.domain.model.Todo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicInteger;

import lib.concurrent.CancellableFuture;
import lib.net.HttpException;
import lib.net.NetworkManager;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private TextView statusTextView;
    private ProgressBar progressBar;
    private Button startRequestsButton;   // (mevcut) Todos için
    private Button postsDemoButton;       // (yeni) Posts CRUD demo için

    private int totalRequests = 0;

    // Todos future
    private CancellableFuture<List<Todo>> todosFuture;

    // Posts future referansları (iptal için)
    private CancellableFuture<Post> createPostF;
    private CancellableFuture<List<Post>> listPostsF;
    private CancellableFuture<Post> onePostF;
    private CancellableFuture<List<Comment>> commentsForPostF;
    private CancellableFuture<Post> putPostF;
    private CancellableFuture<Post> patchPostF;
    private CancellableFuture<Void> deletePostF;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusTextView = findViewById(R.id.statusTextView);
        progressBar = findViewById(R.id.progressBar);
        startRequestsButton = findViewById(R.id.startRequestsButton);
        postsDemoButton = findViewById(R.id.postsDemoButton); // XML’de tanımlı

        startRequestsButton.setOnClickListener(v -> startRequests());
        postsDemoButton.setOnClickListener(v -> startPostsCrudDemo());

        //startActivity(new Intent(this, TodoMenuActivity.class));
    }

    /* ================== ESKİ: Todos örneği (dokunmuyoruz) ================== */
    private void startRequests() {
        totalRequests++;

        startRequestsButton.setVisibility(View.GONE);
        postsDemoButton.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        statusTextView.setText("İstekler başlatılıyor...");

        NetworkManager networkManager = MyApplication.getNetworkManager();
        GetTodosService service = new GetTodosService(networkManager);

        todosFuture = service.handle(GetTodosUseCase.Command.all());

        todosFuture.thenAccept(items -> {
            statusTextView.setText("Toplam: " + (items == null ? 0 : items.size()));
            progressBar.setVisibility(View.GONE);
            startRequestsButton.setVisibility(View.VISIBLE);
            postsDemoButton.setVisibility(View.VISIBLE);
        }).exceptionally(ex -> {
            progressBar.setVisibility(View.GONE);
            startRequestsButton.setVisibility(View.VISIBLE);
            postsDemoButton.setVisibility(View.VISIBLE);

            Throwable cause = (ex.getCause() != null) ? ex.getCause() : ex;
            if (cause instanceof CancellationException) {
                return null; // iptal sessiz
            }
            String msg;
            if (cause instanceof HttpException) {
                HttpException he = (HttpException) cause;
                msg = "Hata: " + he.code + " - " + he.body;
            } else {
                msg = "İstek hatası: " + cause.getMessage();
            }
            statusTextView.setText(msg);
            return null;
        });
    }

    /* ================== YENİ: Posts CRUD demo (Services ile) ================== */
    private void startPostsCrudDemo() {
        startRequestsButton.setVisibility(View.GONE);
        postsDemoButton.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        statusTextView.setText("Posts CRUD demosu başlıyor...");

        NetworkManager nm = MyApplication.getNetworkManager();

        // Servisler
        CreatePostService createSrv = new CreatePostService(nm);
        GetPostsService   listSrv   = new GetPostsService(nm);
        GetPostService    oneSrv    = new GetPostService(nm);
        GetCommentsForPostService commentsSrv = new GetCommentsForPostService(nm);
        UpdatePostService updateSrv = new UpdatePostService(nm);
        PatchPostService  patchSrv  = new PatchPostService(nm);
        DeletePostService deleteSrv = new DeletePostService(nm);

        // Tamamlanacak 7 operasyon
        final AtomicInteger pending = new AtomicInteger(7);

        // CREATE (id=null, userId=1)
        Post toCreate = new Post(null, 1, "başlık", "içerik");
        createPostF = createSrv.handle(new CreatePostUseCase.Command(toCreate));
        createPostF.thenAccept(p -> {
            appendLine("CREATE ✅ id=" + (p == null ? "-" : p.getId()));
            onPostOpDone(pending);
        }).exceptionally(ex -> {
            handlePostOpError("CREATE", ex, pending);
            return null;
        });

        // READ (list)
        listPostsF = listSrv.handle(GetPostsUseCase.Command.all());
        listPostsF.thenAccept(list -> {
            appendLine("GET /posts ✅ count=" + (list == null ? 0 : list.size()));
            onPostOpDone(pending);
        }).exceptionally(ex -> {
            handlePostOpError("GET /posts", ex, pending);
            return null;
        });

        // READ one
        onePostF = oneSrv.handle(new GetPostUseCase.Command(1));
        onePostF.thenAccept(p -> {
            appendLine("GET /posts/1 ✅ title=" + (p == null ? "-" : p.getTitle()));
            onPostOpDone(pending);
        }).exceptionally(ex -> {
            handlePostOpError("GET /posts/1", ex, pending);
            return null;
        });

        // READ comments
        commentsForPostF = commentsSrv.handle(new GetCommentsForPostUseCase.Command(1));
        commentsForPostF.thenAccept(comments -> {
            appendLine("GET /comments?postId=1 ✅ count=" + (comments == null ? 0 : comments.size()));
            onPostOpDone(pending);
        }).exceptionally(ex -> {
            handlePostOpError("GET /comments?postId=1", ex, pending);
            return null;
        });

        // UPDATE (PUT)
        Post updated = new Post(1, 1, "yeni başlık", "yeni içerik"); // id=1, userId=1
        putPostF = updateSrv.handle(new UpdatePostUseCase.Command(1, updated));
        putPostF.thenAccept(p -> {
            appendLine("PUT /posts/1 ✅ title=" + (p == null ? "-" : p.getTitle()));
            onPostOpDone(pending);
        }).exceptionally(ex -> {
            handlePostOpError("PUT /posts/1", ex, pending);
            return null;
        });

        // PATCH
        Map<String,Object> patch = new HashMap<>();
        patch.put("title", "sadece başlık güncellendi");
        patchPostF = patchSrv.handle(new PatchPostUseCase.Command(1, patch));
        patchPostF.thenAccept(p -> {
            appendLine("PATCH /posts/1 ✅ title=" + (p == null ? "-" : p.getTitle()));
            onPostOpDone(pending);
        }).exceptionally(ex -> {
            handlePostOpError("PATCH /posts/1", ex, pending);
            return null;
        });

        // DELETE
        deletePostF = deleteSrv.handle(new DeletePostUseCase.Command(1));
        deletePostF.thenAccept(v -> {
            appendLine("DELETE /posts/1 ✅");
            onPostOpDone(pending);
        }).exceptionally(ex -> {
            handlePostOpError("DELETE /posts/1", ex, pending);
            return null;
        });
    }

    private void onPostOpDone(AtomicInteger pending) {
        if (pending.decrementAndGet() == 0) {
            progressBar.setVisibility(View.GONE);
            startRequestsButton.setVisibility(View.VISIBLE);
            postsDemoButton.setVisibility(View.VISIBLE);
            appendLine("Posts CRUD demo bitti 🎉");
        }
    }

    private void handlePostOpError(String op, Throwable ex, AtomicInteger pending) {
        Throwable cause = (ex.getCause() != null) ? ex.getCause() : ex;
        if (cause instanceof CancellationException) {
            // sessiz geç
        } else if (cause instanceof HttpException) {
            HttpException he = (HttpException) cause;
            appendLine(op + " ❌ " + he.code + " - " + he.body);
        } else {
            appendLine(op + " ❌ " + cause.getMessage());
        }
        onPostOpDone(pending);
    }

    private void appendLine(String line) {
        String prev = statusTextView.getText() == null ? "" : statusTextView.getText().toString();
        if (prev.isEmpty()) {
            statusTextView.setText(line);
        } else {
            statusTextView.setText(prev + "\n" + line);
        }
    }

    @Override
    protected void onDestroy() {
        // Todos
        if (todosFuture != null) todosFuture.cancel(true);

        // Posts – hepsini iptal et
        if (createPostF != null) createPostF.cancel(true);
        if (listPostsF != null) listPostsF.cancel(true);
        if (onePostF != null) onePostF.cancel(true);
        if (commentsForPostF != null) commentsForPostF.cancel(true);
        if (putPostF != null) putPostF.cancel(true);
        if (patchPostF != null) patchPostF.cancel(true);
        if (deletePostF != null) deletePostF.cancel(true);

        super.onDestroy();
    }
}

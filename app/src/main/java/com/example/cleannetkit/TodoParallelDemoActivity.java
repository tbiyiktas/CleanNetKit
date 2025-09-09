package com.example.cleannetkit;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cleannetkit.application.service.todo.CreateTodoService;
import com.example.cleannetkit.application.service.todo.DeleteTodoService;
import com.example.cleannetkit.application.service.todo.GetTodoService;
import com.example.cleannetkit.application.service.todo.GetTodosService;

import com.example.cleannetkit.domain.model.Todo;
import com.example.cleannetkit.domain.todo.CreateTodoUseCase;
import com.example.cleannetkit.domain.todo.DeleteTodoUseCase;
import com.example.cleannetkit.domain.todo.GetTodoUseCase;
import com.example.cleannetkit.domain.todo.GetTodosUseCase;

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicInteger;

import lib.net.CancellableFuture;
import lib.net.HttpException;
import lib.net.NetworkManager;

public class TodoParallelDemoActivity extends AppCompatActivity {

    private Button btnRun, btnCancel, btnBack;
    private ProgressBar progress;
    private TextView tvCreate, tvGetAll, tvGetById, tvDelete;

    private CancellableFuture<Todo> fCreate, fGetById;
    private CancellableFuture<List<Todo>> fGetAll;
    private CancellableFuture<Void> fDelete;

    private volatile boolean isRunning = false;
    private AtomicInteger pending; // her run’da 4’ten geri sayar

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_todo_parallel_demo);

        btnRun   = findViewById(R.id.btnRun);
        btnCancel= findViewById(R.id.btnCancel);
        btnBack  = findViewById(R.id.btnBack);
        progress = findViewById(R.id.progress);
        tvCreate = findViewById(R.id.tvCreate);
        tvGetAll = findViewById(R.id.tvGetAll);
        tvGetById= findViewById(R.id.tvGetById);
        tvDelete = findViewById(R.id.tvDelete);

        btnBack.setOnClickListener(v -> finish());
        btnRun.setOnClickListener(v -> runParallel());
        btnCancel.setOnClickListener(v -> cancelAll(false));

        setRunning(false);
        resetTexts();
    }

    private void runParallel() {
        // Önce olası eski run’ı iptal et & UI resetle
        cancelAll(true);
        resetTexts();
        setRunning(true);

        NetworkManager nm = MyApplication.getNetworkManager();
        CreateTodoService createSrv = new CreateTodoService(nm);
        GetTodosService   getAllSrv = new GetTodosService(nm);
        GetTodoService    getByIdSrv= new GetTodoService(nm);
        DeleteTodoService deleteSrv = new DeleteTodoService(nm);

        pending = new AtomicInteger(4);

        // 1) CREATE
        tvCreate.setText("CREATE: running...");
        Todo toCreate = new Todo(1, "parallel create", false);
        fCreate = createSrv.handle(new CreateTodoUseCase.Command(toCreate));
        fCreate.thenAccept(res ->
                        tvCreate.setText("CREATE: ✅ id=" + (res==null? "-" : res.getId()))
                ).exceptionally(ex -> { tvCreate.setText("CREATE: " + errMsg(ex)); return null; })
                .thenRun(this::onOneDone);

        // 2) GET all
        tvGetAll.setText("GET /todos: running...");
        fGetAll = getAllSrv.handle(GetTodosUseCase.Command.all());
        fGetAll.thenAccept(list ->
                        tvGetAll.setText("GET /todos: ✅ count=" + (list==null? 0 : list.size()))
                ).exceptionally(ex -> { tvGetAll.setText("GET /todos: " + errMsg(ex)); return null; })
                .thenRun(this::onOneDone);

        // 3) GET by id (1)
        tvGetById.setText("GET /todos/1: running...");
        fGetById = getByIdSrv.handle(new GetTodoUseCase.Command(1));
        fGetById.thenAccept(t ->
                        tvGetById.setText("GET /todos/1: ✅ title=" + (t==null? "-" : t.getTitle()))
                ).exceptionally(ex -> { tvGetById.setText("GET /todos/1: " + errMsg(ex)); return null; })
                .thenRun(this::onOneDone);

        // 4) DELETE (1)
        tvDelete.setText("DELETE /todos/1: running...");
        fDelete = deleteSrv.handle(new DeleteTodoUseCase.Command(1));
        fDelete.thenAccept(v ->
                        tvDelete.setText("DELETE /todos/1: ✅")
                ).exceptionally(ex -> { tvDelete.setText("DELETE /todos/1: " + errMsg(ex)); return null; })
                .thenRun(this::onOneDone);
    }

    private void onOneDone() {
        if (pending != null && pending.decrementAndGet() == 0) {
            setRunning(false);
        }
    }

    private void setRunning(boolean running) {
        isRunning = running;
        btnRun.setEnabled(!running);
        btnCancel.setEnabled(running);
        progress.setVisibility(running ? View.VISIBLE : View.GONE);
    }

    private void resetTexts() {
        tvCreate.setText("CREATE: -");
        tvGetAll.setText("GET /todos: -");
        tvGetById.setText("GET /todos/{id}: -");
        tvDelete.setText("DELETE /todos/{id}: -");
    }

    private void cancelAll(boolean silent) {
        if (!isRunning) {
            if (!silent) Toast.makeText(this, "Çalışan istek yok.", Toast.LENGTH_SHORT).show();
            return;
        }
        // Tüm future’ları iptal et
        if (fCreate  != null) fCreate.cancel(true);
        if (fGetAll  != null) fGetAll.cancel(true);
        if (fGetById != null) fGetById.cancel(true);
        if (fDelete  != null) fDelete.cancel(true);

        // UI’ı güncelle
        tvCreate.setText("CREATE: cancelled");
        tvGetAll.setText("GET /todos: cancelled");
        tvGetById.setText("GET /todos/{id}: cancelled");
        tvDelete.setText("DELETE /todos/{id}: cancelled");

        // Durum bayrakları
        if (pending != null) pending.set(0);
        setRunning(false);

        if (!silent) Toast.makeText(this, "Hepsi iptal edildi.", Toast.LENGTH_SHORT).show();
    }

    private String errMsg(Throwable ex) {
        Throwable cause = (ex.getCause()!=null) ? ex.getCause() : ex;
        if (cause instanceof CancellationException) return "cancelled";
        if (cause instanceof HttpException) {
            HttpException he = (HttpException) cause;
            return "❌ " + he.code + " - " + he.body;
        }
        return "❌ " + cause.getMessage();
    }

    @Override
    protected void onDestroy() {
        // Ekran kapanırken de iptal
        cancelAll(true);
        super.onDestroy();
    }
}

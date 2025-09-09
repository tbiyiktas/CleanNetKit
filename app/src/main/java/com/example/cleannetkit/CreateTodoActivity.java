package com.example.cleannetkit;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cleannetkit.application.service.todo.CreateTodoService;
import com.example.cleannetkit.domain.model.Todo;
import com.example.cleannetkit.domain.todo.CreateTodoUseCase;

import lib.net.CancellableFuture;
import lib.net.HttpException;
import lib.net.NetworkManager;

import java.util.concurrent.CancellationException;

public class CreateTodoActivity extends AppCompatActivity {

    private EditText etUserId, etTitle;
    private CheckBox cbCompleted;
    private Button btnCreate, btnBack;
    private ProgressBar progress;
    private TextView tvStatus;

    private CancellableFuture<Todo> future;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_create_todo);

        etUserId = findViewById(R.id.etUserId);
        etTitle = findViewById(R.id.etTitle);
        cbCompleted = findViewById(R.id.cbCompleted);
        btnCreate = findViewById(R.id.btnCreate);
        btnBack = findViewById(R.id.btnBack);
        progress = findViewById(R.id.progress);
        tvStatus = findViewById(R.id.tvStatus);

        btnBack.setOnClickListener(v -> finish());

        btnCreate.setOnClickListener(v -> {
            btnCreate.setEnabled(false);
            progress.setVisibility(View.VISIBLE);
            tvStatus.setText("");

            Integer userId = parseInt(etUserId.getText().toString());
            String title = etTitle.getText().toString().trim();
            boolean completed = cbCompleted.isChecked();

            if (userId == null || title.isEmpty()) {
                tvStatus.setText("UserId ve Title gerekli.");
                btnCreate.setEnabled(true);
                progress.setVisibility(View.GONE);
                return;
            }

            Todo todo = new Todo( userId, title, completed);

            NetworkManager nm = MyApplication.getNetworkManager();
            CreateTodoService svc = new CreateTodoService(nm);

            future = svc.handle(new CreateTodoUseCase.Command(todo));
            future.thenAccept(created -> {
                tvStatus.setText("Created ✅ id=" + (created == null ? "-" : created.getId()));
                btnCreate.setEnabled(true);
                progress.setVisibility(View.GONE);
            }).exceptionally(ex -> {
                btnCreate.setEnabled(true);
                progress.setVisibility(View.GONE);
                handleErr(ex);
                return null;
            });
        });
    }

    private void handleErr(Throwable ex) {
        Throwable cause = (ex.getCause() != null) ? ex.getCause() : ex;
        if (cause instanceof CancellationException) return;
        if (cause instanceof HttpException) {
            HttpException he = (HttpException) cause;
            tvStatus.setText("Hata: " + he.code + " - " + he.body);
        } else {
            tvStatus.setText("İstek hatası: " + cause.getMessage());
        }
    }

    private Integer parseInt(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return null; }
    }

    @Override
    protected void onDestroy() {
        if (future != null) future.cancel(true);
        super.onDestroy();
    }
}

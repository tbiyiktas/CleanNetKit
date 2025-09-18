package com.example.cleannetkit;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cleannetkit.application.service.todo.DeleteTodoService;
import com.example.cleannetkit.domain.todo.DeleteTodoUseCase;

import lib.concurrent.CancellableFuture;
import lib.net.HttpException;
import lib.net.NetworkManager;

import java.util.concurrent.CancellationException;

public class DeleteTodoActivity extends AppCompatActivity {

    private EditText etId;
    private Button btnDelete, btnBack;
    private ProgressBar progress;
    private TextView tvStatus;

    private CancellableFuture<Void> future;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_delete_todo);

        etId = findViewById(R.id.etId);
        btnDelete = findViewById(R.id.btnDelete);
        btnBack = findViewById(R.id.btnBack);
        progress = findViewById(R.id.progress);
        tvStatus = findViewById(R.id.tvStatus);

        btnBack.setOnClickListener(v -> finish());

        btnDelete.setOnClickListener(v -> {
            btnDelete.setEnabled(false);
            progress.setVisibility(View.VISIBLE);
            tvStatus.setText("");

            Integer id = parseInt(etId.getText().toString());
            if (id == null) {
                tvStatus.setText("Id gerekli.");
                btnDelete.setEnabled(true);
                progress.setVisibility(View.GONE);
                return;
            }

            NetworkManager nm = MyApplication.getNetworkManager();
            DeleteTodoService svc = new DeleteTodoService(nm);

            future = svc.handle(new DeleteTodoUseCase.Command(id));
            future.thenAccept(vv -> {
                tvStatus.setText("DELETE ✅ id=" + id);
                btnDelete.setEnabled(true);
                progress.setVisibility(View.GONE);
            }).exceptionally(ex -> {
                btnDelete.setEnabled(true);
                progress.setVisibility(View.GONE);
                handleErr(ex);
                return null;
            });
        });
    }

    private Integer parseInt(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return null; }
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

    @Override
    protected void onDestroy() {
        if (future != null) future.cancel(true);
        super.onDestroy();
    }
}

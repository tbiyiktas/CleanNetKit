package com.example.cleannetkit;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cleannetkit.application.service.todo.GetTodoService;
import com.example.cleannetkit.application.service.todo.UpdateTodoService;
import com.example.cleannetkit.domain.model.Todo;
import com.example.cleannetkit.domain.todo.GetTodoUseCase;
import com.example.cleannetkit.domain.todo.UpdateTodoUseCase;

import lib.net.CancellableFuture;
import lib.net.HttpException;
import lib.net.NetworkManager;

import java.util.concurrent.CancellationException;

public class UpdateTodoActivity extends AppCompatActivity {

    private EditText etId, etUserId, etTitle;
    private CheckBox cbCompleted, cbPartial;
    private Button btnUpdate, btnBack;
    private ProgressBar progress;
    private TextView tvStatus;

    private CancellableFuture<Todo> updateFuture;
    private CancellableFuture<Todo> loadFuture;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_update_todo);

        etId = findViewById(R.id.etId);
        etUserId = findViewById(R.id.etUserId);
        etTitle = findViewById(R.id.etTitle);
        cbCompleted = findViewById(R.id.cbCompleted);
        cbPartial = findViewById(R.id.cbPartial);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnBack = findViewById(R.id.btnBack);
        progress = findViewById(R.id.progress);
        tvStatus = findViewById(R.id.tvStatus);

        btnBack.setOnClickListener(v -> finish());

        // Intent ile gelen todoId varsa otomatik getir
        int passedId = getIntent().getIntExtra("todoId", -1);
        if (passedId > 0) {
            etId.setText(String.valueOf(passedId));
            autoLoadTodo(passedId);
        }

        btnUpdate.setOnClickListener(v -> doUpdate());
    }

    private void autoLoadTodo(int id) {
        setLoading(true, /*allowUpdate=*/false); // yükleme sırasında kilitli
        NetworkManager nm = MyApplication.getNetworkManager();
        GetTodoService svc = new GetTodoService(nm);

        loadFuture = svc.handle(new GetTodoUseCase.Command(id));
        loadFuture.thenAccept(todo -> {
            if (todo != null) fillForm(todo);
            tvStatus.setText("Loaded ✅ id=" + id);
            setLoading(false, /*allowUpdate=*/true); // ✨ YÜKLEME BİTTİ → AÇ
        }).exceptionally(ex -> {
            setLoading(false, /*allowUpdate=*/true); // ✨ HATA OLSA DA AÇ (kullanıcı elle düzeltip gönderebilsin)
            handleErr(ex);
            return null;
        });
    }

    private void fillForm(Todo t) {
        if (t.getUserId() != null) etUserId.setText(String.valueOf(t.getUserId()));
        if (t.getTitle() != null) etTitle.setText(t.getTitle());
        cbCompleted.setChecked(t.isCompleted());
    }

    private void doUpdate() {
        btnUpdate.setEnabled(false);
        progress.setVisibility(View.VISIBLE);
        tvStatus.setText("");

        Integer id = parseInt(etId.getText().toString());
        Integer userId = parseInt(etUserId.getText().toString());
        String title = etTitle.getText().toString().trim();
        boolean completed = cbCompleted.isChecked();
        boolean partial = cbPartial.isChecked();

        if (id == null || userId == null || title.isEmpty()) {
            tvStatus.setText("Id, UserId ve Title gerekli.");
            btnUpdate.setEnabled(true);
            progress.setVisibility(View.GONE);
            return;
        }

        Todo t = new Todo(id, userId, title, completed);
        NetworkManager nm = MyApplication.getNetworkManager();
        UpdateTodoService svc = new UpdateTodoService(nm);

        updateFuture = svc.handle(new UpdateTodoUseCase.Command(id, t, partial));
        updateFuture.thenAccept(updated -> {
            tvStatus.setText((partial ? "PATCH" : "PUT") + " ✅ id=" + (updated == null ? "-" : updated.getId()));
            btnUpdate.setEnabled(true);
            progress.setVisibility(View.GONE);
        }).exceptionally(ex -> {
            btnUpdate.setEnabled(true);
            progress.setVisibility(View.GONE);
            handleErr(ex);
            return null;
        });
    }

    /**
     * loading=true: progress göster, allowUpdate=false ise Update kilitli
     * loading=false: progress gizle, allowUpdate=true ise Update aç
     */
    private void setLoading(boolean loading, boolean allowUpdate) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnUpdate.setEnabled(!loading && allowUpdate);
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
        if (loadFuture != null) loadFuture.cancel(true);
        if (updateFuture != null) updateFuture.cancel(true);
        super.onDestroy();
    }
}

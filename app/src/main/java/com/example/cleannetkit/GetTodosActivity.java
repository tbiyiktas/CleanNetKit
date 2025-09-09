package com.example.cleannetkit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cleannetkit.application.service.todo.DeleteTodoService;
import com.example.cleannetkit.application.service.todo.GetTodosService;
import com.example.cleannetkit.domain.model.Todo;
import com.example.cleannetkit.domain.todo.DeleteTodoUseCase;
import com.example.cleannetkit.domain.todo.GetTodosUseCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;

import lib.net.CancellableFuture;
import lib.net.HttpException;
import lib.net.NetworkManager;

public class GetTodosActivity extends AppCompatActivity {

    private EditText etUserId;
    private Button btnLoadAll, btnLoadByUser, btnBack;
    private ProgressBar progress;
    private androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvTodos;

    private TodoAdapter adapter;
    private CancellableFuture<List<Todo>> loadFuture;
    private CancellableFuture<Void> deleteFuture;

    // son filtre durumu (pull-to-refresh'te aynı sorguyu tekrar etmek için)
    private boolean lastByUser = false;
    private Integer lastUserId = null;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_get_todos);

        etUserId = findViewById(R.id.etUserId);
        btnLoadAll = findViewById(R.id.btnLoadAll);
        btnLoadByUser = findViewById(R.id.btnLoadByUser);
        btnBack = findViewById(R.id.btnBack);
        progress = findViewById(R.id.progress);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        rvTodos = findViewById(R.id.rvTodos);

        rvTodos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TodoAdapter(item -> {
            // TIKLAMA → UpdateTodoActivity’ye geç (id’yi pasla)
            Intent it = new Intent(this, UpdateTodoActivity.class);
            if (item.getId() != null) it.putExtra("todoId", item.getId());
            startActivity(it);
        });
        rvTodos.setAdapter(adapter);

        // Pull-to-refresh
        swipeRefresh.setOnRefreshListener(() -> {
            if (deleteFuture != null && !deleteFuture.isDone()) {
                swipeRefresh.setRefreshing(false);
                return; // silme sürerken refresh yapma
            }
            load(lastByUser, /*fromSwipe=*/true);
        });

        // Swipe-to-delete
        ItemTouchHelper ith = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override public boolean onMove(RecyclerView rv, RecyclerView.ViewHolder vh, RecyclerView.ViewHolder target) {
                return false;
            }
            @Override public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;

                Todo toDelete = adapter.getCurrentList().get(pos);
                if (toDelete == null || toDelete.getId() == null) {
                    adapter.notifyItemChanged(pos);
                    return;
                }
                // optimistic remove (UI'dan kaldır)
                List<Todo> current = new ArrayList<>(adapter.getCurrentList());
                current.remove(pos);
                adapter.submitList(current);

                performDelete(toDelete, pos);
            }
        });
        ith.attachToRecyclerView(rvTodos);

        btnBack.setOnClickListener(v -> finish());
        btnLoadAll.setOnClickListener(v -> load(false, false));
        btnLoadByUser.setOnClickListener(v -> load(true, false));
    }

    private void load(boolean byUser, boolean fromSwipe) {
        setLoading(!fromSwipe, true);
        lastByUser = byUser;

        NetworkManager nm = MyApplication.getNetworkManager();
        GetTodosService svc = new GetTodosService(nm);

        if (byUser) {
            Integer uid = parseInt(etUserId.getText().toString());
            lastUserId = uid;
            if (uid == null) {
                toast("UserId gerekli.");
                setLoading(false, false);
                return;
            }
            cancelLoadIfRunning();
            loadFuture = svc.handle(GetTodosUseCase.Command.byUser(uid));
        } else {
            lastUserId = null;
            cancelLoadIfRunning();
            loadFuture = svc.handle(GetTodosUseCase.Command.all());
        }

        loadFuture.thenAccept(list -> {
            adapter.submitList(list == null ? Collections.emptyList() : list);
            setLoading(false, false);
        }).exceptionally(ex -> {
            setLoading(false, false);
            handleErr(ex);
            return null;
        });
    }

    private void load(boolean byUser) { load(byUser, false); }

    private void performDelete(Todo t, int removedPositionHint) {
        NetworkManager nm = MyApplication.getNetworkManager();
        DeleteTodoService delSvc = new DeleteTodoService(nm);

        // Güvenlik: aynı anda ikinci delete başlatma
        if (deleteFuture != null && !deleteFuture.isDone()) {
            toast("Silme işlemi sürüyor, lütfen bekleyin.");
            // listeyi geri eski haline döndür (swipe edilmiş item geri gelsin)
            refetchList();
            return;
        }

        deleteFuture = delSvc.handle(new DeleteTodoUseCase.Command(t.getId()));
        deleteFuture.thenAccept(v -> {
            toast("Silindi: #" + t.getId());
            // İstersen refetch yerine mevcut listedeki hali bırak
            // Refetch tercih edersek:
            refetchList();
        }).exceptionally(ex -> {
            // Hata: item'ı geri getir
            toast("Silme hatası.");
            refetchList();
            handleErr(ex);
            return null;
        });
    }

    private void refetchList() {
        // mevcut filtreyi tekrar uygula
        if (lastByUser && lastUserId != null) {
            load(true);
        } else {
            load(false);
        }
    }

    private void setLoading(boolean showProgressBar, boolean clearList) {
        btnLoadAll.setEnabled(!showProgressBar);
        btnLoadByUser.setEnabled(!showProgressBar);

        if (showProgressBar) {
            progress.setVisibility(View.VISIBLE);
        } else {
            progress.setVisibility(View.GONE);
            swipeRefresh.setRefreshing(false);
        }
        if (clearList) adapter.submitList(Collections.emptyList());
    }

    private Integer parseInt(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return null; }
    }

    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }

    private void handleErr(Throwable ex) {
        Throwable cause = (ex.getCause() != null) ? ex.getCause() : ex;
        if (cause instanceof CancellationException) return;
        if (cause instanceof HttpException) {
            HttpException he = (HttpException) cause;
            Toast.makeText(this, "Hata: " + he.code + " - " + he.body, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "İstek hatası: " + cause.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void cancelLoadIfRunning() {
        if (loadFuture != null && !loadFuture.isDone()) loadFuture.cancel(true);
    }

    @Override
    protected void onDestroy() {
        cancelLoadIfRunning();
        if (deleteFuture != null) deleteFuture.cancel(true);
        super.onDestroy();
    }
}

package com.example.cleannetkit;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cleannetkit.domain.model.Todo;

public class TodoAdapter extends ListAdapter<Todo, TodoAdapter.TodoVH> {

    public interface OnItemClick {
        void onClick(Todo item);
    }

    private final OnItemClick onItemClick;

    public TodoAdapter(OnItemClick onItemClick) {
        super(DIFF);
        this.onItemClick = onItemClick;
    }

    private static final DiffUtil.ItemCallback<Todo> DIFF = new DiffUtil.ItemCallback<Todo>() {
        @Override
        public boolean areItemsTheSame(@NonNull Todo oldItem, @NonNull Todo newItem) {
            Integer o = oldItem.getId();
            Integer n = newItem.getId();
            return o != null && o.equals(n);
        }
        @Override
        public boolean areContentsTheSame(@NonNull Todo oldItem, @NonNull Todo newItem) {
            return eq(oldItem.getTitle(), newItem.getTitle())
                    && oldItem.isCompleted() == newItem.isCompleted()
                    && eq(oldItem.getUserId(), newItem.getUserId());
        }
        private boolean eq(Object a, Object b){ return a == b || (a != null && a.equals(b)); }
    };

    @NonNull @Override
    public TodoVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_todo, parent, false);
        return new TodoVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TodoVH h, int position) {
        Todo t = getItem(position);
        h.bind(t);
        h.itemView.setOnClickListener(v -> {
            if (onItemClick != null) onItemClick.onClick(t);
        });
    }

    static class TodoVH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMeta, tvCompleted;
        public TodoVH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvMeta = itemView.findViewById(R.id.tvMeta);
            tvCompleted = itemView.findViewById(R.id.tvCompleted);
        }
        void bind(Todo t) {
            tvTitle.setText(t.getTitle() == null ? "(no title)" : t.getTitle());
            tvMeta.setText("#" + t.getId() + " • user=" + t.getUserId());
            tvCompleted.setText(t.isCompleted() ? "COMPLETED" : "PENDING");
            tvCompleted.setBackgroundResource(t.isCompleted()
                    ? android.R.color.holo_green_dark
                    : android.R.color.holo_orange_dark);
        }
    }
}

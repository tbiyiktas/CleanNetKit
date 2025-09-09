package com.example.cleannetkit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class TodoMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_todo_menu);

        findViewById(R.id.btnCreate).setOnClickListener(v ->
                startActivity(new Intent(this, CreateTodoActivity.class)));

        findViewById(R.id.btnGet).setOnClickListener(v ->
                startActivity(new Intent(this, GetTodosActivity.class)));

        findViewById(R.id.btnUpdate).setOnClickListener(v ->
                startActivity(new Intent(this, UpdateTodoActivity.class)));

        findViewById(R.id.btnDelete).setOnClickListener(v ->
                startActivity(new Intent(this, DeleteTodoActivity.class)));

        findViewById(R.id.btnParallel).setOnClickListener(v ->
                startActivity(new Intent(this, TodoParallelDemoActivity.class)));

//        findViewById(R.id.btnBackMain).setOnClickListener(v ->
//                finish()); // MainActivity’e geri
    }
}

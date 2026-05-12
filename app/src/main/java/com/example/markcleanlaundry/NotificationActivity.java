package com.example.markcleanlaundry;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private LinearLayout layoutEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        recyclerView = findViewById(R.id.recyclerNotifications);
        layoutEmpty  = findViewById(R.id.layoutEmpty);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Mark all read when screen opens
        NotificationManager.getInstance().markAllAsRead();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(this, NotificationManager.getInstance().getNotifications());
        recyclerView.setAdapter(adapter);

        checkEmpty();
    }

    @Override
    protected void onResume() {
        super.onResume();
        NotificationManager.getInstance().markAllAsRead();
        adapter.refresh(NotificationManager.getInstance().getNotifications());
        checkEmpty();
    }

    private void checkEmpty() {
        boolean isEmpty = NotificationManager.getInstance().getNotifications().isEmpty();
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }
}
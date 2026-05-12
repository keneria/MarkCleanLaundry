package com.example.markcleanlaundry;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class OrderListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private OrderListAdapter adapter;
    private LinearLayout layoutEmpty;
    private EditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_list);

        recyclerView = findViewById(R.id.recyclerOrders);
        layoutEmpty  = findViewById(R.id.layoutEmpty);
        etSearch     = findViewById(R.id.etSearchOrders);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderListAdapter(this, OrderManager.getInstance().getOrders());
        recyclerView.setAdapter(adapter);

        checkEmpty(OrderManager.getInstance().getOrders());

        // ── Search ────────────────────────────────────────────────────────────
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                filterOrders(s.toString().trim());
            }
            @Override public void afterTextChanged(Editable e) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh list when returning from OrderDetailActivity
        adapter.refresh(OrderManager.getInstance().getOrders());
        checkEmpty(OrderManager.getInstance().getOrders());
    }

    private void filterOrders(String query) {
        if (query.isEmpty()) {
            adapter.refresh(OrderManager.getInstance().getOrders());
            checkEmpty(OrderManager.getInstance().getOrders());
            return;
        }

        List<OrderItem> filtered = new ArrayList<>();
        for (OrderItem order : OrderManager.getInstance().getOrders()) {
            boolean matchesNumber = order.getOrderNumber().toLowerCase().contains(query.toLowerCase());
            boolean matchesStatus = order.getStatus().toLowerCase().contains(query.toLowerCase());
            boolean matchesItem   = false;
            if (order.getItemLabels() != null) {
                for (String label : order.getItemLabels()) {
                    if (label.toLowerCase().contains(query.toLowerCase())) {
                        matchesItem = true;
                        break;
                    }
                }
            }
            if (matchesNumber || matchesStatus || matchesItem) {
                filtered.add(order);
            }
        }
        adapter.refresh(filtered);
        checkEmpty(filtered);
    }

    private void checkEmpty(List<OrderItem> list) {
        if (list.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }
}
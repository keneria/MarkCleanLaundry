package com.example.markcleanlaundry;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView tvTotalOrders, tvPendingOrders, tvActiveOrders, tvDeliveredOrders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        tvTotalOrders     = findViewById(R.id.tvTotalOrders);
        tvPendingOrders   = findViewById(R.id.tvPendingOrders);
        tvActiveOrders    = findViewById(R.id.tvActiveOrders);
        tvDeliveredOrders = findViewById(R.id.tvDeliveredOrders);

        findViewById(R.id.btnAdminLogout).setOnClickListener(v -> {
            startActivity(new Intent(this, Adminloginactivity.class));
            finish();
        });

        findViewById(R.id.cardNavBookings).setOnClickListener(v ->
                startActivity(new Intent(this, AdminBookingsActivity.class)));

        findViewById(R.id.cardNavUsers).setOnClickListener(v ->
                startActivity(new Intent(this, AdminUsersActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshStats();
    }

    private void refreshStats() {
        List<OrderItem> orders = OrderManager.getInstance().getOrders();
        int total     = orders.size();
        int pending   = 0, active = 0, delivered = 0;

        for (OrderItem o : orders) {
            switch (o.getStatus()) {
                case "Pending":    pending++;   break;
                case "Confirmed":
                case "Washing":
                case "Out for Delivery": active++; break;
                case "Delivered":  delivered++; break;
            }
        }

        tvTotalOrders.setText(String.valueOf(total));
        tvPendingOrders.setText(String.valueOf(pending));
        tvActiveOrders.setText(String.valueOf(active));
        tvDeliveredOrders.setText(String.valueOf(delivered));
    }
}
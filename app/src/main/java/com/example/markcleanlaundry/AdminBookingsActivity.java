package com.example.markcleanlaundry;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import java.util.List;

public class AdminBookingsActivity extends AppCompatActivity {

    private LinearLayout llOrderList;
    private TextView tvEmptyState;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_bookings);

        db = new DatabaseHelper(this);
        llOrderList  = findViewById(R.id.llAdminOrderList);
        tvEmptyState = findViewById(R.id.tvEmptyBookings);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders();
    }

    private void loadOrders() {
        llOrderList.removeAllViews();

        // Load all orders from SQLite, newest first
        Cursor cursor = db.getAllOrders();

        if (!cursor.moveToFirst()) {
            cursor.close();
            tvEmptyState.setVisibility(View.VISIBLE);
            llOrderList.setVisibility(View.GONE);
            return;
        }

        tvEmptyState.setVisibility(View.GONE);
        llOrderList.setVisibility(View.VISIBLE);

        do {
            OrderItem order = OrderManager.fromCursor(cursor);
            llOrderList.addView(buildOrderCard(order));
        } while (cursor.moveToNext());

        cursor.close();
    }

    private View buildOrderCard(OrderItem order) {
        CardView card = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.bottomMargin = 12;
        card.setLayoutParams(cardParams);
        card.setRadius(14 * getResources().getDisplayMetrics().density);
        card.setCardElevation(2 * getResources().getDisplayMetrics().density);
        card.setCardBackgroundColor(0xFFFFFFFF);

        LinearLayout inner = new LinearLayout(this);
        inner.setOrientation(LinearLayout.VERTICAL);
        inner.setPadding(48, 40, 48, 40);

        // Row 1: Order number + status badge
        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.setGravity(android.view.Gravity.CENTER_VERTICAL);

        TextView tvOrderNum = new TextView(this);
        tvOrderNum.setText("Order " + order.getOrderNumber());
        tvOrderNum.setTextSize(15);
        tvOrderNum.setTextColor(0xFF1A1A2E);
        tvOrderNum.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams numParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        tvOrderNum.setLayoutParams(numParams);

        TextView tvBadge = new TextView(this);
        tvBadge.setText(order.getStatus().toUpperCase());
        tvBadge.setTextSize(10);
        tvBadge.setTypeface(null, android.graphics.Typeface.BOLD);
        tvBadge.setPadding(16, 6, 16, 6);
        tvBadge.setTextColor(statusColor(order.getStatus()));
        tvBadge.setBackgroundColor(statusBgColor(order.getStatus()));

        row1.addView(tvOrderNum);
        row1.addView(tvBadge);

        // Row 2: Service type + schedule date
        TextView tvDetails = new TextView(this);
        tvDetails.setText(order.getServiceType() + "  ·  " + order.getScheduleDate());
        tvDetails.setTextSize(12);
        tvDetails.setTextColor(0xFF888888);
        LinearLayout.LayoutParams detailParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        detailParams.topMargin = 6;
        tvDetails.setLayoutParams(detailParams);

        // Row 3: Payment method
        TextView tvPayment = new TextView(this);
        tvPayment.setText("Payment: " + order.getPaymentMethod());
        tvPayment.setTextSize(12);
        tvPayment.setTextColor(0xFF555555);
        LinearLayout.LayoutParams payParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        payParams.topMargin = 4;
        tvPayment.setLayoutParams(payParams);

        // Row 4: Total price (shown if already set by admin)
        if (order.getTotalPrice() > 0) {
            TextView tvPrice = new TextView(this);
            tvPrice.setText("Total: ₱" + String.format(java.util.Locale.getDefault(),
                    "%.2f", order.getTotalPrice()));
            tvPrice.setTextSize(12);
            tvPrice.setTextColor(0xFF27AE60);
            tvPrice.setTypeface(null, android.graphics.Typeface.BOLD);
            LinearLayout.LayoutParams priceParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            priceParams.topMargin = 4;
            tvPrice.setLayoutParams(priceParams);
            inner.addView(row1);
            inner.addView(tvDetails);
            inner.addView(tvPayment);
            inner.addView(tvPrice);
        } else {
            inner.addView(row1);
            inner.addView(tvDetails);
            inner.addView(tvPayment);
        }

        card.addView(inner);

        card.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminOrderDetailActivity.class);
            intent.putExtra("order_number", order.getOrderNumber());
            startActivity(intent);
        });

        return card;
    }

    private int statusColor(String status) {
        switch (status) {
            case "Confirmed":        return 0xFF27AE60;
            case "Washing":          return 0xFF2D7DD2;
            case "Out for Delivery": return 0xFF8E44AD;
            case "Delivered":        return 0xFF27AE60;
            case "Ready for Pickup": return 0xFF8E44AD;
            case "Completed":        return 0xFF27AE60;
            default:                 return 0xFFF4A423; // Pending
        }
    }

    private int statusBgColor(String status) {
        switch (status) {
            case "Confirmed":        return 0xFFE8F8F0;
            case "Washing":          return 0xFFE8F2FD;
            case "Out for Delivery": return 0xFFF3E8FD;
            case "Delivered":        return 0xFFE8F8F0;
            case "Ready for Pickup": return 0xFFF3E8FD;
            case "Completed":        return 0xFFE8F8F0;
            default:                 return 0xFFFFF3E0; // Pending
        }
    }
}
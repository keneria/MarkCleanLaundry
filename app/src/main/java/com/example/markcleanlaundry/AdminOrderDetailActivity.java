package com.example.markcleanlaundry;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import java.util.Locale;

public class AdminOrderDetailActivity extends AppCompatActivity {

    private TextView tvOrderNumber, tvServiceType, tvLaundryType, tvScheduleDate;
    private TextView tvPickupAddress, tvDeliveryAddress, tvPaymentMethod;
    private TextView tvStatusBadge, tvOrderStatus;
    private TextView tvWeightPrice;
    private TextView tvClaimingMethod;
    private TextView tvPaymentStatus;
    private MaterialButton btnConfirm, btnWashing, btnOutForDelivery, btnDelivered;
    private MaterialButton btnSetWeightPrice;
    private MaterialButton btnReadyForClaiming;
    private MaterialButton btnMarkAsPaid;
    private TextView tvClientName, tvClientPhone;

    private OrderItem currentOrder;
    private String    cachedClaimingMethod = "";
    private boolean   isPaid               = false;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_order_detail);

        db = new DatabaseHelper(this);
        initViews();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        String orderNumber = getIntent().getStringExtra("order_number");
        loadOrder(orderNumber);
        setupWeightPriceButton();
        setupStatusButtons();
        setupPaymentButton();
    }

    private void initViews() {
        tvOrderNumber       = findViewById(R.id.tvAdminOrderNumber);
        tvServiceType       = findViewById(R.id.tvAdminServiceType);
        tvLaundryType       = findViewById(R.id.tvAdminLaundryType);
        tvScheduleDate      = findViewById(R.id.tvAdminScheduleDate);
        tvPickupAddress     = findViewById(R.id.tvAdminPickupAddress);
        tvDeliveryAddress   = findViewById(R.id.tvAdminDeliveryAddress);
        tvPaymentMethod     = findViewById(R.id.tvAdminPaymentMethod);
        tvStatusBadge       = findViewById(R.id.tvAdminStatusBadge);
        tvOrderStatus       = findViewById(R.id.tvAdminOrderStatus);
        btnConfirm          = findViewById(R.id.btnStatusConfirm);
        btnWashing          = findViewById(R.id.btnStatusWashing);
        btnOutForDelivery   = findViewById(R.id.btnStatusOutForDelivery);
        btnDelivered        = findViewById(R.id.btnStatusDelivered);
        btnReadyForClaiming = findViewById(R.id.btnStatusReadyForClaiming);
        tvWeightPrice       = findViewById(R.id.tvAdminWeightPrice);
        btnSetWeightPrice   = findViewById(R.id.btnSetWeightPrice);
        tvClientName        = findViewById(R.id.tvAdminClientName);
        tvClientPhone       = findViewById(R.id.tvAdminClientPhone);
        tvClaimingMethod    = findViewById(R.id.tvAdminClaimingMethod);
        tvPaymentStatus     = findViewById(R.id.tvAdminPaymentStatus);
        btnMarkAsPaid       = findViewById(R.id.btnMarkAsPaid);
    }

    private void loadOrder(String orderNumber) {
        if (orderNumber == null || orderNumber.isEmpty()) {
            Toast.makeText(this, "No order selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        android.database.Cursor c = db.getOrderByNumber(orderNumber);
        if (!c.moveToFirst()) {
            c.close();
            Toast.makeText(this, "Order not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentOrder = OrderManager.fromCursor(c);

        // Cache claiming method
        int claimingCol = c.getColumnIndex(DatabaseHelper.COL_ORDER_CLAIMING_METHOD);
        cachedClaimingMethod = (claimingCol != -1 && !c.isNull(claimingCol))
                ? c.getString(claimingCol) : "";

        // Cache payment status
        int paidCol = c.getColumnIndex(DatabaseHelper.COL_ORDER_IS_PAID);
        isPaid = (paidCol != -1) && (c.getInt(paidCol) == 1);

        c.close();

        // Client info
        tvClientName.setText(nvl(currentOrder.getClientName()));
        tvClientPhone.setText(nvl(currentOrder.getPhone()));

        // Order header
        tvOrderNumber.setText("Order " + currentOrder.getOrderNumber());

        // Order info
        tvServiceType.setText(nvl(currentOrder.getServiceType()));
        tvLaundryType.setText(nvl(currentOrder.getLaundryType()));
        tvScheduleDate.setText(nvl(currentOrder.getScheduleDate()));

        // Claiming method
        if (cachedClaimingMethod.isEmpty()) {
            tvClaimingMethod.setText("—");
            tvClaimingMethod.setTextColor(0xFF888888);
        } else {
            tvClaimingMethod.setText(cachedClaimingMethod);
            tvClaimingMethod.setTextColor(
                    "Home Delivery".equals(cachedClaimingMethod) ? 0xFF8E44AD : 0xFF16A085);
        }

        // Address section
        boolean isDropOff = "Drop-off".equals(currentOrder.getServiceType());
        View layoutPickup = findViewById(R.id.layoutAdminPickup);
        if (isDropOff) {
            if (layoutPickup != null) layoutPickup.setVisibility(View.GONE);
            tvDeliveryAddress.setText(
                    "Ground floor MIM Bldg., 2993 Kakarong St. Brgy. Sta Cruz, Makati City");
        } else {
            if (layoutPickup != null) layoutPickup.setVisibility(View.VISIBLE);
            tvPickupAddress.setText(nvl(currentOrder.getPickupAddress()));
            tvDeliveryAddress.setText(nvl(currentOrder.getDeliveryAddress()));
        }

        tvPaymentMethod.setText(nvl(currentOrder.getPaymentMethod()));

        refreshPaymentStatusUI();
        refreshWeightPriceDisplay();
        updateStatusUI(currentOrder.getStatus());
    }

    private void setupWeightPriceButton() {
        if (btnSetWeightPrice == null) return;
        btnSetWeightPrice.setOnClickListener(v -> showWeightPriceDialog());
    }

    private void setupPaymentButton() {
        btnMarkAsPaid.setOnClickListener(v -> {
            if (isPaid) {
                // Allow toggling back to unpaid if needed
                new AlertDialog.Builder(this)
                        .setTitle("Mark as Unpaid?")
                        .setMessage("This order is already marked as paid. Do you want to revert it to unpaid?")
                        .setPositiveButton("Yes, revert", (dialog, which) -> {
                            isPaid = false;
                            db.updateOrderPaidStatus(currentOrder.getOrderNumber(), false);
                            refreshPaymentStatusUI();
                            Toast.makeText(this, "Marked as unpaid.", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            } else {
                isPaid = true;
                db.updateOrderPaidStatus(currentOrder.getOrderNumber(), true);
                refreshPaymentStatusUI();
                Toast.makeText(this, "Order marked as paid!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refreshPaymentStatusUI() {
        if (isPaid) {
            tvPaymentStatus.setText("PAID");
            tvPaymentStatus.setTextColor(0xFF27AE60);
            tvPaymentStatus.setBackgroundColor(0xFFE8F8F0);
            btnMarkAsPaid.setText("Mark as Unpaid");
            btnMarkAsPaid.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(0xFFE74C3C));
        } else {
            tvPaymentStatus.setText("UNPAID");
            tvPaymentStatus.setTextColor(0xFFE74C3C);
            tvPaymentStatus.setBackgroundColor(0xFFFDECEA);
            btnMarkAsPaid.setText("Mark as Paid");
            btnMarkAsPaid.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(0xFF27AE60));
        }
    }

    private void showWeightPriceDialog() {
        float dp  = getResources().getDisplayMetrics().density;
        int   pad = (int) (20 * dp);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(pad, pad, pad, 0);

        TextView labelWeight = new TextView(this);
        labelWeight.setText("Weight (kg)");
        labelWeight.setTextSize(13);
        labelWeight.setTextColor(0xFF555555);

        EditText etWeight = new EditText(this);
        etWeight.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etWeight.setHint("e.g. 3.5");
        if (currentOrder.getQuantity() > 0) {
            etWeight.setText(String.valueOf(currentOrder.getQuantity()));
        }

        LinearLayout.LayoutParams priceLabelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        priceLabelParams.topMargin = (int) (14 * dp);

        TextView labelPrice = new TextView(this);
        labelPrice.setText("Total Price (₱)");
        labelPrice.setTextSize(13);
        labelPrice.setTextColor(0xFF555555);
        labelPrice.setLayoutParams(priceLabelParams);

        EditText etPrice = new EditText(this);
        etPrice.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etPrice.setHint("e.g. 120.00");
        if (currentOrder.getTotalPrice() > 0) {
            etPrice.setText(String.format(Locale.getDefault(), "%.2f",
                    currentOrder.getTotalPrice()));
        }

        layout.addView(labelWeight);
        layout.addView(etWeight);
        layout.addView(labelPrice, priceLabelParams);
        layout.addView(etPrice);

        new AlertDialog.Builder(this)
                .setTitle("Set Weight & Price")
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {
                    String wStr = etWeight.getText().toString().trim();
                    String pStr = etPrice.getText().toString().trim();
                    if (wStr.isEmpty() || pStr.isEmpty()) {
                        Toast.makeText(this, "Please fill in both fields.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    double weight = Double.parseDouble(wStr);
                    double price  = Double.parseDouble(pStr);
                    db.updateOrderPrice(currentOrder.getOrderNumber(), weight, price);
                    currentOrder.setQuantity(weight);
                    currentOrder.setTotalPrice(price);
                    refreshWeightPriceDisplay();
                    updateStatusUI(currentOrder.getStatus());
                    Toast.makeText(this, "Weight & price saved!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void refreshWeightPriceDisplay() {
        if (tvWeightPrice == null) return;
        if (currentOrder.getTotalPrice() > 0) {
            tvWeightPrice.setVisibility(View.VISIBLE);
            tvWeightPrice.setText(String.format(Locale.getDefault(),
                    "%.1f kg  —  ₱%.2f",
                    currentOrder.getQuantity(),
                    currentOrder.getTotalPrice()));
        } else {
            tvWeightPrice.setVisibility(View.GONE);
        }
    }

    private void setupStatusButtons() {
        btnConfirm.setOnClickListener(v          -> updateOrderStatus("Confirmed"));
        btnWashing.setOnClickListener(v          -> updateOrderStatus("Washing"));
        btnReadyForClaiming.setOnClickListener(v -> updateOrderStatus("Ready for Claiming"));
        btnOutForDelivery.setOnClickListener(v   -> updateOrderStatus("Out for Delivery"));
        btnDelivered.setOnClickListener(v        -> updateOrderStatus("Claimed"));
    }

    private void updateOrderStatus(String newStatus) {
        if (currentOrder == null) return;

        db.updateOrderStatus(currentOrder.getOrderNumber(), newStatus);
        currentOrder.setStatus(newStatus);
        updateStatusUI(newStatus);

        int targetUserId = getOrderUserId(currentOrder.getOrderNumber());

        AppNotification.Type type;
        switch (newStatus) {
            case "Confirmed":          type = AppNotification.Type.ORDER_CONFIRMED;          break;
            case "Washing":            type = AppNotification.Type.ORDER_WASHING;            break;
            case "Out for Delivery":   type = AppNotification.Type.ORDER_OUT_FOR_DELIVERY;   break;
            case "Delivered":          type = AppNotification.Type.ORDER_DELIVERED;          break;
            case "Ready for Claiming": type = AppNotification.Type.ORDER_READY_FOR_CLAIMING; break;
            default:                   type = AppNotification.Type.ORDER_PLACED;             break;
        }

        if (targetUserId != -1) {
            NotificationManager.getInstance()
                    .postOrderNotificationForUser(type, currentOrder.getOrderNumber(),
                            targetUserId);
        }

        Toast.makeText(this, "Status updated to " + newStatus, Toast.LENGTH_SHORT).show();
    }

    private int getOrderUserId(String orderNumber) {
        android.database.Cursor c = db.getOrderByNumber(orderNumber);
        int userId = -1;
        if (c.moveToFirst()) {
            int col = c.getColumnIndex(DatabaseHelper.COL_ORDER_USER_ID);
            if (col >= 0) userId = c.getInt(col);
        }
        c.close();
        return userId;
    }

    private void updateStatusUI(String status) {
        tvOrderStatus.setText(status);
        tvStatusBadge.setText(status.toUpperCase());

        int color;
        switch (status) {
            case "Confirmed":          color = 0xFF27AE60; break;
            case "Washing":            color = 0xFF2D7DD2; break;
            case "Out for Delivery":   color = 0xFF8E44AD; break;
            case "Claimed":            color = 0xFF27AE60; break;
            case "Ready for Claiming": color = 0xFF16A085; break;
            default:                   color = 0xFFF4A423; break;
        }
        tvOrderStatus.setTextColor(color);
        tvStatusBadge.setTextColor(color);

        boolean weightSet        = currentOrder.getTotalPrice() > 0;
        boolean isPending        = "Pending".equals(status);
        boolean isConfirmed      = "Confirmed".equals(status);
        boolean isWashing        = "Washing".equals(status);
        boolean isReady          = "Ready for Claiming".equals(status);
        boolean isOutForDelivery = "Out for Delivery".equals(status);
        boolean isClaimed        = "Claimed".equals(status);
        boolean isHomeDelivery   = "Home Delivery".equals(cachedClaimingMethod);

        setButtonState(btnConfirm,
                weightSet && (isPending || isConfirmed || isWashing || isReady || isOutForDelivery),
                isConfirmed);

        setButtonState(btnWashing,
                isConfirmed || isWashing || isReady || isOutForDelivery,
                isWashing);

        setButtonState(btnReadyForClaiming,
                isWashing || isReady || isOutForDelivery || isClaimed,
                isReady);

        setButtonState(btnOutForDelivery,
                isHomeDelivery && (isReady || isOutForDelivery || isClaimed),
                isOutForDelivery);

        setButtonState(btnDelivered,
                isReady || isOutForDelivery || isClaimed,
                isClaimed);
    }

    private void setButtonState(MaterialButton button, boolean enabled, boolean active) {
        button.setEnabled(enabled);
        button.setAlpha(active ? 1f : enabled ? 0.6f : 0.3f);
    }

    private String nvl(String s) {
        return (s != null && !s.isEmpty()) ? s : "—";
    }
}
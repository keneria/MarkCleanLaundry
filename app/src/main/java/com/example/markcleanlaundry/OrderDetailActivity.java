package com.example.markcleanlaundry;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class OrderDetailActivity extends AppCompatActivity {

    private TextView tvOrderNumber, tvBagCount, tvOrderDate;
    private TextView tvServiceType, tvLaundryType;
    private TextView tvOrderStatus, tvStatusSubtext, tvStatusBadge;
    private TextView tvScheduleDate;
    private TextView tvPickupAddress, tvDeliveryAddress, tvDeliveryLabel;
    private TextView tvPaymentMethod, tvPaymentSubtext, tvPaymentIcon, tvPaymentName;
    private TextView tvPaymentSectionLabel;
    private TextView tvOrderNotice;
    private TextView tvWeight, tvTotalAmount, tvPaymentStatus, tvDeliveryFee;
    private TextView tvHeaderTitle, tvHeaderSubtitle;
    private LinearLayout llOrderItems;
    private LinearLayout layoutNotice;
    private LinearLayout layoutAddressSection, layoutPaymentSection;
    private LinearLayout layoutPickupAddress;
    private LinearLayout layoutDeliveryRow;
    private View dividerAddress;
    private View layoutWeight, layoutTotal, layoutPaymentStatusRow, layoutDeliveryFeeRow;
    private MaterialButton btnClaimOrder, btnScheduleLaundry;

    private String currentOrderNumber;
    private String lastKnownStatus = "";
    private String currentPaymentMethod = "";
    private String currentServiceType = "";

    private final Handler pollHandler = new Handler(Looper.getMainLooper());
    private static final long POLL_INTERVAL_MS = 5_000;

    private final Runnable pollRunnable = new Runnable() {
        @Override
        public void run() {
            refreshStatusFromDb();
            pollHandler.postDelayed(this, POLL_INTERVAL_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        initViews();
        setupBackButton();
        populateFromIntent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshStatusFromDb();
        pollHandler.postDelayed(pollRunnable, POLL_INTERVAL_MS);
    }

    @Override
    protected void onPause() {
        super.onPause();
        pollHandler.removeCallbacks(pollRunnable);
    }

    // ── Init ──────────────────────────────────────────────────────────────────

    private void initViews() {
        tvOrderNumber         = findViewById(R.id.tvOrderNumber);
        tvBagCount            = findViewById(R.id.tvBagCount);
        tvOrderDate           = findViewById(R.id.tvOrderDate);
        tvServiceType         = findViewById(R.id.tvServiceType);
        tvLaundryType         = findViewById(R.id.tvLaundryType);
        tvOrderStatus         = findViewById(R.id.tvOrderStatus);
        tvStatusSubtext       = findViewById(R.id.tvStatusSubtext);
        tvStatusBadge         = findViewById(R.id.tvStatusBadge);
        tvScheduleDate        = findViewById(R.id.tvScheduleDate);
        tvPickupAddress       = findViewById(R.id.tvPickupAddress);
        tvDeliveryAddress     = findViewById(R.id.tvDeliveryAddress);
        tvDeliveryLabel       = findViewById(R.id.tvDeliveryLabel);
        tvPaymentMethod       = findViewById(R.id.tvPaymentMethod);
        tvPaymentSubtext      = findViewById(R.id.tvPaymentSubtext);
        tvPaymentIcon         = findViewById(R.id.tvPaymentIcon);
        tvPaymentName         = findViewById(R.id.tvPaymentName);
        tvPaymentSectionLabel = findViewById(R.id.tvPaymentSectionLabel);
        tvOrderNotice         = findViewById(R.id.tvOrderNotice);
        tvWeight              = findViewById(R.id.tvWeight);
        tvTotalAmount         = findViewById(R.id.tvTotalAmount);
        tvPaymentStatus       = findViewById(R.id.tvPaymentStatus);
        tvDeliveryFee         = findViewById(R.id.tvDeliveryFee);
        tvHeaderTitle         = findViewById(R.id.tvHeaderTitle);
        tvHeaderSubtitle      = findViewById(R.id.tvHeaderSubtitle);
        llOrderItems          = findViewById(R.id.llOrderItems);
        layoutNotice          = findViewById(R.id.layoutNotice);
        layoutAddressSection  = findViewById(R.id.layoutAddressSection);
        layoutPaymentSection  = findViewById(R.id.layoutPaymentSection);
        layoutPickupAddress   = findViewById(R.id.layoutPickupAddress);
        layoutDeliveryRow     = findViewById(R.id.layoutDeliveryRow);
        dividerAddress        = findViewById(R.id.dividerAddress);
        layoutWeight          = findViewById(R.id.layoutWeight);
        layoutTotal           = findViewById(R.id.layoutTotal);
        layoutPaymentStatusRow= findViewById(R.id.layoutPaymentStatus);
        layoutDeliveryFeeRow  = findViewById(R.id.layoutDeliveryFee);
        btnClaimOrder         = findViewById(R.id.btnClaimOrder);
        btnScheduleLaundry    = findViewById(R.id.btnScheduleLaundry);
    }

    private void setupBackButton() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    // ── Populate ──────────────────────────────────────────────────────────────

    private void populateFromIntent() {
        Intent intent = getIntent();

        currentOrderNumber   = intent.getStringExtra("order_number");
        currentPaymentMethod = intent.getStringExtra("payment_method");
        currentServiceType   = intent.getStringExtra("service_type");

        if (currentPaymentMethod == null) currentPaymentMethod = "";
        if (currentServiceType   == null) currentServiceType   = "";

        int bagCount = intent.getIntExtra("bag_count", 0);
        tvOrderNumber.setText(currentOrderNumber != null ? "Order " + currentOrderNumber : "Order #—");
        tvBagCount.setText("(" + bagCount + (bagCount == 1 ? " bag)" : " bags)"));

        String now = new SimpleDateFormat("hh:mm a, EEE, dd MMM yyyy", Locale.getDefault())
                .format(new Date());
        tvOrderDate.setText(now);

        String serviceType = intent.getStringExtra("service_type");
        String laundryType = intent.getStringExtra("laundry_type");
        tvServiceType.setText(serviceType != null && !serviceType.isEmpty() ? serviceType : "—");
        tvLaundryType.setText(laundryType != null && !laundryType.isEmpty() ? laundryType : "—");

        ArrayList<String> labels = intent.getStringArrayListExtra("item_labels");
        if (labels != null) populateOrderItems(labels);

        setOrderNotice(currentPaymentMethod);

        String selectedDate = intent.getStringExtra("selected_date");
        String selectedTime = intent.getStringExtra("selected_time");
        if (selectedDate != null && !selectedDate.isEmpty()) {
            tvScheduleDate.setText((selectedTime != null && !selectedTime.isEmpty())
                    ? selectedDate + "  •  " + selectedTime
                    : selectedDate);
        } else {
            boolean isDropOff = "Drop-off".equals(currentServiceType);
            tvScheduleDate.setText(isDropOff ? "Walk-in / Drop-off" : "Not specified");
        }

        if (btnScheduleLaundry != null) {
            btnScheduleLaundry.setOnClickListener(v -> {
                Intent scheduleIntent = new Intent(this, HomeActivity.class);
                scheduleIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(scheduleIntent);
            });
        }

        String savedStatus = loadStatusFromDb();
        lastKnownStatus = savedStatus;
        setOrderStatus(savedStatus);

        loadPriceFromDb();
        saveOrderToDatabase(intent);
    }

    // ── Address section ───────────────────────────────────────────────────────

    private void updateAddressSection(String status) {
        boolean isDropOff = "Drop-off".equals(currentServiceType);

        if ("Ready for Claiming".equals(status)
                || "Claimed".equals(status)
                || "Delivered".equals(status)) {
            layoutAddressSection.setVisibility(View.GONE);
            return;
        }

        layoutAddressSection.setVisibility(View.VISIBLE);

        if (isDropOff) {
            layoutPickupAddress.setVisibility(View.GONE);
            dividerAddress.setVisibility(View.GONE);
            if (layoutDeliveryRow != null) layoutDeliveryRow.setVisibility(View.VISIBLE);
            tvDeliveryLabel.setText("Store Address");
            tvDeliveryAddress.setText(
                    "Ground floor MIM Bldg., 2993 Kakarong St. Brgy. Sta Cruz, Makati City");
        } else {
            String pickup = getIntent().getStringExtra("pickup_address");
            if (pickup != null && !pickup.isEmpty()) {
                layoutPickupAddress.setVisibility(View.VISIBLE);
                tvPickupAddress.setText(pickup);
            } else {
                layoutPickupAddress.setVisibility(View.GONE);
            }
            dividerAddress.setVisibility(View.GONE);
            if (layoutDeliveryRow != null) layoutDeliveryRow.setVisibility(View.GONE);
        }
    }

    // ── Payment / Claiming section ────────────────────────────────────────────

    private void updatePaymentSection(String status) {
        boolean isReadyOrDone = "Ready for Claiming".equals(status)
                || "Claimed".equals(status)
                || "Delivered".equals(status);

        if (isReadyOrDone) {
            loadAndShowClaimingSection();
        } else {
            // Show payment method for ALL statuses including Pending
            layoutPaymentSection.setVisibility(View.VISIBLE);
            if (tvPaymentSectionLabel != null) tvPaymentSectionLabel.setText("Payment Method");
            populateBookingPayment(
                    currentPaymentMethod,
                    getIntent().getStringExtra("gcash_number"),
                    getIntent().getStringExtra("gcash_name"));
        }
    }

    private void populateBookingPayment(String method, String gcashNumber, String gcashName) {
        if (method == null) return;
        if (method.equalsIgnoreCase("GCash")) {
            tvPaymentMethod.setText("GCash");
            tvPaymentSubtext.setText(gcashNumber != null && !gcashNumber.isEmpty()
                    ? gcashNumber : "—");
            tvPaymentName.setText(gcashName != null && !gcashName.isEmpty() ? gcashName : "");
            tvPaymentName.setVisibility(View.VISIBLE);
            tvPaymentIcon.setText("G");
            tvPaymentIcon.setBackgroundColor(0xFF007DFF);
        } else if (method.equalsIgnoreCase("Drop-off") || method.equalsIgnoreCase("Cash")) {
            tvPaymentMethod.setText("Cash");
            tvPaymentSubtext.setText("Pay cash at the store when dropping off your laundry");
            tvPaymentName.setVisibility(View.GONE);
            tvPaymentIcon.setText("₱");
            tvPaymentIcon.setBackgroundColor(0xFF2C8FBD);
        } else {
            tvPaymentMethod.setText("Cash");
            tvPaymentSubtext.setText("Pay cash upon claiming your laundry");
            tvPaymentName.setVisibility(View.GONE);
            tvPaymentIcon.setText("₱");
            tvPaymentIcon.setBackgroundColor(0xFF2C8FBD);
        }
    }

    private void loadAndShowClaimingSection() {
        if (currentOrderNumber == null) return;

        DatabaseHelper db = new DatabaseHelper(this);
        android.database.Cursor c = db.getOrderByNumber(currentOrderNumber);

        String claimingMethod  = "";
        String deliveryAddress = "";
        double totalPrice      = 0;

        if (c.moveToFirst()) {
            int colClaim    = c.getColumnIndex(DatabaseHelper.COL_ORDER_CLAIMING_METHOD);
            int colDelivery = c.getColumnIndex(DatabaseHelper.COL_ORDER_DELIVERY_ADDRESS);
            int colPrice    = c.getColumnIndex(DatabaseHelper.COL_ORDER_TOTAL_PRICE);
            if (colClaim    != -1) claimingMethod  = safeStr(c.getString(colClaim));
            if (colDelivery != -1) deliveryAddress = safeStr(c.getString(colDelivery));
            if (colPrice    != -1) totalPrice      = c.getDouble(colPrice);
        }
        c.close();

        if (claimingMethod.isEmpty()) {
            layoutPaymentSection.setVisibility(View.GONE);
            return;
        }

        layoutPaymentSection.setVisibility(View.VISIBLE);
        if (tvPaymentSectionLabel != null) tvPaymentSectionLabel.setText("Claiming Method");

        boolean isHomeDelivery = "Home Delivery".equals(claimingMethod);

        if (isHomeDelivery) {
            tvPaymentMethod.setText("Home Delivery");
            tvPaymentSubtext.setText(!deliveryAddress.isEmpty()
                    ? deliveryAddress : "Delivery address not set");
            tvPaymentName.setVisibility(View.VISIBLE);
            tvPaymentName.setText("Delivery fee: ₱50.00 added to your total");
            tvPaymentIcon.setBackgroundColor(0xFF8E44AD);

            if (layoutDeliveryFeeRow != null) {
                layoutDeliveryFeeRow.setVisibility(View.VISIBLE);
                if (tvDeliveryFee != null) tvDeliveryFee.setText("₱50.00");
            }
            if (totalPrice > 0 && tvTotalAmount != null) {
                tvTotalAmount.setText(String.format(Locale.getDefault(),
                        "₱%.2f", totalPrice + 50.0));
            }
        } else {
            tvPaymentMethod.setText("Store Pick-Up");
            tvPaymentSubtext.setText(
                    "Ground floor MIM Bldg., 2993 Kakarong St. Brgy. Sta Cruz, Makati City");
            tvPaymentName.setVisibility(View.VISIBLE);
            tvPaymentName.setText("Please bring your order receipt when claiming.");
            tvPaymentIcon.setText("🏪");
            tvPaymentIcon.setBackgroundColor(0xFF16A085);
            if (layoutDeliveryFeeRow != null) layoutDeliveryFeeRow.setVisibility(View.GONE);
        }
    }

    // ── Status ────────────────────────────────────────────────────────────────

    public void setOrderStatus(String status) {
        int statusColor;
        String badgeText;
        String subtext;

        switch (status) {
            case "Confirmed":
                statusColor = getResources().getColor(R.color.status_confirmed, null);
                subtext     = "Your order has been confirmed";
                badgeText   = "CONFIRMED";
                break;
            case "Washing":
                statusColor = getResources().getColor(R.color.status_washing, null);
                subtext     = "Your laundry is being washed";
                badgeText   = "WASHING";
                break;
            case "Out for Delivery":
                statusColor = getResources().getColor(R.color.status_delivery, null);
                subtext     = "Your laundry is on its way";
                badgeText   = "OUT FOR DELIVERY";
                break;
            case "Delivered":
            case "Claimed":
                statusColor = getResources().getColor(R.color.status_delivered, null);
                subtext     = "Claimed".equals(status)
                        ? "You've picked up your laundry. All done!"
                        : "Your laundry has been delivered. Enjoy!";
                badgeText   = "Claimed".equals(status) ? "CLAIMED" : "DELIVERED";
                break;
            case "Ready for Claiming":
                statusColor = getResources().getColor(R.color.status_confirmed, null);
                subtext     = "Choose how you'd like to claim it.";
                break;
            default: // Pending
                statusColor = getResources().getColor(R.color.status_pending, null);
                subtext     = "Waiting for admin confirmation";
                badgeText   = "PENDING";
                break;
        }

        tvOrderStatus.setText(status);
        tvOrderStatus.setTextColor(statusColor);
        tvStatusSubtext.setText(subtext);
        tvStatusBadge.setTextColor(statusColor);

        boolean hideNotice = "Ready for Claiming".equals(status)
                || "Claimed".equals(status)
                || "Delivered".equals(status)
                || "Out for Delivery".equals(status);
        layoutNotice.setVisibility(hideNotice ? View.GONE : View.VISIBLE);

        updateAddressSection(status);
        updateClaimButton(status);
        updatePaymentSection(status);
    }

    private void updateClaimButton(String status) {
        if (btnClaimOrder == null) return;

        if ("Ready for Claiming".equals(status)) {
            // Only show if user hasn't chosen a claiming method yet
            DatabaseHelper db = new DatabaseHelper(this);
            android.database.Cursor c = db.getOrderByNumber(currentOrderNumber);
            String existingClaim = "";
            if (c.moveToFirst()) {
                int col = c.getColumnIndex(DatabaseHelper.COL_ORDER_CLAIMING_METHOD);
                if (col != -1) existingClaim = safeStr(c.getString(col));
            }
            c.close();

            if (existingClaim.isEmpty()) {
                btnClaimOrder.setVisibility(View.VISIBLE);
                btnClaimOrder.setOnClickListener(v -> {
                    Intent claimIntent = new Intent(this, ClaimingMethodActivity.class);
                    claimIntent.putExtra("order_number",    currentOrderNumber);
                    claimIntent.putExtra("payment_method",  currentPaymentMethod);
                    // Pass display info for PaymentActivity if needed
                    claimIntent.putExtra("service_name",
                            getIntent().getStringExtra("service_name"));
                    claimIntent.putExtra("method",
                            getIntent().getStringExtra("method"));
                    startActivity(claimIntent);
                });
            } else {
                btnClaimOrder.setVisibility(View.GONE);
            }
        } else {
            btnClaimOrder.setVisibility(View.GONE);
        }
    }

    // ── DB polling ────────────────────────────────────────────────────────────

    private void refreshStatusFromDb() {
        if (currentOrderNumber == null) return;

        DatabaseHelper db = new DatabaseHelper(this);
        android.database.Cursor c = db.getOrderByNumber(currentOrderNumber);

        if (c.moveToFirst()) {
            int col = c.getColumnIndex(DatabaseHelper.COL_ORDER_STATUS);
            if (col != -1) {
                String latestStatus = c.getString(col);
                if (latestStatus != null && !latestStatus.equals(lastKnownStatus)) {
                    lastKnownStatus = latestStatus;
                    setOrderStatus(latestStatus);

                    OrderItem order = OrderManager.getInstance().getOrderByNumber(currentOrderNumber);
                    if (order != null) order.setStatus(latestStatus);
                }
            }
            int colQty   = c.getColumnIndex(DatabaseHelper.COL_ORDER_QUANTITY);
            int colPrice = c.getColumnIndex(DatabaseHelper.COL_ORDER_TOTAL_PRICE);
            int colPaid  = c.getColumnIndex(DatabaseHelper.COL_ORDER_IS_PAID);
            boolean isPaid = (colPaid != -1) && (c.getInt(colPaid) == 1);

            if (colQty != -1 && colPrice != -1) {
                refreshPriceDisplay(c.getDouble(colQty), c.getDouble(colPrice), isPaid);
            }
        }
        c.close();
    }

    // ── Price display ─────────────────────────────────────────────────────────

    private void loadPriceFromDb() {
        if (currentOrderNumber == null) return;
        DatabaseHelper db = new DatabaseHelper(this);
        android.database.Cursor c = db.getOrderByNumber(currentOrderNumber);
        if (c.moveToFirst()) {
            int colQty   = c.getColumnIndex(DatabaseHelper.COL_ORDER_QUANTITY);
            int colPrice = c.getColumnIndex(DatabaseHelper.COL_ORDER_TOTAL_PRICE);
            int colPaid  = c.getColumnIndex(DatabaseHelper.COL_ORDER_IS_PAID); // ✅
            boolean isPaid = (colPaid != -1) && (c.getInt(colPaid) == 1);       // ✅
            if (colQty != -1 && colPrice != -1) {
                refreshPriceDisplay(c.getDouble(colQty), c.getDouble(colPrice), isPaid); // ✅
            }
        }
        c.close();
    }

    private void refreshPriceDisplay(double qty, double price, boolean isPaid) {
        if (price > 0) {
            if (layoutWeight != null)           layoutWeight.setVisibility(View.VISIBLE);
            if (layoutTotal  != null)           layoutTotal.setVisibility(View.VISIBLE);
            if (layoutPaymentStatusRow != null) layoutPaymentStatusRow.setVisibility(View.VISIBLE);
            if (tvWeight != null)      tvWeight.setText(String.format(Locale.getDefault(), "%.1f kg", qty));
            if (tvTotalAmount != null) tvTotalAmount.setText(String.format(Locale.getDefault(), "₱%.2f", price));

            // ✅ Now driven by what the admin actually set
            if (tvPaymentStatus != null) {
                if (isPaid) {
                    tvPaymentStatus.setText("Paid");
                    tvPaymentStatus.setTextColor(0xFF16A085);
                } else {
                    tvPaymentStatus.setText("Unpaid");
                    tvPaymentStatus.setTextColor(0xFFE74C3C);
                }
            }
        } else {
            if (layoutWeight != null)           layoutWeight.setVisibility(View.GONE);
            if (layoutTotal  != null)           layoutTotal.setVisibility(View.GONE);
            if (layoutPaymentStatusRow != null) layoutPaymentStatusRow.setVisibility(View.GONE);
        }
    }

    // ── DB helpers ────────────────────────────────────────────────────────────

    private String loadStatusFromDb() {
        if (currentOrderNumber == null) return "Pending";
        DatabaseHelper db = new DatabaseHelper(this);
        android.database.Cursor c = db.getOrderByNumber(currentOrderNumber);
        String status = "Pending";
        if (c.moveToFirst()) {
            int col = c.getColumnIndex(DatabaseHelper.COL_ORDER_STATUS);
            if (col != -1) {
                String s = c.getString(col);
                if (s != null && !s.isEmpty()) status = s;
            }
        }
        c.close();
        return status;
    }

    private void saveOrderToDatabase(Intent intent) {
        String orderNumber = intent.getStringExtra("order_number");
        if (orderNumber == null) return;

        DatabaseHelper db = new DatabaseHelper(this);
        android.database.Cursor existing = db.getOrderByNumber(orderNumber);
        boolean alreadyExists = existing.moveToFirst();
        existing.close();
        if (alreadyExists) return;

        SessionManager session = new SessionManager(this);
        int userId = session.getUserId();

        String clientName  = intent.getStringExtra("client_name");
        String clientPhone = intent.getStringExtra("client_phone");
        if (clientName  == null) clientName  = session.getFullName();
        if (clientPhone == null) clientPhone = getUserPhone(userId);

        ArrayList<String> labels = intent.getStringArrayListExtra("item_labels");
        String itemLabelsStr = OrderManager.labelsToString(
                labels != null ? labels : new ArrayList<>());

        String serviceType = intent.getStringExtra("service_type");
        String pickup      = intent.getStringExtra("pickup_address");
        String delivery    = intent.getStringExtra("delivery_address");

        if ("Drop-off".equals(serviceType) && (delivery == null || delivery.isEmpty())) {
            delivery = "Ground floor MIM Bldg., 2993 Kakarong St. Brgy. Sta Cruz, Makati City";
        }

        String selDate = intent.getStringExtra("selected_date");
        String selTime = intent.getStringExtra("selected_time");
        String scheduleString = "";
        if (selDate != null && !selDate.isEmpty()) {
            scheduleString = (selTime != null && !selTime.isEmpty())
                    ? selDate + "  •  " + selTime : selDate;
        }

        db.insertOrder(
                orderNumber, userId, clientName, clientPhone,
                serviceType, "", intent.getStringExtra("laundry_type"), serviceType,
                pickup   != null ? pickup   : "",
                delivery != null ? delivery : "",
                scheduleString, "", "per kg", 0, 0, 0,
                intent.getStringExtra("payment_method"),
                intent.getStringExtra("gcash_number"),
                intent.getStringExtra("gcash_name"),
                "", itemLabelsStr, intent.getIntExtra("bag_count", 0)
        );

        NotificationManager.getInstance().postOrderNotification(
                AppNotification.Type.ORDER_PLACED, orderNumber);
    }

    private String getUserPhone(int userId) {
        if (userId == -1) return "";
        DatabaseHelper db = new DatabaseHelper(this);
        android.database.Cursor c = db.getUserById(userId);
        String phone = "";
        if (c.moveToFirst())
            phone = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_USER_PHONE));
        c.close();
        return phone != null ? phone : "";
    }

    // ── Misc ──────────────────────────────────────────────────────────────────

    private void setOrderNotice(String method) {
        if (method != null && method.equalsIgnoreCase("GCash")) {
            tvOrderNotice.setText(
                    "Your laundry will be weighed at the store to determine the final price. " +
                            "Once confirmed, you'll receive a notification — that's your cue to send your GCash payment.");
        } else {
            tvOrderNotice.setText(
                    "Your laundry will be weighed at the store to determine the final price. " +
                            "You'll receive a notification once confirmed, and you can pay in cash upon delivery.");
        }
    }

    private void populateOrderItems(ArrayList<String> labels) {
        llOrderItems.removeAllViews();
        for (String label : labels) {
            View dot = new View(this);
            dot.setBackgroundResource(R.drawable.dot_blue);
            LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(8, 8);
            dotParams.setMarginEnd(10);
            dotParams.topMargin = 6;

            TextView tvLabel = new TextView(this);
            tvLabel.setText(label);
            tvLabel.setTextColor(0xFF555555);
            tvLabel.setTextSize(13);

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            rowParams.bottomMargin = 14;
            row.setLayoutParams(rowParams);

            row.addView(dot, dotParams);
            row.addView(tvLabel);
            llOrderItems.addView(row);
        }
    }

    private String safeStr(String s) {
        return (s != null) ? s : "";
    }
}
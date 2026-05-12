package com.example.markcleanlaundry;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdminHomeActivity extends AppCompatActivity {

    TextView tvTotalOrders, tvPendingOrders, tvCompletedOrders, tvTotalCustomers;
    RecyclerView rvRecentOrders;
    LinearLayout navDashboard, navOrders, navCustomers, navSettings;
    Button btnAddOrder;

    DatabaseHelper dbHelper;

    // Services that support a wash type (Machine/Hand Wash)
    private static final List<String> SERVICES_WITH_WASH_TYPE = new ArrayList<>(
            Arrays.asList("Wash & Fold", "Wash & Press", "Press Only", "Dry Cleaning")
    );

    // Services that require weight input (laundry-type services)
    private static final List<String> SERVICES_WITH_WEIGHT = new ArrayList<>(
            Arrays.asList(
                    "Wash & Fold", "Wash & Press", "Press Only", "Dry Cleaning",
                    "Hand Wash Only", "Curtain Cleaning"
            )
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        OrderManager.init(this);
        NotificationManager.init(this);

        dbHelper = new DatabaseHelper(this);

        tvTotalOrders     = findViewById(R.id.tvTotalOrders);
        tvPendingOrders   = findViewById(R.id.tvPendingOrders);
        tvCompletedOrders = findViewById(R.id.tvCompletedOrders);
        tvTotalCustomers  = findViewById(R.id.tvTotalCustomers);

        rvRecentOrders = findViewById(R.id.rvRecentOrders);
        rvRecentOrders.setLayoutManager(new LinearLayoutManager(this));

        navDashboard = findViewById(R.id.navDashboard);
        navOrders    = findViewById(R.id.navOrders);
        navCustomers = findViewById(R.id.navCustomers);
        navSettings  = findViewById(R.id.navSettings);
        btnAddOrder  = findViewById(R.id.btnAddOrder);

        loadStats();
        loadOrdersList();
        setupBottomNav();

        btnAddOrder.setOnClickListener(v -> showAddOrderDialog());

        findViewById(R.id.btnAdminLogout).setOnClickListener(v -> {
            new SessionManager(this).logout();
            Intent intent = new Intent(this, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadStats() {
        tvTotalOrders.setText(String.valueOf(dbHelper.getTotalOrders()));
        tvPendingOrders.setText(String.valueOf(dbHelper.getPendingOrders()));
        tvCompletedOrders.setText(String.valueOf(dbHelper.getCompletedOrders()));
        tvTotalCustomers.setText(String.valueOf(dbHelper.getTotalCustomers()));
    }

    // ── Orders list: walk-in + app orders all appear here ────────────────────
    private void loadOrdersList() {
        Cursor cursor = dbHelper.getAllOrders();
        List<OrderRow> rows = new ArrayList<>();

        while (cursor.moveToNext()) {
            OrderRow row = new OrderRow();
            row.orderId    = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_NUMBER));
            row.clientName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_CLIENT_NAME));
            row.phone      = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_PHONE));
            row.orderType  = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_SERVICE_TYPE));
            row.date       = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_CREATED_AT));
            row.status     = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_STATUS));
            row.service    = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_SERVICE));
            row.address    = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_ADDRESS));
            row.totalPrice = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_TOTAL_PRICE));

            int userIdCol = cursor.getColumnIndex(DatabaseHelper.COL_ORDER_USER_ID);
            row.isWalkIn = (userIdCol != -1 && cursor.getInt(userIdCol) == -1);

            rows.add(row);
        }
        cursor.close();
        rvRecentOrders.setAdapter(new OrderRowAdapter(rows));
    }

    // ── ADD Walk-in Order Dialog ──────────────────────────────────────────────
    private void showAddOrderDialog() {
        float dp  = getResources().getDisplayMetrics().density;
        int   pad = (int) (20 * dp);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(pad, pad, pad, pad);

        // ── Client Name ──────────────────────────────────────────────────────
        EditText etClientName = new EditText(this);
        etClientName.setHint("Client Name *");
        layout.addView(etClientName);

        // ── Phone ────────────────────────────────────────────────────────────
        EditText etPhone = new EditText(this);
        etPhone.setHint("Phone Number *");
        etPhone.setInputType(InputType.TYPE_CLASS_PHONE);
        LinearLayout.LayoutParams phoneParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        phoneParams.topMargin = (int) (12 * dp);
        etPhone.setLayoutParams(phoneParams);
        layout.addView(etPhone);

        // ── Service label + spinner ──────────────────────────────────────────
        TextView tvService = new TextView(this);
        tvService.setText("Service:");
        tvService.setTextColor(0xFF555555);
        LinearLayout.LayoutParams serviceLabelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        serviceLabelParams.topMargin = (int) (14 * dp);
        tvService.setLayoutParams(serviceLabelParams);
        layout.addView(tvService);

        String[] services = {
                "Wash & Fold",
                "Wash & Press",
                "Press Only",
                "Dry Cleaning",
                "Shoe Cleaning",
                "Bag Cleaning",
                "Steam/Press Only",
                "Hand Wash Only",
                "Curtain Cleaning"
        };
        Spinner spService = new Spinner(this);
        spService.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, services));
        layout.addView(spService);

        // ── Wash Type (Machine/Hand) — only for applicable services ──────────
        TextView tvWashType = new TextView(this);
        tvWashType.setText("Wash Type:");
        tvWashType.setTextColor(0xFF555555);
        LinearLayout.LayoutParams washTypeLabelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        washTypeLabelParams.topMargin = (int) (14 * dp);
        tvWashType.setLayoutParams(washTypeLabelParams);
        layout.addView(tvWashType);

        String[] washTypes = {"Machine Wash", "Hand Wash"};
        Spinner spWashType = new Spinner(this);
        spWashType.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, washTypes));
        layout.addView(spWashType);

        // ── Weight — only for laundry-type services ──────────────────────────
        TextView tvWeight = new TextView(this);
        tvWeight.setText("Weight (kg):");
        tvWeight.setTextColor(0xFF555555);
        LinearLayout.LayoutParams weightLabelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        weightLabelParams.topMargin = (int) (14 * dp);
        tvWeight.setLayoutParams(weightLabelParams);
        layout.addView(tvWeight);

        EditText etWeight = new EditText(this);
        etWeight.setHint("e.g. 2.5");
        etWeight.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(etWeight);

        // ── Total Price — always shown ────────────────────────────────────────
        TextView tvPrice = new TextView(this);
        tvPrice.setText("Total Price (₱):");
        tvPrice.setTextColor(0xFF555555);
        LinearLayout.LayoutParams priceLabelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        priceLabelParams.topMargin = (int) (14 * dp);
        tvPrice.setLayoutParams(priceLabelParams);
        layout.addView(tvPrice);

        EditText etPrice = new EditText(this);
        etPrice.setHint("e.g. 150");
        etPrice.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(etPrice);

        // ── Toggle wash type + weight visibility based on selected service ────
        spService.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view,
                                       int position, long id) {
                String selected = services[position];

                boolean showWashType = SERVICES_WITH_WASH_TYPE.contains(selected);
                tvWashType.setVisibility(showWashType ? View.VISIBLE : View.GONE);
                spWashType.setVisibility(showWashType ? View.VISIBLE : View.GONE);

                boolean showWeight = SERVICES_WITH_WEIGHT.contains(selected);
                tvWeight.setVisibility(showWeight ? View.VISIBLE : View.GONE);
                etWeight.setVisibility(showWeight ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // Trigger initial visibility state for the default selected service
        String initialService = services[0];
        boolean initialShowWash   = SERVICES_WITH_WASH_TYPE.contains(initialService);
        boolean initialShowWeight = SERVICES_WITH_WEIGHT.contains(initialService);
        tvWashType.setVisibility(initialShowWash   ? View.VISIBLE : View.GONE);
        spWashType.setVisibility(initialShowWash   ? View.VISIBLE : View.GONE);
        tvWeight.setVisibility(initialShowWeight   ? View.VISIBLE : View.GONE);
        etWeight.setVisibility(initialShowWeight   ? View.VISIBLE : View.GONE);

        // ── Build dialog ──────────────────────────────────────────────────────
        new AlertDialog.Builder(this)
                .setTitle("Add Walk-in Order")
                .setView(layout)
                .setPositiveButton("Add", (dialog, which) -> {
                    String clientName = etClientName.getText().toString().trim();
                    String phone      = etPhone.getText().toString().trim();
                    String service    = spService.getSelectedItem().toString();

                    String washType = SERVICES_WITH_WASH_TYPE.contains(service)
                            ? spWashType.getSelectedItem().toString()
                            : "N/A";

                    boolean needsWeight = SERVICES_WITH_WEIGHT.contains(service);
                    String  weightStr   = etWeight.getText().toString().trim();
                    String  priceStr    = etPrice.getText().toString().trim();

                    // ── Validation ────────────────────────────────────────────
                    if (clientName.isEmpty() || phone.isEmpty()) {
                        showInfo("Client name and phone are required.");
                        return;
                    }
                    if (needsWeight && weightStr.isEmpty()) {
                        showInfo("Please enter the weight for this service.");
                        return;
                    }
                    if (priceStr.isEmpty()) {
                        showInfo("Please enter the total price.");
                        return;
                    }

                    double weight     = needsWeight && !weightStr.isEmpty()
                            ? Double.parseDouble(weightStr) : 0;
                    double totalPrice = Double.parseDouble(priceStr);

                    String orderNumber = dbHelper.generateOrderNumber();

                    // userId = -1 → walk-in sentinel, never fires notifications
                    long result = dbHelper.insertOrder(
                            orderNumber,
                            -1,           // walk-in
                            clientName,
                            phone,
                            service,
                            "Walk-in",    // serviceType
                            washType,     // laundryType
                            "Standard",   // service tier
                            "",           // address — not needed for walk-in
                            "", "", "",
                            "kg",
                            weight,       // actual weight entered by admin
                            weight,       // use same as estimated weight
                            totalPrice,   // price set on the spot
                            "Cash", "", "", "", "", 0
                    );

                    if (result != -1) {
                        showInfo("Walk-in order #" + orderNumber + " added.\n"
                                + "Service: " + service + "\n"
                                + (needsWeight ? "Weight: " + weight + " kg\n" : "")
                                + "Total: ₱" + totalPrice);
                        loadStats();
                        loadOrdersList();
                    } else {
                        showInfo("Failed to add order. Please try again.");
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ── VIEW ──────────────────────────────────────────────────────────────────
    private void showViewOrderDialog(OrderRow row) {
        String details =
                "Order #: "      + row.orderId    + "\n" +
                        "Type: "         + (row.isWalkIn ? "Walk-in" : "App Order") + "\n" +
                        "Client: "       + row.clientName + "\n" +
                        "Phone: "        + row.phone      + "\n" +
                        "Service: "      + row.service    + "\n" +
                        "Order Type: "   + row.orderType  + "\n" +
                        "Address: "      + (row.address.isEmpty() ? "—" : row.address) + "\n" +
                        "Total Price: ₱" + row.totalPrice + "\n" +
                        "Status: "       + row.status     + "\n" +
                        "Date: "         + row.date;

        new AlertDialog.Builder(this)
                .setTitle("Order Details")
                .setMessage(details)
                .setPositiveButton("Close", null)
                .show();
    }

    // ── EDIT ──────────────────────────────────────────────────────────────────
    private void showEditOrderDialog(OrderRow row) {
        float dp  = getResources().getDisplayMetrics().density;
        int   pad = (int) (20 * dp);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(pad, pad, pad, pad);

        EditText etClientName = new EditText(this);
        etClientName.setHint("Client Name");
        etClientName.setText(row.clientName);
        layout.addView(etClientName);

        EditText etPhone = new EditText(this);
        etPhone.setHint("Phone");
        etPhone.setText(row.phone);
        etPhone.setInputType(InputType.TYPE_CLASS_PHONE);
        layout.addView(etPhone);

        new AlertDialog.Builder(this)
                .setTitle("Edit Order #" + row.orderId)
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName  = etClientName.getText().toString().trim();
                    String newPhone = etPhone.getText().toString().trim();

                    if (newName.isEmpty() || newPhone.isEmpty()) {
                        showInfo("Name and phone cannot be empty.");
                        return;
                    }

                    dbHelper.getWritableDatabase().execSQL(
                            "UPDATE " + DatabaseHelper.TABLE_ORDERS +
                                    " SET " + DatabaseHelper.COL_ORDER_CLIENT_NAME + " = ?, " +
                                    DatabaseHelper.COL_ORDER_PHONE + " = ?" +
                                    " WHERE " + DatabaseHelper.COL_ORDER_NUMBER + " = ?",
                            new String[]{ newName, newPhone, row.orderId }
                    );

                    showInfo("Order #" + row.orderId + " updated successfully.");
                    loadOrdersList();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ── DELETE ────────────────────────────────────────────────────────────────
    private void showDeleteOrderDialog(OrderRow row) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Order")
                .setMessage("Delete Order #" + row.orderId + "? This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.getWritableDatabase().delete(
                            DatabaseHelper.TABLE_ORDERS,
                            DatabaseHelper.COL_ORDER_NUMBER + "=?",
                            new String[]{ row.orderId }
                    );
                    showInfo("Order #" + row.orderId + " deleted.");
                    loadStats();
                    loadOrdersList();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showInfo(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Information")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    // ── Bottom Nav ────────────────────────────────────────────────────────────
    private void setupBottomNav() {
        navDashboard.setOnClickListener(v -> { });
        // Walk-ins do NOT appear in AdminBookingsActivity — homepage list only
        navOrders.setOnClickListener(v -> startActivity(
                new Intent(this, AdminBookingsActivity.class)));
        navCustomers.setOnClickListener(v -> startActivity(
                new Intent(this, AdminUsersActivity.class)));
        navSettings.setOnClickListener(v -> { });
    }

    // ── Data model ────────────────────────────────────────────────────────────
    static class OrderRow {
        String  orderId, clientName, phone, orderType, date, status, service, address, totalPrice;
        boolean isWalkIn;
    }

    // ── Adapter ───────────────────────────────────────────────────────────────
    class OrderRowAdapter extends RecyclerView.Adapter<OrderRowAdapter.VH> {

        private final List<OrderRow> data;
        OrderRowAdapter(List<OrderRow> data) { this.data = data; }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_admin_order_row, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {
            OrderRow row = data.get(position);
            holder.tvOrderId.setText(row.orderId);
            holder.tvClientName.setText(row.clientName);
            holder.tvPhone.setText(row.phone);

            // Show service name instead of just order type
            holder.tvService.setText(row.service != null && !row.service.isEmpty()
                    ? row.service : row.orderType);

            // Show price with ₱ prefix; show "—" if 0 or empty
            boolean hasPrice = row.totalPrice != null
                    && !row.totalPrice.isEmpty()
                    && !row.totalPrice.equals("0")
                    && !row.totalPrice.equals("0.0");
            holder.tvPrice.setText(hasPrice ? "₱" + row.totalPrice : "—");

            holder.tvDate.setText(row.date);

            // Walk-in badge visibility
            if (holder.tvWalkInBadge != null) {
                holder.tvWalkInBadge.setVisibility(row.isWalkIn ? View.VISIBLE : View.GONE);
            }

            holder.itemView.setOnClickListener(v -> showViewOrderDialog(row));
            holder.btnEdit.setOnClickListener(v -> showEditOrderDialog(row));
            holder.btnDelete.setOnClickListener(v -> showDeleteOrderDialog(row));
        }

        @Override
        public int getItemCount() { return data.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView tvOrderId, tvClientName, tvPhone, tvService, tvPrice, tvDate, tvWalkInBadge;
            Button   btnEdit, btnDelete;

            VH(View v) {
                super(v);
                tvOrderId     = v.findViewById(R.id.tvOrderId);
                tvClientName  = v.findViewById(R.id.tvClientName);
                tvPhone       = v.findViewById(R.id.tvPhone);
                tvService     = v.findViewById(R.id.tvService);     // NEW — was tvOrderType
                tvPrice       = v.findViewById(R.id.tvPrice);       // NEW
                tvDate        = v.findViewById(R.id.tvDate);
                btnEdit       = v.findViewById(R.id.btnEditOrder);
                btnDelete     = v.findViewById(R.id.btnDeleteOrder);
            }
        }
    }
}
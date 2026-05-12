package com.example.markcleanlaundry;

import android.content.Context;
import android.database.Cursor;
import java.util.ArrayList;
import java.util.List;

public class OrderManager {

    private static OrderManager instance;
    private Context appContext;

    private OrderManager(Context context) {
        this.appContext = context.getApplicationContext();
    }

    public static void init(Context context) {
        if (instance == null) {
            instance = new OrderManager(context);
        }
    }

    public static OrderManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException(
                    "OrderManager not initialized. Call OrderManager.init(context) first.");
        }
        return instance;
    }

    // ── Serialization helpers ─────────────────────────────────────────────────

    public static String labelsToString(ArrayList<String> labels) {
        if (labels == null || labels.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < labels.size(); i++) {
            sb.append(labels.get(i).replace(",", ";"));
            if (i < labels.size() - 1) sb.append(",");
        }
        return sb.toString();
    }

    public static ArrayList<String> labelsFromString(String raw) {
        ArrayList<String> list = new ArrayList<>();
        if (raw == null || raw.trim().isEmpty()) return list;
        for (String part : raw.split(",")) {
            list.add(part.replace(";", ",").trim());
        }
        return list;
    }

    // ── Cursor → OrderItem ────────────────────────────────────────────────────

    public static OrderItem fromCursor(Cursor c) {
        String orderNumber   = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_NUMBER));
        String status        = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_STATUS));
        String pickupDate    = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_PICKUP_DATE));
        String paymentMethod = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_PAYMENT_METHOD));
        String pickupAddr    = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_ADDRESS));
        String deliveryAddr  = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_DELIVERY_ADDRESS));
        String service       = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_SERVICE));
        String laundryType   = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_LAUNDRY_TYPE));
        int    numBags       = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_NUM_BAGS));
        long   timestamp     = c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_CREATED_AT));
        String gcashNumber   = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_GCASH_NUMBER));
        String rawLabels     = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_ITEM_LABELS));
        String serviceType   = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_SERVICE_TYPE));
        double totalPrice    = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_TOTAL_PRICE));
        double quantity      = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_QUANTITY));
        String unitLabel     = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_UNIT_LABEL));
        String clientName    = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_CLIENT_NAME));
        String phone         = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_PHONE));

        ArrayList<String> labels = labelsFromString(rawLabels);

        return new OrderItem(
                orderNumber, status, pickupDate, paymentMethod,
                pickupAddr, deliveryAddr, service != null ? service : serviceType,
                numBags, timestamp, labels, gcashNumber, laundryType,
                totalPrice, quantity, unitLabel,
                clientName, phone
        );
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public long addOrder(OrderItem order) {
        DatabaseHelper db = new DatabaseHelper(appContext);
        SessionManager session = new SessionManager(appContext);
        int userId = session.getUserId();

        return db.insertOrder(
                order.getOrderNumber(),
                userId,
                order.getClientName(),
                order.getPhone(),
                order.getServiceType(),
                "",
                order.getLaundryType(),
                order.getServiceType(),
                order.getPickupAddress(),
                order.getDeliveryAddress(),
                order.getScheduleDate(),
                "",
                order.getUnitLabel(),
                0,
                order.getQuantity(),
                order.getTotalPrice(),
                order.getPaymentMethod(),
                order.getGcashNumber(),
                "",
                "",
                labelsToString(order.getItemLabels()),
                order.getBagCount()
        );
    }

    public List<OrderItem> getOrders() {
        DatabaseHelper db = new DatabaseHelper(appContext);
        SessionManager session = new SessionManager(appContext);
        int userId = session.getUserId();

        List<OrderItem> result = new ArrayList<>();
        Cursor c = (userId == -1)
                ? db.getAllOrders()
                : db.getOrdersByUser(userId);

        if (c.moveToFirst()) {
            do {
                result.add(fromCursor(c));
            } while (c.moveToNext());
        }
        c.close();
        return result;
    }

    public List<OrderItem> getAllOrders() {
        DatabaseHelper db = new DatabaseHelper(appContext);
        List<OrderItem> result = new ArrayList<>();
        Cursor c = db.getAllOrders();
        if (c.moveToFirst()) {
            do {
                result.add(fromCursor(c));
            } while (c.moveToNext());
        }
        c.close();
        return result;
    }

    public OrderItem getOrderByNumber(String orderNumber) {
        DatabaseHelper db = new DatabaseHelper(appContext);
        Cursor c = db.getOrderByNumber(orderNumber);
        OrderItem order = null;
        if (c.moveToFirst()) {
            order = fromCursor(c);
        }
        c.close();
        return order;
    }

    public void clear() {
        // SQLite is the source of truth
    }
}
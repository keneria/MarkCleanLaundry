package com.example.markcleanlaundry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME    = "markclean.db";
    private static final int    DATABASE_VERSION = 4;

    // ── Table Names ───────────────────────────────────────────────────────────
    public static final String TABLE_USERS         = "users";
    public static final String TABLE_ORDERS        = "orders";
    public static final String TABLE_ORDER_ITEMS   = "order_items";
    public static final String TABLE_NOTIFICATIONS = "notifications";

    // ── USERS columns ─────────────────────────────────────────────────────────
    public static final String COL_USER_ID         = "id";
    public static final String COL_USER_USERNAME   = "username";
    public static final String COL_USER_FULLNAME   = "full_name";
    public static final String COL_USER_EMAIL      = "email";
    public static final String COL_USER_PHONE      = "phone";
    public static final String COL_USER_PASSWORD   = "password";
    public static final String COL_USER_ADDRESS    = "address";
    public static final String COL_USER_CREATED_AT = "created_at";

    // ── ORDERS columns ────────────────────────────────────────────────────────
    public static final String COL_ORDER_ID               = "id";
    public static final String COL_ORDER_NUMBER           = "order_number";
    public static final String COL_ORDER_USER_ID          = "user_id";
    public static final String COL_ORDER_CLIENT_NAME      = "client_name";
    public static final String COL_ORDER_PHONE            = "phone";
    public static final String COL_ORDER_SERVICE          = "service";
    public static final String COL_ORDER_METHOD           = "method";
    public static final String COL_ORDER_SERVICE_TYPE     = "service_type";
    public static final String COL_ORDER_ADDRESS          = "address";
    public static final String COL_ORDER_PICKUP_DATE      = "pickup_date";
    public static final String COL_ORDER_PICKUP_TIME      = "pickup_time";
    public static final String COL_ORDER_UNIT_LABEL       = "unit_label";
    public static final String COL_ORDER_UNIT_PRICE       = "unit_price";
    public static final String COL_ORDER_QUANTITY         = "quantity";
    public static final String COL_ORDER_TOTAL_PRICE      = "total_price";
    public static final String COL_ORDER_PAYMENT_METHOD   = "payment_method";
    public static final String COL_ORDER_GCASH_NUMBER     = "gcash_number";
    public static final String COL_ORDER_GCASH_NAME       = "gcash_account_name";
    public static final String COL_ORDER_INSTRUCTIONS     = "special_instructions";
    public static final String COL_ORDER_STATUS           = "status";
    public static final String COL_ORDER_NUM_BAGS         = "num_bags";
    public static final String COL_ORDER_CREATED_AT       = "date";

    // ── NEW columns added in version 2 ────────────────────────────────────────
    public static final String COL_ORDER_ITEM_LABELS      = "item_labels";
    public static final String COL_ORDER_LAUNDRY_TYPE     = "laundry_type";
    public static final String COL_ORDER_DELIVERY_ADDRESS = "delivery_address";
    public static final String COL_ORDER_CLAIMING_METHOD  = "claiming_method";

    // ── NEW column added in version 4 ─────────────────────────────────────────
    public static final String COL_ORDER_IS_PAID          = "is_paid";

    // ── NOTIFICATIONS columns ─────────────────────────────────────────────────
    public static final String COL_NOTIF_ID           = "id";
    public static final String COL_NOTIF_USER_ID      = "user_id";
    public static final String COL_NOTIF_TYPE         = "type";
    public static final String COL_NOTIF_ORDER_NUMBER = "order_number";
    public static final String COL_NOTIF_IS_READ      = "is_read";
    public static final String COL_NOTIF_TIMESTAMP    = "timestamp";

    // ── CREATE statements ─────────────────────────────────────────────────────

    private static final String CREATE_USERS =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COL_USER_ID         + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_USER_USERNAME   + " TEXT NOT NULL UNIQUE, " +
                    COL_USER_FULLNAME   + " TEXT, " +
                    COL_USER_EMAIL      + " TEXT, " +
                    COL_USER_PHONE      + " TEXT, " +
                    COL_USER_PASSWORD   + " TEXT NOT NULL, " +
                    COL_USER_ADDRESS    + " TEXT, " +
                    COL_USER_CREATED_AT + " TEXT DEFAULT (datetime('now','localtime'))" +
                    ");";

    private static final String CREATE_ORDERS =
            "CREATE TABLE " + TABLE_ORDERS + " (" +
                    COL_ORDER_ID               + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_ORDER_NUMBER           + " TEXT NOT NULL UNIQUE, " +
                    COL_ORDER_USER_ID          + " INTEGER, " +
                    COL_ORDER_CLIENT_NAME      + " TEXT, " +
                    COL_ORDER_PHONE            + " TEXT, " +
                    COL_ORDER_SERVICE          + " TEXT NOT NULL, " +
                    COL_ORDER_METHOD           + " TEXT, " +
                    COL_ORDER_LAUNDRY_TYPE     + " TEXT, " +
                    COL_ORDER_SERVICE_TYPE     + " TEXT NOT NULL, " +
                    COL_ORDER_ADDRESS          + " TEXT, " +
                    COL_ORDER_DELIVERY_ADDRESS + " TEXT, " +
                    COL_ORDER_PICKUP_DATE      + " TEXT, " +
                    COL_ORDER_PICKUP_TIME      + " TEXT, " +
                    COL_ORDER_UNIT_LABEL       + " TEXT, " +
                    COL_ORDER_UNIT_PRICE       + " REAL DEFAULT 0, " +
                    COL_ORDER_QUANTITY         + " REAL DEFAULT 0, " +
                    COL_ORDER_TOTAL_PRICE      + " REAL DEFAULT 0, " +
                    COL_ORDER_PAYMENT_METHOD   + " TEXT, " +
                    COL_ORDER_GCASH_NUMBER     + " TEXT, " +
                    COL_ORDER_GCASH_NAME       + " TEXT, " +
                    COL_ORDER_INSTRUCTIONS     + " TEXT, " +
                    COL_ORDER_ITEM_LABELS      + " TEXT, " +
                    COL_ORDER_CLAIMING_METHOD  + " TEXT DEFAULT '', " +
                    COL_ORDER_STATUS           + " TEXT DEFAULT 'Pending', " +
                    COL_ORDER_NUM_BAGS         + " INTEGER DEFAULT 0, " +
                    COL_ORDER_IS_PAID          + " INTEGER DEFAULT 0, " +
                    COL_ORDER_CREATED_AT       + " TEXT DEFAULT (datetime('now','localtime')), " +
                    "FOREIGN KEY(" + COL_ORDER_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ")" +
                    ");";

    private static final String CREATE_NOTIFICATIONS =
            "CREATE TABLE " + TABLE_NOTIFICATIONS + " (" +
                    COL_NOTIF_ID           + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_NOTIF_USER_ID      + " INTEGER NOT NULL, " +
                    COL_NOTIF_TYPE         + " TEXT NOT NULL, " +
                    COL_NOTIF_ORDER_NUMBER + " TEXT NOT NULL, " +
                    COL_NOTIF_IS_READ      + " INTEGER DEFAULT 0, " +
                    COL_NOTIF_TIMESTAMP    + " INTEGER NOT NULL, " +
                    "FOREIGN KEY(" + COL_NOTIF_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ")" +
                    ");";

    // ── Constructor ───────────────────────────────────────────────────────────

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USERS);
        db.execSQL(CREATE_ORDERS);
        db.execSQL(CREATE_NOTIFICATIONS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_ORDERS + " ADD COLUMN " + COL_ORDER_ITEM_LABELS      + " TEXT;");
            db.execSQL("ALTER TABLE " + TABLE_ORDERS + " ADD COLUMN " + COL_ORDER_LAUNDRY_TYPE     + " TEXT;");
            db.execSQL("ALTER TABLE " + TABLE_ORDERS + " ADD COLUMN " + COL_ORDER_DELIVERY_ADDRESS + " TEXT;");
            db.execSQL(CREATE_NOTIFICATIONS);
        }

        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_ORDERS + " ADD COLUMN "
                    + COL_ORDER_CLAIMING_METHOD + " TEXT DEFAULT '';");
        }

        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE " + TABLE_ORDERS + " ADD COLUMN "
                    + COL_ORDER_IS_PAID + " INTEGER DEFAULT 0;");
        }
    }

    // =========================================================================
    // USER methods
    // =========================================================================

    public long registerUser(String username, String fullName, String email,
                             String phone, String password, String address) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_USER_USERNAME, username);
        cv.put(COL_USER_FULLNAME, fullName);
        cv.put(COL_USER_EMAIL,    email);
        cv.put(COL_USER_PHONE,    phone);
        cv.put(COL_USER_PASSWORD, password);
        cv.put(COL_USER_ADDRESS,  address);
        return db.insert(TABLE_USERS, null, cv);
    }

    public Cursor loginUser(String username, String password) {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_USERS, null,
                COL_USER_USERNAME + "=? AND " + COL_USER_PASSWORD + "=?",
                new String[]{username, password}, null, null, null);
    }

    public boolean usernameExists(String username) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_USERS, new String[]{COL_USER_ID},
                COL_USER_USERNAME + "=?", new String[]{username}, null, null, null);
        boolean exists = c.getCount() > 0;
        c.close();
        return exists;
    }

    public Cursor getUserById(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_USERS, null,
                COL_USER_ID + "=?", new String[]{String.valueOf(userId)},
                null, null, null);
    }

    public int updateUser(int userId, String fullName, String email,
                          String phone, String address) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_USER_FULLNAME, fullName);
        cv.put(COL_USER_EMAIL,    email);
        cv.put(COL_USER_PHONE,    phone);
        cv.put(COL_USER_ADDRESS,  address);
        return db.update(TABLE_USERS, cv,
                COL_USER_ID + "=?", new String[]{String.valueOf(userId)});
    }

    public int getTotalCustomers() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_USERS, null);
        int count = 0;
        if (c.moveToFirst()) count = c.getInt(0);
        c.close();
        return count;
    }

    // =========================================================================
    // ORDER methods
    // =========================================================================

    public long insertOrder(String orderNumber, int userId, String clientName,
                            String phone, String service, String method,
                            String laundryType, String serviceType,
                            String pickupAddress, String deliveryAddress,
                            String pickupDate, String pickupTime,
                            String unitLabel, double unitPrice, double quantity,
                            double totalPrice, String paymentMethod,
                            String gcashNumber, String gcashName,
                            String instructions, String itemLabels, int numBags) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_ORDER_NUMBER,           orderNumber);
        cv.put(COL_ORDER_USER_ID,          userId);
        cv.put(COL_ORDER_CLIENT_NAME,      clientName);
        cv.put(COL_ORDER_PHONE,            phone);
        cv.put(COL_ORDER_SERVICE,          service);
        cv.put(COL_ORDER_METHOD,           method);
        cv.put(COL_ORDER_LAUNDRY_TYPE,     laundryType);
        cv.put(COL_ORDER_SERVICE_TYPE,     serviceType);
        cv.put(COL_ORDER_ADDRESS,          pickupAddress);
        cv.put(COL_ORDER_DELIVERY_ADDRESS, deliveryAddress);
        cv.put(COL_ORDER_PICKUP_DATE,      pickupDate);
        cv.put(COL_ORDER_PICKUP_TIME,      pickupTime);
        cv.put(COL_ORDER_UNIT_LABEL,       unitLabel);
        cv.put(COL_ORDER_UNIT_PRICE,       unitPrice);
        cv.put(COL_ORDER_QUANTITY,         quantity);
        cv.put(COL_ORDER_TOTAL_PRICE,      totalPrice);
        cv.put(COL_ORDER_PAYMENT_METHOD,   paymentMethod);
        cv.put(COL_ORDER_GCASH_NUMBER,     gcashNumber);
        cv.put(COL_ORDER_GCASH_NAME,       gcashName);
        cv.put(COL_ORDER_INSTRUCTIONS,     instructions);
        cv.put(COL_ORDER_ITEM_LABELS,      itemLabels);
        cv.put(COL_ORDER_STATUS,           "Pending");
        cv.put(COL_ORDER_NUM_BAGS,         numBags);
        cv.put(COL_ORDER_IS_PAID,          0);
        return db.insert(TABLE_ORDERS, null, cv);
    }

    public Cursor getOrdersByUser(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_ORDERS, null,
                COL_ORDER_USER_ID + "=?", new String[]{String.valueOf(userId)},
                null, null, COL_ORDER_CREATED_AT + " DESC");
    }

    public Cursor getOrderByNumber(String orderNumber) {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_ORDERS, null,
                COL_ORDER_NUMBER + "=?", new String[]{orderNumber},
                null, null, null);
    }

    public Cursor getOrderById(int orderId) {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_ORDERS, null,
                COL_ORDER_ID + "=?", new String[]{String.valueOf(orderId)},
                null, null, null);
    }

    public Cursor getAllOrders() {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_ORDERS, null, null, null,
                null, null, COL_ORDER_CREATED_AT + " DESC");
    }

    public Cursor getOrdersByStatus(String status) {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_ORDERS, null,
                COL_ORDER_STATUS + "=?", new String[]{status},
                null, null, COL_ORDER_CREATED_AT + " DESC");
    }

    public int updateOrderStatus(String orderNumber, String newStatus) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_ORDER_STATUS, newStatus);
        return db.update(TABLE_ORDERS, cv,
                COL_ORDER_NUMBER + "=?", new String[]{orderNumber});
    }

    public int updateOrderPrice(String orderNumber, double quantity, double totalPrice) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_ORDER_QUANTITY,    quantity);
        cv.put(COL_ORDER_TOTAL_PRICE, totalPrice);
        return db.update(TABLE_ORDERS, cv,
                COL_ORDER_NUMBER + "=?", new String[]{orderNumber});
    }

    public int updateNumBags(String orderNumber, int numBags) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_ORDER_NUM_BAGS, numBags);
        return db.update(TABLE_ORDERS, cv,
                COL_ORDER_NUMBER + "=?", new String[]{orderNumber});
    }

    public int updateClaimingMethod(String orderNumber, String claimingMethod) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_ORDER_CLAIMING_METHOD, claimingMethod);
        return db.update(TABLE_ORDERS, cv,
                COL_ORDER_NUMBER + "=?", new String[]{orderNumber});
    }

    public int updateDeliveryAddress(String orderNumber, String deliveryAddress) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_ORDER_DELIVERY_ADDRESS, deliveryAddress);
        return db.update(TABLE_ORDERS, cv,
                COL_ORDER_NUMBER + "=?", new String[]{orderNumber});
    }

    /** Admin: mark an order as paid or unpaid. */
    public int updateOrderPaidStatus(String orderNumber, boolean isPaid) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_ORDER_IS_PAID, isPaid ? 1 : 0);
        return db.update(TABLE_ORDERS, cv,
                COL_ORDER_NUMBER + "=?", new String[]{orderNumber});
    }

    // ── Dashboard stat helpers ─────────────────────────────────────────────────

    public int getTotalOrders() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_ORDERS, null);
        int count = 0;
        if (c.moveToFirst()) count = c.getInt(0);
        c.close();
        return count;
    }

    public int getPendingOrders() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_ORDERS +
                        " WHERE " + COL_ORDER_STATUS + " != 'Delivered'" +
                        " AND "   + COL_ORDER_STATUS + " != 'Completed'", null);
        int count = 0;
        if (c.moveToFirst()) count = c.getInt(0);
        c.close();
        return count;
    }

    public int getCompletedOrders() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_ORDERS +
                        " WHERE " + COL_ORDER_STATUS + " = 'Delivered'" +
                        " OR "    + COL_ORDER_STATUS + " = 'Completed'", null);
        int count = 0;
        if (c.moveToFirst()) count = c.getInt(0);
        c.close();
        return count;
    }

    // =========================================================================
    // NOTIFICATION methods
    // =========================================================================

    public long insertNotification(int userId, String type, String orderNumber, long timestamp) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_NOTIF_USER_ID,      userId);
        cv.put(COL_NOTIF_TYPE,         type);
        cv.put(COL_NOTIF_ORDER_NUMBER, orderNumber);
        cv.put(COL_NOTIF_IS_READ,      0);
        cv.put(COL_NOTIF_TIMESTAMP,    timestamp);
        return db.insert(TABLE_NOTIFICATIONS, null, cv);
    }

    public Cursor getNotificationsByUser(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_NOTIFICATIONS, null,
                COL_NOTIF_USER_ID + "=?", new String[]{String.valueOf(userId)},
                null, null, COL_NOTIF_TIMESTAMP + " DESC");
    }

    public int getUnreadCount(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_NOTIFICATIONS +
                        " WHERE " + COL_NOTIF_USER_ID + "=?" +
                        " AND "   + COL_NOTIF_IS_READ + "=0",
                new String[]{String.valueOf(userId)});
        int count = 0;
        if (c.moveToFirst()) count = c.getInt(0);
        c.close();
        return count;
    }

    public void markAllNotificationsRead(int userId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_NOTIF_IS_READ, 1);
        db.update(TABLE_NOTIFICATIONS, cv,
                COL_NOTIF_USER_ID + "=?", new String[]{String.valueOf(userId)});
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    public String generateOrderNumber() {
        int num = 10000 + (int)(Math.random() * 90000);
        return String.valueOf(num);
    }
}
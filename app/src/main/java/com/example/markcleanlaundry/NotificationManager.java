package com.example.markcleanlaundry;

import android.content.Context;
import android.database.Cursor;
import java.util.ArrayList;
import java.util.List;

/**
 * NotificationManager — now backed by SQLite via DatabaseHelper.
 *
 * All notifications are stored per-user (linked by user_id from SessionManager).
 * The in-memory listener callback is kept so UI can still react to new notifications
 * arriving in the same session.
 *
 * Usage:
 *   NotificationManager.init(context);   // call once alongside OrderManager.init()
 *   NotificationManager.getInstance().postOrderNotification(type, orderNumber);
 *   NotificationManager.getInstance().getNotifications();   // returns List<AppNotification>
 */
public class NotificationManager {

    private static NotificationManager instance;
    private Context appContext;
    private OnNotificationAddedListener listener;

    public interface OnNotificationAddedListener {
        void onNotificationAdded();
    }

    private NotificationManager(Context context) {
        this.appContext = context.getApplicationContext();
    }

    /** Call once (e.g. in Application.onCreate or your launch Activity). */
    public static void init(Context context) {
        if (instance == null) {
            instance = new NotificationManager(context);
        }
    }

    public static NotificationManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException(
                    "NotificationManager not initialized. Call NotificationManager.init(context) first.");
        }
        return instance;
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    /**
     * Post a notification for the currently logged-in user.
     * Also fires the in-session listener so the bell icon updates immediately.
     *
     * For admin-triggered status changes, pass the target user's ID explicitly
     * via postOrderNotificationForUser().
     */
    public void postOrderNotification(AppNotification.Type type, String orderNumber) {
        SessionManager session = new SessionManager(appContext);
        int userId = session.getUserId();
        if (userId == -1) return; // no logged-in user to notify

        postOrderNotificationForUser(type, orderNumber, userId);
    }

    /**
     * Post a notification for a specific user by ID.
     * Use this from AdminOrderDetailActivity so the notification goes to the
     * correct customer, not the admin.
     */
    public void postOrderNotificationForUser(AppNotification.Type type,
                                             String orderNumber, int targetUserId) {
        long timestamp = System.currentTimeMillis();
        DatabaseHelper db = new DatabaseHelper(appContext);
        db.insertNotification(targetUserId, type.name(), orderNumber, timestamp);

        if (listener != null) listener.onNotificationAdded();
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    /**
     * Load all notifications for the currently logged-in user from SQLite.
     * Returns newest first.
     */
    public List<AppNotification> getNotifications() {
        SessionManager session = new SessionManager(appContext);
        int userId = session.getUserId();
        List<AppNotification> result = new ArrayList<>();
        if (userId == -1) return result;

        DatabaseHelper db = new DatabaseHelper(appContext);
        Cursor c = db.getNotificationsByUser(userId);
        if (c.moveToFirst()) {
            do {
                String typeName   = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_NOTIF_TYPE));
                String orderNum   = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_NOTIF_ORDER_NUMBER));
                long   timestamp  = c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_NOTIF_TIMESTAMP));
                boolean isRead    = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_NOTIF_IS_READ)) == 1;

                AppNotification.Type type;
                try {
                    type = AppNotification.Type.valueOf(typeName);
                } catch (IllegalArgumentException e) {
                    type = AppNotification.Type.ORDER_PLACED; // fallback
                }

                AppNotification notif = new AppNotification(type, orderNum, timestamp);
                notif.setRead(isRead);
                result.add(notif);
            } while (c.moveToNext());
        }
        c.close();
        return result;
    }

    /** Count unread notifications for the currently logged-in user. */
    public int getUnreadCount() {
        SessionManager session = new SessionManager(appContext);
        int userId = session.getUserId();
        if (userId == -1) return 0;

        DatabaseHelper db = new DatabaseHelper(appContext);
        return db.getUnreadCount(userId);
    }

    /** Mark all notifications read for the currently logged-in user. */
    public void markAllAsRead() {
        SessionManager session = new SessionManager(appContext);
        int userId = session.getUserId();
        if (userId == -1) return;

        DatabaseHelper db = new DatabaseHelper(appContext);
        db.markAllNotificationsRead(userId);
    }

    // ── Listener ──────────────────────────────────────────────────────────────

    public void setOnNotificationAddedListener(OnNotificationAddedListener l) {
        this.listener = l;
    }

    /** No-op kept for backwards compatibility. */
    public void clear() {
        // SQLite is the source of truth; nothing to clear from memory.
    }
}
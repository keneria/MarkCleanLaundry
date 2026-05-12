package com.example.markcleanlaundry;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class AdminUsersActivity extends AppCompatActivity {

    private LinearLayout llUserList;
    private TextView tvEmptyUsers;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_users);

        llUserList   = findViewById(R.id.llAdminUserList);
        tvEmptyUsers = findViewById(R.id.tvEmptyUsers);
        db           = new DatabaseHelper(this);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        loadUsers();
    }

    private void loadUsers() {
        // Query all registered users directly from the users table
        Cursor cursor = db.getReadableDatabase().query(
                DatabaseHelper.TABLE_USERS,
                new String[]{
                        DatabaseHelper.COL_USER_ID,
                        DatabaseHelper.COL_USER_FULLNAME,
                        DatabaseHelper.COL_USER_USERNAME,
                        DatabaseHelper.COL_USER_PHONE
                },
                null, null, null, null,
                DatabaseHelper.COL_USER_ID + " ASC"
        );

        if (cursor == null || cursor.getCount() == 0) {
            if (cursor != null) cursor.close();
            tvEmptyUsers.setVisibility(View.VISIBLE);
            llUserList.setVisibility(View.GONE);
            return;
        }

        tvEmptyUsers.setVisibility(View.GONE);
        llUserList.setVisibility(View.VISIBLE);
        llUserList.removeAllViews();

        int index = 1;
        while (cursor.moveToNext()) {
            int    userId   = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID));
            String fullName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_FULLNAME));
            String username = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_USERNAME));
            String phone    = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_PHONE));

            // Count this user's orders
            Cursor orderCursor = db.getOrdersByUser(userId);
            int orderCount = orderCursor != null ? orderCursor.getCount() : 0;
            if (orderCursor != null) orderCursor.close();

            llUserList.addView(buildUserCard(index++, fullName, username, phone, orderCount));
        }
        cursor.close();
    }

    private View buildUserCard(int index, String fullName, String username,
                               String phone, int orderCount) {
        float dp = getResources().getDisplayMetrics().density;

        CardView card = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.bottomMargin = (int)(12 * dp);
        card.setLayoutParams(cardParams);
        card.setRadius(14 * dp);
        card.setCardElevation(2 * dp);
        card.setCardBackgroundColor(0xFFFFFFFF);

        LinearLayout inner = new LinearLayout(this);
        inner.setOrientation(LinearLayout.HORIZONTAL);
        inner.setGravity(android.view.Gravity.CENTER_VERTICAL);
        inner.setPadding((int)(16*dp), (int)(14*dp), (int)(16*dp), (int)(14*dp));

        // Avatar circle with number
        TextView tvAvatar = new TextView(this);
        tvAvatar.setText(String.valueOf(index));
        tvAvatar.setTextSize(15);
        tvAvatar.setTextColor(0xFFFFFFFF);
        tvAvatar.setTypeface(null, android.graphics.Typeface.BOLD);
        tvAvatar.setGravity(android.view.Gravity.CENTER);
        tvAvatar.setBackgroundColor(0xFF2D7DD2);
        int size = (int)(44 * dp);
        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(size, size);
        avatarParams.setMarginEnd((int)(14 * dp));
        tvAvatar.setLayoutParams(avatarParams);

        // Info column
        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        // Full Name (primary)
        TextView tvFullName = new TextView(this);
        tvFullName.setText(fullName != null && !fullName.isEmpty() ? fullName : "No name");
        tvFullName.setTextSize(14);
        tvFullName.setTextColor(0xFF1B3A5C);
        tvFullName.setTypeface(null, android.graphics.Typeface.BOLD);

        // Username (secondary)
        TextView tvUsername = new TextView(this);
        tvUsername.setText(username != null && !username.isEmpty() ? "@" + username : "");
        tvUsername.setTextSize(12);
        tvUsername.setTextColor(0xFF888888);
        LinearLayout.LayoutParams unParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        unParams.topMargin = (int)(2 * dp);
        tvUsername.setLayoutParams(unParams);

        // Phone
        TextView tvPhone = new TextView(this);
        tvPhone.setText(phone != null && !phone.isEmpty() ? phone : "No number");
        tvPhone.setTextSize(12);
        tvPhone.setTextColor(0xFF888888);
        LinearLayout.LayoutParams phoneParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        phoneParams.topMargin = (int)(2 * dp);
        tvPhone.setLayoutParams(phoneParams);

        // Order count (accent)
        TextView tvOrderCount = new TextView(this);
        tvOrderCount.setText(orderCount + (orderCount == 1 ? " order" : " orders"));
        tvOrderCount.setTextSize(11);
        tvOrderCount.setTextColor(0xFF2D7DD2);
        LinearLayout.LayoutParams countParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        countParams.topMargin = (int)(4 * dp);
        tvOrderCount.setLayoutParams(countParams);

        info.addView(tvFullName);
        info.addView(tvUsername);
        info.addView(tvPhone);
        info.addView(tvOrderCount);

        inner.addView(tvAvatar, avatarParams);
        inner.addView(info);
        card.addView(inner);

        return card;
    }
}
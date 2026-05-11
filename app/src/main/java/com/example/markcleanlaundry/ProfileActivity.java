package com.example.markcleanlaundry;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvProfileName, tvProfileUsername, tvProfilePhone;
    private SessionManager session;
    private DatabaseHelper db;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        session = new SessionManager(this);
        db      = new DatabaseHelper(this);

        tvProfileName     = findViewById(R.id.tvProfileName);
        tvProfileUsername = findViewById(R.id.tvProfileUsername);
        tvProfilePhone    = findViewById(R.id.tvProfilePhone);

        currentUserId = session.getUserId();
        loadProfileFromDb();

        // ── Edit Profile ──────────────────────────────────────────────────────
        findViewById(R.id.btnEditProfile).setOnClickListener(v -> showEditProfileDialog());

        // ── My Orders ─────────────────────────────────────────────────────────
        findViewById(R.id.layoutMyOrders).setOnClickListener(v ->
                startActivity(new Intent(this, OrderListActivity.class)));

        // ── Notifications ─────────────────────────────────────────────────────
        findViewById(R.id.layoutNotifications).setOnClickListener(v ->
                startActivity(new Intent(this, NotificationActivity.class)));

        // ── Sign Out ──────────────────────────────────────────────────────────
        findViewById(R.id.layoutSignOut).setOnClickListener(v -> {
            session.logout();
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // ── Bottom Nav ────────────────────────────────────────────────────────
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_profile);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Intent i = new Intent(this, HomeActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
                finish();
                return true;
            } else if (id == R.id.nav_orders) {
                startActivity(new Intent(this, OrderListActivity.class));
                return true;
            } else if (id == R.id.nav_notifications) {
                startActivity(new Intent(this, NotificationActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                return true;
            }
            return false;
        });
    }

    // ── Load profile data from DB ─────────────────────────────────────────────
    private void loadProfileFromDb() {
        Cursor c = db.getUserById(currentUserId);
        if (c != null && c.moveToFirst()) {
            String fullName = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_USER_FULLNAME));
            String username = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_USER_USERNAME));
            String phone    = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_USER_PHONE));
            c.close();

            tvProfileName.setText(fullName != null && !fullName.isEmpty() ? fullName : "No name set");
            tvProfileUsername.setText(username != null && !username.isEmpty() ? "@" + username : "");
            tvProfilePhone.setText(phone != null && !phone.isEmpty() ? phone : "No number set");
        }
    }

    // ── Edit Profile Dialog — only name and phone are editable ───────────────
    private void showEditProfileDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_edit_profile);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.90),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        );
        dialog.setCancelable(true);

        EditText etName  = dialog.findViewById(R.id.etEditName);
        EditText etPhone = dialog.findViewById(R.id.etEditPhone);

        // Pre-fill current values (skip placeholder strings)
        String currentName  = tvProfileName.getText().toString();
        String currentPhone = tvProfilePhone.getText().toString();
        etName.setText(currentName.equals("No name set")    ? "" : currentName);
        etPhone.setText(currentPhone.equals("No number set") ? "" : currentPhone);

        ((MaterialButton) dialog.findViewById(R.id.btnCancelEdit))
                .setOnClickListener(v -> dialog.dismiss());

        ((MaterialButton) dialog.findViewById(R.id.btnSaveEdit))
                .setOnClickListener(v -> {
                    String newName  = etName.getText().toString().trim();
                    String newPhone = etPhone.getText().toString().trim();

                    if (newName.isEmpty()) {
                        etName.setError("Name cannot be empty");
                        return;
                    }
                    if (newPhone.isEmpty()) {
                        etPhone.setError("Phone number cannot be empty");
                        return;
                    }

                    // Fetch current email + address so we don't overwrite them with blanks
                    String email   = "";
                    String address = "";
                    Cursor c = db.getUserById(currentUserId);
                    if (c != null && c.moveToFirst()) {
                        email   = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EMAIL));
                        address = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ADDRESS));
                        if (email   == null) email   = "";
                        if (address == null) address = "";
                        c.close();
                    }

                    int rows = db.updateUser(currentUserId, newName, email, newPhone, address);

                    if (rows > 0) {
                        session.saveFullName(newName); // keep cache in sync
                        tvProfileName.setText(newName);
                        tvProfilePhone.setText(newPhone);
                        Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(this, "Update failed. Try again.", Toast.LENGTH_SHORT).show();
                    }
                });

        dialog.show();
    }
}
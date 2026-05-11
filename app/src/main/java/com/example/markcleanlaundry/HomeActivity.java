package com.example.markcleanlaundry;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    TextView tvGreetingName, tvWelcome;
    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tvGreetingName = findViewById(R.id.tvGreetingName);
        tvWelcome      = findViewById(R.id.tvWelcome);
        bottomNav      = findViewById(R.id.bottomNav);

        String username  = getIntent().getStringExtra("username");
        boolean isNewUser = getIntent().getBooleanExtra("isNewUser", false);

        tvGreetingName.setText(username != null && !username.isEmpty() ? "Hey " + username : "Hey there");
        tvWelcome.setText(isNewUser ? "Welcome!" : "Welcome back!");

        // ── Service cards ─────────────────────────────────────────────────────
        findViewById(R.id.cardWashFold).setOnClickListener(v -> {
            Intent i = new Intent(this, MethodActivity.class);
            i.putExtra("service_name", "Wash & Fold");
            startActivity(i);
        });

        findViewById(R.id.cardWashPress).setOnClickListener(v -> {
            Intent i = new Intent(this, MethodActivity.class);
            i.putExtra("service_name", "Wash & Press");
            startActivity(i);
        });

        findViewById(R.id.cardPressOnly).setOnClickListener(v -> {
            Intent i = new Intent(this, SchedulePickupActivity.class);
            i.putExtra("service_name", "Press Only");
            i.putExtra("method", "");
            startActivity(i);
        });

        findViewById(R.id.cardDryCleaning).setOnClickListener(v -> {
            Intent i = new Intent(this, SchedulePickupActivity.class);
            i.putExtra("service_name", "Dry Cleaning");
            i.putExtra("method", "");
            startActivity(i);
        });

        // ── Other services ────────────────────────────────────────────────────
        findViewById(R.id.chipSneaker).setOnClickListener(v -> startOther("Sneaker Cleaning"));
        findViewById(R.id.chipBag).setOnClickListener(v -> startOther("Bag Cleaning"));
        findViewById(R.id.chipSteam).setOnClickListener(v -> startOther("Press/Steam"));
        findViewById(R.id.chipHandWash).setOnClickListener(v -> startOther("Hand Wash"));
        findViewById(R.id.chipCurtain).setOnClickListener(v -> startOther("Curtain Cleaning"));

        // ── Bottom nav ────────────────────────────────────────────────────────
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_orders) {
                startActivity(new Intent(this, OrderListActivity.class));
                return true;
            } else if (id == R.id.nav_notifications) {
                startActivity(new Intent(this, NotificationActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                Intent i = new Intent(this, ProfileActivity.class);
                i.putExtra("username",
                        tvGreetingName.getText().toString().replace("Hey ", ""));
                startActivity(i);
                return true;
            }
            return false;
        });

        // ── Live badge update if a notif arrives while on this screen ─────────
        NotificationManager.getInstance().setOnNotificationAddedListener(
                this::updateNotifBadge);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh badge every time user comes back to Home
        updateNotifBadge();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void startOther(String serviceName) {
        Intent i = new Intent(this, OtherServicesBookingActivity.class);
        i.putExtra(OtherServicesBookingActivity.EXTRA_SERVICE_NAME, serviceName);
        startActivity(i);
    }

    private void updateNotifBadge() {
        int unread = NotificationManager.getInstance().getUnreadCount();
        BadgeDrawable badge = bottomNav.getOrCreateBadge(R.id.nav_notifications);
        if (unread > 0) {
            badge.setVisible(true);
            badge.setNumber(unread);
            badge.setBackgroundColor(0xFFE53935);   // red
            badge.setBadgeTextColor(0xFFFFFFFF);    // white text
        } else {
            badge.setVisible(false);
            bottomNav.removeBadge(R.id.nav_notifications);
        }
    }
}
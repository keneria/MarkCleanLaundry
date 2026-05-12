package com.example.markcleanlaundry;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class SchedulePickupActivity extends AppCompatActivity {

    private String selectedPickupDate   = "";
    private String selectedDeliveryDate = "";
    private String selectedPickupTime   = "";
    private String selectedDeliveryTime = "";
    private boolean isPickupMode = true;
    private boolean isClaimingMode = false; // ← new

    private TextView tvPickupDate, tvDeliveryDate, tvToolbarTitle;
    private TextView tvPickupTime, tvDeliveryTime;
    private LinearLayout btnTogglePickup, btnToggleDelivery;
    private LinearLayout sectionPickupDate, sectionDeliveryDate;
    private LinearLayout sectionPickupTime, sectionDeliveryTime;

    private Intent incomingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_pickup);

        incomingIntent = getIntent();

        String serviceName   = incomingIntent.getStringExtra("service_name");
        String method        = incomingIntent.getStringExtra("method");
        String orderNumber   = incomingIntent.getStringExtra("order_number"); // ← new
        isClaimingMode       = incomingIntent.getBooleanExtra("claiming_mode", false); // ← new

        if (serviceName == null) serviceName = "";
        if (method == null)      method = "";

        tvToolbarTitle      = findViewById(R.id.tvToolbarTitle);
        btnTogglePickup     = findViewById(R.id.btnTogglePickup);
        btnToggleDelivery   = findViewById(R.id.btnToggleDelivery);
        sectionPickupDate   = findViewById(R.id.sectionPickupDate);
        sectionDeliveryDate = findViewById(R.id.sectionDeliveryDate);
        sectionPickupTime   = findViewById(R.id.sectionPickupTime);
        sectionDeliveryTime = findViewById(R.id.sectionDeliveryTime);
        tvPickupDate        = findViewById(R.id.tvPickupDate);
        tvDeliveryDate      = findViewById(R.id.tvDeliveryDate);
        tvPickupTime        = findViewById(R.id.tvPickupTime);
        tvDeliveryTime      = findViewById(R.id.tvDeliveryTime);

        // ── Rename toggle labels in claiming mode ─────────────────────────────
        if (isClaimingMode) {
            tvToolbarTitle.setText("Claim Your Laundry");
            ((TextView) btnTogglePickup.getChildAt(0)).setText("Store Pick-Up");
            ((TextView) btnToggleDelivery.getChildAt(0)).setText("Home Delivery");
        }

        btnTogglePickup.setOnClickListener(v -> setToggle(true));
        btnToggleDelivery.setOnClickListener(v -> setToggle(false));

        findViewById(R.id.llPickupDate).setOnClickListener(v -> showDatePicker(true));
        findViewById(R.id.llDeliveryDate).setOnClickListener(v -> showDatePicker(false));
        findViewById(R.id.llPickupTime).setOnClickListener(v -> showTimePicker(true));
        findViewById(R.id.llDeliveryTime).setOnClickListener(v -> showTimePicker(false));

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        final String finalServiceName = serviceName;
        final String finalMethod      = method;
        final String finalOrderNumber = orderNumber;

        ((MaterialButton) findViewById(R.id.btnConfirmOrder)).setOnClickListener(v -> {

            if (isPickupMode) {
                if (selectedPickupDate.isEmpty()) {
                    Toast.makeText(this,
                            isClaimingMode ? "Please select a pick-up date." : "Please select a pickup date.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (selectedPickupTime.isEmpty()) {
                    Toast.makeText(this,
                            isClaimingMode ? "Please select a pick-up time." : "Please select a pickup time.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                if (selectedDeliveryDate.isEmpty()) {
                    Toast.makeText(this, "Please select a date.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (selectedDeliveryTime.isEmpty()) {
                    Toast.makeText(this, "Please select a time.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            String serviceType  = isPickupMode
                    ? (isClaimingMode ? "Store Pick-Up" : "Home Pickup")
                    : (isClaimingMode ? "Home Delivery" : "Drop-off");
            String selectedDate = isPickupMode ? selectedPickupDate   : selectedDeliveryDate;
            String selectedTime = isPickupMode ? selectedPickupTime   : selectedDeliveryTime;

            Intent payIntent = new Intent(this, PaymentActivity.class);

            payIntent.putExtra("service_name",    finalServiceName);
            payIntent.putExtra("method",          finalMethod);
            payIntent.putExtra("service_type",    serviceType);
            payIntent.putExtra("selected_date",   selectedDate);
            payIntent.putExtra("selected_time",   selectedTime);
            payIntent.putExtra("claiming_mode",   isClaimingMode);   // ← forward flag
            payIntent.putExtra("order_number",    finalOrderNumber); // ← forward order number

            if (!isClaimingMode) {
                // Only forward booking extras when not in claiming mode
                payIntent.putExtra("sub_total", incomingIntent.getDoubleExtra("sub_total", 0));
                payIntent.putExtra("notes",     incomingIntent.getStringExtra("notes") != null
                        ? incomingIntent.getStringExtra("notes") : "");

                payIntent.putExtra("qty_sneaker_basic",   incomingIntent.getIntExtra("qty_sneaker_basic", 0));
                payIntent.putExtra("qty_sneaker_deep",    incomingIntent.getIntExtra("qty_sneaker_deep", 0));
                payIntent.putExtra("qty_sneaker_premium", incomingIntent.getIntExtra("qty_sneaker_premium", 0));
                payIntent.putExtra("price_sneaker_basic",   incomingIntent.getDoubleExtra("price_sneaker_basic", 150));
                payIntent.putExtra("price_sneaker_deep",    incomingIntent.getDoubleExtra("price_sneaker_deep", 250));
                payIntent.putExtra("price_sneaker_premium", incomingIntent.getDoubleExtra("price_sneaker_premium", 350));

                payIntent.putExtra("qty_bag_small",  incomingIntent.getIntExtra("qty_bag_small", 0));
                payIntent.putExtra("qty_bag_medium", incomingIntent.getIntExtra("qty_bag_medium", 0));
                payIntent.putExtra("qty_bag_large",  incomingIntent.getIntExtra("qty_bag_large", 0));
                payIntent.putExtra("price_bag_small",  incomingIntent.getDoubleExtra("price_bag_small", 150));
                payIntent.putExtra("price_bag_medium", incomingIntent.getDoubleExtra("price_bag_medium", 250));
                payIntent.putExtra("price_bag_large",  incomingIntent.getDoubleExtra("price_bag_large", 350));

                payIntent.putExtra("qty_steam_regular", incomingIntent.getIntExtra("qty_steam_regular", 0));
                payIntent.putExtra("qty_steam_heavy",   incomingIntent.getIntExtra("qty_steam_heavy", 0));
                payIntent.putExtra("price_steam_regular", incomingIntent.getDoubleExtra("price_steam_regular", 25));
                payIntent.putExtra("price_steam_heavy",   incomingIntent.getDoubleExtra("price_steam_heavy", 45));

                payIntent.putExtra("qty_handwash_kg",       incomingIntent.getIntExtra("qty_handwash_kg", 1));
                payIntent.putExtra("price_handwash_per_kg", incomingIntent.getDoubleExtra("price_handwash_per_kg", 60));

                payIntent.putExtra("qty_curtain_meters",      incomingIntent.getIntExtra("qty_curtain_meters", 1));
                payIntent.putExtra("price_curtain_per_meter", incomingIntent.getDoubleExtra("price_curtain_per_meter", 80));
            }

            startActivity(payIntent);
        });
    }

    private void setToggle(boolean pickupMode) {
        isPickupMode = pickupMode;

        if (isClaimingMode) {
            tvToolbarTitle.setText(pickupMode ? "Store Pick-Up" : "Home Delivery");
        } else {
            tvToolbarTitle.setText(pickupMode ? "Schedule A Pickup" : "Schedule A Drop-off");
        }

        if (pickupMode) {
            btnTogglePickup.setBackgroundResource(R.drawable.toggle_active_bg);
            btnToggleDelivery.setBackgroundResource(R.drawable.toggle_inactive_bg);
            ((TextView) btnTogglePickup.getChildAt(0)).setTextColor(getColor(android.R.color.white));
            ((TextView) btnToggleDelivery.getChildAt(0)).setTextColor(getColor(R.color.navy));
            sectionPickupDate.setVisibility(View.VISIBLE);
            sectionDeliveryDate.setVisibility(View.GONE);
            sectionPickupTime.setVisibility(View.VISIBLE);
            sectionDeliveryTime.setVisibility(View.GONE);
        } else {
            btnTogglePickup.setBackgroundResource(R.drawable.toggle_inactive_bg);
            btnToggleDelivery.setBackgroundResource(R.drawable.toggle_active_bg);
            ((TextView) btnTogglePickup.getChildAt(0)).setTextColor(getColor(R.color.navy));
            ((TextView) btnToggleDelivery.getChildAt(0)).setTextColor(getColor(android.R.color.white));
            sectionPickupDate.setVisibility(View.GONE);
            sectionDeliveryDate.setVisibility(View.VISIBLE);
            sectionPickupTime.setVisibility(View.GONE);
            sectionDeliveryTime.setVisibility(View.VISIBLE);
        }
    }

    private void showDatePicker(boolean isPickup) {
        DatePickerFragment fragment = new DatePickerFragment();
        fragment.setMode(isPickup ? "Pickup" : "Drop-off");
        fragment.setOnDateSelectedListener(date -> {
            if (isPickup) {
                selectedPickupDate = date;
                tvPickupDate.setText(date);
            } else {
                selectedDeliveryDate = date;
                tvDeliveryDate.setText(date);
            }
        });
        fragment.show(getSupportFragmentManager(), "datePicker");
    }

    private void showTimePicker(boolean isPickup) {
        TimePickerFragment fragment = new TimePickerFragment();
        fragment.setMode(isPickup ? "Pickup" : "Drop-off");
        fragment.setOnTimeSelectedListener(time -> {
            if (isPickup) {
                selectedPickupTime = time;
                tvPickupTime.setText(time);
            } else {
                selectedDeliveryTime = time;
                tvDeliveryTime.setText(time);
            }
        });
        fragment.show(getSupportFragmentManager(), "timePicker");
    }
}
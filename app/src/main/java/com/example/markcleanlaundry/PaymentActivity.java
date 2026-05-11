package com.example.markcleanlaundry;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.material.button.MaterialButton;

public class PaymentActivity extends AppCompatActivity {

    private String selectedPayment = "";
    private boolean isDropOff      = false;
    private boolean isClaimingMode = false;
    private boolean isHomeDelivery = false;
    private String clientName      = "";
    private String clientPhone     = "";

    private LinearLayout layoutPickupAddress, tvStoreAddress;
    private EditText etPickupAddress;
    private LinearLayout layoutPaymentPickup, layoutPaymentDropoff;
    private CardView cardGcash, cardCod;
    private RadioButton rbGcash, rbCod;
    private LinearLayout layoutGcashFields;
    private EditText etGcashNumber, etGcashName;
    private CardView cardCash, cardGcashDropoff;
    private RadioButton rbCash, rbGcashDropoff;
    private LinearLayout layoutGcashDropoffFields;
    private EditText etGcashDropoffNumber, etGcashDropoffName;
    private LinearLayout layoutClaimingDeliveryAddress;
    private EditText etClaimingDeliveryAddress;
    private View layoutRateNotice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        SessionManager session = new SessionManager(this);
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        android.database.Cursor userCursor = dbHelper.getUserById(session.getUserId());
        if (userCursor.moveToFirst()) {
            clientName  = userCursor.getString(userCursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_FULLNAME));
            clientPhone = userCursor.getString(userCursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_PHONE));
        }
        userCursor.close();
        if (clientName  == null) clientName  = "";
        if (clientPhone == null) clientPhone = "";

        String serviceName = getIntent().getStringExtra("service_name");
        String method      = getIntent().getStringExtra("method");
        String serviceType = getIntent().getStringExtra("service_type");
        isClaimingMode     = getIntent().getBooleanExtra("claiming_mode", false);

        if (serviceName == null) serviceName = "";
        if (method == null)      method = "";
        if (serviceType == null) serviceType = isClaimingMode ? "Store Pick-Up" : "Drop-off";

        isHomeDelivery = isClaimingMode && "Home Delivery".equals(serviceType);
        isDropOff      = !isClaimingMode && "Drop-off".equals(serviceType);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        String displayService = method.isEmpty() ? serviceName : serviceName + " (" + method + ")";
        ((TextView) findViewById(R.id.tvPaymentServiceName)).setText(
                isClaimingMode ? "Claiming: " + serviceType : displayService);
        ((TextView) findViewById(R.id.tvPaymentMethod)).setText("Not selected");
        ((TextView) findViewById(R.id.tvPaymentServiceType)).setText(serviceType);

        layoutPickupAddress          = findViewById(R.id.layoutPickupAddress);
        tvStoreAddress               = findViewById(R.id.tvStoreAddress);
        etPickupAddress              = findViewById(R.id.etPickupAddress);
        layoutPaymentPickup          = findViewById(R.id.layoutPaymentPickup);
        layoutPaymentDropoff         = findViewById(R.id.layoutPaymentDropoff);
        cardGcash                    = findViewById(R.id.cardGcash);
        rbGcash                      = findViewById(R.id.rbGcash);
        layoutGcashFields            = findViewById(R.id.layoutGcashFields);
        etGcashNumber                = findViewById(R.id.etGcashNumber);
        etGcashName                  = findViewById(R.id.etGcashName);
        cardCod                      = findViewById(R.id.cardCod);
        rbCod                        = findViewById(R.id.rbCod);
        cardCash                     = findViewById(R.id.cardCash);
        rbCash                       = findViewById(R.id.rbCash);
        cardGcashDropoff             = findViewById(R.id.cardGcashDropoff);
        rbGcashDropoff               = findViewById(R.id.rbGcashDropoff);
        layoutGcashDropoffFields     = findViewById(R.id.layoutGcashDropoffFields);
        etGcashDropoffNumber         = findViewById(R.id.etGcashDropoffNumber);
        etGcashDropoffName           = findViewById(R.id.etGcashDropoffName);
        layoutClaimingDeliveryAddress = findViewById(R.id.layoutClaimingDeliveryAddress);
        etClaimingDeliveryAddress    = findViewById(R.id.etClaimingDeliveryAddress);
        layoutRateNotice             = findViewById(R.id.layoutRateNotice);

        // Hide rate notice in claiming mode
        if (layoutRateNotice != null) {
            layoutRateNotice.setVisibility(isClaimingMode ? View.GONE : View.VISIBLE);
        }

        if (isClaimingMode) {
            if (isHomeDelivery) {
                // ── Home Delivery claiming ────────────────────────────────────
                // Show delivery address input
                if (layoutClaimingDeliveryAddress != null)
                    layoutClaimingDeliveryAddress.setVisibility(View.VISIBLE);
                tvStoreAddress.setVisibility(View.GONE);
                layoutPickupAddress.setVisibility(View.GONE);

                // Payment: Cash on Delivery + GCash
                layoutPaymentPickup.setVisibility(View.GONE);
                layoutPaymentDropoff.setVisibility(View.VISIBLE);
                cardCash.setOnClickListener(v -> selectPayment("Cash on Delivery"));
                rbCash.setOnClickListener(v -> selectPayment("Cash on Delivery"));
                cardGcashDropoff.setOnClickListener(v -> selectPayment("GCash"));
                rbGcashDropoff.setOnClickListener(v -> selectPayment("GCash"));

            } else {
                // ── Store Pick-Up claiming ────────────────────────────────────
                // Show store address
                if (layoutClaimingDeliveryAddress != null)
                    layoutClaimingDeliveryAddress.setVisibility(View.GONE);
                tvStoreAddress.setVisibility(View.VISIBLE);
                layoutPickupAddress.setVisibility(View.GONE);

                // Payment: Cash + GCash
                layoutPaymentPickup.setVisibility(View.VISIBLE);
                layoutPaymentDropoff.setVisibility(View.GONE);
                cardGcash.setOnClickListener(v -> selectPayment("GCash"));
                rbGcash.setOnClickListener(v -> selectPayment("GCash"));
                cardCod.setOnClickListener(v -> selectPayment("Cash"));
                rbCod.setOnClickListener(v -> selectPayment("Cash"));
            }

        } else if (isDropOff) {
            // ── Normal Drop-off booking ───────────────────────────────────────
            tvStoreAddress.setVisibility(View.VISIBLE);
            layoutPickupAddress.setVisibility(View.GONE);
            layoutPaymentPickup.setVisibility(View.GONE);
            layoutPaymentDropoff.setVisibility(View.VISIBLE);
            if (layoutClaimingDeliveryAddress != null)
                layoutClaimingDeliveryAddress.setVisibility(View.GONE);

            cardCash.setOnClickListener(v -> selectPayment("Cash"));
            rbCash.setOnClickListener(v -> selectPayment("Cash"));
            cardGcashDropoff.setOnClickListener(v -> selectPayment("GCash"));
            rbGcashDropoff.setOnClickListener(v -> selectPayment("GCash"));

        } else {
            // ── Normal Home Pickup booking ────────────────────────────────────
            tvStoreAddress.setVisibility(View.GONE);
            layoutPickupAddress.setVisibility(View.VISIBLE);
            layoutPaymentPickup.setVisibility(View.VISIBLE);
            layoutPaymentDropoff.setVisibility(View.GONE);
            if (layoutClaimingDeliveryAddress != null)
                layoutClaimingDeliveryAddress.setVisibility(View.GONE);

            cardGcash.setOnClickListener(v -> selectPayment("GCash"));
            rbGcash.setOnClickListener(v -> selectPayment("GCash"));
            cardCod.setOnClickListener(v -> selectPayment("Cash"));
            rbCod.setOnClickListener(v -> selectPayment("Cash"));
        }

        final String finalServiceName = displayService;
        final String finalServiceType = serviceType;
        ((MaterialButton) findViewById(R.id.btnConfirmOrder))
                .setOnClickListener(v -> onConfirm(finalServiceName, finalServiceType));
    }

    private void selectPayment(String method) {
        selectedPayment = method;
        ((TextView) findViewById(R.id.tvPaymentMethod)).setText(method);

        if (isDropOff || isHomeDelivery) {
            rbCash.setChecked(method.equals("Cash") || method.equals("Cash on Delivery"));
            rbGcashDropoff.setChecked(method.equals("GCash"));
            layoutGcashDropoffFields.setVisibility(
                    method.equals("GCash") ? View.VISIBLE : View.GONE);
        } else {
            rbGcash.setChecked(method.equals("GCash"));
            rbCod.setChecked(method.equals("Cash on Delivery") || method.equals("Cash"));
            layoutGcashFields.setVisibility(
                    method.equals("GCash") ? View.VISIBLE : View.GONE);
        }
    }

    private void onConfirm(String serviceName, String serviceType) {

        // Validate pickup address for normal Home Pickup booking
        if (!isClaimingMode && !isDropOff) {
            String addr = etPickupAddress.getText().toString().trim();
            if (addr.isEmpty()) {
                Toast.makeText(this, "Please enter your pickup address.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Validate delivery address for Home Delivery claiming
        if (isClaimingMode && isHomeDelivery) {
            if (layoutClaimingDeliveryAddress == null
                    || etClaimingDeliveryAddress.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Please enter your delivery address.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (selectedPayment.isEmpty()) {
            Toast.makeText(this, "Please select a payment method.", Toast.LENGTH_SHORT).show();
            return;
        }

        String gcashNum  = "";
        String gcashName = "";

        if (selectedPayment.equals("GCash")) {
            if (isDropOff || isHomeDelivery) {
                gcashNum  = etGcashDropoffNumber.getText().toString().trim();
                gcashName = etGcashDropoffName.getText().toString().trim();
            } else {
                gcashNum  = etGcashNumber.getText().toString().trim();
                gcashName = etGcashName.getText().toString().trim();
            }
            if (gcashNum.isEmpty() || gcashName.isEmpty()) {
                Toast.makeText(this, "Please complete your GCash details.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (isClaimingMode) {
            // Save claiming method — admin controls the final status change
            String orderNumber = getIntent().getStringExtra("order_number");
            DatabaseHelper db  = new DatabaseHelper(this);
            db.updateClaimingMethod(orderNumber, serviceType);

            // Save delivery address if Home Delivery
            if (isHomeDelivery && etClaimingDeliveryAddress != null) {
                String deliveryAddr = etClaimingDeliveryAddress.getText().toString().trim();
                db.updateDeliveryAddress(orderNumber, deliveryAddr);
            }

            Toast.makeText(this,
                    isHomeDelivery
                            ? "Got it! The admin will arrange your delivery."
                            : "Got it! Please come to the store to pick up your laundry.",
                    Toast.LENGTH_LONG).show();

            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return;
        }

        // Normal booking flow
        String orderId = "#" + System.currentTimeMillis() % 100000;

        String rawServiceName = getIntent().getStringExtra("service_name");
        String rawMethod      = getIntent().getStringExtra("method");
        String laundryType;
        if (rawServiceName != null && !rawServiceName.isEmpty()
                && rawMethod != null && !rawMethod.isEmpty()) {
            laundryType = rawServiceName + " – " + rawMethod;
        } else if (rawServiceName != null && !rawServiceName.isEmpty()) {
            laundryType = rawServiceName;
        } else {
            laundryType = "";
        }

        String pickupAddress = "";
        if (!isDropOff) {
            pickupAddress = etPickupAddress.getText().toString().trim();
        }

        Intent intent = new Intent(this, OrderDetailActivity.class);
        intent.putExtra("order_number",   orderId);
        intent.putExtra("service_name",   serviceName);
        intent.putExtra("service_type",   serviceType);
        intent.putExtra("laundry_type",   laundryType);
        intent.putExtra("pickup_address", pickupAddress);
        intent.putExtra("payment_method", selectedPayment);
        intent.putExtra("gcash_number",   gcashNum);
        intent.putExtra("gcash_name",     gcashName);
        intent.putExtra("status",         "Pending");
        intent.putExtra("selected_date",  getIntent().getStringExtra("selected_date"));
        intent.putExtra("selected_time",  getIntent().getStringExtra("selected_time"));
        intent.putExtra("client_name",    clientName);
        intent.putExtra("client_phone",   clientPhone);
        startActivity(intent);
        finish();
    }
}
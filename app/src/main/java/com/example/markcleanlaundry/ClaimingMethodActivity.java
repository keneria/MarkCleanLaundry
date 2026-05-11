package com.example.markcleanlaundry;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.material.button.MaterialButton;

public class ClaimingMethodActivity extends AppCompatActivity {

    private RadioButton rbStorePickup, rbHomeDelivery;
    private CardView cardStorePickup, cardHomeDelivery;
    private LinearLayout layoutDeliveryAddress;
    private EditText etDeliveryAddress;
    private String selectedMethod = "";
    private String orderNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_claiming_method);

        orderNumber = getIntent().getStringExtra("order_number");

        rbStorePickup       = findViewById(R.id.rbStorePickup);
        rbHomeDelivery      = findViewById(R.id.rbHomeDelivery);
        cardStorePickup     = findViewById(R.id.cardStorePickup);
        cardHomeDelivery    = findViewById(R.id.cardHomeDelivery);
        layoutDeliveryAddress = findViewById(R.id.layoutDeliveryAddress);
        etDeliveryAddress   = findViewById(R.id.etDeliveryAddress);

        cardStorePickup.setOnClickListener(v  -> selectMethod("Store Pick-Up"));
        rbStorePickup.setOnClickListener(v    -> selectMethod("Store Pick-Up"));
        cardHomeDelivery.setOnClickListener(v -> selectMethod("Home Delivery"));
        rbHomeDelivery.setOnClickListener(v   -> selectMethod("Home Delivery"));

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        ((MaterialButton) findViewById(R.id.btnContinue))
                .setOnClickListener(v -> onContinue());
    }

    private void selectMethod(String method) {
        selectedMethod = method;
        rbStorePickup.setChecked("Store Pick-Up".equals(method));
        rbHomeDelivery.setChecked("Home Delivery".equals(method));

        // Show address field only when Home Delivery is selected
        if (layoutDeliveryAddress != null) {
            layoutDeliveryAddress.setVisibility(
                    "Home Delivery".equals(method) ? View.VISIBLE : View.GONE
            );
        }
    }

    private void onContinue() {
        if (selectedMethod.isEmpty()) {
            Toast.makeText(this, "Please select a claiming method.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseHelper db = new DatabaseHelper(this);
        db.updateClaimingMethod(orderNumber, selectedMethod);

        if ("Home Delivery".equals(selectedMethod)) {
            String address = etDeliveryAddress != null
                    ? etDeliveryAddress.getText().toString().trim()
                    : "";
            if (address.isEmpty()) {
                Toast.makeText(this, "Please enter your delivery address.", Toast.LENGTH_SHORT).show();
                return;
            }
            db.updateDeliveryAddress(orderNumber, address);
        }

        String msg = "Home Delivery".equals(selectedMethod)
                ? "Got it! The admin will arrange your delivery."
                : "Got it! Please come to the store to pick up your laundry.";
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
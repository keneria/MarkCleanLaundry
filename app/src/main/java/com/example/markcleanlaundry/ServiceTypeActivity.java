package com.example.markcleanlaundry;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ServiceTypeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_type);

        String serviceName = getIntent().getStringExtra("service_name");
        String method      = getIntent().getStringExtra("method");
        if (serviceName == null) serviceName = "";
        if (method == null) method = "";

        final String finalService = serviceName;
        final String finalMethod  = method;

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.cardDropOff).setOnClickListener(v ->
                goToPayment("Drop-off", finalService, finalMethod));

        findViewById(R.id.cardHomePickup).setOnClickListener(v ->
                goToPayment("Home Pickup", finalService, finalMethod));
    }

    private void goToPayment(String serviceType, String serviceName, String method) {
        Intent i = new Intent(this, PaymentActivity.class);

        // Core extras
        i.putExtra("service_name", serviceName);
        i.putExtra("method",       method);
        i.putExtra("service_type", serviceType);

        // Forward all Other Services extras if present
        i.putExtra("sub_total", getIntent().getDoubleExtra("sub_total", 0));
        i.putExtra("notes",     getIntent().getStringExtra("notes") != null
                ? getIntent().getStringExtra("notes") : "");

        i.putExtra("qty_sneaker_basic",   getIntent().getIntExtra("qty_sneaker_basic", 0));
        i.putExtra("qty_sneaker_deep",    getIntent().getIntExtra("qty_sneaker_deep", 0));
        i.putExtra("qty_sneaker_premium", getIntent().getIntExtra("qty_sneaker_premium", 0));
        i.putExtra("price_sneaker_basic",   getIntent().getDoubleExtra("price_sneaker_basic", 150));
        i.putExtra("price_sneaker_deep",    getIntent().getDoubleExtra("price_sneaker_deep", 250));
        i.putExtra("price_sneaker_premium", getIntent().getDoubleExtra("price_sneaker_premium", 350));

        i.putExtra("qty_bag_small",  getIntent().getIntExtra("qty_bag_small", 0));
        i.putExtra("qty_bag_medium", getIntent().getIntExtra("qty_bag_medium", 0));
        i.putExtra("qty_bag_large",  getIntent().getIntExtra("qty_bag_large", 0));
        i.putExtra("price_bag_small",  getIntent().getDoubleExtra("price_bag_small", 150));
        i.putExtra("price_bag_medium", getIntent().getDoubleExtra("price_bag_medium", 250));
        i.putExtra("price_bag_large",  getIntent().getDoubleExtra("price_bag_large", 350));

        i.putExtra("qty_steam_regular", getIntent().getIntExtra("qty_steam_regular", 0));
        i.putExtra("qty_steam_heavy",   getIntent().getIntExtra("qty_steam_heavy", 0));
        i.putExtra("price_steam_regular", getIntent().getDoubleExtra("price_steam_regular", 25));
        i.putExtra("price_steam_heavy",   getIntent().getDoubleExtra("price_steam_heavy", 45));

        i.putExtra("qty_handwash_kg",       getIntent().getIntExtra("qty_handwash_kg", 1));
        i.putExtra("price_handwash_per_kg", getIntent().getDoubleExtra("price_handwash_per_kg", 60));

        i.putExtra("qty_curtain_meters",      getIntent().getIntExtra("qty_curtain_meters", 1));
        i.putExtra("price_curtain_per_meter", getIntent().getDoubleExtra("price_curtain_per_meter", 80));

        startActivity(i);
    }
}
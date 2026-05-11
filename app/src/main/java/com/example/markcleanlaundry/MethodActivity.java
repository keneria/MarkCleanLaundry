package com.example.markcleanlaundry;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MethodActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_method);

        String serviceName = getIntent().getStringExtra("service_name");
        if (serviceName == null) serviceName = "";

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        ((TextView) findViewById(R.id.tvMethodTitle)).setText(serviceName);

        final String finalService = serviceName;

        findViewById(R.id.cardMachine).setOnClickListener(v -> {
            Intent i = new Intent(this, SchedulePickupActivity.class);
            i.putExtra("service_name", finalService);
            i.putExtra("method", "Machine");
            startActivity(i);
        });

        findViewById(R.id.cardHand).setOnClickListener(v -> {
            Intent i = new Intent(this, SchedulePickupActivity.class);
            i.putExtra("service_name", finalService);
            i.putExtra("method", "Hand");
            startActivity(i);
        });
    }
}
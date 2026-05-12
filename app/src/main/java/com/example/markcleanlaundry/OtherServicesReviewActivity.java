package com.example.markcleanlaundry;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class OtherServicesReviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_services_review);

        String serviceName = getIntent().getStringExtra("service_name");
        String notes       = getIntent().getStringExtra("notes");
        double subTotal    = getIntent().getDoubleExtra("sub_total", 0.0);
        if (serviceName == null) serviceName = "";
        if (notes == null) notes = "";

        ((TextView) findViewById(R.id.tvReviewOtherServiceName)).setText(serviceName);

        // Notes
        TextView tvNotes      = findViewById(R.id.tvReviewNotes);
        TextView tvNotesLabel = findViewById(R.id.tvReviewNotesLabel);
        if (notes.isEmpty()) {
            tvNotes.setVisibility(View.GONE);
            tvNotesLabel.setVisibility(View.GONE);
        } else {
            tvNotes.setText(notes);
        }

        // Build item rows
        LinearLayout itemsContainer = findViewById(R.id.llReviewItems);
        itemsContainer.removeAllViews();

        switch (serviceName) {
            case "Sneaker Cleaning": addSneakerLines(itemsContainer); break;
            case "Bag Cleaning":     addBagLines(itemsContainer);     break;
            case "Press/Steam":      addSteamLines(itemsContainer);   break;
            case "Hand Wash":        addHandWashLine(itemsContainer); break;
            case "Curtain Cleaning": addCurtainLine(itemsContainer);  break;
        }

        double tax   = Math.ceil(subTotal * 0.05);
        double total = subTotal + tax;

        ((TextView) findViewById(R.id.tvReviewSubTotal)).setText(fmt(subTotal));
        ((TextView) findViewById(R.id.tvReviewTotal)).setText(fmt(total));

        findViewById(R.id.btnReviewBack).setOnClickListener(v -> finish());

        // ── Confirm → PaymentActivity ─────────────────────────────────────────
        final String finalServiceName = serviceName;
        final String finalNotes       = notes;

        ((MaterialButton) findViewById(R.id.btnReviewConfirm))
                .setOnClickListener(v -> {

                    Intent intent = new Intent(this, SchedulePickupActivity.class);
                    intent.putExtra("service_name", finalServiceName);
                    intent.putExtra("method", "");
                    intent.putExtra("sub_total", subTotal);
                    intent.putExtra("notes", finalNotes);

                    intent.putExtra("qty_sneaker_basic",   getIntent().getIntExtra("qty_sneaker_basic", 0));
                    intent.putExtra("qty_sneaker_deep",    getIntent().getIntExtra("qty_sneaker_deep", 0));
                    intent.putExtra("qty_sneaker_premium", getIntent().getIntExtra("qty_sneaker_premium", 0));
                    intent.putExtra("price_sneaker_basic",   getIntent().getDoubleExtra("price_sneaker_basic", 150));
                    intent.putExtra("price_sneaker_deep",    getIntent().getDoubleExtra("price_sneaker_deep", 250));
                    intent.putExtra("price_sneaker_premium", getIntent().getDoubleExtra("price_sneaker_premium", 350));

                    intent.putExtra("qty_bag_small",  getIntent().getIntExtra("qty_bag_small", 0));
                    intent.putExtra("qty_bag_medium", getIntent().getIntExtra("qty_bag_medium", 0));
                    intent.putExtra("qty_bag_large",  getIntent().getIntExtra("qty_bag_large", 0));
                    intent.putExtra("price_bag_small",  getIntent().getDoubleExtra("price_bag_small", 150));
                    intent.putExtra("price_bag_medium", getIntent().getDoubleExtra("price_bag_medium", 250));
                    intent.putExtra("price_bag_large",  getIntent().getDoubleExtra("price_bag_large", 350));

                    intent.putExtra("qty_steam_regular", getIntent().getIntExtra("qty_steam_regular", 0));
                    intent.putExtra("qty_steam_heavy",   getIntent().getIntExtra("qty_steam_heavy", 0));
                    intent.putExtra("price_steam_regular", getIntent().getDoubleExtra("price_steam_regular", 25));
                    intent.putExtra("price_steam_heavy",   getIntent().getDoubleExtra("price_steam_heavy", 45));

                    intent.putExtra("qty_handwash_kg",       getIntent().getIntExtra("qty_handwash_kg", 1));
                    intent.putExtra("price_handwash_per_kg", getIntent().getDoubleExtra("price_handwash_per_kg", 60));

                    intent.putExtra("qty_curtain_meters",      getIntent().getIntExtra("qty_curtain_meters", 1));
                    intent.putExtra("price_curtain_per_meter", getIntent().getDoubleExtra("price_curtain_per_meter", 80));

                    startActivity(intent);
                });
    }

    // -------------------------------------------------------------------------
    // Per-service line builders
    // -------------------------------------------------------------------------

    private void addSneakerLines(LinearLayout container) {
        int basic   = getIntent().getIntExtra("qty_sneaker_basic", 0);
        int deep    = getIntent().getIntExtra("qty_sneaker_deep", 0);
        int premium = getIntent().getIntExtra("qty_sneaker_premium", 0);
        double pBasic   = getIntent().getDoubleExtra("price_sneaker_basic", 150);
        double pDeep    = getIntent().getDoubleExtra("price_sneaker_deep", 250);
        double pPremium = getIntent().getDoubleExtra("price_sneaker_premium", 350);

        if (basic   > 0) addLine(container, "Basic Clean",         basic,   "pair",  pBasic);
        if (deep    > 0) addLine(container, "Deep Clean",          deep,    "pair",  pDeep);
        if (premium > 0) addLine(container, "Premium Restoration", premium, "pair",  pPremium);
    }

    private void addBagLines(LinearLayout container) {
        int small  = getIntent().getIntExtra("qty_bag_small", 0);
        int medium = getIntent().getIntExtra("qty_bag_medium", 0);
        int large  = getIntent().getIntExtra("qty_bag_large", 0);
        double pSmall  = getIntent().getDoubleExtra("price_bag_small", 150);
        double pMedium = getIntent().getDoubleExtra("price_bag_medium", 250);
        double pLarge  = getIntent().getDoubleExtra("price_bag_large", 350);

        if (small  > 0) addLine(container, "Small Bag",  small,  "piece", pSmall);
        if (medium > 0) addLine(container, "Medium Bag", medium, "piece", pMedium);
        if (large  > 0) addLine(container, "Large Bag",  large,  "piece", pLarge);
    }

    private void addSteamLines(LinearLayout container) {
        int regular = getIntent().getIntExtra("qty_steam_regular", 0);
        int heavy   = getIntent().getIntExtra("qty_steam_heavy", 0);
        double pRegular = getIntent().getDoubleExtra("price_steam_regular", 25);
        double pHeavy   = getIntent().getDoubleExtra("price_steam_heavy", 45);

        if (regular > 0) addLine(container, "Regular Item", regular, "piece", pRegular);
        if (heavy   > 0) addLine(container, "Heavy Item",   heavy,   "piece", pHeavy);
    }

    private void addHandWashLine(LinearLayout container) {
        int kg    = getIntent().getIntExtra("qty_handwash_kg", 1);
        double pr = getIntent().getDoubleExtra("price_handwash_per_kg", 60);
        addLine(container, "Hand Wash", kg, "kg", pr);
    }

    private void addCurtainLine(LinearLayout container) {
        int meters = getIntent().getIntExtra("qty_curtain_meters", 1);
        double pr  = getIntent().getDoubleExtra("price_curtain_per_meter", 80);
        addLine(container, "Curtain Cleaning", meters, "m", pr);
    }

    // -------------------------------------------------------------------------
    // Generic line-item row builder
    // -------------------------------------------------------------------------

    private void addLine(LinearLayout container, String label, int qty, String unit, double unitPrice) {
        View row = getLayoutInflater().inflate(R.layout.item_review_row, container, false);
        ((TextView) row.findViewById(R.id.tvReviewItemName)).setText(label);
        ((TextView) row.findViewById(R.id.tvReviewItemQty)).setText(qty + " " + unit);
        ((TextView) row.findViewById(R.id.tvReviewItemPrice)).setText(fmt(qty * unitPrice));
        container.addView(row);
    }

    private String fmt(double amount) {
        return String.format("₱ %.2f", amount);
    }
}
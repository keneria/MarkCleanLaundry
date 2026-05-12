package com.example.markcleanlaundry;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

public class OtherServicesBookingActivity extends AppCompatActivity {

    public static final String EXTRA_SERVICE_NAME = "service_name";

    // Service identifiers
    private static final String SVC_SNEAKER  = "Sneaker Cleaning";
    private static final String SVC_BAG      = "Bag Cleaning";
    private static final String SVC_STEAM    = "Press/Steam";
    private static final String SVC_HANDWASH = "Hand Wash";
    private static final String SVC_CURTAIN  = "Curtain Cleaning";

    // ----- Pricing -----
    // Sneaker tiers (per pair)
    private static final double PRICE_SNEAKER_BASIC   = 150.0;
    private static final double PRICE_SNEAKER_DEEP    = 250.0;
    private static final double PRICE_SNEAKER_PREMIUM = 350.0;

    // Bag tiers (per piece)
    private static final double PRICE_BAG_SMALL  = 150.0;
    private static final double PRICE_BAG_MEDIUM = 250.0;
    private static final double PRICE_BAG_LARGE  = 350.0;

    // Press/Steam (per piece)
    private static final double PRICE_STEAM_REGULAR = 25.0;
    private static final double PRICE_STEAM_HEAVY   = 45.0;

    // Hand Wash (per kg, minimum 1 kg)
    private static final double PRICE_HANDWASH_PER_KG = 60.0;
    private static final double PRICE_HANDWASH_MIN_KG = 1.0;

    // Curtain (per meter)
    private static final double PRICE_CURTAIN_PER_METER = 80.0;
    private static final double PRICE_CURTAIN_MIN_METER = 1.0;

    // ----- State -----
    private String serviceName;

    // Sneaker
    private int qtySneakerBasic, qtySneakerDeep, qtySneakerPremium;

    // Bag
    private int qtyBagSmall, qtyBagMedium, qtyBagLarge;

    // Press/Steam
    private int qtySteamRegular, qtySteamHeavy;

    // Hand Wash — quantity in kg (integer steps)
    private int qtyHandWashKg;

    // Curtain — quantity in meters (integer steps, min 1)
    private int qtyCurtainMeters;

    // UI
    private TextView tvTotalPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_services_booking);

        serviceName = getIntent().getStringExtra(EXTRA_SERVICE_NAME);
        if (serviceName == null) serviceName = SVC_SNEAKER;

        ((TextView) findViewById(R.id.tvOtherServiceHeader)).setText(serviceName);
        tvTotalPrice = findViewById(R.id.tvOtherServiceTotal);

        findViewById(R.id.btnOtherServiceBack).setOnClickListener(v -> finish());

        // Show only the relevant section
        showRelevantSection();

        ((MaterialButton) findViewById(R.id.btnOtherServiceContinue))
                .setOnClickListener(v -> onContinue());
    }

    // -------------------------------------------------------------------------
    // Layout orchestration
    // -------------------------------------------------------------------------

    private void showRelevantSection() {
        int[] allSections = {
                R.id.sectionSneaker,
                R.id.sectionBag,
                R.id.sectionSteam,
                R.id.sectionHandWash,
                R.id.sectionCurtain
        };
        for (int id : allSections) {
            findViewById(id).setVisibility(View.GONE);
        }

        switch (serviceName) {
            case SVC_SNEAKER:
                setupSneaker();
                break;
            case SVC_BAG:
                setupBag();
                break;
            case SVC_STEAM:
                setupSteam();
                break;
            case SVC_HANDWASH:
                setupHandWash();
                break;
            case SVC_CURTAIN:
                setupCurtain();
                break;
        }
        updateTotal();
    }

    // -------------------------------------------------------------------------
    // SNEAKER CLEANING
    // -------------------------------------------------------------------------

    private void setupSneaker() {
        findViewById(R.id.sectionSneaker).setVisibility(View.VISIBLE);

        wireCounter(R.id.btnSneakerBasicMinus, R.id.btnSneakerBasicPlus,
                R.id.tvSneakerBasicQty, "sneaker_basic");
        wireCounter(R.id.btnSneakerDeepMinus, R.id.btnSneakerDeepPlus,
                R.id.tvSneakerDeepQty, "sneaker_deep");
        wireCounter(R.id.btnSneakerPremiumMinus, R.id.btnSneakerPremiumPlus,
                R.id.tvSneakerPremiumQty, "sneaker_premium");
    }

    // -------------------------------------------------------------------------
    // BAG CLEANING
    // -------------------------------------------------------------------------

    private void setupBag() {
        findViewById(R.id.sectionBag).setVisibility(View.VISIBLE);

        wireCounter(R.id.btnBagSmallMinus, R.id.btnBagSmallPlus,
                R.id.tvBagSmallQty, "bag_small");
        wireCounter(R.id.btnBagMediumMinus, R.id.btnBagMediumPlus,
                R.id.tvBagMediumQty, "bag_medium");
        wireCounter(R.id.btnBagLargeMinus, R.id.btnBagLargePlus,
                R.id.tvBagLargeQty, "bag_large");
    }

    // -------------------------------------------------------------------------
    // PRESS / STEAM
    // -------------------------------------------------------------------------

    private void setupSteam() {
        findViewById(R.id.sectionSteam).setVisibility(View.VISIBLE);

        wireCounter(R.id.btnSteamRegularMinus, R.id.btnSteamRegularPlus,
                R.id.tvSteamRegularQty, "steam_regular");
        wireCounter(R.id.btnSteamHeavyMinus, R.id.btnSteamHeavyPlus,
                R.id.tvSteamHeavyQty, "steam_heavy");
    }

    // -------------------------------------------------------------------------
    // HAND WASH
    // -------------------------------------------------------------------------

    private void setupHandWash() {
        findViewById(R.id.sectionHandWash).setVisibility(View.VISIBLE);
        qtyHandWashKg = 1; // minimum 1 kg
        ((TextView) findViewById(R.id.tvHandWashKg)).setText(String.valueOf(qtyHandWashKg));

        findViewById(R.id.btnHandWashMinus).setOnClickListener(v -> {
            if (qtyHandWashKg > 1) {
                qtyHandWashKg--;
                ((TextView) findViewById(R.id.tvHandWashKg)).setText(String.valueOf(qtyHandWashKg));
                updateTotal();
            }
        });
        findViewById(R.id.btnHandWashPlus).setOnClickListener(v -> {
            qtyHandWashKg++;
            ((TextView) findViewById(R.id.tvHandWashKg)).setText(String.valueOf(qtyHandWashKg));
            updateTotal();
        });
    }

    // -------------------------------------------------------------------------
    // CURTAIN CLEANING
    // -------------------------------------------------------------------------

    private void setupCurtain() {
        findViewById(R.id.sectionCurtain).setVisibility(View.VISIBLE);
        qtyCurtainMeters = 1;
        ((TextView) findViewById(R.id.tvCurtainMeters)).setText(String.valueOf(qtyCurtainMeters));

        findViewById(R.id.btnCurtainMinus).setOnClickListener(v -> {
            if (qtyCurtainMeters > 1) {
                qtyCurtainMeters--;
                ((TextView) findViewById(R.id.tvCurtainMeters)).setText(String.valueOf(qtyCurtainMeters));
                updateTotal();
            }
        });
        findViewById(R.id.btnCurtainPlus).setOnClickListener(v -> {
            qtyCurtainMeters++;
            ((TextView) findViewById(R.id.tvCurtainMeters)).setText(String.valueOf(qtyCurtainMeters));
            updateTotal();
        });
    }

    // -------------------------------------------------------------------------
    // Generic counter wiring
    // -------------------------------------------------------------------------

    private void wireCounter(int minusId, int plusId, int qtyId, String key) {
        TextView tvQty = findViewById(qtyId);
        findViewById(minusId).setOnClickListener(v -> {
            int cur = getQty(key);
            if (cur > 0) {
                setQty(key, cur - 1);
                tvQty.setText(String.valueOf(getQty(key)));
                updateTotal();
            }
        });
        findViewById(plusId).setOnClickListener(v -> {
            setQty(key, getQty(key) + 1);
            tvQty.setText(String.valueOf(getQty(key)));
            updateTotal();
        });
    }

    // -------------------------------------------------------------------------
    // Qty helpers
    // -------------------------------------------------------------------------

    private int getQty(String key) {
        switch (key) {
            case "sneaker_basic":   return qtySneakerBasic;
            case "sneaker_deep":    return qtySneakerDeep;
            case "sneaker_premium": return qtySneakerPremium;
            case "bag_small":       return qtyBagSmall;
            case "bag_medium":      return qtyBagMedium;
            case "bag_large":       return qtyBagLarge;
            case "steam_regular":   return qtySteamRegular;
            case "steam_heavy":     return qtySteamHeavy;
            default:                return 0;
        }
    }

    private void setQty(String key, int val) {
        switch (key) {
            case "sneaker_basic":   qtySneakerBasic   = val; break;
            case "sneaker_deep":    qtySneakerDeep    = val; break;
            case "sneaker_premium": qtySneakerPremium = val; break;
            case "bag_small":       qtyBagSmall       = val; break;
            case "bag_medium":      qtyBagMedium      = val; break;
            case "bag_large":       qtyBagLarge       = val; break;
            case "steam_regular":   qtySteamRegular   = val; break;
            case "steam_heavy":     qtySteamHeavy     = val; break;
        }
    }

    // -------------------------------------------------------------------------
    // Total calculation
    // -------------------------------------------------------------------------

    private double computeTotal() {
        switch (serviceName) {
            case SVC_SNEAKER:
                return qtySneakerBasic   * PRICE_SNEAKER_BASIC
                        + qtySneakerDeep    * PRICE_SNEAKER_DEEP
                        + qtySneakerPremium * PRICE_SNEAKER_PREMIUM;
            case SVC_BAG:
                return qtyBagSmall  * PRICE_BAG_SMALL
                        + qtyBagMedium * PRICE_BAG_MEDIUM
                        + qtyBagLarge  * PRICE_BAG_LARGE;
            case SVC_STEAM:
                return qtySteamRegular * PRICE_STEAM_REGULAR
                        + qtySteamHeavy   * PRICE_STEAM_HEAVY;
            case SVC_HANDWASH:
                return Math.max(qtyHandWashKg, PRICE_HANDWASH_MIN_KG) * PRICE_HANDWASH_PER_KG;
            case SVC_CURTAIN:
                return Math.max(qtyCurtainMeters, PRICE_CURTAIN_MIN_METER) * PRICE_CURTAIN_PER_METER;
            default:
                return 0;
        }
    }

    private void updateTotal() {
        tvTotalPrice.setText(fmt(computeTotal()));
    }

    private String fmt(double amount) {
        return String.format("₱ %.2f", amount);
    }

    // -------------------------------------------------------------------------
    // Continue → Review
    // -------------------------------------------------------------------------

    private void onContinue() {
        double total = computeTotal();
        if (total <= 0) {
            Toast.makeText(this, "Please select at least one item.", Toast.LENGTH_SHORT).show();
            return;
        }

        String notes = "";
        EditText etNotes = findViewById(R.id.etOtherServiceNotes);
        if (etNotes != null) notes = etNotes.getText().toString().trim();

        Intent intent = new Intent(this, OtherServicesReviewActivity.class);
        intent.putExtra("service_name", serviceName);
        intent.putExtra("notes", notes);
        intent.putExtra("sub_total", total);

        // Pass all quantities
        intent.putExtra("qty_sneaker_basic",   qtySneakerBasic);
        intent.putExtra("qty_sneaker_deep",    qtySneakerDeep);
        intent.putExtra("qty_sneaker_premium", qtySneakerPremium);
        intent.putExtra("price_sneaker_basic",   PRICE_SNEAKER_BASIC);
        intent.putExtra("price_sneaker_deep",    PRICE_SNEAKER_DEEP);
        intent.putExtra("price_sneaker_premium", PRICE_SNEAKER_PREMIUM);

        intent.putExtra("qty_bag_small",  qtyBagSmall);
        intent.putExtra("qty_bag_medium", qtyBagMedium);
        intent.putExtra("qty_bag_large",  qtyBagLarge);
        intent.putExtra("price_bag_small",  PRICE_BAG_SMALL);
        intent.putExtra("price_bag_medium", PRICE_BAG_MEDIUM);
        intent.putExtra("price_bag_large",  PRICE_BAG_LARGE);

        intent.putExtra("qty_steam_regular", qtySteamRegular);
        intent.putExtra("qty_steam_heavy",   qtySteamHeavy);
        intent.putExtra("price_steam_regular", PRICE_STEAM_REGULAR);
        intent.putExtra("price_steam_heavy",   PRICE_STEAM_HEAVY);

        intent.putExtra("qty_handwash_kg", qtyHandWashKg);
        intent.putExtra("price_handwash_per_kg", PRICE_HANDWASH_PER_KG);

        intent.putExtra("qty_curtain_meters", qtyCurtainMeters);
        intent.putExtra("price_curtain_per_meter", PRICE_CURTAIN_PER_METER);

        startActivity(intent);
    }
}
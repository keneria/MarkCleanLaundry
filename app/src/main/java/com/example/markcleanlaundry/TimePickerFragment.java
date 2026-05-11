package com.example.markcleanlaundry;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class TimePickerFragment extends DialogFragment {

    public interface OnTimeSelectedListener {
        void onTimeSelected(String time);
    }

    private OnTimeSelectedListener listener;
    private String mode = "Pickup";

    public void setMode(String mode) { this.mode = mode; }
    public void setOnTimeSelectedListener(OnTimeSelectedListener listener) { this.listener = listener; }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_time_picker_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvHeader      = view.findViewById(R.id.tvTimeHeaderLabel);
        TextView tvSelected    = view.findViewById(R.id.tvSelectedTime);
        TextView tvConfirmBar  = view.findViewById(R.id.tvTimeConfirmBar);
        LinearLayout btnConfirm = view.findViewById(R.id.btnTimeConfirm);
        NumberPicker pickerHour   = view.findViewById(R.id.pickerHour);
        NumberPicker pickerMinute = view.findViewById(R.id.pickerMinute);
        NumberPicker pickerAmPm   = view.findViewById(R.id.pickerAmPm);

        tvHeader.setText("Schedule " + mode);

        pickerHour.setMinValue(1);
        pickerHour.setMaxValue(12);
        pickerHour.setValue(8);

        String[] minutes = {"00", "30"};
        pickerMinute.setMinValue(0);
        pickerMinute.setMaxValue(1);
        pickerMinute.setDisplayedValues(minutes);

        pickerAmPm.setMinValue(0);
        pickerAmPm.setMaxValue(1);
        pickerAmPm.setDisplayedValues(new String[]{"AM", "PM"});
        pickerAmPm.setValue(0);

        Runnable updateHeader = () -> {
            String ap = pickerAmPm.getValue() == 0 ? "AM" : "PM";
            tvSelected.setText(pickerHour.getValue() + ":" + minutes[pickerMinute.getValue()] + " " + ap);
            tvConfirmBar.setText(pickerHour.getValue() + ":" + minutes[pickerMinute.getValue()] + " " + ap);
        };

        updateHeader.run();
        pickerHour.setOnValueChangedListener((p, o, n) -> updateHeader.run());
        pickerMinute.setOnValueChangedListener((p, o, n) -> updateHeader.run());
        pickerAmPm.setOnValueChangedListener((p, o, n) -> updateHeader.run());

        btnConfirm.setOnClickListener(v -> {
            int h  = pickerHour.getValue();
            int ap = pickerAmPm.getValue();
            int hour24 = (ap == 0) ? (h == 12 ? 0 : h) : (h == 12 ? 12 : h + 12);

            if (hour24 < 8 || hour24 > 18 || (hour24 == 18 && pickerMinute.getValue() > 0)) {
                Toast.makeText(getContext(),
                        "Please select a time between 8:00 AM and 6:00 PM.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            String time = h + ":" + minutes[pickerMinute.getValue()] + " " + (ap == 0 ? "AM" : "PM");
            if (listener != null) listener.onTimeSelected(time);
            dismiss();
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }
}
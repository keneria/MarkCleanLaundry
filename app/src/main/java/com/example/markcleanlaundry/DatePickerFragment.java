package com.example.markcleanlaundry;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import java.util.Calendar;

public class DatePickerFragment extends DialogFragment {

    public interface OnDateSelectedListener {
        void onDateSelected(String datetime);
    }

    private OnDateSelectedListener listener;
    private String mode = "Pickup";

    public void setMode(String mode) { this.mode = mode; }
    public void setOnDateSelectedListener(OnDateSelectedListener listener) { this.listener = listener; }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_date_picker_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        CalendarView calendarView = view.findViewById(R.id.calendarView);
        TextView tvHeaderLabel    = view.findViewById(R.id.tvDialogHeaderLabel);
        TextView tvSelectedDate   = view.findViewById(R.id.tvDialogSelectedDate);
        TextView tvConfirmBar     = view.findViewById(R.id.tvConfirmBar);
        LinearLayout btnDone      = view.findViewById(R.id.btnDone);

        tvHeaderLabel.setText("Schedule " + mode);

        String[] monthNames = {"Jan","Feb","Mar","Apr","May","Jun",
                "Jul","Aug","Sep","Oct","Nov","Dec"};

        Calendar cal = Calendar.getInstance();
        calendarView.setMinDate(cal.getTimeInMillis());
        calendarView.setDate(cal.getTimeInMillis(), false, true);

        final int[] selectedDay   = { cal.get(Calendar.DAY_OF_MONTH) };
        final int[] selectedMonth = { cal.get(Calendar.MONTH) + 1 };
        final int[] selectedYear  = { cal.get(Calendar.YEAR) };

        // Show today's date initially
        String initial = String.format("%s %d, %d",
                monthNames[selectedMonth[0] - 1], selectedDay[0], selectedYear[0]);
        tvSelectedDate.setText(initial);
        tvConfirmBar.setText("Continue");

        calendarView.setOnDateChangeListener((v, year, month, day) -> {
            selectedDay[0]   = day;
            selectedMonth[0] = month + 1;
            selectedYear[0]  = year;
            String date = String.format("%s %d, %d",
                    monthNames[month], day, year);
            tvSelectedDate.setText(date);
            tvConfirmBar.setText("Continue");
        });

        btnDone.setOnClickListener(v -> {
            String date = String.format("%s %d, %d",
                    monthNames[selectedMonth[0] - 1], selectedDay[0], selectedYear[0]);
            if (listener != null) listener.onDateSelected(date);
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
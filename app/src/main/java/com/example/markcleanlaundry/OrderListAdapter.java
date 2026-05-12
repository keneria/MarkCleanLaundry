package com.example.markcleanlaundry;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrderListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM   = 1;

    private final Context context;
    private final List<Object> flatList = new ArrayList<>();

    public OrderListAdapter(Context context, List<OrderItem> orders) {
        this.context = context;
        buildFlatList(orders);
    }

    private void buildFlatList(List<OrderItem> orders) {
        flatList.clear();

        SimpleDateFormat dayFmt = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        String today = dayFmt.format(new Date());

        LinkedHashMap<String, List<OrderItem>> grouped = new LinkedHashMap<>();
        for (OrderItem order : orders) {
            String dateKey = dayFmt.format(new Date(order.getTimestamp()));
            if (dateKey.equals(today)) dateKey = "Today";
            grouped.computeIfAbsent(dateKey, k -> new ArrayList<>()).add(order);
        }

        for (Map.Entry<String, List<OrderItem>> entry : grouped.entrySet()) {
            flatList.add(entry.getKey());
            flatList.addAll(entry.getValue());
        }
    }

    public void refresh(List<OrderItem> orders) {
        buildFlatList(orders);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return flatList.get(position) instanceof String ? TYPE_HEADER : TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == TYPE_HEADER) {
            View v = inflater.inflate(R.layout.item_order_header, parent, false);
            return new HeaderViewHolder(v);
        } else {
            View v = inflater.inflate(R.layout.item_order, parent, false);
            return new OrderViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind((String) flatList.get(position));
        } else {
            ((OrderViewHolder) holder).bind((OrderItem) flatList.get(position));
        }
    }

    @Override
    public int getItemCount() { return flatList.size(); }

    // ── Header ViewHolder ─────────────────────────────────────────────────────

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate;
        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDateHeader);
        }
        void bind(String date) { tvDate.setText(date); }
    }

    // ── Order ViewHolder ──────────────────────────────────────────────────────

    class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderTitle, tvLocation, tvStatus, tvTotal, tvTime;
        ImageView ivOrderIcon;

        OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderTitle = itemView.findViewById(R.id.tvOrderTitle);
            tvLocation   = itemView.findViewById(R.id.tvLocation);
            tvStatus     = itemView.findViewById(R.id.tvStatus);
            tvTotal      = itemView.findViewById(R.id.tvTotal);
            tvTime       = itemView.findViewById(R.id.tvTime);
            ivOrderIcon  = itemView.findViewById(R.id.ivOrderIcon);
        }

        void bind(OrderItem order) {
            // Title
            String serviceType = order.getServiceType();
            String laundryType = order.getLaundryType();
            String title;
            if (serviceType != null && !serviceType.isEmpty()) {
                title = serviceType;
                if (laundryType != null && !laundryType.isEmpty()) {
                    title += " · " + laundryType;
                }
            } else {
                title = "Order " + order.getOrderNumber();
            }
            tvOrderTitle.setText(title);

            // Location
            String loc = "Drop-off".equals(order.getServiceType())
                    ? "Mark Clean Store, Makati"
                    : order.getPickupAddress();
            tvLocation.setText(loc != null ? loc : "—");

            // Status text + color
            String status = order.getStatus() != null ? order.getStatus() : "Pending";
            tvStatus.setText(status);
            tvStatus.setTextColor(getStatusColor(status));

            // Status icon
            ivOrderIcon.setImageResource(getStatusIcon(status));

            // Total
            tvTotal.setText("Price TBD");

            // Time
            String time = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                    .format(new Date(order.getTimestamp()));
            tvTime.setText(time);

            // Click → OrderDetailActivity
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, OrderDetailActivity.class);
                intent.putExtra("order_number",     order.getOrderNumber());
                intent.putExtra("bag_count",        order.getBagCount());
                intent.putExtra("pickup_address",   order.getPickupAddress());
                intent.putExtra("delivery_address", order.getDeliveryAddress());
                intent.putExtra("payment_method",   order.getPaymentMethod());
                intent.putExtra("gcash_number",     order.getGcashNumber());
                intent.putExtra("schedule_date",    order.getScheduleDate());
                intent.putExtra("service_type",     order.getServiceType());
                intent.putExtra("laundry_type",     order.getLaundryType());
                intent.putStringArrayListExtra("item_labels", order.getItemLabels());
                context.startActivity(intent);
            });
        }

        private int getStatusColor(String status) {
            switch (status) {
                case "Confirmed":          return 0xFF27AE60; // green
                case "Washing":            return 0xFF2C8FBD; // blue
                case "Ready for Claiming": return 0xFF16A085; // teal
                case "Out for Delivery":   return 0xFF8E44AD; // purple
                case "Claimed":
                case "Delivered":          return 0xFF27AE60; // green
                case "Pending":
                default:                   return 0xFFF4A423; // amber
            }
        }

        private int getStatusIcon(String status) {
            switch (status) {
                case "Confirmed":
                case "Washing":
                case "Ready for Claiming":
                case "Out for Delivery":
                case "Claimed":
                case "Delivered":
                case "Pending":
                default:
                    return R.drawable.order_confirmed;
            }
        }
    }
}
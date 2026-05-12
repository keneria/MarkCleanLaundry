package com.example.markcleanlaundry;

import android.content.Context;
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

public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_NOTIF  = 1;

    private final Context context;
    private final List<Object> flatList = new ArrayList<>();

    public NotificationAdapter(Context context, List<AppNotification> notifications) {
        this.context = context;
        buildFlatList(notifications);
    }

    private void buildFlatList(List<AppNotification> notifications) {
        flatList.clear();

        SimpleDateFormat dayFmt = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        String today = dayFmt.format(new Date());

        LinkedHashMap<String, List<AppNotification>> grouped = new LinkedHashMap<>();
        for (AppNotification n : notifications) {
            String dateKey = dayFmt.format(new Date(n.getTimestamp()));
            if (dateKey.equals(today)) dateKey = "Today";
            grouped.computeIfAbsent(dateKey, k -> new ArrayList<>()).add(n);
        }

        for (Map.Entry<String, List<AppNotification>> entry : grouped.entrySet()) {
            flatList.add(entry.getKey());
            flatList.addAll(entry.getValue());
        }
    }

    public void refresh(List<AppNotification> notifications) {
        buildFlatList(notifications);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return flatList.get(position) instanceof String ? TYPE_HEADER : TYPE_NOTIF;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == TYPE_HEADER) {
            View v = inflater.inflate(R.layout.item_order_header, parent, false);
            return new HeaderViewHolder(v);
        } else {
            View v = inflater.inflate(R.layout.item_notification, parent, false);
            return new NotifViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind((String) flatList.get(position));
        } else {
            ((NotifViewHolder) holder).bind((AppNotification) flatList.get(position));
        }
    }

    @Override
    public int getItemCount() { return flatList.size(); }

    // ── Header ────────────────────────────────────────────────────────────────

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate;
        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDateHeader);
        }
        void bind(String date) { tvDate.setText(date); }
    }

    // ── Notification row ──────────────────────────────────────────────────────

    class NotifViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle, tvMessage, tvTime, tvUnreadDot;

        NotifViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon      = itemView.findViewById(R.id.ivNotifIcon);
            tvTitle     = itemView.findViewById(R.id.tvNotifTitle);
            tvMessage   = itemView.findViewById(R.id.tvNotifMessage);
            tvTime      = itemView.findViewById(R.id.tvNotifTime);
            tvUnreadDot = itemView.findViewById(R.id.tvUnreadDot);
        }

        void bind(AppNotification notif) {
            tvTitle.setText(notif.getTitle());
            tvMessage.setText(notif.getMessage());

            // Time: show "Just Now" if within 1 min, else hh:mm a
            long diff = System.currentTimeMillis() - notif.getTimestamp();
            if (diff < 60_000) {
                tvTime.setText("Just Now");
            } else {
                tvTime.setText(new SimpleDateFormat("hh:mm a", Locale.getDefault())
                        .format(new Date(notif.getTimestamp())));
            }

            // Unread dot
            tvUnreadDot.setVisibility(notif.isRead() ? View.GONE : View.VISIBLE);

            // Unread = bold title, read = normal
            tvTitle.setTypeface(null, notif.isRead()
                    ? android.graphics.Typeface.NORMAL
                    : android.graphics.Typeface.BOLD);

            // Background: unread = light blue tint, read = white
            itemView.setBackgroundColor(notif.isRead() ? 0xFFFFFFFF : 0xFFEEF6FB);

            // Icon per type
            ivIcon.setImageResource(getIconRes(notif.getType()));

            // Mark as read on tap
            itemView.setOnClickListener(v -> {
                notif.setRead(true);
                notifyItemChanged(getAdapterPosition());
            });
        }

        private int getIconRes(AppNotification.Type type) {
            switch (type) {
                case ORDER_PLACED:
                case ORDER_CONFIRMED:       return R.drawable.order_confirmed;
                case ORDER_WASHING:         return R.drawable.washing;
                case ORDER_OUT_FOR_DELIVERY: return R.drawable.dropoff;
                case ORDER_DELIVERED:       return R.drawable.delivered;
                default:                    return R.drawable.order_confirmed;
            }
        }
    }
}
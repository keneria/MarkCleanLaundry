package com.example.markcleanlaundry;

public class AppNotification {

    public enum Type {
        ORDER_PLACED,
        ORDER_CONFIRMED,
        ORDER_WASHING,
        ORDER_OUT_FOR_DELIVERY,
        ORDER_DELIVERED,
        ORDER_READY_FOR_CLAIMING
    }

    private String title;
    private String message;
    private String orderNumber;
    private long timestamp;
    private boolean isRead;
    private Type type;

    public AppNotification(Type type, String orderNumber, long timestamp) {
        this.type        = type;
        this.orderNumber = orderNumber;
        this.timestamp   = timestamp;
        this.isRead      = false;

        switch (type) {
            case ORDER_PLACED:
                this.title   = "Order Placed!";
                this.message = "Your order " + orderNumber + " has been placed successfully. Waiting for admin confirmation.";
                break;
            case ORDER_CONFIRMED:
                this.title   = "Order Confirmed";
                this.message = "Great news! Your order " + orderNumber + " has been confirmed by our team.";
                break;
            case ORDER_WASHING:
                this.title   = "Laundry in Progress";
                this.message = "Your laundry for order " + orderNumber + " is now being washed.";
                break;
            case ORDER_OUT_FOR_DELIVERY:
                this.title   = "Out for Delivery";
                this.message = "Your laundry for order " + orderNumber + " is on its way to you!";
                break;
            case ORDER_DELIVERED:
                this.title   = "Order Delivered!";
                this.message = "Your laundry for order " + orderNumber + " has been delivered. Enjoy your fresh clothes!";
                break;
            case ORDER_READY_FOR_CLAIMING:
                this.title   = "Laundry Ready!";
                this.message = "Your laundry for order " + orderNumber + " is done! Please choose how you'd like to claim it.";
                break;
        }
    }

    public String getTitle()       { return title; }
    public String getMessage()     { return message; }
    public String getOrderNumber() { return orderNumber; }
    public long getTimestamp()     { return timestamp; }
    public boolean isRead()        { return isRead; }
    public Type getType()          { return type; }

    public void setRead(boolean read) { this.isRead = read; }
}
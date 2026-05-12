package com.example.markcleanlaundry;

import java.util.ArrayList;

public class OrderItem {

    private String orderNumber;
    private String status;
    private String scheduleDate;
    private String paymentMethod;
    private String pickupAddress;
    private String deliveryAddress;
    private String serviceType;
    private String laundryType;
    private String clientName;        // NEW
    private String phone;             // NEW
    private int    bagCount;
    private long   timestamp;
    private ArrayList<String> itemLabels;
    private String gcashNumber;
    private double totalPrice;
    private double quantity;
    private String unitLabel;

    // ── Full Constructor ──────────────────────────────────────────────────────

    public OrderItem(String orderNumber, String status, String scheduleDate,
                     String paymentMethod, String pickupAddress, String deliveryAddress,
                     String serviceType, int bagCount, long timestamp,
                     ArrayList<String> itemLabels, String gcashNumber, String laundryType,
                     double totalPrice, double quantity, String unitLabel,
                     String clientName, String phone) {
        this.orderNumber     = orderNumber;
        this.status          = status;
        this.scheduleDate    = scheduleDate;
        this.paymentMethod   = paymentMethod;
        this.pickupAddress   = pickupAddress;
        this.deliveryAddress = deliveryAddress;
        this.serviceType     = serviceType;
        this.laundryType     = laundryType;
        this.clientName      = clientName;
        this.phone           = phone;
        this.bagCount        = bagCount;
        this.timestamp       = timestamp;
        this.itemLabels      = itemLabels;
        this.gcashNumber     = gcashNumber;
        this.totalPrice      = totalPrice;
        this.quantity        = quantity;
        this.unitLabel       = unitLabel;
    }

    // ── Convenience Constructor ───────────────────────────────────────────────

    public OrderItem(String orderNumber, String status, String scheduleDate,
                     String paymentMethod, String pickupAddress, String deliveryAddress,
                     String serviceType, int bagCount, long timestamp,
                     ArrayList<String> itemLabels, String gcashNumber, String laundryType) {
        this(orderNumber, status, scheduleDate, paymentMethod, pickupAddress, deliveryAddress,
                serviceType, bagCount, timestamp, itemLabels, gcashNumber, laundryType,
                0.0, 0.0, "per kg", "", "");
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String getOrderNumber()            { return orderNumber; }
    public String getStatus()                 { return status; }
    public String getScheduleDate()           { return scheduleDate; }
    public String getPaymentMethod()          { return paymentMethod; }
    public String getPickupAddress()          { return pickupAddress; }
    public String getDeliveryAddress()        { return deliveryAddress; }
    public String getServiceType()            { return serviceType; }
    public String getLaundryType()            { return laundryType; }
    public String getClientName()             { return clientName; }
    public String getPhone()                  { return phone; }
    public int    getBagCount()               { return bagCount; }
    public long   getTimestamp()              { return timestamp; }
    public ArrayList<String> getItemLabels()  { return itemLabels; }
    public String getGcashNumber()            { return gcashNumber; }
    public double getTotalPrice()             { return totalPrice; }
    public double getQuantity()               { return quantity; }
    public String getUnitLabel()              { return unitLabel; }

    // ── Setters ───────────────────────────────────────────────────────────────

    public void setStatus(String status)          { this.status = status; }
    public void setTotalPrice(double totalPrice)  { this.totalPrice = totalPrice; }
    public void setQuantity(double quantity)      { this.quantity = quantity; }
    public void setUnitLabel(String unitLabel)    { this.unitLabel = unitLabel; }
    public void setClientName(String clientName)  { this.clientName = clientName; }
    public void setPhone(String phone)            { this.phone = phone; }
}
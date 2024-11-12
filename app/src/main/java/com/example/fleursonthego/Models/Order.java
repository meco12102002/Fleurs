package com.example.fleursonthego.Models;

import java.util.ArrayList;

public class Order {
    private String userId;
    private String orderId;// User ID who placed the order
    private ArrayList<CartItem> items;    // List of items in the order
    private String deliveryDate;           // Delivery date
    private String deliveryTime;           // Delivery time
    private double totalPrice;             // Total price of the order
    private String deliveryDetails;



    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getReceiptImageUrl() {
        return receiptImageUrl;
    }

    public void setReceiptImageUrl(String receiptImageUrl) {
        this.receiptImageUrl = receiptImageUrl;
    }

    private String paymentMethod; // Add this line for payment method
    private String receiptImageUrl; // Add this line for receipt image URL

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    private String status;// Delivery location

    // Default constructor required for calls to DataSnapshot.getValue(Order.class)
    public Order() {
        this.items = new ArrayList<>();   // Initialize items list
    }

    public Order(String orderId,String userId, ArrayList<CartItem> items, String deliveryDate, String deliveryTime, double totalPrice, String deliveryDetails, String status,String name, String paymentMethod, String receiptImageUrl) {
        this.userId = userId;
        this.items = items;
        this.deliveryDate = deliveryDate;
        this.deliveryTime = deliveryTime;
        this.totalPrice = totalPrice;
        this.deliveryDetails = deliveryDetails;
        this.name = name;
        this.status = "Order Placed";
        this.paymentMethod = paymentMethod;
        this.receiptImageUrl = receiptImageUrl;
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public ArrayList<CartItem> getItems() {
        return items;
    }

    public void setItems(ArrayList<CartItem> items) {
        this.items = items;
    }

    public String getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(String deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public String getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(String deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getDeliveryDetails() {
        return deliveryDetails;
    }

    public void setDeliveryDetails(String deliveryDetails) {
        this.deliveryDetails = deliveryDetails;
    }
}

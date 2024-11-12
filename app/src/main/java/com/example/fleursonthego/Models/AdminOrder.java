package com.example.fleursonthego.Models;

import java.util.List;

public class AdminOrder {

    private String name;

    private String orderId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    private String userId;
    private String paymentMethod;
    private String status;
    private String deliveryDate;
    private String deliveryDetails;
    private String deliveryTime;
    private double totalPrice; // Use double for numeric values

    private List<AdminItem> items;


    private String deliveryPersonName;
    private String deliveryPersonPhone;
    private String estimatedDeliveryTime;


    public AdminOrder() {
    }

    public AdminOrder(String name, String orderId, String userId, String paymentMethod, String status, String deliveryDate, String deliveryDetails, String deliveryTime, double totalPrice, List<AdminItem> items) {
        this.name = name;
        this.orderId = orderId;
        this.userId = userId;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.deliveryDate = deliveryDate;
        this.deliveryDetails = deliveryDetails;
        this.deliveryTime = deliveryTime;
        this.totalPrice = totalPrice;
        this.items = items;
    }


    public String getDeliveryPersonName() { return deliveryPersonName; }
    public void setDeliveryPersonName(String deliveryPersonName) { this.deliveryPersonName = deliveryPersonName; }

    public String getDeliveryPersonPhone() { return deliveryPersonPhone; }
    public void setDeliveryPersonPhone(String deliveryPersonPhone) { this.deliveryPersonPhone = deliveryPersonPhone; }

    public String getEstimatedDeliveryTime() { return estimatedDeliveryTime; }
    public void setEstimatedDeliveryTime(String estimatedDeliveryTime) { this.estimatedDeliveryTime = estimatedDeliveryTime; }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getDeliveryDetails() {
        return deliveryDetails;
    }

    public void setDeliveryDetails(String deliveryDetails) {
        this.deliveryDetails = deliveryDetails;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public List<AdminItem> getItems() {
        return items;
    }

    public void setItems(List<AdminItem> items) {
        this.items = items;
    }
}

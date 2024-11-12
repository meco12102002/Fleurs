package com.example.fleursonthego.Models;

import java.util.List;

public class AdminOrderModel {
    private String orderId;
    private String name;
    private String deliveryDate;
    private String deliveryTime;
    private String deliveryDetails;
    private double totalPrice;
    private String paymentMethod;
    private String status;
    private String receiptImageUrl;
    private List<Item> items;

    // Constructor, getters, and setters
    public AdminOrderModel(String orderId, String name, String deliveryDate, String deliveryTime,
                           String deliveryDetails, double totalPrice, String paymentMethod,
                           String status, String receiptImageUrl, List<Item> items) {
        this.orderId = orderId;
        this.name = name;
        this.deliveryDate = deliveryDate;
        this.deliveryTime = deliveryTime;
        this.deliveryDetails = deliveryDetails;
        this.totalPrice = totalPrice;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.receiptImageUrl = receiptImageUrl;
        this.items = items;
    }

    // Getters and setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(String deliveryDate) { this.deliveryDate = deliveryDate; }

    public String getDeliveryTime() { return deliveryTime; }
    public void setDeliveryTime(String deliveryTime) { this.deliveryTime = deliveryTime; }

    public String getDeliveryDetails() { return deliveryDetails; }
    public void setDeliveryDetails(String deliveryDetails) { this.deliveryDetails = deliveryDetails; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReceiptImageUrl() { return receiptImageUrl; }
    public void setReceiptImageUrl(String receiptImageUrl) { this.receiptImageUrl = receiptImageUrl; }

    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }

    public static class Item {
        private String productId;
        private String productName;
        private String image;
        private double price;
        private int quantity;
        private boolean selected;

        // Constructor, getters, and setters
        public Item(String productId, String productName, String image, double price, int quantity, boolean selected) {
            this.productId = productId;
            this.productName = productName;
            this.image = image;
            this.price = price;
            this.quantity = quantity;
            this.selected = selected;
        }

        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

        public String getImage() { return image; }
        public void setImage(String image) { this.image = image; }

        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }

        public boolean isSelected() { return selected; }
        public void setSelected(boolean selected) { this.selected = selected; }
    }
}

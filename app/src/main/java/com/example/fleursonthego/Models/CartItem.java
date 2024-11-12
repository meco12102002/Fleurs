package com.example.fleursonthego.Models;

import java.io.Serializable;

public class CartItem implements Serializable {
    private String image;
    private String productName;
    private String productId;
    private double price;
    private int quantity;
    private boolean selected; // New field for selection state

    // Default constructor required for Firebase
    public CartItem() {
        // Initialize the selected state to false by default
        this.selected = false;
    }

    public CartItem(String image, String productName, double price, int quantity) {
        this.image = image;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
        this.selected = false; // Default to not selected
    }

    // Getter and setter for image
    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    // Getter and setter for productName
    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    // Getter and setter for productId
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    // Getter and setter for price
    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    // Getter and setter for quantity
    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // Getter and setter for selected state
    public boolean isSelected() {
        return selected; // Getter for selection state
    }

    public void setSelected(boolean selected) {
        this.selected = selected; // Setter for selection state
    }
}

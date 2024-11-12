package com.example.fleursonthego.Models;


public class Item {
    private String image;
    private long price;  // Change to long if you store as a number
    private String productName;
    private int quantity;

    public Item() {
        // Default constructor required for calls to DataSnapshot.getValue(Item.class)
    }

    public Item(String image, long price, String productName, int quantity) {
        this.image = image;
        this.price = price; // Change to long if you store as a number
        this.productName = productName;
        this.quantity = quantity;
    }

    public String getImage() {
        return image;
    }

    public long getPrice() {
        return price;  // Change to long if you store as a number
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }
}

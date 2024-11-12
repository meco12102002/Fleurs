package com.example.fleursonthego.Models;

public class AdminProduct2 {
    private String productId;
    private String productName;
    private String productDescription;
    private double price;  // Changed from productPrice to price for consistency
    private int stock;     // Changed from productStock to stock for consistency
    private String category;
    private String image;  // Changed from imageUrl to image for consistency

    // Default constructor required for calls to DataSnapshot.getValue(AdminProduct.class)
    public AdminProduct2() {
    }

    // Constructor with all fields
    public AdminProduct2(String productId, String productName, String productDescription, double price,
                         int stock, String category, String image) {
        this.productId = productId;
        this.productName = productName;
        this.productDescription = productDescription;
        this.price = price;
        this.stock = stock;
        this.category = category;
        this.image = image;  // Updated to match the field name
    }

    // Getters and Setters
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public double getPrice() {
        return price;  // Updated to match the field name
    }

    public void setPrice(double price) {
        this.price = price;  // Updated to match the field name
    }

    public int getStock() {
        return stock;  // Updated to match the field name
    }

    public void setStock(int stock) {
        this.stock = stock;  // Updated to match the field name
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImage() {
        return image;  // Updated to match the field name
    }

    public void setImage(String image) {
        this.image = image;  // Updated to match the field name
    }
}

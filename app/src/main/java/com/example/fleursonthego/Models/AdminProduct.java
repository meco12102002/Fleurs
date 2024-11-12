package com.example.fleursonthego.Models;

public class AdminProduct {
    private String productId;
    private String productName;
    private double price; // Changed to double for numeric values
    private String image;
    private String productDescription;
    private String category;
    private int stock; // Keep stock as an integer if it's a whole number

    // Default constructor required for Firebase
    public AdminProduct() {}

    // Constructor for initializing Product object
    public AdminProduct(String productId, String productName, double price, String image,
                        String productDescription, String category, int stock) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.image = image;
        this.productDescription = productDescription;
        this.category = category;
        this.stock = stock;
    }


    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }
    // Getters and setters for each field
    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getPrice() { // Getter updated to double
        return price;
    }

    public void setPrice(double price) { // Setter updated to double
        this.price = price;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }
}

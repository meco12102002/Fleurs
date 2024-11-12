package com.example.fleursonthego.Models;

public class Product {
    private String productId;
    private String productName;
    private String productDescription;
    private String image; // Change to match Firebase field
    private double price;
    private double rating; // New field for rating
    private String category; // New field for category

    // No-argument constructor required for Firebase
    public Product() {
    }

    // Parameterized constructor
    public Product(String productName, String productDescription, String image, double price, double rating, String category) {
        this.productName = productName;
        this.productDescription = productDescription;
        this.image = image; // Update parameter name
        this.price = price;
        this.rating = rating; // Initialize rating
        this.category = category; // Initialize category
    }


    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }




    // Getters and Setters
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

    public String getImage() { // Update method name
        return image;
    }

    public void setImage(String image) { // Update method name
        this.image = image;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getRating() { // New getter for rating
        return rating;
    }

    public void setRating(double rating) { // New setter for rating
        this.rating = rating;
    }

    public String getCategory() { // New getter for category
        return category;
    }

    public void setCategory(String category) { // New setter for category
        this.category = category;
    }
}


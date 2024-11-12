package com.example.fleursonthego.Models;

public class Review {
    private String comment;
    private float rating;

    public Review() {
        // Default constructor required for calls to DataSnapshot.getValue(Review.class)
    }

    public Review(String comment, float rating) {
        this.comment = comment;
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public float getRating() {
        return rating;
    }
}

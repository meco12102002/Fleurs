package com.example.fleursonthego.views;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fleursonthego.Models.Cart;
import com.example.fleursonthego.Models.Product;
import com.example.fleursonthego.R;
import com.example.fleursonthego.Adapters.ReviewAdapter;
import com.example.fleursonthego.Models.Review;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ProductDetailsActivity extends AppCompatActivity {

    // Firebase references
    private FirebaseDatabase database;
    private DatabaseReference productRef;

    // UI components
    private TextView productName;
    private TextView productCategory;
    private TextView productPrice;
    private RatingBar productRating;
    private ImageView productImage;
    private TextView productDescription;
    private Button addToCart;
    private TextView quantityText;
    private ImageButton increaseQuantityButton;
    private ImageButton decreaseQuantityButton;


    // Data variables
    private String productId;
    private int quantity = 1;
    private RecyclerView reviewsRecyclerView;
    private ReviewAdapter reviewAdapter;
    private List<Review> reviewsList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        // Initialize UI components
        initializeUIComponents();

        // Initialize Firebase Realtime Database
        database = FirebaseDatabase.getInstance();

        // Initialize RecyclerView for reviews
        setupReviewsRecyclerView();

        // Get data from intent
        retrieveIntentData();

        // Set product details if available
        setProductDetails();

        // Fetch reviews from Firebase
        if (productId != null) {
            fetchProductReviews(productId);
        }

        // Set up quantity buttons
        setupQuantityButtons();
        addToCart.setOnClickListener(v -> addToCart());

    }

    private void initializeUIComponents() {
        productName = findViewById(R.id.productTitle);
        productCategory = findViewById(R.id.productCategory);
        productPrice = findViewById(R.id.productPrice);
        productRating = findViewById(R.id.productRating);
        productImage = findViewById(R.id.productImage);
        productDescription = findViewById(R.id.productDescription);
        increaseQuantityButton = findViewById(R.id.increaseQuantity);
        decreaseQuantityButton = findViewById(R.id.decreaseQuantity);
        quantityText = findViewById(R.id.quantity);
        addToCart = findViewById(R.id.addToCartButton);

    }

    private void addToCart() {
        // Get the current user's UID
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Get a reference to the user's cart
        DatabaseReference cartRef = database.getReference("users").child(userId).child("cart").child(productId);

        // Use a listener to check if the product already exists in the cart
        cartRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // If the product exists, get the current quantity and update it
                    Cart existingCartItem = dataSnapshot.getValue(Cart.class);
                    if (existingCartItem != null) {
                        int newQuantity = existingCartItem.getQuantity() + quantity; // Increment the quantity
                        existingCartItem.setQuantity(newQuantity);

                        // Update the cart item with the new quantity
                        cartRef.setValue(existingCartItem)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(ProductDetailsActivity.this, "Cart updated", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(ProductDetailsActivity.this, "Failed to update cart: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                } else {
                    // If the product does not exist in the cart, create a new Cart object
                    Cart cartItem = new Cart();
                    cartItem.setProductId(productId);
                    cartItem.setProductName(productName.getText().toString());
                    cartItem.setCategory(productCategory.getText().toString());
                    cartItem.setPrice(Double.parseDouble(productPrice.getText().toString().replace("P", "").trim()));
                    cartItem.setRating(productRating.getRating());
                    cartItem.setImage(getIntent().getStringExtra("productImage"));
                    cartItem.setProductDescription(productDescription.getText().toString());
                    cartItem.setQuantity(quantity); // Set the initial quantity

                    // Store the product in the cart
                    cartRef.setValue(cartItem)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(ProductDetailsActivity.this, "Added to Cart", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(ProductDetailsActivity.this, "Failed to add to cart: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ProductDetailsActivity.this, "Error retrieving cart: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }




    private void setupReviewsRecyclerView() {
        reviewsRecyclerView = findViewById(R.id.reviewsRecyclerView);
        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reviewsList = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(reviewsList);
        reviewsRecyclerView.setAdapter(reviewAdapter);
    }

    private void retrieveIntentData() {
        Intent intent = getIntent();
        productId = intent.getStringExtra("productId");
        String name = intent.getStringExtra("productName");
        String category = intent.getStringExtra("productCategory");
        Double price = intent.getDoubleExtra("productPrice", 0);
        Float rating = intent.getFloatExtra("productRating", 0);
        String image = intent.getStringExtra("productImage");
        String description = intent.getStringExtra("productDescription");

        Log.d(TAG, "retrieveIntentData: productId=" + productId);
        Log.d(TAG, "retrieveIntentData: productName=" + name);
        Log.d(TAG, "retrieveIntentData: productCategory=" + category);
        Log.d(TAG, "retrieveIntentData: productPrice=" + price);
        Log.d(TAG, "retrieveIntentData: productRating=" + rating);
        Log.d(TAG, "retrieveIntentData: productImage=" + image);
        Log.d(TAG, "retrieveIntentData: productDescription=" + description);

        // Set product details if available
        if (name != null) productName.setText(name);
        if (category != null) productCategory.setText(category);
        if (price != null) productPrice.setText(String.format("P%.2f", price));
        if (rating != null) productRating.setRating(rating);
        if (image != null) Glide.with(this).load(image).into(productImage);
        if (description != null) productDescription.setText(description);
    }

    private void setProductDetails() {
        // If productId is available, fetch reviews from Firebase
        if (productId != null) {
            fetchProductReviews(productId);
        }
    }

    private void fetchProductReviews(String productId) {
        productRef = database.getReference("products").child(productId);
        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    reviewsList.clear();
                    for (DataSnapshot reviewSnapshot : dataSnapshot.child("reviews").getChildren()) {
                        String comment = reviewSnapshot.child("comment").getValue(String.class);
                        Float rating = reviewSnapshot.child("rating").getValue(Float.class);
                        if (comment != null && rating != null) {
                            reviewsList.add(new Review(comment, rating));
                        }
                    }
                    reviewAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(ProductDetailsActivity.this, "No reviews found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ProductDetailsActivity.this, "Error retrieving reviews: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupQuantityButtons() {
        increaseQuantityButton.setOnClickListener(v -> updateQuantity(1));
        decreaseQuantityButton.setOnClickListener(v -> updateQuantity(-1));
    }

    private void updateQuantity(int delta) {
        if (quantity + delta >= 1) {
            quantity += delta;
            quantityText.setText(String.valueOf(quantity));
        } else {
            Toast.makeText(this, "Quantity cannot be less than 1", Toast.LENGTH_SHORT).show();
        }
    }
}

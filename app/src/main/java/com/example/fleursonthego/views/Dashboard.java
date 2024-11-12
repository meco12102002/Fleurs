package com.example.fleursonthego.views;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // Add this import for Glide
import com.example.fleursonthego.R;
import com.example.fleursonthego.Adapters.ProductsAdapter;
import com.example.fleursonthego.Models.Product;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import utils.CircleImageTransformation;

public class Dashboard extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView viewAll;
    private ImageButton customizeBouquetButton;
    private TextView helloTextView;
    private ImageView profileImageView; // ImageView for the profile picture
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private List<String> selectedCategories = new ArrayList<>();

    private EditText searchEditText;  // Reference to the search EditText
    private List<Product> allProducts = new ArrayList<>(); // To store all products fetched from Firebase
    private ProductsAdapter productsAdapter;  // The adapter for the RecyclerView


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard); // Set the layout

        initializeViews();
        setupRecyclerView();
        setupListeners();
        fetchProducts();
        setupCategoryButtons();  // Add this line to set up category button click listeners


        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            retrieveUserName(userId);
            retrieveProfileImage(userId); // Fetch the profile image
        } else {
            helloTextView.setText("User not logged in");
        }


        // Set up search functionality
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                // Not used in this case
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                filterProducts(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }

        });


    }


    private void filterProducts(String query) {
        List<Product> filteredList = new ArrayList<>();

        for (Product product : allProducts) {
            if (product.getProductName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(product);
            }
        }

        updateRecyclerView(filteredList); // Update the RecyclerView with filtered list
    }
    private void toggleCategory(String category) {
        if (selectedCategories.contains(category)) {
            selectedCategories.remove(category); // If already selected, deselect it
        } else {
            selectedCategories.add(category); // If not selected, add it
        }

        // Optionally, you can change the image or style to show if the category is selected or not.
        updateCategoryButtonStyles();

        // Log the selected categories for debugging (optional)
        Log.d("Selected Categories", selectedCategories.toString());

        // Fetch and filter products based on the selected categories
        fetchProducts();
    }


    private void updateCategoryButtonStyles() {
        ImageView weddingsButton = findViewById(R.id.icon_wedding);
        ImageView birthdaysButton = findViewById(R.id.icon_birthday);
        ImageView anniversariesButton = findViewById(R.id.icon_anniversary);

        // Check if the category is selected and update the image accordingly
        weddingsButton.setImageResource(selectedCategories.contains("Weddings") ? R.drawable.ic_wedding : R.drawable.ic_eye_on);
        birthdaysButton.setImageResource(selectedCategories.contains("Birthdays") ? R.drawable.ic_birthday : R.drawable.ic_eye_on);
        anniversariesButton.setImageResource(selectedCategories.contains("Anniversaries") ? R.drawable.ic_anniversary : R.drawable.ic_eye_on);
    }
    private void retrieveUserName(String userId) {
        usersRef.child(userId).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.getValue(String.class);
                    helloTextView.setText("Hello, " + name); // Display user's name
                } else {
                    helloTextView.setText("User name not found");

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                helloTextView.setText("Error retrieving name: " + error.getMessage());
            }
        });
    }

    private void retrieveProfileImage(String userId) {
        usersRef.child(userId).child("profileImage").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String profileImageUrl = snapshot.getValue(String.class);
                    // Load the image using Glide
                    Glide.with(Dashboard.this)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.womanboquet)
                            .transform(new CircleImageTransformation())
                            .into(profileImageView);
                } else {
                    // If no image URL is found, you can set a default image
                    profileImageView.setImageResource(R.drawable.womanboquet);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Dashboard", "Error retrieving profile image: " + error.getMessage());
            }
        });
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerViewProducts);
        viewAll = findViewById(R.id.textViewViewAll);
        customizeBouquetButton = findViewById(R.id.fab_customize_order);
        helloTextView = findViewById(R.id.helloTextView);
        profileImageView = findViewById(R.id.profileImageView); // Initialize profileImageView
        searchEditText = findViewById(R.id.searchTextField); // Initialize the EditText for search

    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false));
    }
    private void setupCategoryButtons() {
        ImageView weddingsButton = findViewById(R.id.icon_wedding); // Replace with actual ID
        ImageView birthdaysButton = findViewById(R.id.icon_birthday); // Replace with actual ID
        ImageView anniversariesButton = findViewById(R.id.icon_anniversary); // Replace with actual ID

        // Set OnClickListeners for each category button
        weddingsButton.setOnClickListener(v -> toggleCategory("Weddings"));
        birthdaysButton.setOnClickListener(v -> toggleCategory("Birthdays"));
        anniversariesButton.setOnClickListener(v -> toggleCategory("Anniversaries"));
    }
    private void setupListeners() {
        viewAll.setOnClickListener(v -> {
            Intent intent = new Intent(Dashboard.this, AllProductsActivity.class);
            startActivity(intent);
        });

        customizeBouquetButton.setOnClickListener(v -> {
            Intent intent = new Intent(Dashboard.this, BouquetCustomizationActivity.class);
            startActivity(intent);
        });

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.nav_home) {
                    startActivity(new Intent(Dashboard.this, Dashboard.class));
                    return true;
                } else if (item.getItemId() == R.id.nav_messages) {
                    startActivity(new Intent(Dashboard.this, ChatListActivity.class));
                    return true;
                } else if (item.getItemId() == R.id.nav_cart) {
                    startActivity(new Intent(Dashboard.this, CartActivity.class));
                    return true;
                } else if (item.getItemId() == R.id.nav_account) {
                    startActivity(new Intent(Dashboard.this, AccountActivity.class));
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    private void fetchProducts() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("products");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allProducts.clear();  // Clear the list to avoid duplicates
                List<Product> productList = new ArrayList<>();

                if (selectedCategories.isEmpty()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String productId = snapshot.getKey();
                        Product product = snapshot.getValue(Product.class);
                        if (product != null) {
                            product.setProductId(productId);
                            allProducts.add(product); // Save all products to the list
                            productList.add(product);
                        }
                    }
                } else {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String productId = snapshot.getKey();
                        Product product = snapshot.getValue(Product.class);
                        if (product != null) {
                            product.setProductId(productId);
                            String productCategories = product.getCategory();
                            for (String selectedCategory : selectedCategories) {
                                if (productCategories.contains(selectedCategory)) {
                                    allProducts.add(product);
                                    productList.add(product);
                                    break;
                                }
                            }
                        }
                    }
                }

                updateRecyclerView(productList); // Initially show all products
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error if needed
            }
        });
    }




    private void updateRecyclerView(List<Product> productList) {
        ProductsAdapter adapter = new ProductsAdapter(this,productList);
        recyclerView.setAdapter(adapter);
    }
}

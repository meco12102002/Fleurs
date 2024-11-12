package com.example.fleursonthego.views;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fleursonthego.Adapters.CartAdapter;
import com.example.fleursonthego.Models.CartItem;
import com.example.fleursonthego.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnTotalPriceChangeListener {

    private RecyclerView cartRecyclerView;
    private double totalPrice = 0.0;  // Add this variable to store the total price

    TextView totalPriceTextView;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItemList = new ArrayList<>();
    Button checkoutButton;
    private static final String TAG = "CartActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Initialize RecyclerView and set its properties
        totalPriceTextView = findViewById(R.id.totalPriceTextView);
        cartRecyclerView = findViewById(R.id.cartRecyclerView);
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartAdapter(this, cartItemList, this);
        cartRecyclerView.setAdapter(cartAdapter);
        checkoutButton = findViewById(R.id.checkoutButton);

        Log.d(TAG, "RecyclerView initialized: " + cartRecyclerView);
        Log.d(TAG, "Adapter initialized: " + cartAdapter);



        Toolbar cartToolbar = findViewById(R.id.cartToolbar);
        setSupportActionBar(cartToolbar);

        // Set up the back arrow to navigate to DashboardActivity
        cartToolbar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(CartActivity.this, Dashboard.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); // Clear stack and bring Dashboard to top
            startActivity(intent);
            finish(); // Close CartActivity
        });

        // Load cart items from Firebase
        loadCartItems();

        // Initially disable the checkout button
        checkoutButton.setEnabled(false);

        checkoutButton.setOnClickListener(v -> {

            ArrayList<CartItem> selectedItems = new ArrayList<>();
            for (CartItem cartItem : cartItemList) {
                if (cartItem.isSelected()) { // Assuming isSelected() checks if the item is selected
                    selectedItems.add(cartItem);
                }
            }

            if (!selectedItems.isEmpty()) {
                Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
                intent.putExtra("selectedItems", selectedItems);
                intent.putExtra("totalPrice", totalPrice);  // Pass total price to CheckoutActivity
                startActivity(intent);
            } else {
                Toast.makeText(CartActivity.this, "Please select at least one item.", Toast.LENGTH_SHORT).show();
            }


        });



    }

    // Check if at least one item is selected in the cart
    private void updateCheckoutButtonState() {
        boolean hasSelectedItems = !cartItemList.isEmpty(); // Adjust if selection is tracked per item
        checkoutButton.setEnabled(hasSelectedItems);
    }

    // Update RecyclerView when new data is loaded
    private void updateRecyclerView() {
        cartAdapter.notifyDataSetChanged();
    }

    // Load cart items from Firebase Realtime Database
    private void loadCartItems() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("users")
                .child(auth.getCurrentUser().getUid())
                .child("cart");

        cartRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cartItemList.clear(); // Clear old items
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    CartItem cartItem = itemSnapshot.getValue(CartItem.class);
                    if (cartItem != null) {
                        cartItemList.add(cartItem);
                        Log.d(TAG, "Loaded CartItem: " + cartItem.toString());
                    } else {
                        Log.e(TAG, "Failed to parse CartItem for: " + itemSnapshot.getKey());
                    }
                }
                cartAdapter.updateTotalPrice();
                updateRecyclerView(); // Notify adapter of data changes
                updateCheckoutButtonState(); // Update button state based on item count
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CartActivity.this, "Failed to load cart items.", Toast.LENGTH_SHORT).show();
            }
        });
    }



    @Override
    public void onTotalPriceChanged(double total) {
        totalPrice = total; // Update the totalPrice variable
        totalPriceTextView.setText("Total: ₱" + String.format("%.2f", total));
        Log.d(TAG, "Total price updated in variable and UI: ₱" + String.format("%.2f", total)); // Log to confirm update
        updateCheckoutButtonState();
    }
}

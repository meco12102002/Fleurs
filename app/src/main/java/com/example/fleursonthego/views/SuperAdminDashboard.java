package com.example.fleursonthego.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.fleursonthego.R;

public class SuperAdminDashboard extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    Button goToInventory,goToOrders, goToTransactions;
    TextView tvPendingOrdersCount, tvTransactionsCount, tvActiveAccountsCount, tvProductsCount;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference ordersRef, usersRef, productsRef, transactionsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_super_admin_dashboard);

        // Initialize BottomNavigationView
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set up BottomNavigationView item click listener
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                startActivity(new Intent(SuperAdminDashboard.this, SuperAdminDashboard.class));
                return true;
            } else if (item.getItemId() == R.id.nav_messages) {
                startActivity(new Intent(SuperAdminDashboard.this, ChatListActivity.class));
                return true;
            } else if (item.getItemId() == R.id.nav_account) {
                startActivity(new Intent(SuperAdminDashboard.this, AdminAccountActivity.class));
                return true;
            }
            return false;
        });

        // Initialize views
        tvPendingOrdersCount = findViewById(R.id.tv_pending_orders_count);
        tvTransactionsCount = findViewById(R.id.tv_transactions_count);
        tvActiveAccountsCount = findViewById(R.id.tv_active_accounts_count);
        tvProductsCount = findViewById(R.id.tv_products_count);

        // Initialize Firebase
        firebaseDatabase = FirebaseDatabase.getInstance();

        // Set up references to the Firebase nodes
        ordersRef = firebaseDatabase.getReference("orders");
        usersRef = firebaseDatabase.getReference("users");
        productsRef = firebaseDatabase.getReference("products");
        transactionsRef = firebaseDatabase.getReference("transactions");

        // Set OnClickListener for Inventory button
        goToInventory = findViewById(R.id.btn_products);
        goToInventory.setOnClickListener(v -> {
            Intent intent = new Intent(SuperAdminDashboard.this, InventoryActivity.class);
            startActivity(intent);
        });

        goToOrders = findViewById(R.id.btn_orders);
        goToOrders.setOnClickListener(v -> {
            Intent intent = new Intent(SuperAdminDashboard.this, AdminOrdersViewActivity.class);
            startActivity(intent);
        });

        goToTransactions = findViewById(R.id.btn_transactions);
        goToTransactions.setOnClickListener(v -> {
            Intent intent = new Intent(SuperAdminDashboard.this, TransactionsViewAdmin.class);
            startActivity(intent);
        });

        // Fetch counts from Firebase
        fetchPendingOrdersCount();
        fetchActiveAccountsCount();
        fetchProductsCount();
        fetchTransactionsCount();
    }

    private void fetchPendingOrdersCount() {
        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int count = (int) dataSnapshot.getChildrenCount();
                tvPendingOrdersCount.setText(String.valueOf(count));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SuperAdminDashboard.this, "Failed to load pending orders count", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchActiveAccountsCount() {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int count = (int) dataSnapshot.getChildrenCount();
                tvActiveAccountsCount.setText(String.valueOf(count));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SuperAdminDashboard.this, "Failed to load active accounts count", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchProductsCount() {
        productsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int count = (int) dataSnapshot.getChildrenCount();
                tvProductsCount.setText(String.valueOf(count));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SuperAdminDashboard.this, "Failed to load products count", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchTransactionsCount() {
        transactionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int orderCount = 0;

                // Iterate over each userID to count the orderIDs
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    // Iterate over each orderID under the current userID
                    for (DataSnapshot orderSnapshot : userSnapshot.getChildren()) {
                        orderCount++; // Increment the count for each orderID
                    }
                }

                // Update the UI with the total order count
                tvTransactionsCount.setText(String.valueOf(orderCount));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SuperAdminDashboard.this, "Failed to load transactions count", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

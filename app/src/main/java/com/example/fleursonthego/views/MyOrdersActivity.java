package com.example.fleursonthego.views;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fleursonthego.Adapters.OrdersAdapter;
import com.example.fleursonthego.Models.Orders;
import com.example.fleursonthego.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class MyOrdersActivity extends AppCompatActivity {

    private RecyclerView ordersRecyclerView;
    private OrdersAdapter ordersAdapter;
    private List<Orders> orderList = new ArrayList<>();
    private DatabaseReference ordersDatabaseReference;

    private MaterialButton AllOrders, OnGoingOrders, PastOrders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_orders);

        ordersRecyclerView = findViewById(R.id.ordersRecyclerView);
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        ordersAdapter = new OrdersAdapter(this, orderList);
        ordersRecyclerView.setAdapter(ordersAdapter);

        Toolbar toolbar = findViewById(R.id.toolbarOrders);
        setSupportActionBar(toolbar);

        // Enable the back button in the AppBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Get the current user's UID from Firebase Authentication
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d("MyOrdersActivity", "User ID: " + userId);  // Log user ID for debugging

        // Initialize Firebase Realtime Database reference
        ordersDatabaseReference = FirebaseDatabase.getInstance().getReference("orders").child(userId);

        // Initialize buttons
        AllOrders = findViewById(R.id.btnAll);
        OnGoingOrders = findViewById(R.id.btnOngoing);
        PastOrders = findViewById(R.id.btnPast);

        // Set button click listeners
        AllOrders.setOnClickListener(v -> fetchFilteredOrders("All"));
        OnGoingOrders.setOnClickListener(v -> fetchFilteredOrders("Ongoing"));
        PastOrders.setOnClickListener(v -> fetchFilteredOrders("Past"));

        // Initially fetch all orders
        fetchFilteredOrders("All");
    }

    private void fetchFilteredOrders(String filter) {
        orderList.clear(); // Clear existing list before fetching new orders

        // Get user ID
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Reference for both "orders" and "cancelled_orders" nodes
        DatabaseReference ordersReference = FirebaseDatabase.getInstance().getReference("orders").child(userId);
        DatabaseReference cancelledOrdersReference = FirebaseDatabase.getInstance().getReference("cancelled_orders").child(userId);

        // Fetch orders from the "orders" node
        ordersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot orderSnapshot : dataSnapshot.getChildren()) {
                    Orders order = orderSnapshot.getValue(Orders.class);
                    if (order != null) {
                        // Apply the filter based on the selected filter
                        if ("All".equals(filter)) {
                            orderList.add(order);  // Add all orders
                        } else if ("Ongoing".equals(filter)) {
                            // Only show orders that are not cancelled
                            if (!"Order Cancelled".equals(order.getStatus())) {
                                orderList.add(order);
                            }
                        } else if ("Past".equals(filter)) {
                            // Only show orders that are neither cancelled nor placed
                            if (!"Order Cancelled".equals(order.getStatus()) && !"Order Placed".equals(order.getStatus())) {
                                orderList.add(order);
                            }
                        }
                    }
                }
                ordersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MyOrdersActivity.this, "Failed to load orders: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("MyOrdersActivity", "Error loading orders", databaseError.toException());
            }
        });

        // Fetch cancelled orders from the "cancelled_orders" node
        cancelledOrdersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot orderSnapshot : dataSnapshot.getChildren()) {
                    Orders order = orderSnapshot.getValue(Orders.class);
                    if (order != null) {
                        // Apply the filter based on the selected filter
                        if ("All".equals(filter)) {
                            orderList.add(order);  // Add all cancelled orders
                        } else if ("Ongoing".equals(filter)) {
                            // Only show orders that are not cancelled
                            if (!"Order Cancelled".equals(order.getStatus())) {
                                orderList.add(order);
                            }
                        } else if ("Past".equals(filter)) {
                            // Only show orders that are cancelled and not placed
                            if ("Order Cancelled".equals(order.getStatus())) {
                                orderList.add(order);
                            }
                        }
                    }
                }
                ordersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MyOrdersActivity.this, "Failed to load cancelled orders: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("MyOrdersActivity", "Error loading cancelled orders", databaseError.toException());
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed(); // Call this to go back to the previous screen
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

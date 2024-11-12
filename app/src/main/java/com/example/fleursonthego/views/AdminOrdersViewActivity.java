package com.example.fleursonthego.views;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fleursonthego.Adapters.AdminOrdersAdapter;
import com.example.fleursonthego.Models.AdminOrder;
import com.example.fleursonthego.Models.AdminItem; // Ensure that AdminItem is imported
import com.example.fleursonthego.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminOrdersViewActivity extends AppCompatActivity {




    private RecyclerView ordersRecyclerView;
    private String statusFilter = "All"; // Default to "All"

    private AdminOrdersAdapter ordersAdapter;
    private List<AdminOrder> orderList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_orders_view);
        fetchOrdersFromFirebase(statusFilter);
        MaterialButton btnAll = findViewById(R.id.btnAll);
        MaterialButton btnPending = findViewById(R.id.btnPending);
        MaterialButton btnAccepted = findViewById(R.id.btnAccepted);
        MaterialButton btnRejected = findViewById(R.id.btnRejected);
        orderList = new ArrayList<>();



        ordersRecyclerView = findViewById(R.id.ordersRecyclerView);
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderList = new ArrayList<>();
        ordersAdapter = new AdminOrdersAdapter(orderList);
        ordersRecyclerView.setAdapter(ordersAdapter);

        // Find the toolbar and set it up
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Enable the back button functionality
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        btnAll.setOnClickListener(v -> {
            statusFilter = "All";
            fetchOrdersFromFirebase(statusFilter);
        });

        btnPending.setOnClickListener(v -> {
            statusFilter = "Order Placed";
            fetchOrdersFromFirebase(statusFilter);
        });

        btnAccepted.setOnClickListener(v -> {
            statusFilter = "Accepted";
            fetchOrdersFromFirebase(statusFilter);
        });

        btnRejected.setOnClickListener(v -> {
            statusFilter = "Rejected";
            fetchOrdersFromFirebase(statusFilter);
        });
    }

    private void fetchOrdersFromFirebase(String statusFilter) {
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("orders");

        if (orderList != null) {
            orderList.clear();  // This line should work now, as orderList is initialized.
        }
        // Fetch all orders, but filter by status if necessary
        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Iterate through orders data snapshot
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot orderSnapshot : userSnapshot.getChildren()) {
                        AdminOrder order = orderSnapshot.getValue(AdminOrder.class);
                        if (order != null) {
                            // Check if order matches the filter status
                            if (statusFilter.equals("All") || order.getStatus().equalsIgnoreCase(statusFilter)) {
                                // Ensure that each order has its list of AdminItems
                                List<AdminItem> items = new ArrayList<>();
                                for (DataSnapshot itemSnapshot : orderSnapshot.child("items").getChildren()) {
                                    AdminItem item = itemSnapshot.getValue(AdminItem.class);
                                    if (item != null) {
                                        items.add(item);
                                    }
                                }
                                order.setItems(items); // Set the list of items for the order
                                orderList.add(order); // Add the order to the list
                            }
                        }
                    }
                }
                ordersAdapter.notifyDataSetChanged(); // Notify adapter of data change
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(AdminOrdersViewActivity.this, "Failed to load orders: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

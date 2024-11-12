package com.example.fleursonthego.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fleursonthego.Adapters.SearchUserRecyclerAdapter;
import com.example.fleursonthego.Models.UserModel;
import com.example.fleursonthego.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.firebase.ui.database.FirebaseRecyclerOptions;

public class ChatListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SearchUserRecyclerAdapter adapter;
    private DatabaseReference databaseReference;
    ImageButton customBackButton;  // Declare the custom back button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        // Initialize the RecyclerView
        recyclerView = findViewById(R.id.recyclerViewMessages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users");

        // Initialize the custom back button
        customBackButton = findViewById(R.id.buttonBack);  // Ensure this ID matches the one in your layout

        // Set up the OnClickListener for the custom back button
        // Inside onCreate()
        customBackButton.setOnClickListener(v -> {
            // Get the current logged-in user
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                String currentUserId = currentUser.getUid();
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);

                // Check the user type of the currently logged-in user
                userRef.child("userType").get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userType = task.getResult().getValue(String.class);

                        // Navigate based on userType
                        if ("superAdmin".equals(userType)) {
                            // If superAdmin, navigate to SuperAdminDashboard
                            Intent intent = new Intent(ChatListActivity.this, SuperAdminDashboard.class);
                            startActivity(intent);
                        } else {
                            // If not superAdmin, navigate to Dashboard
                            Intent intent = new Intent(ChatListActivity.this, Dashboard.class);
                            startActivity(intent);
                        }
                        finish();  // Optional, to explicitly finish the current activity
                    }
                });
            }
        });

        // Get the current logged-in user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);

            // Check the user type of the currently logged-in user
            userRef.child("userType").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String userType = task.getResult().getValue(String.class);

                    // If the current user is a "customer", exclude other customers
                    if (userType != null && userType.equals("customer")) {
                        // Fetch users where userType is not "customer"
                        Query query = databaseReference.orderByChild("userType").equalTo("superAdmin");

                        // Setup FirebaseRecyclerOptions with the query excluding customers
                        FirebaseRecyclerOptions<UserModel> options = new FirebaseRecyclerOptions.Builder<UserModel>()
                                .setQuery(query, UserModel.class)
                                .build();

                        // Initialize the adapter with the filtered options
                        adapter = new SearchUserRecyclerAdapter(options);
                        recyclerView.setAdapter(adapter);
                    }
                    // If the current user is a "superAdmin", fetch all users
                    else if (userType != null && userType.equals("superAdmin")) {
                        // Fetch all users for superAdmin
                        FirebaseRecyclerOptions<UserModel> options = new FirebaseRecyclerOptions.Builder<UserModel>()
                                .setQuery(databaseReference, UserModel.class)
                                .build();

                        // Initialize the adapter with the full list of users
                        adapter = new SearchUserRecyclerAdapter(options);
                        recyclerView.setAdapter(adapter);
                    }
                    else {
                        // If the user is "admin", fetch all users
                        FirebaseRecyclerOptions<UserModel> options = new FirebaseRecyclerOptions.Builder<UserModel>()
                                .setQuery(databaseReference, UserModel.class)
                                .build();

                        // Initialize the adapter with the full list
                        adapter = new SearchUserRecyclerAdapter(options);
                        recyclerView.setAdapter(adapter);
                    }

                    // Start listening for data changes
                    adapter.startListening();
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Start listening for data changes
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Stop listening for data changes
        if (adapter != null) {
            adapter.stopListening();
        }
    }

    @Override
    public void onBackPressed() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);

            // Check the user type of the currently logged-in user
            userRef.child("userType").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String userType = task.getResult().getValue(String.class);

                    // Navigate based on userType
                    Intent intent;
                    if ("superAdmin".equals(userType)) {
                        intent = new Intent(ChatListActivity.this, SuperAdminDashboard.class);
                    } else {
                        intent = new Intent(ChatListActivity.this, Dashboard.class);
                    }
                    startActivity(intent);
                    finish();
                }
            });
        } else {
            super.onBackPressed();
        }
    }
}

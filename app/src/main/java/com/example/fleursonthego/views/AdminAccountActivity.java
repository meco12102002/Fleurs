package com.example.fleursonthego.views;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.fleursonthego.R;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminAccountActivity extends AppCompatActivity {

    private TextView tvAdminAccount;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_account);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // Initialize Firebase Database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        tvAdminAccount = findViewById(R.id.tv_admin_account);
        Button btnLogout = findViewById(R.id.btn_logout);

        // Get the current logged in user ID
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            userId = user.getUid();
        }

        // Fetch the user data from Firebase Realtime Database
        fetchUserData();

        // Logout Button functionality
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            // Redirect to login screen (replace with actual intent)
            Intent intent = new Intent(AdminAccountActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void fetchUserData() {
        // Reference to the user's data node in the database
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Retrieve the name and userType
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String userType = dataSnapshot.child("userType").getValue(String.class);

                    // Set the text of the TextView
                    if (name != null && userType != null) {
                        tvAdminAccount.setText("Name: " + name + "\nUser Type: " + userType);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
                Log.w("AdminAccountActivity", "loadUserData:onCancelled", databaseError.toException());
            }
        });
    }
}
package com.example.fleursonthego.views;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.fleursonthego.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Firebase setup
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Adjust for system bars (edge-to-edge support)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Check if the user is logged in and navigate accordingly
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User is logged in, check their type and navigate
            checkUserType(currentUser.getUid());
        } else {
            // User is not logged in, navigate to login screen
            navigateToLogin();
        }
    }

    private void checkUserType(String userId) {
        // Fetch the user type from the database
        usersRef.child(userId).child("userType").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String userType = snapshot.getValue(String.class);
                    if ("superadmin".equals(userType)) {
                        // Navigate to Super Admin Dashboard
                        navigateToSuperAdminDashboard();
                    } else if ("admin".equals(userType)) {
                        // Navigate to Admin Dashboard
                        navigateToAdminDashboard();
                    } else if ("customer".equals(userType)) {
                        // Navigate to Customer Dashboard
                        navigateToCustomerDashboard();
                    } else {
                        Log.d("MainActivity", "User type is unknown");
                        navigateToLogin();
                    }
                } else {
                    Log.d("MainActivity", "User type not found in database");
                    navigateToLogin();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("MainActivity", "Error fetching user type: " + error.getMessage());
            }
        });
    }

    private void navigateToSuperAdminDashboard() {
        Intent intent = new Intent(MainActivity.this, SuperAdminDashboard.class);
        startActivity(intent);
        finish();  // Finish the current activity so the user can't go back to it
    }

    private void navigateToAdminDashboard() {
        Intent intent = new Intent(MainActivity.this, AdminDashboard.class);
        startActivity(intent);
        finish();  // Finish the current activity so the user can't go back to it
    }

    private void navigateToCustomerDashboard() {
        Intent intent = new Intent(MainActivity.this, Dashboard.class);
        startActivity(intent);
        finish();  // Finish the current activity so the user can't go back to it
    }

    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();  // Finish the current activity so the user can't go back to it
    }
}

package com.example.fleursonthego.views;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.fleursonthego.InputPhoneNumberActivity;
import com.example.fleursonthego.R;
import com.example.fleursonthego.views.Dashboard; // Import your Dashboard activity
import com.example.fleursonthego.views.LoginActivity; // Import your Login activity
import com.example.fleursonthego.Models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;


public class IntroActivity extends AppCompatActivity {

    // UI components
    Button getStartedButton;
    TextView txt_navigate_to_login;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted ->{
                if (isGranted){
                    // FCM SDK (and your app) can post notifications.

                    fetchFCMToken();
                }else{
                    // TODO: Inform user that that your app will not show notifications.
                }
            }
    );
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_intro);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference("users");

        // Retrieve FCM token


        // Check if the user is already signed in
        if (mAuth.getCurrentUser() != null) {
            checkUserTypeAndNavigate();
            fetchFCMToken();
        }

        getStartedButton = findViewById(R.id.getStartedButton);
        txt_navigate_to_login = findViewById(R.id.txt_to_login);

        getStartedButton.setOnClickListener(v -> navigateToMain());
        txt_navigate_to_login.setOnClickListener(v -> navigateToLogin());
        requestPermission();
    }

    // Fetches FCM Token
    private void fetchFCMToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.w("FCM Token", "Fetching FCM registration token failed", task.getException());
                return;
            }

            // Get the new FCM registration token
            String token = task.getResult();
            Log.d("FCM Token", "Token: " + token);

            // Get the current logged-in user's UID from Firebase Authentication
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            // Store the FCM token in the Firebase Realtime Database under the user's UID
            if (uid != null) {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
                databaseReference.child(uid).child("fcmToken").setValue(token)
                        .addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                Log.d("FCM Token", "Token saved to database");
                            } else {
                                Log.w("FCM Token", "Failed to save token", task1.getException());
                            }
                        });
            } else {
                Log.w("FCM Token", "No user is logged in, cannot store token");
            }
        });
    }

    // Send FCM token to server
    private void sendTokenToServer(String token) {
        // Add your logic to send the token to your server here
    }

    // Check user type and navigate accordingly
    private void checkUserTypeAndNavigate() {
        String userId = mAuth.getCurrentUser().getUid();
        userRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    String userType = user.getUserType();
                    Intent intent;

                    if ("superAdmin".equals(userType)) {
                        intent = new Intent(IntroActivity.this, SuperAdminDashboard.class);
                    } else if ("admin".equals(userType)) {
                        intent = new Intent(IntroActivity.this, AdminDashboard.class);
                    } else {
                        intent = new Intent(IntroActivity.this, Dashboard.class);
                    }

                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle potential error in fetching data from Firebase
            }
        });
    }

    private void navigateToMain() {
        Intent intent = new Intent(IntroActivity.this, InputPhoneNumberActivity.class);
        startActivity(intent);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(IntroActivity.this, LoginActivity.class);
        startActivity(intent);
    }


    public void requestPermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED){

            }else if(shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)){

            }else{
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }else{

        }
    }
}

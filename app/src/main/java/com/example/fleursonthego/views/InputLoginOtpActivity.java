package com.example.fleursonthego.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fleursonthego.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class InputLoginOtpActivity extends AppCompatActivity {
    private Button nextButton;
    private EditText otpCode;
    private TextView phoneNumberPrompt;
    private ProgressBar otpProgressBar;
    private String phoneNumber;
    private String verificationId; // Store the verification ID
    private FirebaseAuth mAuth;
    private DatabaseReference usersDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_otp);

        // Initialize Firebase Auth and Database Reference
        mAuth = FirebaseAuth.getInstance();
        usersDatabase = FirebaseDatabase.getInstance().getReference("users");

        initializeUIComponents();
        retrieveDataFromIntent();
        displayPhoneNumberPrompt();

        nextButton.setOnClickListener(v -> verifyOtpCode());
    }

    private void retrieveDataFromIntent() {
        phoneNumber = Objects.requireNonNull(getIntent().getExtras()).getString("phoneNumber");
        verificationId = getIntent().getExtras().getString("verificationId"); // Get the verification ID
    }

    private void displayPhoneNumberPrompt() {
        phoneNumberPrompt.setText(getString(R.string.the_otp_has_been_sent_to_your_phone_number) + phoneNumber);
        Toast.makeText(getApplicationContext(), phoneNumber, Toast.LENGTH_SHORT).show(); // Debugging purpose
    }

    private void initializeUIComponents() {
        nextButton = findViewById(R.id.btn_next);
        otpCode = findViewById(R.id.txt_otp_code);
        phoneNumberPrompt = findViewById(R.id.txt_number_prompt);
        otpProgressBar = findViewById(R.id.otp_progressBar);
    }

    private void verifyOtpCode() {
        String code = otpCode.getText().toString().trim();
        if (code.isEmpty() || code.length() < 6) {
            otpCode.setError("Please enter a valid OTP");
            otpCode.requestFocus();
            return;
        }

        otpProgressBar.setVisibility(View.VISIBLE); // Show progress bar
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    otpProgressBar.setVisibility(View.GONE); // Hide progress bar
                    if (task.isSuccessful()) {
                        // OTP verification successful
                        Toast.makeText(InputLoginOtpActivity.this, "OTP verified successfully", Toast.LENGTH_SHORT).show();
                        checkUserRole(); // Check the user role after successful OTP verification
                    } else {
                        // OTP verification failed
                        Toast.makeText(InputLoginOtpActivity.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUserRole() {
        String currentUserId = mAuth.getCurrentUser().getUid();
        usersDatabase.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String userType = dataSnapshot.child("userType").getValue(String.class);

                    // Navigate based on user role
                    Intent intent;
                    if ("superAdmin".equals(userType)) {
                        intent = new Intent(InputLoginOtpActivity.this, SuperAdminDashboard.class);
                    } else if ("admin".equals(userType)) {
                        intent = new Intent(InputLoginOtpActivity.this, AdminDashboard.class);
                    } else {
                        intent = new Intent(InputLoginOtpActivity.this, Dashboard.class); // Default user dashboard
                    }

                    // Clear the activity stack
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish(); // Close the OTP activity
                } else {
                    Toast.makeText(InputLoginOtpActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(InputLoginOtpActivity.this, "Failed to retrieve user data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

}

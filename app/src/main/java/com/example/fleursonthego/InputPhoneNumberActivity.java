package com.example.fleursonthego;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fleursonthego.views.InputOtpActivity;
import com.google.firebase.auth.PhoneAuthCredential;
import com.hbb20.CountryCodePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.FirebaseException;

import java.util.concurrent.TimeUnit;

public class InputPhoneNumberActivity extends AppCompatActivity {

    private Button sendPhoneNumberButton;
    private EditText txt_mobileNum;
    private CountryCodePicker ccp_countryCode;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private DatabaseReference usersDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.input_phone_number_activity);

        // Initialize all UI components
        initializeUIComponents();

        // Initialize Firebase Auth and Database reference
        mAuth = FirebaseAuth.getInstance();
        usersDatabaseReference = FirebaseDatabase.getInstance().getReference("users");

        // Set click listener for the button
        sendPhoneNumberButton.setOnClickListener(view -> {
            // Validate the entered phone number
            if (!ccp_countryCode.isValidFullNumber()) {
                txt_mobileNum.setError("Phone number is not valid");
                return;
            }

            String phoneNumber = ccp_countryCode.getFullNumberWithPlus();

            // Check if phone number exists in Firebase Authentication or database
            checkPhoneNumberExistence(phoneNumber);
        });
    }

    /**
     * Method to initialize the UI components like buttons, text fields, and progress bar.
     */
    private void initializeUIComponents() {
        sendPhoneNumberButton = findViewById(R.id.btn_sendOtp); // Can rename this button in your layout if OTP is removed
        txt_mobileNum = findViewById(R.id.txt_mobileNum);
        ccp_countryCode = findViewById(R.id.ccp_countryCode);
        progressBar = findViewById(R.id.pb_phoneNumPage);

        // Initially hide the progress bar
        progressBar.setVisibility(View.GONE);

        // Link the country code picker with the mobile number text field
        ccp_countryCode.registerCarrierNumberEditText(txt_mobileNum);
    }

    /**
     * Method to check if the phone number exists in Firebase Authentication or in the users' node.
     * @param phoneNumber The phone number entered by the user.
     */
    private void checkPhoneNumberExistence(final String phoneNumber) {
        setInProgress(true);

        // Check if the phone number exists in Firebase Realtime Database
        checkPhoneNumberInDatabase(phoneNumber);
    }

    /**
     * Method to check if the phone number exists in the Firebase Realtime Database.
     * @param phoneNumber The phone number entered by the user.
     */
    private void checkPhoneNumberInDatabase(String phoneNumber) {
        usersDatabaseReference.orderByChild("phoneNumber").equalTo(phoneNumber)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Phone number already exists in the database
                            Toast.makeText(InputPhoneNumberActivity.this, "Phone number is already in use.", Toast.LENGTH_SHORT).show();
                        } else {
                            // Proceed to send OTP since phone number is available
                            sendOtp(phoneNumber);
                        }
                        setInProgress(false);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(InputPhoneNumberActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        setInProgress(false);
                    }
                });
    }

    /**
     * Method to manage the UI when an action is in progress (like sending phone number).
     * It shows or hides the progress bar based on the progress state.
     * @param inProgress Boolean indicating whether to show or hide progress.
     */
    private void setInProgress(boolean inProgress) {
        if (inProgress) {
            // Hide the button and show the progress bar
            sendPhoneNumberButton.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            // Show the button and hide the progress bar
            sendPhoneNumberButton.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }
    }

    /**
     * Method to send OTP using Firebase Authentication for phone number verification
     * @param phoneNumber The phone number entered by the user.
     */
    private void sendOtp(final String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity for callback
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        // Handle successful verification if needed
                        Toast.makeText(InputPhoneNumberActivity.this, "Phone number verified!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(InputPhoneNumberActivity.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        setInProgress(false);
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        // Store verificationId and token to use later
                        Intent intent = new Intent(InputPhoneNumberActivity.this, InputOtpActivity.class);
                        intent.putExtra("phoneNumber", phoneNumber);
                        intent.putExtra("verificationId", verificationId);
                        startActivity(intent);
                        setInProgress(false);
                    }
                });
    }

    /**
     * Method to navigate to the next activity, passing the phone number.
     * @param phoneNumber The phone number entered by the user.
     */
    private void navigateToNextActivity(@NonNull String phoneNumber) {
        // Show a toast for debugging or move to the next activity
        Toast.makeText(InputPhoneNumberActivity.this, "Phone Number: " + phoneNumber, Toast.LENGTH_SHORT).show();

        // Navigate to the next activity (e.g., registration)
        Intent intent = new Intent(InputPhoneNumberActivity.this, InputOtpActivity.class);
        intent.putExtra("phoneNumber", phoneNumber);
        startActivity(intent);
    }
}

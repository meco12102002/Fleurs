package com.example.fleursonthego.views;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fleursonthego.R;
import com.hbb20.CountryCodePicker; // Import the CountryCodePicker
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText editPhoneNumber, editPassword, editConfirmPassword, editOtp;
    private TextView textResendOtp;
    private Button btnResetPassword, btnSendOtp;
    private CountryCodePicker countryCodePicker; // Declare the CountryCodePicker

    private FirebaseAuth mAuth;
    private String verificationId;
    private boolean isOtpSent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        initializeViews();
        mAuth = FirebaseAuth.getInstance();

        btnSendOtp.setOnClickListener(v -> {
            if (!isOtpSent) {
                sendOtp();
            }
        });

        textResendOtp.setOnClickListener(v -> {
            if (isOtpSent) {
                sendOtp();
            }
        });

        btnResetPassword.setOnClickListener(v -> {
            String password = editPassword.getText().toString();
            String confirmPassword = editConfirmPassword.getText().toString();
            String otp = editOtp.getText().toString();

            if (validateInputs(password, confirmPassword, otp)) {
                verifyOtpAndResetPassword(otp, password);
            }
        });
    }

    private void initializeViews() {
        countryCodePicker = findViewById(R.id.countryCodePicker); // Initialize the CountryCodePicker
        editPhoneNumber = findViewById(R.id.editPhoneNumber);
        editPassword = findViewById(R.id.editPassword);
        editConfirmPassword = findViewById(R.id.editConfirmPassword);
        editOtp = findViewById(R.id.editOtp);
        textResendOtp = findViewById(R.id.textResendOtp);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        btnSendOtp = findViewById(R.id.btnSendOtp);
    }

    private void sendOtp() {
        String phoneNumber = editPhoneNumber.getText().toString().trim();
        String fullPhoneNumber = countryCodePicker.getSelectedCountryCodeWithPlus() + phoneNumber; // Get the full phone number with country code

        if (!TextUtils.isEmpty(phoneNumber)) {
            PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                    .setPhoneNumber(fullPhoneNumber)  // Phone number to send the OTP
                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout duration
                    .setActivity(this)                 // Activity for the callback binding
                    .setCallbacks(otpCallbacks)        // OnVerificationStateChangedCallbacks
                    .build();
            PhoneAuthProvider.verifyPhoneNumber(options);

            startOtpCountdown(); // Start the countdown for resend OTP
        } else {
            Toast.makeText(this, "Please enter your phone number.", Toast.LENGTH_SHORT).show();
        }
    }

    private void startOtpCountdown() {
        isOtpSent = true;
        new CountDownTimer(60000, 1000) {
            public void onTick(long millisUntilFinished) {
                textResendOtp.setText(String.format("Resend OTP in %d seconds", millisUntilFinished / 1000));
            }

            public void onFinish() {
                textResendOtp.setText("Resend OTP");
                isOtpSent = false;
            }
        }.start();
    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks otpCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                    String code = credential.getSmsCode();
                    if (code != null) {
                        editOtp.setText(code);  // Auto-fill the OTP field
                        verifyOtpAndResetPassword(code, editPassword.getText().toString());
                    }
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    Toast.makeText(ForgotPasswordActivity.this, "OTP verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    ForgotPasswordActivity.this.verificationId = verificationId;
                    Toast.makeText(ForgotPasswordActivity.this, "OTP sent successfully.", Toast.LENGTH_SHORT).show();
                }
            };

    private boolean validateInputs(String password, String confirmPassword, String otp) {
        if (TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword) || TextUtils.isEmpty(otp)) {
            Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void verifyOtpAndResetPassword(String otp, String newPassword) {
        if (verificationId != null) {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
            mAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    updatePassword(newPassword);
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "Invalid OTP.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Verification ID is null.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePassword(String newPassword) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());

            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                    user.updatePassword(newPassword).addOnCompleteListener(updateTask -> {
                        if (updateTask.isSuccessful()) {
                            String hashedPassword = hashPassword(newPassword);
                            if (hashedPassword != null) {
                                userRef.child("password").setValue(hashedPassword).addOnCompleteListener(dbTask -> {
                                    if (dbTask.isSuccessful()) {
                                        Toast.makeText(ForgotPasswordActivity.this, "Password updated successfully.", Toast.LENGTH_SHORT).show();
                                        finish(); // Close activity on success
                                    } else {
                                        Toast.makeText(ForgotPasswordActivity.this, "Failed to update password in database.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                Toast.makeText(ForgotPasswordActivity.this, "Failed to hash password.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(ForgotPasswordActivity.this, "Failed to update password.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "User not found in the database.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}

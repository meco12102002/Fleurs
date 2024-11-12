package com.example.fleursonthego.views;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fleursonthego.InputPhoneNumberActivity;
import com.example.fleursonthego.R;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

import utils.AndroidUtils;
import utils.HashUtils; // Create a utility for hashing

public class LoginActivity extends AppCompatActivity {

    private EditText txtPhoneNum, txtPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private TextView forgotPass;
    private CountryCodePicker ccpCountryCode;
    private FirebaseAuth mAuth;
    private String otp;
    private DatabaseReference userDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        initializeUI();
        ccpCountryCode.registerCarrierNumberEditText(txtPhoneNum);

        // Set up password visibility toggle
        setupPasswordToggle();

        findViewById(R.id.signUpTextView).setOnClickListener(v -> navigateToSignUp());
        btnLogin.setOnClickListener(v -> {
            if (ccpCountryCode.isValidFullNumber()) {
                login(); // Call login to verify credentials first
            } else {
                txtPhoneNum.setError("Phone number is not valid");
            }
        });

        forgotPass.setOnClickListener(v -> {
            startActivity(new Intent(this, ForgotPasswordActivity.class));
        });
    }

    private void initializeUI() {
        txtPhoneNum = findViewById(R.id.txt_phone_num);
        txtPassword = findViewById(R.id.passwordEditText);
        btnLogin = findViewById(R.id.loginButton);
        progressBar = findViewById(R.id.progressBar);
        ccpCountryCode = findViewById(R.id.ccp_countryCode_login);
        mAuth = FirebaseAuth.getInstance();
        forgotPass = findViewById(R.id.forgotPasswordTextView);
        userDatabaseRef = FirebaseDatabase.getInstance().getReference("users"); // Adjust the reference according to your database structure
    }

    private void setupPasswordToggle() {
        ImageView togglePasswordVisibility = findViewById(R.id.showPasswordImageView);
        togglePasswordVisibility.setOnClickListener(v -> {
            if (txtPassword.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                txtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                togglePasswordVisibility.setImageResource(R.drawable.ic_eye_on); // Set drawable for visible state
            } else {
                txtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                togglePasswordVisibility.setImageResource(R.drawable.ic_eye_off); // Set drawable for hidden state
            }
            // Move the cursor to the end of the text
            txtPassword.setSelection(txtPassword.length());
        });
    }

    private void setInProgress(boolean inProgress) {
        btnLogin.setVisibility(inProgress ? View.GONE : View.VISIBLE);
        progressBar.setVisibility(inProgress ? View.VISIBLE : View.GONE);
    }

    private void login() {
        String phoneNumber = ccpCountryCode.getFullNumberWithPlus();
        String password = txtPassword.getText().toString().trim();

        // Fetch user data based on phone number
        userDatabaseRef.orderByChild("phoneNumber").equalTo(phoneNumber)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                String hashedPassword = userSnapshot.child("password").getValue(String.class);
                                if (hashedPassword != null && HashUtils.verifyPassword(password, hashedPassword)) {
                                    // Password matches, proceed with OTP
                                    Toast.makeText(LoginActivity.this, "Verify identity in the next step", Toast.LENGTH_SHORT).show();
                                    sendOtp(phoneNumber); // Send OTP after successful password verification
                                    return; // Exit loop after finding and verifying user
                                } else {
                                    // Password does not match
                                    Toast.makeText(LoginActivity.this, "Incorrect password", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            // User not found
                            Toast.makeText(LoginActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                        }
                        setInProgress(false);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(LoginActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        setInProgress(false);
                    }
                });
    }

    private void sendOtp(String phoneNumber) {
        setInProgress(true);
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(mCallbacks)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void navigateToOtpActivity(String phoneNumber) {
        Intent intent = new Intent(this, InputLoginOtpActivity.class);
        intent.putExtra("phoneNumber", phoneNumber);
        intent.putExtra("verificationId", otp); // Pass the OTP verification ID
        startActivity(intent);
        finish(); // Optional: Close the login activity
    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onCodeSent(@NonNull String verificationId,
                                       @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    otp = verificationId; // Store the verification ID
                    navigateToOtpActivity(ccpCountryCode.getFullNumberWithPlus()); // Navigate to OTP activity
                }

                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                    String code = credential.getSmsCode();
                    if (code != null) {
                        AndroidUtils.showToast(LoginActivity.this, "OTP received");
                    }
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    Toast.makeText(LoginActivity.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    setInProgress(false);
                }
            };

    private void navigateToSignUp() {
        startActivity(new Intent(this, InputPhoneNumberActivity.class));
    }
}

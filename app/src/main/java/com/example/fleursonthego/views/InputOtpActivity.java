package com.example.fleursonthego.views;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fleursonthego.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.Objects;

import utils.AndroidUtils;

public class InputOtpActivity extends AppCompatActivity {
    private Button nextButton;
    private TextView otpCode;
    private TextView phoneNumberPrompt;
    private TextView resendOtp;
    private ProgressBar otpProgressBar;
    private String phoneNumber;
    private Long timeoutSeconds = 60L;
    private FirebaseAuth mAuth;
    private String verificationId; // Store the verification ID sent by Firebase
    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_otp_register);

        // Initialize UI components
        initializeUIComponents();

        // Initialize Firebase Authentication instance
        mAuth = FirebaseAuth.getInstance();

        // Get the phone number passed from the previous activity
        retrievePhoneNumberFromIntent();

        // Display the prompt with the phone number
        displayPhoneNumberPrompt();

        // Send OTP to the phone number
        sendOtp(phoneNumber,false);

        // Set up click listener for the next button
        setNextButtonClickListener();

        resendOtp.setOnClickListener(v -> resendOtp());


    }

    private void initializeUIComponents() {
        nextButton = findViewById(R.id.btn_next);
        otpCode = findViewById(R.id.txt_otp_code);
        phoneNumberPrompt = findViewById(R.id.txt_number_prompt);
        otpProgressBar = findViewById(R.id.otp_progressBar);
        resendOtp = findViewById(R.id.txt_resend_otp);
    }

    private void retrievePhoneNumberFromIntent() {
        phoneNumber = Objects.requireNonNull(getIntent().getExtras()).getString("phoneNumber");
    }

    private void displayPhoneNumberPrompt() {
        phoneNumberPrompt.setText(getString(R.string.the_otp_has_been_sent_to_your_phone_number) + phoneNumber);
        Toast.makeText(getApplicationContext(), phoneNumber, Toast.LENGTH_SHORT).show(); // Debugging purpose
    }

    private void setNextButtonClickListener() {
        nextButton.setOnClickListener(view -> {
            String enteredOtp = otpCode.getText().toString().trim();
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, enteredOtp);
            signIn(credential);
        });
    }

    private void navigateToRegisterActivity() {
        setInProgress(false);
        Intent intent = new Intent(InputOtpActivity.this, RegisterInputActivity.class);
        intent.putExtra("PHONE_NUMBER", phoneNumber); // Pass the phone number to RegisterInputActivity
        startActivity(intent);
        finish(); // Close InputOtpActivity to prevent going back to it
    }

    /**
     * Method to trigger sending an OTP to the provided phone number.
     * Firebase is used to send OTP and manage the verification process.
     */
    private void sendOtp(@NonNull String phoneNumber,boolean isResend) {
        // Show the progress bar while sending OTP
        setInProgress(true);
        startResendTimer();

        // Build the PhoneAuthOptions with required parameters like phone number and timeout
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)         // Phone number to verify
                        .setTimeout(timeoutSeconds, TimeUnit.SECONDS)   // Timeout for OTP verification
                        .setActivity(this)                   // Bind activity for callbacks
                        .setCallbacks(mCallbacks)            // Callbacks for verification
                        .build();
        // Send the OTP using Firebase's verifyPhoneNumber method
        PhoneAuthProvider.verifyPhoneNumber(options);
    }
    private void resendOtp() {
        // Reset the verification ID to ensure a new OTP is requested
        verificationId = null;

        // Resend the OTP to the phone number
        sendOtp(phoneNumber, true);

        // Show a toast to indicate that a new OTP has been sent
        Toast.makeText(this, "New OTP sent", Toast.LENGTH_SHORT).show();
    }
    private void startResendTimer() {
        resendOtp.setEnabled(false);
        timeoutSeconds = 60L; // Reset timeout to 60 seconds
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    if (timeoutSeconds > 0) {
                        resendOtp.setText(String.format("Resend OTP in %d seconds", timeoutSeconds));
                        timeoutSeconds--;
                    } else {
                        resendOtp.setEnabled(true);
                        resendOtp.setText("Resend OTP");
                        timer.cancel();
                    }
                });
            }
        }, 0, 1000); // Schedule the task to run every second
    }

    /**
     * Method to manage the UI when an action is in progress (like sending OTP).
     * It shows or hides the progress bar based on the progress state.
     * @param inProgress Boolean indicating whether to show or hide progress.
     */
    private void setInProgress(boolean inProgress) {
        if (inProgress) {
            // Hide the send OTP button and show the progress bar
            nextButton.setVisibility(View.GONE);
            otpProgressBar.setVisibility(View.VISIBLE);
        } else {
            // Show the send OTP button and hide the progress bar
            nextButton.setVisibility(View.VISIBLE);
            otpProgressBar.setVisibility(View.GONE);
        }
    }

    /**
     * Callbacks for phone number verification events like OTP sent, verification completed, or failed.
     * This handles events during the OTP verification process.
     */
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    super.onCodeSent(verificationId, token);
                    // Save the verification ID for later use
                    InputOtpActivity.this.verificationId = verificationId;
                    setInProgress(false);
                    // Log and display a message indicating that OTP was sent
                    Log.d("OTP Verification", "Verification code sent: " + verificationId);
                    Toast.makeText(InputOtpActivity.this, "Verification code sent", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                    // Auto-retrieved OTP code if available
                    String code = phoneAuthCredential.getSmsCode();
                    if (code != null) {
                        // Display the OTP for informational purposes
                        Toast.makeText(InputOtpActivity.this, "OTP received: " + code, Toast.LENGTH_SHORT).show();
                        // Attempt sign-in with the received credential
                        signIn(phoneAuthCredential);
                    }
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    // Display error message if OTP verification fails

                    Toast.makeText(InputOtpActivity.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    // Hide the progress bar and show the button again

                }
            };

    /**
     * Method to sign in using the phone authentication credential.
     * This is called when OTP verification is successful.
     * @param credential The PhoneAuthCredential obtained after OTP verification.
     */
    private void signIn(@NonNull PhoneAuthCredential credential) {
        // Handle successful sign-in logic here
        setInProgress(true);
        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    AndroidUtils.showToast(getApplicationContext(), "OTP Verification Success!");
                    navigateToRegisterActivity();
                } else {
                    setInProgress(false);
                    AndroidUtils.showToast(getApplicationContext(), "OTP Verification Failed");
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel the timer if it is still running
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
}

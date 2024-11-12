package com.example.fleursonthego.views;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fleursonthego.Models.User;
import com.example.fleursonthego.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

import utils.FirebaseUtil;

public class RegisterInputActivity extends AppCompatActivity {

    private EditText etFullName, etPassword, etConfirmPassword,txtemail;
    private Button btnRegister;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private String phoneNumber;
    private static final String DEFAULT_USER_TYPE = "customer";
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;
    private ImageView passwordToggle, confirmPasswordToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_input);

        initializeFirebase();
        initializeUI();
    }

    // Initialize Firebase components
    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
    }

    // Set up UI elements and listeners
    private void initializeUI() {
        etFullName = findViewById(R.id.txt_fullName);
        etPassword = findViewById(R.id.txt_password);
        etConfirmPassword = findViewById(R.id.txt_confirmPassword);
        passwordToggle = findViewById(R.id.password_toggle);
        confirmPasswordToggle = findViewById(R.id.confirm_password_toggle);
        btnRegister = findViewById(R.id.btn_register);
        txtemail = findViewById(R.id.txt_email);

        phoneNumber = getIntent().getStringExtra("PHONE_NUMBER");

        passwordToggle.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            togglePasswordVisibility(etPassword, passwordToggle, isPasswordVisible);
        });

        confirmPasswordToggle.setOnClickListener(v -> {
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
            togglePasswordVisibility(etConfirmPassword, confirmPasswordToggle, isConfirmPasswordVisible);
        });

        btnRegister.setOnClickListener(v -> registerUser());
    }

    // Toggle password visibility
    private void togglePasswordVisibility(EditText passwordField, ImageView toggleIcon, boolean isVisible) {
        if (isVisible) {
            passwordField.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            toggleIcon.setImageResource(R.drawable.ic_eye_on); // Replace with your "eye open" icon
        } else {
            passwordField.setTransformationMethod(PasswordTransformationMethod.getInstance());
            toggleIcon.setImageResource(R.drawable.ic_eye_off); // Replace with your "eye closed" icon
        }
        passwordField.setSelection(passwordField.getText().length());
    }

    // Handle user registration
    private void registerUser() {
        String fullName = etFullName.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String email = txtemail.getText().toString().trim();
        String userType = DEFAULT_USER_TYPE;

        if (!validateInput(fullName, phoneNumber, password, confirmPassword)) return;

        String hashedPassword = hashPassword(password);
        User user = createUser(fullName, email, phoneNumber, userType, hashedPassword);
        storeUserData(user);
    }

    // Validate user input fields
    private boolean validateInput(String fullName, String phoneNumber, String password, String confirmPassword) {
        if (fullName.isEmpty()) {
            showToast("Full name is required.");
            return false;
        }
        if (phoneNumber == null || phoneNumber.isEmpty() || phoneNumber.length() < 10) { // Customize as needed
            showToast("A valid phone number is required.");
            return false;
        }
        if (!isPasswordStrong(password)) {
            showToast("Password must be at least 8 characters, include uppercase, lowercase, a number, and a special character.");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            showToast("Passwords do not match.");
            return false;
        }
        return true;
    }

    // Check if password is strong
    private boolean isPasswordStrong(String password) {
        Pattern passwordPattern = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$");
        return passwordPattern.matcher(password).matches();
    }

    // Create a User object
    private User createUser(String fullName, String email, String phoneNumber, String userType, String password) {
        User user = new User();
        user.setName(fullName);
        user.setEmail(email);
        user.setPhoneNumber(phoneNumber);
        user.setUserType(userType);
        user.setPassword(password); // Set the hashed password
        return user;
    }

    // Hash the password using SHA-256
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
            throw new RuntimeException(e);
        }
    }

    // Store user data in Firebase Database
    // Modify the storeUserData method to check for duplicates
    private void storeUserData(User user) {
        Query query = databaseReference.orderByChild("phoneNumber").equalTo(user.getPhoneNumber());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean userExists = false;

                // Check if any user with the same phone number, email, or name exists
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User existingUser = userSnapshot.getValue(User.class);

                    if (existingUser != null &&
                            (existingUser.getEmail().equals(user.getEmail()) ||
                                    existingUser.getName().equals(user.getName()))) {
                        userExists = true;
                        break;
                    }
                }

                if (!userExists) {
                    // No duplicate user found, proceed with registration
                    DatabaseReference userRef = databaseReference.child(mAuth.getCurrentUser().getUid());
                    userRef.setValue(user).addOnCompleteListener(task -> onUserRegistrationComplete(task));
                } else {
                    showToast("User with this phone number, email, or name already exists.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToast("Database error: " + error.getMessage());
            }
        });
    }

    // Handle the registration result
    private void onUserRegistrationComplete(@NonNull Task<Void> task) {
        if (task.isSuccessful()) {
            showToast("User registered successfully!");
            navigateToDashboard();
        } else {
            showToast("Registration failed: " + task.getException().getMessage());
        }
    }

    // Show toast message
    private void showToast(String message) {
        Toast.makeText(RegisterInputActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    // Navigate to the Dashboard activity
    private void navigateToDashboard() {
        startActivity(new Intent(RegisterInputActivity.this, Dashboard.class));
        finish();
    }
}

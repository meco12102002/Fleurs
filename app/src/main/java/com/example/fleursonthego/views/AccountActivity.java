package com.example.fleursonthego.views;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fleursonthego.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import utils.CircleImageTransformation;
import utils.CircleImageTransformation2;

public class AccountActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 100; // Request code for image picking
    private static final int TAKE_PHOTO = 101; // Request code for taking photo
    private FirebaseAuth mAuth;
    private DatabaseReference db;
    private StorageReference storageRef; // Reference for Firebase Storage
    private TextView textName;
    private TextView textPhone;
    private ImageView imageProfile;
    private Button btnLogout;

    private Uri imageUri; // To hold the image URI

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        initializeFirebase();
        initializeViews();
        loadUserProfile();
        setupLogoutButton();
        setupImageProfileClick(); // Set up click listener for image profile


        TextView textMyOrders = findViewById(R.id.textMyOrders);

        // Set an OnClickListener to navigate to MyOrders Activity
        textMyOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to open the MyOrders activity
                Intent intent = new Intent(AccountActivity.this, MyOrdersActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance().getReference(); // Get reference to the database
        storageRef = FirebaseStorage.getInstance().getReference(); // Get reference to Firebase Storage
    }

    private void initializeViews() {
        textName = findViewById(R.id.textName);
        textPhone = findViewById(R.id.textPhone);
        imageProfile = findViewById(R.id.imageProfile);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            textPhone.setText(getPhoneNumber(currentUser));

            // Retrieve user data from the Firebase Realtime Database
            DatabaseReference userRef = db.child("users").child(currentUser.getUid());
            userRef.addListenerForSingleValueEvent(new UserProfileValueEventListener());
        }
    }

    private String getPhoneNumber(FirebaseUser user) {
        String phoneNumber = user.getPhoneNumber();
        return phoneNumber != null ? phoneNumber : "No phone number";
    }

    private class UserProfileValueEventListener implements ValueEventListener {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
                String userName = dataSnapshot.child("name").getValue(String.class);
                textName.setText(userName != null ? userName : "No name available");
                loadProfileImage(dataSnapshot);
            } else {
                showToast("User data does not exist");
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            showToast("Failed to load user data: " + databaseError.getMessage());
        }

        private void loadProfileImage(DataSnapshot dataSnapshot) {
            String profileImageUrl = dataSnapshot.child("profileImage").getValue(String.class);
            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                // Use Picasso with the custom CircleImageTransformation
                Picasso.get()
                        .load(profileImageUrl)
                        .into(imageProfile);
            } else {
                imageProfile.setImageResource(R.drawable.womanboquet);
            }
        }
    }

    private void setupLogoutButton() {
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            showToast("Logged out successfully");
            redirectToLogin();
        });
    }

    private void redirectToLogin() {
        Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(AccountActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void setupImageProfileClick() {
        imageProfile.setOnClickListener(v -> {
            // Open a dialog to select image source
            showImageSourceOptions();
        });
    }

    private void showImageSourceOptions() {
        // Here you can show a dialog to choose between Camera and Gallery
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE && data != null) {
                imageUri = data.getData();
                updateProfileImage();
            } else if (requestCode == TAKE_PHOTO && data != null) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                // Handle taking photo and convert Bitmap to Uri
                // Implement method to save Bitmap and upload to Firebase
            }
        }
    }

    private void updateProfileImage() {
        if (imageUri != null) {
            imageProfile.setImageURI(imageUri);
            // Upload the image to Firebase Storage and get the download URL
            uploadImageToFirebaseStorage(imageUri);
        }
    }

    private void uploadImageToFirebaseStorage(Uri imageUri) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            StorageReference fileReference = storageRef.child("profileImages/" + currentUser.getUid() + ".jpg");
            fileReference.putFile(imageUri).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    fileReference.getDownloadUrl().addOnCompleteListener(urlTask -> {
                        if (urlTask.isSuccessful()) {
                            Uri downloadUri = urlTask.getResult();
                            // Store the download URL in Firebase Realtime Database
                            updateDatabaseWithImageUrl(downloadUri.toString());
                        } else {
                            showToast("Failed to get download URL");
                        }
                    });
                } else {
                    showToast("Failed to upload image");
                }
            });
        }
    }

    private void updateDatabaseWithImageUrl(String imageUrl) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            DatabaseReference userRef = db.child("users").child(currentUser.getUid());
            userRef.child("profileImage").setValue(imageUrl).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    showToast("Profile image updated successfully");
                } else {
                    showToast("Failed to update profile image URL");
                }
            });
        }
    }
}

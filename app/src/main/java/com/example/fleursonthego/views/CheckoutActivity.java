package com.example.fleursonthego.views;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fleursonthego.Adapters.OrderSummaryAdapter;
import com.example.fleursonthego.FCMNotificationHelper;
import com.example.fleursonthego.FCMNotificationService;
import com.example.fleursonthego.Models.CartItem;
import com.example.fleursonthego.Models.Order;
import com.example.fleursonthego.Models.SmsSender;
import com.example.fleursonthego.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hbb20.CountryCodePicker;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class CheckoutActivity extends AppCompatActivity {

    private RecyclerView orderSummaryRecyclerView;
    private OrderSummaryAdapter orderSummaryAdapter;
    private ArrayList<CartItem> selectedItems;
    private TextView selectedDateTextView;
    private String userPhoneNumber;
    private TextView totalPriceTextView;
    private ImageButton selectTimeButton;
    private EditText timeInput;
    private String userId;
    private DatabaseReference userRef;
    private Spinner locationSpinner;
    private ImageButton datePickerButton;
    private String userName;
    private Button addLocation;
    TextView selectedTimeTextView;
    private LinearLayout uploadReceiptLayout;
    private ImageView receiptImageView;
    private Button uploadReceiptButton;
    private RadioGroup paymentMethodRadioGroup;
    private static final int PICK_IMAGE_REQUEST = 1;

    private String paymentMethod;
    private int selectedPaymentId;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        initializeViews();
        setupToolbar();
        initializeFirebase();

        // Get the checked payment method id
        selectedPaymentId = paymentMethodRadioGroup.getCheckedRadioButtonId();



        datePickerButton.setOnClickListener(v -> showDatePicker());
        addLocation.setOnClickListener(v -> showLocationInputDialog());
        selectTimeButton.setOnClickListener(v -> showTimePickerDialog());
        selectedTimeTextView = findViewById(R.id.timeInput);

        Button checkoutButton = findViewById(R.id.placeOrderButton);
        checkoutButton.setOnClickListener(v -> processOrder());
    }
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Receipt"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            receiptImageView.setImageURI(imageUri); // Display the selected image
        }
    }


    private void initializeViews() {

        uploadReceiptLayout = findViewById(R.id.uploadReceiptLayout);
        receiptImageView = findViewById(R.id.receiptImageView);
        uploadReceiptButton = findViewById(R.id.uploadReceiptButton);
        paymentMethodRadioGroup = findViewById(R.id.paymentMethodRadioGroup);
        selectedDateTextView = findViewById(R.id.selectedDateTextView);
        datePickerButton = findViewById(R.id.datePickerButton);
        orderSummaryRecyclerView = findViewById(R.id.orderSummaryRecyclerView);
        orderSummaryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        totalPriceTextView = findViewById(R.id.totalPriceText);
        selectTimeButton = findViewById(R.id.timePickerButton);
        timeInput = findViewById(R.id.timeInput);
        locationSpinner = findViewById(R.id.addressSpinner);
        addLocation = findViewById(R.id.addAddressButton);

        paymentMethodRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId != -1) {
                View selectedPaymentMethodView = findViewById(checkedId);
                if (selectedPaymentMethodView != null && selectedPaymentMethodView.getTag() != null) {
                    paymentMethod = selectedPaymentMethodView.getTag().toString();
                } else {
                    paymentMethod = "Cash"; // Default or handle accordingly
                }
                Log.d(TAG, "Payment Method Changed: " + paymentMethod); // Log each time payment method changes
            } else {
                Log.e(TAG, "No payment method selected.");
                paymentMethod = "Cash"; // Default or handle accordingly
            }

            if (checkedId == R.id.gcashRadioButton) {
                uploadReceiptLayout.setVisibility(View.VISIBLE); // Show upload layout
            } else {
                uploadReceiptLayout.setVisibility(View.GONE); // Hide upload layout
                receiptImageView.setImageDrawable(null); // Clear any selected image
            }
        });

        uploadReceiptButton.setOnClickListener(v -> openFileChooser());
    }

    private void setupToolbar() {
        Toolbar checkoutToolbar = findViewById(R.id.checkoutToolbar);
        setSupportActionBar(checkoutToolbar);
        checkoutToolbar.setNavigationIcon(R.drawable.ic_left_arrow);
        checkoutToolbar.setNavigationOnClickListener(v -> {
            Log.d(TAG, "Back button clicked");
            startActivity(new Intent(CheckoutActivity.this, CartActivity.class));
            finish();
        });
    }

    private void initializeFirebase() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
            getUserDetails();
        }
        retrieveIntentData();
    }

    private void getUserDetails() {
        getUserLocation();
        getUserPhoneNumber();
        getUserName();
    }

    private void getUserName() {
        userRef.child("name").addListenerForSingleValueEvent(new UserNameValueEventListener());
    }

    private void getUserPhoneNumber() {
        userRef.child("phoneNumber").addListenerForSingleValueEvent(new PhoneNumberValueEventListener());
    }

    private void getUserLocation() {
        userRef.child("locations").addListenerForSingleValueEvent(new UserLocationValueEventListener());
    }

    private void showLocationInputDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_location_layout);
        CountryCodePicker countryCodePicker = dialog.findViewById(R.id.countryCodePicker);
        dialog.setCancelable(true);
        countryCodePicker.setDefaultCountryUsingNameCode("PH");
        EditText recipientNameEditText = dialog.findViewById(R.id.recipientNameEditText);
        EditText streetAddressEditText = dialog.findViewById(R.id.streetAddressEditText);
        EditText barangayEditText = dialog.findViewById(R.id.barangayEditText);
        EditText cityEditText = dialog.findViewById(R.id.cityEditText);
        EditText provinceEditText = dialog.findViewById(R.id.provinceEditText);
        EditText postalCodeEditText = dialog.findViewById(R.id.postalCodeEditText);
        EditText contactNumberEditText = dialog.findViewById(R.id.contactNumberEditText);
        Button saveLocationButton = dialog.findViewById(R.id.saveLocationButton);

        saveLocationButton.setOnClickListener(v -> {
            String location = String.format("%s, %s, %s, %s, %s, %s, %s",
                    recipientNameEditText.getText().toString(),
                    streetAddressEditText.getText().toString(),
                    barangayEditText.getText().toString(),
                    cityEditText.getText().toString(),
                    provinceEditText.getText().toString(),
                    postalCodeEditText.getText().toString(),
                    contactNumberEditText.getText().toString());
            if (validateLocationInput(recipientNameEditText, streetAddressEditText, barangayEditText, cityEditText, provinceEditText, postalCodeEditText, contactNumberEditText)) {
                saveUserLocation(location);
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Please fill in all fields correctly.", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private boolean validateLocationInput(EditText... fields) {
        for (EditText field : fields) {
            if (field.getText().toString().trim().isEmpty()) {
                return false; // Return false if any field is empty
            }
        }
        return true; // All fields are filled
    }

    private void saveUserLocation(String location) {
        userRef.child("locations").push().setValue(location).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Location saved successfully.");
                getUserLocation(); // Fetch updated locations
            } else {
                Log.e(TAG, "Failed to save location: ", task.getException());
            }
        });
    }

    private void updateLocationSpinner(ArrayList<String> locations) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, locations);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(adapter);
    }

    private void removeItemsFromCart() {
        DatabaseReference cartRef = userRef.child("cart");
        for (CartItem item : selectedItems) {
            cartRef.child(item.getProductId()).removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Item removed from cart: " + item.getProductName());
                } else {
                    Log.e(TAG, "Failed to remove item from cart: ", task.getException());
                }
            });
        }
    }

    private void processOrder() {
        // Retrieve the selected payment method when the user clicks the checkout button
        selectedPaymentId = paymentMethodRadioGroup.getCheckedRadioButtonId();

        // Check if a payment method is selected
        if (selectedPaymentId != -1) {
            View selectedPaymentMethodView = findViewById(selectedPaymentId);
            if (selectedPaymentMethodView != null && selectedPaymentMethodView.getTag() != null) {
                paymentMethod = selectedPaymentMethodView.getTag().toString();
            } else {
                Log.e(TAG, "Selected payment method view or tag is null.");
                paymentMethod = "Cash"; // Default or handle accordingly
            }
        } else {
            Log.e(TAG, "No payment method selected.");
            paymentMethod = "Cash"; // Default or handle accordingly
        }
        Log.d(TAG, "Selected Payment Method: " + paymentMethod);

        if (validateOrderDetails()) {
            String currentDate = new SimpleDateFormat("MMMM d, yyyy").format(new Date());
            String message = "Your order has been placed on " + currentDate + ". Total: ₱" + String.format("%.2f", getTotalPrice());
            sendSms(userPhoneNumber, message);
            FCMNotificationHelper.sendNotification(this, "Order Update", message);

            uploadReceiptAndSaveOrder();
        } else {
            Toast.makeText(this, "Please fill in all order details correctly.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateOrderDetails() {
        return !selectedDateTextView.getText().toString().isEmpty() &&
                !timeInput.getText().toString().isEmpty() &&
                locationSpinner.getSelectedItem() != null; // Ensure a location is selected
    }

    private void uploadReceiptAndSaveOrder() {
        // Check if an image is selected in receiptImageView
        if (receiptImageView.getDrawable() != null && receiptImageView.getDrawable() instanceof BitmapDrawable) {
            // Create a reference in Firebase Storage
            StorageReference storageRef = FirebaseStorage.getInstance().getReference("receipts").child(userId + "_" + System.currentTimeMillis());

            // Convert the image to a byte array
            receiptImageView.setDrawingCacheEnabled(true);
            receiptImageView.buildDrawingCache();
            Bitmap bitmap = ((BitmapDrawable) receiptImageView.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] data = baos.toByteArray();

            // Upload the image to Firebase Storage
            UploadTask uploadTask = storageRef.putBytes(data);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                // Retrieve the download URL after a successful upload
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String receiptUrl = uri.toString();
                    // Proceed with saving the order with the receipt URL
                    saveOrder(receiptUrl);
                }).addOnFailureListener(e -> Log.e(TAG, "Failed to get download URL", e));
            }).addOnFailureListener(e -> Log.e(TAG, "Failed to upload receipt image", e));
        } else {
            // Handle order save without a receipt
            saveOrder(null);  // Pass null if no receipt image is available
        }
    }


    private void showRedirectDialog() {
        // Create an AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(CheckoutActivity.this);
        builder.setMessage("What would you like to do next?")
                .setCancelable(false)
                .setPositiveButton("Go to My Orders", (dialog, id) -> {
                    // Redirect to My Orders activity
                    Intent intent = new Intent(CheckoutActivity.this, MyOrdersActivity.class);
                    startActivity(intent);
                    finish(); // Optionally close the CheckoutActivity
                })
                .setNegativeButton("Keep Shopping", (dialog, id) -> {
                    // Redirect to All Products activity
                    Intent intent = new Intent(CheckoutActivity.this, AllProductsActivity.class);
                    startActivity(intent);
                    finish(); // Optionally close the CheckoutActivity
                });

        // Create and show the dialog
        AlertDialog alert = builder.create();
        alert.show();
    }
    private void saveOrder(String receiptUrl) {

        double totalPrice = getTotalPrice();
        String deliveryDate = selectedDateTextView.getText().toString();
        String deliveryTime = timeInput.getText().toString();
        String location = locationSpinner.getSelectedItem().toString();

        // Generate the orderId first before creating the order object
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("orders");
        String orderId = ordersRef.push().getKey();

        if (orderId != null) {
            // Create the Order object with the orderId as the first argument
            Order order = new Order(orderId, userId, selectedItems, deliveryDate, deliveryTime, totalPrice, location, "Order Placed", userName, paymentMethod, receiptUrl);

            // Save the order to the database under the current user's UID and orderId
            ordersRef.child(userId).child(orderId).setValue(order).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Order saved successfully.");
                    showRedirectDialog();
                    Toast.makeText(CheckoutActivity.this, "Order placed successfully!", Toast.LENGTH_SHORT).show();
                    removeItemsFromCart();


                } else {
                    Log.e(TAG, "Failed to save order: ", task.getException());
                    Toast.makeText(CheckoutActivity.this, "Failed to place order. Please try again.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.e(TAG, "Failed to generate order ID");
            Toast.makeText(CheckoutActivity.this, "Failed to generate order ID. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }



    private void retrieveIntentData() {
        selectedItems = (ArrayList<CartItem>) getIntent().getSerializableExtra("selectedItems");
        orderSummaryAdapter = new OrderSummaryAdapter( selectedItems);
        orderSummaryRecyclerView.setAdapter(orderSummaryAdapter);
        totalPriceTextView.setText("₱" + String.format("%.2f", getTotalPrice()));
    }

    private double getTotalPrice() {
        double totalPrice = 0;
        for (CartItem item : selectedItems) {
            totalPrice += item.getPrice() * item.getQuantity();
        }
        return totalPrice;
    }

    private void sendSms(String to, String message) {
        // Initialize the SmsSender
        SmsSender smsSender = new SmsSender();

        // Use the sendSms method from SmsSender class
        smsSender.sendSms(to, message, new SmsSender.SmsSendCallback() {
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "SMS sent successfully: " + response);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to send SMS: ", e);
            }
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        // Set the minimum date to today + 2 days
        calendar.add(Calendar.DAY_OF_MONTH, 2);
        long minDate = calendar.getTimeInMillis();

        // Reset calendar to current date for the dialog
        calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> selectedDateTextView.setText(String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // Set the minimum date for the date picker
        datePickerDialog.getDatePicker().setMinDate(minDate);

        datePickerDialog.show();
    }

    private void showTimePickerDialog() {
        // Get current time as default for the TimePicker
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY); // 24-hour format
        int minute = calendar.get(Calendar.MINUTE);

        // Initialize TimePickerDialog with 12-hour format
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (TimePicker view, int selectedHour, int selectedMinute) -> {
                    // Check if the selected time is between 8 AM (08:00) and 8 PM (20:00)
                    if (selectedHour >= 8 && selectedHour < 20) {
                        // Convert to 12-hour format
                        String formattedTime = String.format("%02d:%02d %s",
                                selectedHour % 12 == 0 ? 12 : selectedHour % 12, // Convert hour to 12-hour format
                                selectedMinute,
                                selectedHour < 12 ? "AM" : "PM"); // Determine AM/PM

                        // Display the selected time
                        selectedTimeTextView.setText(formattedTime);
                        Log.d(TAG, "Time selected: " + formattedTime);
                    } else {
                        // Show a message to the user that the selected time is not allowed
                        Toast.makeText(this, "Please select a time between 8 AM and 8 PM.", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Selected time is outside of the allowed range.");
                    }
                },
                hour,
                minute,
                false // Set to false for 12-hour format
        );

        // Show the TimePickerDialog
        timePickerDialog.show();
    }



    private class UserNameValueEventListener implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            userName = dataSnapshot.getValue(String.class);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.e(TAG, "Failed to read user name: ", databaseError.toException());
        }
    }

    private class PhoneNumberValueEventListener implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            userPhoneNumber = dataSnapshot.getValue(String.class);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.e(TAG, "Failed to read phone number: ", databaseError.toException());
        }
    }

    private class UserLocationValueEventListener implements ValueEventListener {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            ArrayList<String> locations = new ArrayList<>();
            for (DataSnapshot locationSnapshot : dataSnapshot.getChildren()) {
                String location = locationSnapshot.getValue(String.class);
                locations.add(location);
            }
            updateLocationSpinner(locations);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.e(TAG, "Failed to read locations: ", databaseError.toException());
        }
    }
}

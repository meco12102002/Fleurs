package com.example.fleursonthego.views;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fleursonthego.FCMNotificationHelper;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckOutCustomizedActivity extends AppCompatActivity {

    private TextView productIDTextView, quantityTextView, productNameTextView, totalPriceTextView, selectedTimeTextView1, selectedDateTextView1;
    private ImageView bouquetImageView;
    private ImageButton selectTimeButton;

    private TextView selectedDateTextView;
    double totalPrice;
    private Button checkOutProceedButton;
    private String userPhoneNumber;

    private EditText timeInput;
    private String userId;
    private DatabaseReference userRef;
    private Spinner locationSpinner;
    private ImageButton datePickerButton;
    private String userName;
    private Button addLocation;
    TextView selectedTimeTextView;
    private LinearLayout uploadReceiptLayout1;
    private ImageView receiptImageView;
    private Button uploadReceiptButton;
    private RadioGroup paymentMethodRadioGroup1;
    private static final int PICK_IMAGE_REQUEST = 1;

    private String paymentMethod;
    private int selectedPaymentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_check_out_customized);

        // Initialize UI elements
        initializeUI();

        // Get the intent and extract the data passed from BouquetPreviewActivity
        extractIntentData();

        // Set values to UI elements
        setUIValues();
        initializeFirebase();


        // Set date picker button click listener
        setUpDatePicker();
        selectTimeButton.setOnClickListener(v -> showTimePickerDialog());

    }

    private void updateLocationSpinner(ArrayList<String> locations) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, locations);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(adapter);
    }

    private void initializeUI() {
        addLocation = findViewById(R.id.addAddressButton1);
        receiptImageView = findViewById(R.id.receiptImageView1);
        addLocation.setOnClickListener(v -> showLocationInputDialog());


        uploadReceiptButton = findViewById(R.id.uploadReceiptButton1);
        uploadReceiptLayout1 = findViewById(R.id.uploadReceiptLayout1);
        quantityTextView = findViewById(R.id.txt_quantity);
        productNameTextView = findViewById(R.id.txt_custom_prodname);
        totalPriceTextView = findViewById(R.id.txt_custom_totalprice);
        bouquetImageView = findViewById(R.id.ImageViewCustomizedImage1);
        selectedDateTextView1 = findViewById(R.id.selectedDateTextView1);
        selectedTimeTextView1 = findViewById(R.id.timeInput1);
        selectTimeButton = findViewById(R.id.timePickerButton1);
        checkOutProceedButton = findViewById(R.id.checkoutProceedButton1);
        checkOutProceedButton.setOnClickListener(v -> processOrder());
        locationSpinner = findViewById(R.id.addressSpinner1);
        paymentMethodRadioGroup1 = findViewById(R.id.paymentMethodRadioGroup1);

        selectedPaymentId = paymentMethodRadioGroup1.getCheckedRadioButtonId();


        paymentMethodRadioGroup1.setOnCheckedChangeListener((group, checkedId) -> {
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
                uploadReceiptLayout1.setVisibility(View.VISIBLE); // Show upload layout
            } else {
                uploadReceiptLayout1.setVisibility(View.GONE); // Hide upload layout
                receiptImageView.setImageDrawable(null); // Clear any selected image
            }
        });

        uploadReceiptButton.setOnClickListener(v -> openFileChooser());



    }


    private boolean validateOrderDetails() {
        return !selectedDateTextView1.getText().toString().isEmpty() &&
                !selectedTimeTextView1.getText().toString().isEmpty() &&
                locationSpinner.getSelectedItem() != null; // Ensure a location is selected
    }


    private void processOrder() {
        // Retrieve the selected payment method when the user clicks the checkout button
        selectedPaymentId = paymentMethodRadioGroup1.getCheckedRadioButtonId();

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
            String message = "Your order has been placed on " + currentDate + ". Total: ₱" + String.format("%.2f", totalPrice);
            sendSms(userPhoneNumber, message);
            FCMNotificationHelper.sendNotification(this, "Order Update", message);

            uploadReceiptAndSaveOrder();
        } else {
            Toast.makeText(this, "Please fill in all order details correctly.", Toast.LENGTH_SHORT).show();
        }
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
        AlertDialog.Builder builder = new AlertDialog.Builder(CheckOutCustomizedActivity.this);
        builder.setMessage("What would you like to do next?")
                .setCancelable(false)
                .setPositiveButton("Go to My Orders", (dialog, id) -> {
                    // Redirect to My Orders activity
                    Intent intent = new Intent(CheckOutCustomizedActivity.this, MyOrdersActivity.class);
                    startActivity(intent);
                    finish(); // Optionally close the CheckoutActivity
                })
                .setNegativeButton("Keep Shopping", (dialog, id) -> {
                    // Redirect to All Products activity
                    Intent intent = new Intent(CheckOutCustomizedActivity.this, AllProductsActivity.class);
                    startActivity(intent);
                    finish(); // Optionally close the CheckoutActivity
                });

        // Create and show the dialog
        AlertDialog alert = builder.create();
        alert.show();
    }


    private void saveOrder(String receiptUrl) {


        String deliveryDate = selectedDateTextView1.getText().toString();
        String deliveryTime = selectedTimeTextView1.getText().toString();
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
                    Toast.makeText(CheckOutCustomizedActivity.this, "Order placed successfully!", Toast.LENGTH_SHORT).show();



                } else {
                    Log.e(TAG, "Failed to save order: ", task.getException());
                    Toast.makeText(CheckOutCustomizedActivity.this, "Failed to place order. Please try again.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.e(TAG, "Failed to generate order ID");
            Toast.makeText(CheckOutCustomizedActivity.this, "Failed to generate order ID. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }


    private void initializeFirebase() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
            getUserDetails();
        }

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



    private boolean validateLocationInput(EditText... fields) {
        for (EditText field : fields) {
            if (field.getText().toString().trim().isEmpty()) {
                return false; // Return false if any field is empty
            }
        }
        return true; // All fields are filled
    }
    ArrayList<CartItem> selectedItems = new ArrayList<>();

    private void extractIntentData() {
        Intent intent = getIntent();

        // Extract data from the intent
        String productID = intent.getStringExtra("productID");
        int quantity = intent.getIntExtra("quantity", 0);
        String productName = intent.getStringExtra("productName");
        String productImage = intent.getStringExtra("productImage");  // Assuming this is the image URL or path
        double price = intent.getDoubleExtra("totalPrice", 0.0);

        // Create an ArrayList to store CartItem objects

        // Create a new CartItem based on the extracted data
        CartItem cartItem = new CartItem(productImage, productName, price, quantity);

        // Add the created CartItem to the ArrayList
        selectedItems.add(cartItem);

        // For debugging purposes, log the added data
        Log.d(TAG, "Added product to cart: " + productName);
        Log.d(TAG, "Total price: " + price);
    }




    private void setUIValues() {
        // Extracted data passed from intent
        Intent intent = getIntent();
        int quantity = intent.getIntExtra("quantity", 0);
        String productID = intent.getStringExtra("productID");
        double totalPrice = intent.getDoubleExtra("totalPrice", 0.0);

        // Set values to UI
        quantityTextView.setText("Quantity: " + quantity);
        productNameTextView.setText(productID);

        // Format and set the total price
        String formattedPrice = String.format("Total: P%.2f", totalPrice);
        totalPriceTextView.setText(formattedPrice);
    }

    private void setBouquetImage(byte[] byteArray) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        bouquetImageView.setImageBitmap(bitmap);
    }

    private void setUpDatePicker() {
        ImageButton datePickerButton = findViewById(R.id.datePickerButton1);
        datePickerButton.setOnClickListener(v -> openDatePicker());
    }


    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Receipt"), PICK_IMAGE_REQUEST);
    }

    private void openDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            String selectedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
            selectedDateTextView1.setText(selectedDate);
        }, year, month, day);

        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        datePickerDialog.show();
    }

    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY); // 24-hour format
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, selectedHour, selectedMinute) -> {
            if (selectedHour >= 8 && selectedHour < 20) {
                String formattedTime = String.format("%02d:%02d %s",
                        selectedHour % 12 == 0 ? 12 : selectedHour % 12,
                        selectedMinute,
                        selectedHour < 12 ? "AM" : "PM");
                selectedTimeTextView1.setText(formattedTime);
                Log.d(TAG, "Time selected: " + formattedTime);
            } else {
                Toast.makeText(this, "Please select a time between 8 AM and 8 PM.", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Selected time is outside of the allowed range.");
            }
        }, hour, minute, false);

        timePickerDialog.show();
    }

    private class UserLocationValueEventListener implements ValueEventListener {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            ArrayList<String> locations = new ArrayList<>();
            for (DataSnapshot locationSnapshot : snapshot.getChildren()) {
                String location = locationSnapshot.getValue(String.class);
                locations.add(location);
            }
            updateLocationSpinner(locations);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    }

    private class UserNameValueEventListener implements ValueEventListener {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            userName = snapshot.getValue(String.class);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    }

    private class PhoneNumberValueEventListener implements ValueEventListener {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            userPhoneNumber = snapshot.getValue(String.class);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    }
}

package com.example.fleursonthego.views;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fleursonthego.R;

public class BouquetPreviewActivity extends AppCompatActivity {





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bouquet_preview);

        // Initialize views
        ImageView screenshotImageView = findViewById(R.id.bouquetPreviewImage);
        TextView totalPriceTextView = findViewById(R.id.totalPriceTextView);
        TextView bouquetNameTextView = findViewById(R.id.bouquetName);
        Button orderButton = findViewById(R.id.confirmOrderButton);  // Button to proceed to order

        // Get the intent and extract the screenshot and total price
        Intent intent = getIntent();
        byte[] byteArray = intent.getByteArrayExtra("screenshot");
        double totalPrice = intent.getDoubleExtra("totalPrice", 0.0);

        // Format the total price to two decimal places
        String formattedPrice = String.format("P%.2f", totalPrice);
        totalPriceTextView.setText("Total Price: " + formattedPrice);

        // Generate a random 7-digit number for the product ID
        String productID = "CustomBouquet" + generateRandomNumber();
        bouquetNameTextView.setText(productID);  // Set product ID as bouquet name

        // If the byteArray is not null, convert it to a Bitmap and display it
        if (byteArray != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            screenshotImageView.setImageBitmap(bitmap);
        }

        // Set up the order button to pass data to the next activity
        orderButton.setOnClickListener(view -> {
            // Prepare the data to pass to the next activity (OrderConfirmationActivity)
            Intent orderIntent = new Intent(BouquetPreviewActivity.this, CheckOutCustomizedActivity.class);

            // Pass all the data (product ID, quantity, screenshot, total price)
            orderIntent.putExtra("productID", productID);
            orderIntent.putExtra("quantity", 1);  // As per your request, the quantity is always 1
            orderIntent.putExtra("screenshot", byteArray);
            orderIntent.putExtra("totalPrice", totalPrice);

            // Start the next activity
            startActivity(orderIntent);
        });
    }

    // Function to generate a random 7-digit number for the product ID
    private String generateRandomNumber() {
        int min = 1000000; // Minimum value for a 7-digit number
        int max = 9999999; // Maximum value for a 7-digit number
        int randomNum = (int) (Math.random() * (max - min + 1)) + min;
        return String.valueOf(randomNum);
    }
}

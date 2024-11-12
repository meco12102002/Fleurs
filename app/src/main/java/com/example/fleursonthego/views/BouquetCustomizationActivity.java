package com.example.fleursonthego.views;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fleursonthego.Adapters.WrapAdapter;
import com.example.fleursonthego.R;
import com.example.fleursonthego.Adapters.FlowerAdapter;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class BouquetCustomizationActivity extends AppCompatActivity {
    private RelativeLayout canvas;
    private RelativeLayout paletteFlowers, paletteWraps, paletteRibbons;

    private final double[] flowerPrices = {100, 100, 100,100,100};  // Prices for red_rose, white_rose, yellow_rose, etc.
    private final double[] wrapPrices = {35, 35, 35, 35,35, 35, 35};  // Prices for different wraps
    private final double[] ribbonPrices = {25,25,25,25};  // Prices for ribbons (if applicable)
    private double totalPrice = 0.0;
    private final List<Integer> selectedFlowers = new ArrayList<>();
    private final List<Integer> selectedWraps = new ArrayList<>();
    private final List<Integer> selectedRibbons = new ArrayList<>();


    TextView priceTotaltxt;
    private final int[] flowerImages = {
            R.drawable.red_rose,
            R.drawable.white_rose,
            R.drawable.yellow_rose,
            R.drawable.ecuadorian_blue_rose,
            R.drawable.ecuadorian_cool_water

    };

    private final int[] wrapImages = {
            R.drawable.boquetwrap,
            R.drawable.white_wrap,
            R.drawable.violet_wrap,
            R.drawable.sunny_wrap,
            R.drawable.cute_theme_wrap,
            R.drawable.greenish_white_wrap,
            R.drawable.purple_wrap_1
    };

    private final int[] ribbonImages = {
            R.drawable.blue_ribbon,
            R.drawable.golden_ribbon,
            R.drawable.card,
            R.drawable.red_ribbon,
    };

    private final List<ImageView> clonedImages = new ArrayList<>();
    private ImageView currentSelectedImage = null;

    private final Handler rotationHandler = new Handler();
    private Runnable rotateRunnable;
    private boolean isRotatingRight = false;

    private float initialDistance = 0f;
    private float initialScale = 1f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_boquet_customization);
        priceTotaltxt = findViewById(R.id.priceTextView);

        initializeViews();
        setupRecyclerViews();
        setupButtonListeners();
    }


    // Method to find flower index
    private int getFlowerIndex(int imageResource) {
        for (int i = 0; i < flowerImages.length; i++) {
            if (flowerImages[i] == imageResource) {
                return i;
            }
        }
        return -1; // Return -1 if not found
    }

    // Method to find wrap index
    private int getWrapIndex(int imageResource) {
        for (int i = 0; i < wrapImages.length; i++) {
            if (wrapImages[i] == imageResource) {
                return i;
            }
        }
        return -1; // Return -1 if not found
    }

    private String getItemType(ImageView imageView) {
        // Example of how to determine the type of the image (flower, wrap, etc.)
        // You might need to create a helper method or add some tags to your image views to track types.
        if (imageView.getTag() != null) {
            return imageView.getTag().toString(); // Use the tag to store the item type
        }
        return "";
    }

    // Recalculate the price based on all the items on the canvas
    private void recalculatePrice() {
        totalPrice = 0.0; // Reset the total price
        for (ImageView imageView : clonedImages) {
            String itemType = getItemType(imageView);
            int index = clonedImages.indexOf(imageView);

            // Add the price based on the item type
            if (itemType.equals("flower")) {
                totalPrice += flowerPrices[index];
            } else if (itemType.equals("wrap")) {
                totalPrice += wrapPrices[index];
            } else if (itemType.equals("ribbon")) {
                totalPrice += ribbonPrices[index];
            }
        }

        // Update the price text
        priceTotaltxt.setText("Total Price: P" + String.format("%.2f", totalPrice));
    }

    // Method to find ribbon index
    private int getRibbonIndex(int imageResource) {
        for (int i = 0; i < ribbonImages.length; i++) {
            if (ribbonImages[i] == imageResource) {
                return i;
            }
        }
        return -1; // Return -1 if not found
    }


    private void updatePrice() {
        totalPrice = 0.0;

        // Add the prices of selected flowers
        for (int flowerIndex : selectedFlowers) {
            totalPrice += flowerPrices[flowerIndex];
        }

        // Add the prices of selected wraps
        for (int wrapIndex : selectedWraps) {
            totalPrice += wrapPrices[wrapIndex];
        }

        // Add the prices of selected ribbons
        for (int ribbonIndex : selectedRibbons) {
            totalPrice += ribbonPrices[ribbonIndex];
        }

        // Update the price text
        priceTotaltxt.setText("Total Price: P" + String.format("%.2f", totalPrice));
    }


    private void initializeViews() {
        canvas = findViewById(R.id.canvas);
        paletteFlowers = findViewById(R.id.palette_flowers);
        paletteWraps = findViewById(R.id.palette_wraps);
        paletteRibbons = findViewById(R.id.palette_ribbons);

        hideAllPalettes();
    }

    private void hideAllPalettes() {
        paletteFlowers.setVisibility(View.GONE);
        paletteWraps.setVisibility(View.GONE);
        paletteRibbons.setVisibility(View.GONE);
    }

    private void setupRecyclerViews() {
        RecyclerView recyclerViewFlowers = findViewById(R.id.recyclerView_flowers);
        recyclerViewFlowers.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewFlowers.setAdapter(new FlowerAdapter(this, flowerImages, this::onFlowerSelected));

        RecyclerView recyclerViewWraps = findViewById(R.id.recyclerView_wraps);
        recyclerViewWraps.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewWraps.setAdapter(new WrapAdapter(this, wrapImages, this::onWrapSelected));

        RecyclerView recyclerViewRibbons = findViewById(R.id.recyclerView_ribbons);
        recyclerViewRibbons.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewRibbons.setAdapter(new FlowerAdapter(this, ribbonImages, this::onRibbonSelected));
    }

    private void setupButtonListeners() {
        ImageButton buttonFlowers = findViewById(R.id.button_flowers);
        ImageButton buttonWraps = findViewById(R.id.button_wraps);
        ImageButton buttonRibbons = findViewById(R.id.button_ribbons);
        ImageButton buttonUndo = findViewById(R.id.button_undo);
        ImageButton buttonRotateLeft = findViewById(R.id.button_rotate_left);
        ImageButton buttonRotateRight = findViewById(R.id.button_rotate_right);
        ImageButton buttonDelete = findViewById(R.id.button_delete);
        ImageButton buttonSendToBack = findViewById(R.id.button_send_back);

        buttonFlowers.setOnClickListener(v -> showPalette("flowers"));
        buttonWraps.setOnClickListener(v -> showPalette("wraps"));
        buttonRibbons.setOnClickListener(v -> showPalette("ribbons"));

        buttonUndo.setOnClickListener(v -> undoLastAction());

        buttonRotateLeft.setOnTouchListener((v, event) -> handleRotation(event, true));
        buttonRotateRight.setOnTouchListener((v, event) -> handleRotation(event, false));

        buttonDelete.setOnClickListener(v -> deleteSelectedImage());
        buttonSendToBack.setOnClickListener(v -> sendSelectedImageToBack());



        Button confirmBouquetButton = findViewById(R.id.button_confirm_bouquet);
        confirmBouquetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Capture the screenshot of the canvas
                Bitmap bitmap = captureCanvasScreenshot(canvas);

                // Check if totalPrice is 0, if so show the Toast and do not proceed
                if (totalPrice == 0.00) {
                    Toast.makeText(BouquetCustomizationActivity.this, "Please select at least one item", Toast.LENGTH_SHORT).show();
                    return;  // Return here to prevent starting the next activity
                }

                // Pass the Bitmap to the next activity
                Intent intent = new Intent(BouquetCustomizationActivity.this, BouquetPreviewActivity.class);

                // Convert Bitmap to ByteArray
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();

                // Add ByteArray and totalPrice to Intent extras
                intent.putExtra("screenshot", byteArray);
                intent.putExtra("totalPrice", totalPrice);

                // Start the next activity
                startActivity(intent);
            }

        });
    }
    private Bitmap captureCanvasScreenshot(View view) {
        // Create a bitmap from the view
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    private boolean handleRotation(MotionEvent event, boolean rotateLeft) {
        boolean isRotatingLeft = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (rotateLeft) {
                    isRotatingLeft = true;
                    startRotation(-5);
                } else {
                    isRotatingRight = true;
                    startRotation(5);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (rotateLeft) {
                    isRotatingLeft = false;
                } else {
                    isRotatingRight = false;
                }
                stopRotation();
                break;
        }
        return true;
    }

    private void showPalette(String paletteType) {
        hideAllPalettes();
        switch (paletteType) {
            case "flowers":
                paletteFlowers.setVisibility(View.VISIBLE);
                break;
            case "wraps":
                paletteWraps.setVisibility(View.VISIBLE);
                break;
            case "ribbons":
                paletteRibbons.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void onFlowerSelected(int imageResource) {
        int flowerIndex = getFlowerIndex(imageResource); // Method to find the flower index
        updateTotalPrice("flower", flowerIndex); // Update total price for flower
        ImageView clonedImage = createClonedImage(imageResource);
        canvas.addView(clonedImage);
        setupTouchListener(clonedImage);

    }




    private void onRibbonSelected(int imageResource) {
        int flowerIndex = getRibbonIndex(imageResource); // Method to find the flower index
        updateTotalPrice("ribbon", flowerIndex); // Update total price for flower
        ImageView clonedImage = createClonedImage(imageResource);
        canvas.addView(clonedImage);
        setupTouchListener(clonedImage);
    }



    private void updateTotalPrice(String itemType, int itemIndex) {
        double itemPrice = 0.0;

        switch (itemType) {
            case "flower":
                itemPrice = flowerPrices[itemIndex];
                break;
            case "wrap":
                itemPrice = wrapPrices[itemIndex];
                break;
            case "ribbon":
                itemPrice = ribbonPrices[itemIndex];
                break;
        }

        totalPrice += itemPrice; // Add the selected item's price to the total price
        priceTotaltxt.setText("Total Price: P" + totalPrice); // Update the price text view
    }

    // Method to subtract item price from the total price when removed
    private void subtractFromTotalPrice(String itemType, int itemIndex) {
        double itemPrice = 0.0;

        switch (itemType) {
            case "flower":
                itemPrice = flowerPrices[itemIndex];
                break;
            case "wrap":
                itemPrice = wrapPrices[itemIndex];
                break;
            case "ribbon":
                itemPrice = ribbonPrices[itemIndex];
                break;
        }

        totalPrice -= itemPrice; // Subtract the selected item's price from the total price
        priceTotaltxt.setText("Total Price: P" + totalPrice); // Update the price text view
    }

    private void onWrapSelected(int imageResource) {
        int wrapIndex = getWrapIndex(imageResource); // Method to find the wrap index
        updateTotalPrice("wrap", wrapIndex); // Update total price for wrap
        ImageView clonedImage = createClonedImage(imageResource);
        canvas.addView(clonedImage);
        setupTouchListener(clonedImage);
    }

    private ImageView createClonedImage(int imageResource) {
        ImageView clonedImage = new ImageView(this);
        clonedImage.setImageResource(imageResource);
        clonedImage.setLayoutParams(new RelativeLayout.LayoutParams(200, 200));
        clonedImage.setX(200);
        clonedImage.setY(200);
        return clonedImage;
    }

    private void setupTouchListener(ImageView clonedImage) {
        clonedImages.add(clonedImage);
        clonedImage.setOnTouchListener(new View.OnTouchListener() {
            private float initialX, initialY, dX, dY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.bringToFront();

                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = event.getRawX();
                        initialY = event.getRawY();
                        dX = v.getX() - initialX;
                        dY = v.getY() - initialY;
                        currentSelectedImage = clonedImage;
                        initialDistance = 0f; // Reset the initial distance for pinch zoom
                        initialScale = v.getScaleX(); // Store the initial scale
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        if (event.getPointerCount() == 1) {
                            v.setX(event.getRawX() + dX);
                            v.setY(event.getRawY() + dY);
                        } else if (event.getPointerCount() == 2) {
                            handlePinchZoom(v, event);
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                        return true;

                    default:
                        return false;
                }
            }

            private void handlePinchZoom(View v, MotionEvent event) {
                float newDistance = getDistance(event);
                if (initialDistance == 0) {
                    initialDistance = newDistance;
                } else {
                    float scale = newDistance / initialDistance;
                    // Gradually adjust scale for a smoother effect
                    float newScale = initialScale * scale;
                    v.setScaleX(newScale);
                    v.setScaleY(newScale);
                }
            }

            private float getDistance(MotionEvent event) {
                float x = event.getX(0) - event.getX(1);
                float y = event.getY(0) - event.getY(1);
                return (float) Math.sqrt(x * x + y * y);
            }
        });
    }

    private void undoLastAction() {
        if (!clonedImages.isEmpty()) {
            ImageView lastClonedImage = clonedImages.remove(clonedImages.size() - 1);
            canvas.removeView(lastClonedImage);
            currentSelectedImage = null;
            Toast.makeText(this, "Undid last action", Toast.LENGTH_SHORT).show();
            recalculatePrice();
        }
    }

    private void startRotation(int degrees) {
        if (rotateRunnable == null) {
            rotateRunnable = () -> {
                if (currentSelectedImage != null) {
                    float newRotation = currentSelectedImage.getRotation() + degrees;
                    currentSelectedImage.setRotation(newRotation);
                }
                rotationHandler.postDelayed(rotateRunnable, 100);
            };
            rotationHandler.post(rotateRunnable);
        }
    }

    private void stopRotation() {
        if (rotateRunnable != null) {
            rotationHandler.removeCallbacks(rotateRunnable);
            rotateRunnable = null;
        }
    }



    private void deleteSelectedImage() {
        if (currentSelectedImage != null) {
            canvas.removeView(currentSelectedImage);
            clonedImages.remove(currentSelectedImage);
            currentSelectedImage = null;
            Toast.makeText(this, "Deleted selected item", Toast.LENGTH_SHORT).show();
            recalculatePrice();

        }
    }

    private void sendSelectedImageToBack() {
        if (currentSelectedImage != null) {
            // Remove the current selected image and re-add it to the canvas
            canvas.removeView(currentSelectedImage);
            canvas.addView(currentSelectedImage, 0); // Add at index 0 to send it to the back
            currentSelectedImage = null; // Deselect the current image
            Toast.makeText(this, "Sent selected item to back", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No item selected", Toast.LENGTH_SHORT).show();
        }
    }
}

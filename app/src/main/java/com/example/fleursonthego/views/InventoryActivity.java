package com.example.fleursonthego.views;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fleursonthego.Models.AdminProduct2;
import com.example.fleursonthego.Models.Product;
import com.example.fleursonthego.R;
import com.example.fleursonthego.Adapters.AdminProductAdapter;
import com.example.fleursonthego.Models.AdminProduct;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InventoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdminProductAdapter adminProductAdapter;
    private List<AdminProduct> adminProductList;
    private FirebaseDatabase database;
    private DatabaseReference productRef;
    private FloatingActionButton btnAddProduct;
    private TextView goToArchived;
    private static final int PICK_IMAGE_REQUEST = 1;
    private ProgressDialog progressDialog;
    private Uri image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Enable edge-to-edge on the activity
        setContentView(R.layout.activity_inventory);
        btnAddProduct = findViewById(R.id.btnAddProduct);

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize product list and Firebase
        adminProductList = new ArrayList<>();
        database = FirebaseDatabase.getInstance();
        productRef = database.getReference("products"); // Change path as per your Firebase structure

        // Initialize Adapter
        adminProductAdapter = new AdminProductAdapter(this);
        recyclerView.setAdapter(adminProductAdapter);
        btnAddProduct.setOnClickListener(v -> showAddProductDialog());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Enable the back button functionality
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Fetch product data from Firebase
        fetchAdminProductData();

        goToArchived = findViewById(R.id.btnArchivedProducts);

        goToArchived.setOnClickListener(v -> {
            Intent intent = new Intent(InventoryActivity.this, ArchivedProductsActivity.class);
            startActivity(intent);
        });
    }


    private void showLoadingDialog() {
        progressDialog = new ProgressDialog(InventoryActivity.this);
        progressDialog.setMessage("Adding product...");
        progressDialog.setCancelable(false); // Disable dismissing the dialog by tapping outside
        progressDialog.show();
    }

    private void dismissLoadingDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void saveProductToDatabase(String productName, String productDescription, String price,
                                       String stock, String category, String image) {
        showLoadingDialog();  // Show the loading dialog

        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("products");
        String productId = databaseRef.push().getKey();

        AdminProduct2 newProduct = new AdminProduct2(productId, productName, productDescription,
                Double.parseDouble(price), Integer.parseInt(stock), category, image);

        if (productId != null) {
            databaseRef.child(productId).setValue(newProduct)
                    .addOnSuccessListener(aVoid -> {
                        dismissLoadingDialog();  // Dismiss loading dialog
                        Toast.makeText(this, "Product Saved", Toast.LENGTH_SHORT).show();
                        refreshProductList();  // Refresh the RecyclerView
                    })
                    .addOnFailureListener(e -> {
                        dismissLoadingDialog();  // Dismiss loading dialog
                        Toast.makeText(this, "Failed to Save Product", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void refreshProductList() {
        // This method fetches the latest data from Firebase and updates the RecyclerView
        fetchAdminProductData();  // Assuming you have this method to fetch data and refresh the UI
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            image = data.getData();
            Log.d("image", "Selected Image URI: " + image.toString());

            // Safely set the image in the ImageView
            ImageView imagePreview = findViewById(R.id.imagePreview123);
            if (imagePreview != null && image != null) {
                imagePreview.setImageURI(image);
            }
        }
    }

    private void showAddProductDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_product, null);
        builder.setView(dialogView);

        // Create the dialog after setting the view
        AlertDialog dialog = builder.create();

        // Initialize the views
        EditText productNameEditText = dialogView.findViewById(R.id.editProductName);
        EditText productPriceEditText = dialogView.findViewById(R.id.editProductPrice);
        EditText productDescriptionEditText = dialogView.findViewById(R.id.editProductDescription);
        EditText productStockEditText = dialogView.findViewById(R.id.editProductStock);
        ImageView imagePreview = dialogView.findViewById(R.id.imagePreview123);
        Button saveProductButton = dialogView.findViewById(R.id.btnSaveProduct);
        Button uploadImageButton = dialogView.findViewById(R.id.btnUploadImage);

        // Initialize checkboxes for category selection
        CheckBox checkboxRomantic = dialogView.findViewById(R.id.checkboxRomantic);
        CheckBox checkboxWeddings = dialogView.findViewById(R.id.checkboxWeddings);
        CheckBox checkboxAnniversaries = dialogView.findViewById(R.id.checkboxAnniversaries);
        CheckBox checkboxBirthdays = dialogView.findViewById(R.id.checkboxBirthdays);

        // Display selected image in imagePreview if image is available
        if (image != null && imagePreview != null) {
            imagePreview.setImageURI(image);  // Safely set image URI to preview
        }

        uploadImageButton.setOnClickListener(v -> openFileChooser());

        saveProductButton.setOnClickListener(v -> {
            String productName = productNameEditText.getText().toString();
            String price = productPriceEditText.getText().toString();
            String productDescription = productDescriptionEditText.getText().toString();
            String stock = productStockEditText.getText().toString();

            // Collect selected categories from checkboxes
            StringBuilder selectedCategories = new StringBuilder();
            if (checkboxRomantic.isChecked()) {
                selectedCategories.append(checkboxRomantic.getText().toString()).append(", ");
            }
            if (checkboxWeddings.isChecked()) {
                selectedCategories.append(checkboxWeddings.getText().toString()).append(", ");
            }
            if (checkboxAnniversaries.isChecked()) {
                selectedCategories.append(checkboxAnniversaries.getText().toString()).append(", ");
            }
            if (checkboxBirthdays.isChecked()) {
                selectedCategories.append(checkboxBirthdays.getText().toString()).append(", ");
            }

            // Remove trailing comma if categories are selected
            if (selectedCategories.length() > 0) {
                selectedCategories.setLength(selectedCategories.length() - 2); // Remove last comma
            }

            if (image != null) {
                uploadImageToFirebaseStorage(productName, productDescription, price, stock, selectedCategories.toString());
            } else {
                saveProductToDatabase(productName, productDescription, price, stock, selectedCategories.toString(), null);
            }

            dialog.dismiss();  // Dismiss the dialog when done
        });

        dialog.show();  // Show the dialog
    }


    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    // Function to upload the selected image to Firebase Storage
    private void uploadImageToFirebaseStorage(String productName, String productDescription, String productPrice,
                                              String productStock, String category) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("product_images/" + UUID.randomUUID().toString());

        storageRef.putFile(image)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    saveProductToDatabase(productName, productDescription, productPrice, productStock, category, imageUrl);
                }))
                .addOnFailureListener(e -> Toast.makeText(this, "Image Upload Failed", Toast.LENGTH_SHORT).show());
    }

    private void fetchAdminProductData() {
        productRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                AdminProduct adminProduct = dataSnapshot.getValue(AdminProduct.class);
                if (adminProduct != null) {
                    adminProductList.add(adminProduct);  // Add new product
                    adminProductAdapter.notifyItemInserted(adminProductList.size() - 1); // Insert only the new item
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                AdminProduct updatedProduct = dataSnapshot.getValue(AdminProduct.class);
                if (updatedProduct != null) {
                    // Find the product in the list and update it
                    for (int i = 0; i < adminProductList.size(); i++) {
                        AdminProduct product = adminProductList.get(i);
                        if (product.getProductId().equals(updatedProduct.getProductId())) {
                            adminProductList.set(i, updatedProduct); // Update the product
                            adminProductAdapter.notifyItemChanged(i); // Notify the adapter about the change
                            break;
                        }
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                AdminProduct removedProduct = dataSnapshot.getValue(AdminProduct.class);
                if (removedProduct != null) {
                    // Find and remove the product
                    for (int i = 0; i < adminProductList.size(); i++) {
                        if (adminProductList.get(i).getProductId().equals(removedProduct.getProductId())) {
                            adminProductList.remove(i); // Remove the product
                            adminProductAdapter.notifyItemRemoved(i); // Notify the adapter about the removal
                            break;
                        }
                    }
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                // This can be handled if your list order matters and you want to update the position
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(InventoryActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

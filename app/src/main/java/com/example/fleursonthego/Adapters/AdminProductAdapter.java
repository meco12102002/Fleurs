package com.example.fleursonthego.Adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.fleursonthego.Models.AdminProduct;
import com.example.fleursonthego.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.ProductViewHolder> {

    private Context context;
    private List<AdminProduct> productList;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;

    public AdminProductAdapter(Context context) {
        this.context = context;
        this.productList = new ArrayList<>();
        fetchProductsFromFirebase(); // Fetch products when adapter is initialized
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.admin_item_product, parent, false);
        return new ProductViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        AdminProduct product = productList.get(position);

        holder.productName.setText(product.getProductName());
        holder.productPrice.setText("P" + product.getPrice());
        holder.productDescription.setText(product.getProductDescription());
        holder.productCategory.setText("Category: " + product.getCategory());
        holder.productStock.setText("Stock: " + product.getStock());

        Glide.with(context).load(product.getImage()).into(holder.productImage);

        // Set click listener for the edit button
        holder.editButton.setOnClickListener(v -> showEditStockDialog(product));

        // Set click listener for the visibility (archive) button
        holder.visibilityButton.setOnClickListener(v -> showArchiveConfirmationDialog(product));
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productPrice, productDescription, productCategory, productStock;
        ImageView productImage;
        MaterialButton editButton;
        MaterialButton visibilityButton;

        public ProductViewHolder(View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            productDescription = itemView.findViewById(R.id.productDescription);
            productCategory = itemView.findViewById(R.id.productCategory);
            productStock = itemView.findViewById(R.id.productStock);
            productImage = itemView.findViewById(R.id.productImage);
            editButton = itemView.findViewById(R.id.editButton);
            visibilityButton = itemView.findViewById(R.id.visibilityButton);
        }
    }

    // Method to fetch products from Firebase
    private void fetchProductsFromFirebase() {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("products");
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                productList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String productId = snapshot.getKey();
                    AdminProduct product = snapshot.getValue(AdminProduct.class);
                    if (product != null) {
                        product.setProductId(productId);
                        productList.add(product);
                    }
                }
                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, "Failed to load products.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to show confirmation dialog before archiving a product
    private void showArchiveConfirmationDialog(AdminProduct product) {
        // Create the confirmation dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Are you sure?")
                .setMessage("Do you really want to archive this product?")
                .setPositiveButton("Yes", (dialog, which) -> archiveProduct(product)) // If yes, archive the product
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss()) // If no, dismiss the dialog
                .setCancelable(true) // Allow dismiss on outside touch
                .show();
    }

    // Method to archive a product
    private void archiveProduct(AdminProduct product) {
        DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference("products");
        DatabaseReference archivedProductsRef = FirebaseDatabase.getInstance().getReference("archivedProducts");

        String productId = product.getProductId();
        if (productId != null) {
            archivedProductsRef.child(productId).setValue(product) // Copy to archivedProducts
                    .addOnSuccessListener(aVoid -> {
                        // Remove from current products node
                        productsRef.child(productId).removeValue()
                                .addOnSuccessListener(aVoid1 -> {
                                    Toast.makeText(context, "Product archived successfully!", Toast.LENGTH_SHORT).show();
                                    productList.remove(product); // Remove from local list
                                    notifyDataSetChanged();
                                })
                                .addOnFailureListener(e -> Toast.makeText(context, "Failed to remove from products.", Toast.LENGTH_SHORT).show());
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Failed to archive product.", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(context, "Product ID is missing. Cannot archive.", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to display the stock management dialog
    private void showEditStockDialog(AdminProduct product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_stock, null);
        builder.setView(view);

        TextView editStockQuantity = view.findViewById(R.id.editStockQuantity);
        MaterialButton btnUpdateStock = view.findViewById(R.id.btnUpdateStock);

        AlertDialog dialog = builder.create();

        btnUpdateStock.setOnClickListener(v -> {
            String stockInput = editStockQuantity.getText().toString();
            if (!stockInput.isEmpty()) {
                int newStock = Integer.parseInt(stockInput);

                if (product.getProductId() != null) {
                    DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("products");

                    databaseRef.child(product.getProductId())
                            .child("stock")
                            .setValue(newStock)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(context, "Stock updated!", Toast.LENGTH_SHORT).show();
                                product.setStock(newStock);
                                notifyDataSetChanged();
                            })
                            .addOnFailureListener(e -> Toast.makeText(context, "Failed to update stock.", Toast.LENGTH_SHORT).show());
                } else {
                    Toast.makeText(context, "Product ID is missing. Cannot update stock.", Toast.LENGTH_SHORT).show();
                }
            } else {
                editStockQuantity.setError("Enter a valid stock quantity");
            }
        });

        dialog.show();
    }
}

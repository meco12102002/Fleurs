package com.example.fleursonthego.Adapters;

import android.content.Context;
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

public class ArchivedAdminProductAdapter extends RecyclerView.Adapter<ArchivedAdminProductAdapter.ProductViewHolder> {

    private Context context;
    private List<AdminProduct> archivedProductList;

    public ArchivedAdminProductAdapter(Context context) {
        this.context = context;
        this.archivedProductList = new ArrayList<>();
        fetchArchivedProductsFromFirebase(); // Fetch archived products when adapter is initialized
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.admin_item_product, parent, false);
        return new ProductViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        AdminProduct product = archivedProductList.get(position);

        holder.productName.setText(product.getProductName());
        holder.productPrice.setText("$" + product.getPrice());
        holder.productDescription.setText(product.getProductDescription());
        holder.productCategory.setText("Category: " + product.getCategory());
        holder.productStock.setText("Stock: " + product.getStock());

        Glide.with(context).load(product.getImage()).into(holder.productImage);

        // Set click listener for the restore button
        holder.restoreButton.setOnClickListener(v -> showRestoreConfirmationDialog(product));
    }

    @Override
    public int getItemCount() {
        return archivedProductList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productPrice, productDescription, productCategory, productStock;
        ImageView productImage;
        MaterialButton restoreButton, editButton;

        public ProductViewHolder(View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            productDescription = itemView.findViewById(R.id.productDescription);
            productCategory = itemView.findViewById(R.id.productCategory);
            productStock = itemView.findViewById(R.id.productStock);
            productImage = itemView.findViewById(R.id.productImage);
            restoreButton = itemView.findViewById(R.id.visibilityButton);
            editButton = itemView.findViewById(R.id.editButton);
            editButton.setVisibility(View.GONE);
        }
    }

    // Method to fetch archived products from Firebase
    private void fetchArchivedProductsFromFirebase() {
        DatabaseReference archivedProductsRef = FirebaseDatabase.getInstance().getReference("archivedProducts");
        archivedProductsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                archivedProductList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String productId = snapshot.getKey();
                    AdminProduct product = snapshot.getValue(AdminProduct.class);
                    if (product != null) {
                        product.setProductId(productId);
                        archivedProductList.add(product);
                    }
                }
                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, "Failed to load archived products.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to show confirmation dialog before restoring a product
    private void showRestoreConfirmationDialog(AdminProduct product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Restore Product")
                .setMessage("Are you sure you want to restore this product?")
                .setPositiveButton("Yes", (dialog, which) -> restoreProduct(product))
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .show();
    }

    // Method to restore a product
    private void restoreProduct(AdminProduct product) {
        DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference("products");
        DatabaseReference archivedProductsRef = FirebaseDatabase.getInstance().getReference("archivedProducts");

        String productId = product.getProductId();
        if (productId != null) {
            productsRef.child(productId).setValue(product) // Copy to main products node
                    .addOnSuccessListener(aVoid -> {
                        archivedProductsRef.child(productId).removeValue() // Remove from archivedProducts
                                .addOnSuccessListener(aVoid1 -> {
                                    Toast.makeText(context, "Product restored successfully!", Toast.LENGTH_SHORT).show();
                                    archivedProductList.remove(product); // Remove from local list
                                    notifyDataSetChanged();
                                })
                                .addOnFailureListener(e -> Toast.makeText(context, "Failed to remove from archive.", Toast.LENGTH_SHORT).show());
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Failed to restore product.", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(context, "Product ID is missing. Cannot restore.", Toast.LENGTH_SHORT).show();
        }
    }
}

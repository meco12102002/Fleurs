package com.example.fleursonthego.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // Glide for image loading
import com.example.fleursonthego.Models.Product;
import com.example.fleursonthego.R;
import com.example.fleursonthego.views.ProductDetailsActivity; // Import your ProductDetailActivity
import com.example.fleursonthego.views.ProductDetailsActivity;

import java.util.List;

public class ProductsAdapter extends RecyclerView.Adapter<ProductsAdapter.ProductViewHolder> {

    private final List<Product> productList;
    private final Context context; // Add a context for starting activities

    public ProductsAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        // Set product details
        holder.productName.setText(product.getProductName());
        holder.productCategory.setText(product.getCategory());
        holder.productPrice.setText(String.format("P%.2f", product.getPrice()));
        holder.productRating.setText(String.format("Rating: %.1f", product.getRating()));

        // Load product image
        Glide.with(holder.itemView.getContext())
                .load(product.getImage())
                .into(holder.productImage);

        // Set click listener to open ProductDetailActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailsActivity.class);
            intent.putExtra("productId", product.getProductId()); // Pass the product ID
            intent.putExtra("productName", product.getProductName());
            intent.putExtra("productCategory", product.getCategory());
            intent.putExtra("productPrice", product.getPrice());
            intent.putExtra("productRating", product.getRating());
            intent.putExtra("productImage", product.getImage());
            intent.putExtra("productDescription", product.getProductDescription());// Pass the image URL if needed
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView productName;
        TextView productPrice;
        TextView productCategory;
        TextView productRating;
        ImageView productImage;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.product_name);
            productPrice = itemView.findViewById(R.id.product_price);
            productCategory = itemView.findViewById(R.id.product_category);
            productRating = itemView.findViewById(R.id.product_rating);
            productImage = itemView.findViewById(R.id.product_image);
        }
    }
}

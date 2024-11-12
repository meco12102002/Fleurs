package com.example.fleursonthego.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.fleursonthego.Models.AdminItem;
import com.example.fleursonthego.R;
import com.squareup.picasso.Picasso;  // Add Picasso for image loading

import java.util.List;

public class AdminItemAdapter extends RecyclerView.Adapter<AdminItemAdapter.ItemViewHolder> {

    private List<AdminItem> items;

    public AdminItemAdapter(List<AdminItem> items) {
        this.items = items;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the layout for item details (price, quantity, etc.)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        AdminItem item = items.get(position);

        // Bind the item details to the views
        holder.productNameTextView.setText(item.getProductName());
        holder.productPriceTextView.setText(String.valueOf(item.getPrice()));
        holder.productQuantityTextView.setText("Qty: " + item.getQuantity());

        // Load the product image using Picasso (or Glide)
        // Assuming item.getImageUrl() contains the image URL
        Picasso.get()
                .load(item.getImage())  // URL or resource ID of the image
                .into(holder.productImageView);  // ImageView to load the image into
    }

    @Override
    public int getItemCount() {
        return items.size(); // Return the size of the items list
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        // Declare the TextViews for product name, price, and quantity
        TextView productNameTextView, productPriceTextView, productQuantityTextView;
        ImageView productImageView;  // ImageView for product image

        public ItemViewHolder(View itemView) {
            super(itemView);
            // Initialize the views from the item layout
            productNameTextView = itemView.findViewById(R.id.productName);
            productPriceTextView = itemView.findViewById(R.id.productPrice);
            productQuantityTextView = itemView.findViewById(R.id.productQuantity);
            productImageView = itemView.findViewById(R.id.productImage);  // Make sure this ID matches your XML
        }
    }
}

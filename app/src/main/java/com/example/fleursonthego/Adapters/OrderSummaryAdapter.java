package com.example.fleursonthego.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.fleursonthego.Models.CartItem;
import com.example.fleursonthego.R;
import java.util.List;

public class OrderSummaryAdapter extends RecyclerView.Adapter<OrderSummaryAdapter.OrderSummaryViewHolder> {
    private final List<CartItem> orderItems;

    public OrderSummaryAdapter(List<CartItem> orderItems) {
        this.orderItems = orderItems;
    }

    @NonNull
    @Override
    public OrderSummaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_summary, parent, false);
        return new OrderSummaryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderSummaryViewHolder holder, int position) {
        CartItem item = orderItems.get(position);

        // Set product name, price, and quantity
        holder.productNameTextView.setText(item.getProductName());
        holder.priceTextView.setText("₱" + String.format("%.2f", item.getPrice()));
        holder.quantityTextView.setText("Quantity: " + item.getQuantity());

        // Load product image with Glide
        Glide.with(holder.itemView.getContext())
                .load(item.getImage())  // Assuming `getProductImageUrl` returns the URL of the image
                .placeholder(R.drawable.womanboquet) // Placeholder image if loading takes time
                .error(R.drawable.womanboquet) // Image to show on error
                .into(holder.productImageView);
    }

    @Override
    public int getItemCount() {
        return orderItems.size();
    }

    static class OrderSummaryViewHolder extends RecyclerView.ViewHolder {
        TextView productNameTextView;
        TextView priceTextView;
        TextView quantityTextView;
        ImageView productImageView;

        public OrderSummaryViewHolder(@NonNull View itemView) {
            super(itemView);
            productNameTextView = itemView.findViewById(R.id.productNameTextView);
            priceTextView = itemView.findViewById(R.id.productPriceTextView);
            quantityTextView = itemView.findViewById(R.id.productQuantityTextView);
            productImageView = itemView.findViewById(R.id.productImageView);  // Reference to the ImageView
        }
    }
}

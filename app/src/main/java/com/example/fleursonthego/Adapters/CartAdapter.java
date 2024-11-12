package com.example.fleursonthego.Adapters;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fleursonthego.Models.CartItem;
import com.example.fleursonthego.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    private List<CartItem> cartItemList;
    private Context context;
    private OnTotalPriceChangeListener totalPriceChangeListener;

    public CartAdapter(Context context, List<CartItem> cartItemList , OnTotalPriceChangeListener listener) {
        this.context = context;
        this.cartItemList = cartItemList;
        this.totalPriceChangeListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(view);
    }
    public interface OnTotalPriceChangeListener {
        void onTotalPriceChanged(double total);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem cartItem = cartItemList.get(position);
        if (cartItem != null) {
            holder.productName.setText(cartItem.getProductName());
            holder.productPrice.setText("₱" + String.format("%.2f", cartItem.getPrice()));
            holder.productQuantity.setText("Qty: " + cartItem.getQuantity());

            // Load the product image using Glide
            Glide.with(context).load(cartItem.getImage()).into(holder.productImage);

            // Set the checkbox state
            holder.selectCheckbox.setChecked(cartItem.isSelected());


            // Set listeners for quantity buttons
            holder.minusButton.setOnClickListener(v -> {
                int quantity = cartItem.getQuantity();
                if (quantity > 1) {
                    cartItem.setQuantity(quantity - 1); // Update quantity
                    holder.productQuantity.setText("Qty: " + (quantity - 1));
                }
                updateTotalPrice();
                updateDatabaseQuantity(cartItem);
            });

            holder.plusButton.setOnClickListener(v -> {
                int quantity = cartItem.getQuantity();
                cartItem.setQuantity(quantity + 1); // Update quantity
                holder.productQuantity.setText("Qty: " + (quantity + 1));
                updateTotalPrice();
                updateDatabaseQuantity(cartItem);
            });

            // Set checkbox listener
            holder.selectCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                cartItem.setSelected(isChecked); // Manage selection state
                updateTotalPrice(); // Update total price if selection changes

            });
        } else {
            Log.e(TAG, "CartItem is null at position: " + position);
        }
    }

    @Override
    public int getItemCount() {
        return cartItemList != null ? cartItemList.size() : 0;
    }

    public void updateTotalPrice() {
        double total = calculateTotalSelectedPrice();
        Log.d(TAG, "Total price of selected items: ₱" + String.format("%.2f", total));

        // Notify the listener (CartActivity) about the total price change
        if (totalPriceChangeListener != null) {
            totalPriceChangeListener.onTotalPriceChanged(total);
        }
    }





    public double calculateTotalSelectedPrice() {
        double total = 0.0;

        for (CartItem cartItem : cartItemList) {
            if (cartItem.isSelected()) { // Only consider selected items
                total += cartItem.getPrice() * cartItem.getQuantity(); // Calculate total price
            }
        }
        Log.d(TAG, "Calculated total selected price: ₱" + String.format("%.2f", total));
        return total; // Return the total price
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView productName;
        TextView productPrice;
        TextView productQuantity;
        ImageView productImage;
        CheckBox selectCheckbox; // Add checkbox
        Button minusButton; // Button for decreasing quantity
        Button plusButton; // Button for increasing quantity

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            productQuantity = itemView.findViewById(R.id.productQuantity);
            productImage = itemView.findViewById(R.id.productImage);
            selectCheckbox = itemView.findViewById(R.id.selectCheckbox); // Initialize checkbox
            minusButton = itemView.findViewById(R.id.minusButton); // Initialize minus button
            plusButton = itemView.findViewById(R.id.plusButton); // Initialize plus button
            Log.d(TAG, "ViewHolder initialized: " + productName + ", " + productPrice + ", " + productQuantity + ", " + productImage);
        }
    }

    private void updateDatabaseQuantity(CartItem cartItem) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("cart").child(cartItem.getProductId()); // Assuming getId() returns the unique key of the cart item

        cartRef.child("quantity").setValue(cartItem.getQuantity()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Quantity updated in database for item: " + cartItem.getProductName());
            } else {
                Log.e(TAG, "Failed to update quantity in database: " + task.getException());
            }
        });

    }}

package com.example.fleursonthego.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fleursonthego.Models.AdminOrderModel;
import com.example.fleursonthego.R;

import java.util.List;

public class AdminItemsAdapter extends RecyclerView.Adapter<AdminItemsAdapter.ItemViewHolder> {
    private Context context;
    private List<AdminOrderModel.Item> itemList;

    public AdminItemsAdapter(Context context, List<AdminOrderModel.Item> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_order, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        AdminOrderModel.Item item = itemList.get(position);

        // Set product image using Glide (or any image loading library)
        Glide.with(context).load(item.getImage()).into(holder.productImageView);

        // Set other details
        holder.productNameTextView.setText(item.getProductName());
        holder.productPriceTextView.setText("₱" + item.getPrice());
        holder.productQuantityTextView.setText("Qty: " + item.getQuantity());
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView productImageView;
        TextView productNameTextView, productPriceTextView, productQuantityTextView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            productImageView = itemView.findViewById(R.id.productImageView);
            productNameTextView = itemView.findViewById(R.id.productNameTextView);
            productPriceTextView = itemView.findViewById(R.id.productPriceTextView);
            productQuantityTextView = itemView.findViewById(R.id.productQuantityTextView);
        }
    }
}

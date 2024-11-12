package com.example.fleursonthego.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fleursonthego.Models.Orders;
import com.example.fleursonthego.R;

import java.util.List;

public class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.ItemViewHolder> {

    private List<Orders.Item> itemList;

    public ItemsAdapter(List<Orders.Item> itemList) {
        this.itemList = itemList;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_products, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        Orders.Item item = itemList.get(position);
        holder.productName.setText(item.getProductName());
        holder.productPrice.setText("P" + item.getPrice());
        holder.productQuantity.setText("Quantity: " + item.getQuantity());
        Glide.with(holder.productImage.getContext())
                .load(item.getImage())
                .into(holder.productImage);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productPrice, productQuantity;
        ImageView productImage;

        public ItemViewHolder(View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.productName1);
            productPrice = itemView.findViewById(R.id.productPrice1);
            productQuantity = itemView.findViewById(R.id.productQuantity1);
            productImage = itemView.findViewById(R.id.productImage1);
        }
    }
}

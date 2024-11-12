package com.example.fleursonthego.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fleursonthego.R;

public class FlowerAdapter extends RecyclerView.Adapter<FlowerAdapter.ViewHolder> {
    private final Context context;
    private final int[] images;
    private final OnFlowerClickListener onFlowerClickListener;

    public FlowerAdapter(Context context, int[] images, OnFlowerClickListener onFlowerClickListener) {
        this.context = context;
        this.images = images;
        this.onFlowerClickListener = onFlowerClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.flower_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.flowerImage.setImageResource(images[position]);
        holder.itemView.setOnClickListener(v -> onFlowerClickListener.onFlowerSelected(images[position]));
    }

    @Override
    public int getItemCount() {
        return images.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView flowerImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            flowerImage = itemView.findViewById(R.id.flowerImage); // Make sure this ID matches your layout
        }
    }

    public interface OnFlowerClickListener {
        void onFlowerSelected(int imageResource);


    }
}

package com.example.fleursonthego.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fleursonthego.R;

public class WrapAdapter extends RecyclerView.Adapter<WrapAdapter.ViewHolder> {
    private final Context context;
    private final int[] images; // Array of wrap images
    private final OnWrapClickListener onWrapClickListener;

    public WrapAdapter(Context context, int[] images, OnWrapClickListener onWrapClickListener) {
        this.context = context;
        this.images = images;
        this.onWrapClickListener = onWrapClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.wrap_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.wrapImage.setImageResource(images[position]);
        holder.itemView.setOnClickListener(v -> onWrapClickListener.onWrapSelected(images[position]));
    }

    @Override
    public int getItemCount() {
        return images.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView wrapImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            wrapImage = itemView.findViewById(R.id.wrapImage); // Ensure this ID matches your layout
        }
    }

    public interface OnWrapClickListener {
        void onWrapSelected(int imageResource);
    }
}

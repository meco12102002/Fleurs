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

import com.example.fleursonthego.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.squareup.picasso.Picasso;
import com.example.fleursonthego.Models.UserModel;
import com.example.fleursonthego.views.ChatActivity;

public class SearchUserRecyclerAdapter extends FirebaseRecyclerAdapter<UserModel, SearchUserRecyclerAdapter.UserModelViewHolder> {

    private Context context;

    public SearchUserRecyclerAdapter(@NonNull FirebaseRecyclerOptions<UserModel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull UserModelViewHolder holder, int position, @NonNull UserModel model) {
        holder.name.setText(model.getName());
        holder.phoneNumber.setText(model.getPhoneNumber());

        if (model.getProfileImageUrl() != null && !model.getProfileImageUrl().isEmpty()) {
            Picasso.get().load(model.getProfileImageUrl()).into(holder.profileImage);
        }

        // Set the onClick listener for each item
        holder.itemView.setOnClickListener(v -> {
            // Get the clicked user's data
            String userId = getRef(position).getKey();
            String userName = model.getName();
            String profileImageUrl = model.getProfileImageUrl();
            String phoneNumber = model.getPhoneNumber();

            // Open the ChatActivity with the selected data
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("userName", userName);
            intent.putExtra("profileImageUrl", profileImageUrl);
            intent.putExtra("phoneNumber", phoneNumber);  // Add phone number
            context.startActivity(intent);
        });
    }


    @NonNull
    @Override
    public UserModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.search_user_recycler_row, parent, false);
        return new UserModelViewHolder(itemView);
    }

    public class UserModelViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView phoneNumber;
        ImageView profileImage;

        public UserModelViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.textUserName_row);
            phoneNumber = itemView.findViewById(R.id.textPhoneNumber_row);
            profileImage = itemView.findViewById(R.id.profilePictureView_row);
        }
    }
}

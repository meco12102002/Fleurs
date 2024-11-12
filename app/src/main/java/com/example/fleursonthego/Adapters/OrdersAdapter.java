package com.example.fleursonthego.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fleursonthego.Models.Orders;
import com.example.fleursonthego.R;
import com.example.fleursonthego.views.ChatListActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {

    private List<Orders> orderList;
    private Context context;

    public OrdersAdapter(Context context, List<Orders> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @Override
    public OrderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_orders, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(OrderViewHolder holder, int position) {
        Orders order = orderList.get(position);

        holder.orderName.setText(order.getName());
        holder.orderDate.setText(order.getDeliveryDate());
        holder.orderStatus.setText("Status: " + order.getStatus());
        holder.totalPrice.setText("Total: P" + order.getTotalPrice());
        holder.orderId.setText("Order ID: " + order.getOrderId());
        holder.paymentMethod.setText("Payment Method: " + order.getPaymentMethod());
        holder.deliveryDetails.setText("Delivery Details: " + order.getDeliveryDetails());
        holder.deliveryRiderName.setText("Delivery Rider: " + order.getRiderName());
        holder.deliveryPhoneNumber.setText("Phone: " + order.getRiderPhone());

        // Set up nested RecyclerView for items
        ItemsAdapter itemsAdapter = new ItemsAdapter(order.getItems());
        holder.itemsRecyclerView.setAdapter(itemsAdapter);

        // Enable or disable Cancel Order button based on order status
        holder.btnCancelOrder.setEnabled(order.getStatus().equals("Order Placed"));

        // Check if the status is "Order Placed" to disable the rate button
        if (order.getStatus().equals("Order Placed")) {
            holder.review.setEnabled(false);  // Disable the "Rate" button
            holder.review.setText("Cannot rate until order is completed");

        }else if(order.getStatus().equals("Accepted")){
            holder.review.setEnabled(false);
            holder.review.setText("Cannot rate until order is completed");
        } else {
            // Check if the user has already rated this order
            DatabaseReference reviewRef = FirebaseDatabase.getInstance().getReference("reviews").child(order.getOrderId());
            reviewRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // The user has already rated the order, show the rating
                        String rating = dataSnapshot.child("rating").getValue(String.class);
                        holder.review.setText("You rated: " + rating + " Stars");  // Display the rating instead of the 'Rate' button
                        holder.review.setEnabled(false);  // Disable the "Rate" button
                    } else {
                        // The user hasn't rated the order, show the "Rate" button
                        holder.review.setText("Rate this order");
                        holder.review.setEnabled(true);  // Enable the "Rate" button
                        holder.review.setOnClickListener(v -> showRatingDialog(order));  // Set listener for rating dialog
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle any errors (e.g., connection issues)
                    Toast.makeText(context, "Error loading review", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Contact Seller button action
        holder.btnContactSeller.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatListActivity.class);
            intent.putExtra("orderId", order.getOrderId());
            context.startActivity(intent);
        });

        // Cancel Order button action
        holder.btnCancelOrder.setOnClickListener(v -> showCancelOrderDialog(order, order.getUserId()));
    }


    @Override
    public int getItemCount() {
        return orderList.size();
    }

    private void showCancelOrderDialog(Orders order, String uid) {
        String[] reasons = {"Change of mind", "Wrong address", "Wrong mode of payment", "Others"};
        final String[] selectedReason = new String[1];

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select a reason for cancellation")
                .setSingleChoiceItems(reasons, -1, (dialog, which) -> selectedReason[0] = reasons[which])
                .setPositiveButton("Cancel Order", (dialog, which) -> {
                    if (selectedReason[0] != null) {
                        removeOrderFromFirebase(order, uid, selectedReason[0]);


                    } else {
                        Toast.makeText(context, "Please select a reason.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Dismiss", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void removeOrderFromFirebase(Orders order, String uid, String reason) {
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("orders").child(uid).child(order.getOrderId());
        DatabaseReference cancelledOrdersRef = FirebaseDatabase.getInstance().getReference("cancelled_orders").child(uid).child(order.getOrderId());

        // Set the order status to "Order Cancelled" before moving it
        order.setStatus("Order Cancelled");

        // Add order to cancelled_orders with cancellation reason
        cancelledOrdersRef.setValue(order).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Add the cancellation reason to the cancelled order
                cancelledOrdersRef.child("cancellationReason").setValue(reason).addOnCompleteListener(innerTask -> {
                    if (innerTask.isSuccessful()) {
                        // Remove the order from the orders node
                        ordersRef.removeValue().addOnCompleteListener(removeTask -> {
                            if (removeTask.isSuccessful()) {
                                Toast.makeText(context, "Order canceled. Reason: " + reason, Toast.LENGTH_SHORT).show();
                                orderList.remove(order);
                                notifyDataSetChanged();
                            } else {
                                Toast.makeText(context, "Failed to remove order. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(context, "Failed to add cancellation reason. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(context, "Failed to move order to cancelled_orders. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }




    private void saveReviewToFirebase(Orders order, String rating, String review) {
        DatabaseReference reviewRef = FirebaseDatabase.getInstance().getReference("reviews").child(order.getOrderId());

        // Add the order details to the review
        reviewRef.child("rating").setValue(rating);
        reviewRef.child("review").setValue(review);
        reviewRef.child("customerName").setValue(order.getName());  // Assuming the customer name is in the Order model
        reviewRef.child("totalPrice").setValue(order.getTotalPrice());
        reviewRef.child("orderItems").setValue(order.getItems());  // This assumes you have a method that returns the items as a list

        // You can add any other order details that are relevant
        reviewRef.child("orderDate").setValue(order.getDeliveryDate());
        reviewRef.child("paymentMethod").setValue(order.getPaymentMethod());
        reviewRef.child("deliveryDetails").setValue(order.getDeliveryDetails());

        // Use a value event listener to track the completion of the operation
        reviewRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This will be called once the write is complete
                Toast.makeText(context, "Thank you for your review!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // This will be called if the operation is cancelled
                Toast.makeText(context, "Failed to submit review. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void showRatingDialog(Orders order) {
        View ratingDialogView = LayoutInflater.from(context).inflate(R.layout.dialog_review_order, null);

        final RadioGroup ratingGroup = ratingDialogView.findViewById(R.id.ratingGroup);
        final TextInputEditText reviewText = ratingDialogView.findViewById(R.id.reviewText);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Rate your Order")
                .setView(ratingDialogView)
                .setPositiveButton("Submit", (dialog, which) -> {
                    int selectedRatingId = ratingGroup.getCheckedRadioButtonId();
                    RadioButton selectedRadioButton = ratingDialogView.findViewById(selectedRatingId);
                    String rating = selectedRadioButton != null ? selectedRadioButton.getText().toString() : "No rating";

                    String review = reviewText.getText().toString();

                    // Save the rating and review to Firebase
                    saveReviewToFirebase(order, rating, review);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }



    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderName, orderDate, orderStatus, totalPrice;
        TextView orderId, paymentMethod, deliveryDetails;
        RecyclerView itemsRecyclerView;
        Button btnContactSeller, btnCancelOrder;
        MaterialButton review;
        MaterialTextView deliveryRiderName, deliveryPhoneNumber;

        public OrderViewHolder(View itemView) {
            super(itemView);
            orderName = itemView.findViewById(R.id.orderName1);
            orderDate = itemView.findViewById(R.id.orderDate1);
            orderStatus = itemView.findViewById(R.id.orderStatus1);
            totalPrice = itemView.findViewById(R.id.totalPrice);
            deliveryRiderName = itemView.findViewById(R.id.deliveryRiderName);
            deliveryPhoneNumber = itemView.findViewById(R.id.deliveryRiderPhoneNumber);

            orderId = itemView.findViewById(R.id.orderId1);
            paymentMethod = itemView.findViewById(R.id.paymentMethod1);
            deliveryDetails = itemView.findViewById(R.id.deliveryDetails1);

            itemsRecyclerView = itemView.findViewById(R.id.itemsRecyclerView1);
            itemsRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));

            // Initialize buttons
            btnContactSeller = itemView.findViewById(R.id.btnContactSeller);
            btnCancelOrder = itemView.findViewById(R.id.btnCancelOrder);

            review = itemView.findViewById(R.id.btnRateOrder);
        }
    }
}

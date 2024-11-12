package com.example.fleursonthego.Adapters;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fleursonthego.FCMNotificationHelper;
import com.example.fleursonthego.Models.AdminOrder;
import com.example.fleursonthego.Models.AdminItem;
import com.example.fleursonthego.Models.SmsSender;
import com.example.fleursonthego.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminOrdersAdapter extends RecyclerView.Adapter<AdminOrdersAdapter.OrderViewHolder> {

    private List<AdminOrder> orders;

    public AdminOrdersAdapter(List<AdminOrder> orders) {
        this.orders = orders;
    }

    @Override
    public OrderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the layout for the orders and return the ViewHolder
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_order, parent, false);
        return new OrderViewHolder(view);
    }


    @Override
    public void onBindViewHolder(OrderViewHolder holder, int position) {
        AdminOrder order = orders.get(position);

        // Set data for the order fields
        holder.orderNameTextView.setText(order.getName());
        holder.orderDateTextView.setText(order.getDeliveryDate());
        holder.orderStatusTextView.setText(order.getStatus());
        holder.totalPriceTextView.setText(String.valueOf(order.getTotalPrice()));

        // Enable/Disable and change button text based on the order's status
        if ("Order Placed".equalsIgnoreCase(order.getStatus())) {
            // Set button text and enable it for orders with "Order Placed" status
            holder.acceptButton.setText("Accept");
            holder.acceptButton.setEnabled(true);
            holder.acceptButton.setAlpha(1.0f); // Set alpha to 100% (fully visible)
            holder.acceptButton.setOnClickListener(v -> {
                // Show the confirmation dialog before accepting the order
                showAcceptConfirmationDialog(v, order);
            });

            // Set Reject button functionality
            holder.rejectButton.setText("Reject");
            holder.rejectButton.setEnabled(true);
            holder.rejectButton.setAlpha(1.0f); // Set alpha to 100% (fully visible)
            holder.rejectButton.setOnClickListener(v -> {
                // Show reject confirmation dialog
                showRejectConfirmationDialog(v, order);
            });
        } else if ("Accepted".equalsIgnoreCase(order.getStatus())) {



            // Change button text to "Transaction Done" for accepted orders
            holder.acceptButton.setText("Transaction Done");
            holder.acceptButton.setEnabled(true);
            holder.acceptButton.setAlpha(1.0f); // Keep it visible (no dimming)
            holder.acceptButton.setOnClickListener(v -> {
                // Handle the transaction completion here
                showTransactionDoneDialog(v, order);
            });

            // Disable reject button for accepted orders
            holder.rejectButton.setText("Reject");
            holder.rejectButton.setEnabled(false);
            holder.rejectButton.setAlpha(0.5f); // Set alpha to 50% (dimmed)
        } else {
            // Disable both buttons and set them as "Transaction Completed" for completed or rejected orders
            holder.acceptButton.setText("Transaction Completed");
            holder.acceptButton.setEnabled(false);
            holder.acceptButton.setAlpha(0.5f); // Set alpha to 50% (dimmed)

            holder.rejectButton.setText("--------");
            holder.rejectButton.setEnabled(false);
            holder.rejectButton.setAlpha(0.5f); // Set alpha to 50% (dimmed)
        }

        // Create and set the items adapter for the nested RecyclerView (order items)
        AdminItemAdapter itemAdapter = new AdminItemAdapter(order.getItems());
        holder.itemsRecyclerView.setAdapter(itemAdapter);
    }


    @Override
    public int getItemCount() {
        return orders.size(); // Return the size of the orders list
    }


    private void showTransactionDoneDialog(View view, AdminOrder order) {
        new AlertDialog.Builder(view.getContext())
                .setTitle("Transaction Completed")
                .setMessage("Are you sure you want to mark this order as completed?")
                .setCancelable(false) // Disable cancel by tapping outside the dialog
                .setPositiveButton("Yes", (dialog, id) -> {
                    completeTransaction(view, order); // Mark transaction as completed if confirmed
                    Toast.makeText(view.getContext(), "Order " + order.getName() + " is marked as completed.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", (dialog, id) -> dialog.dismiss()) // Close dialog on 'No'
                .create()
                .show();
    }



    // Method to decrease the product quantity in the inventory
    private void decreaseProductQuantities(List<AdminItem> orderedItems, Context context) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        for (AdminItem item : orderedItems) {
            DatabaseReference productRef = database.getReference("products")
                    .child(item.getProductId()); // Reference to the specific product by product ID

            // Decrease the stock of the product by the quantity ordered
            productRef.child("stock")
                    .runTransaction(new Transaction.Handler() {
                        @Override
                        public Transaction.Result doTransaction(MutableData mutableData) {
                            Integer currentStock = mutableData.getValue(Integer.class);
                            if (currentStock == null) {
                                return Transaction.success(mutableData); // No stock data
                            }

                            // Ensure stock doesn't go negative
                            if (currentStock < item.getQuantity()) {
                                // Optionally show a toast or handle this case separately
                                return Transaction.abort(); // Abort the transaction if stock is insufficient
                            }

                            // Decrease the stock
                            mutableData.setValue(currentStock - item.getQuantity());
                            return Transaction.success(mutableData); // Update the stock in the database
                        }

                        @Override
                        public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                            if (databaseError != null) {
                                Toast.makeText(context, "Failed to update product stock.", Toast.LENGTH_SHORT).show();
                            } else {
                                // Optionally show success toast or log
                                Toast.makeText(context, "Product stock updated successfully.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void completeTransaction(View view, AdminOrder order) {
        // Get the reference to your Firebase database
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        // Reference to the user's data in the 'users' node
        DatabaseReference userRef = database.getReference("users")
                .child(order.getUserId()); // Reference to the specific user by their UID

        // Retrieve the user's phone number from the 'users' node
        userRef.child("phoneNumber").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // If the phone number is retrieved successfully
                String userPhoneNumber = task.getResult().getValue(String.class); // Assuming phoneNumber is a String field

                // Now, move the order to the transactions node
                DatabaseReference ordersRef = database.getReference("orders")
                        .child(order.getUserId())  // User ID to navigate to specific user
                        .child(order.getOrderId()); // Specific order ID

                DatabaseReference transactionsRef = database.getReference("transactions")
                        .child(order.getUserId())  // User ID to store under their transactions
                        .child(order.getOrderId()); // Store the order in the transactions node

                // Prepare the order data to be moved, including the user's phone number
                HashMap<String, Object> orderData = new HashMap<>();
                orderData.put("orderId", order.getOrderId());
                orderData.put("userId", order.getUserId());
                orderData.put("name", order.getName());
                orderData.put("deliveryDate", order.getDeliveryDate());
                orderData.put("status", "Completed");
                orderData.put("totalPrice", order.getTotalPrice());
                orderData.put("items", order.getItems()); // Include all items in the order
                orderData.put("paymentMethod", order.getPaymentMethod()); // Include payment method
                orderData.put("deliveryDetails", order.getDeliveryDetails()); // Include delivery details
                orderData.put("deliveryTime", order.getDeliveryTime()); // Include delivery time
                orderData.put("riderName", order.getDeliveryPersonName());
                orderData.put("riderPhone", order.getDeliveryPersonPhone());
                orderData.put("userPhoneNumber", userPhoneNumber); // Add the user's phone number to the transaction

                // Move the order to the transactions node
                transactionsRef.setValue(orderData)
                        .addOnCompleteListener(transactionTask -> {
                            if (transactionTask.isSuccessful()) {
                                // Successfully transferred order to transactions node
                                Toast.makeText(view.getContext(), "Transaction Completed!", Toast.LENGTH_SHORT).show();
                                sendSms(userPhoneNumber, "Your order has been completed! Thank you for your patronage! Please send us a Review in our facebook page or even in the application! Happy shopping!");

                                // Optionally, delete the order from the orders node after it's been moved
                                ordersRef.removeValue()
                                        .addOnCompleteListener(removeTask -> {
                                            if (removeTask.isSuccessful()) {
                                                // Successfully deleted the order from orders
                                                Toast.makeText(view.getContext(), "Order removed from orders.", Toast.LENGTH_SHORT).show();
                                                decreaseProductQuantities(order.getItems(), view.getContext());

                                                // Update the UI (notify adapter, etc.)
                                                order.setStatus("Transaction Completed");
                                                notifyItemChanged(orders.indexOf(order));  // Update the UI in RecyclerView
                                            } else {
                                                Toast.makeText(view.getContext(), "Failed to remove the order from orders.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                // Handle failure to move order
                                Toast.makeText(view.getContext(), "Failed to complete transaction.", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                // If the phone number retrieval failed
                Toast.makeText(view.getContext(), "Failed to retrieve user's phone number", Toast.LENGTH_SHORT).show();
            }
        });
    }





    private void showRejectConfirmationDialog(View view, AdminOrder order) {
        new AlertDialog.Builder(view.getContext())
                .setTitle("Reject Order")
                .setMessage("Are you sure you want to reject this order?")
                .setCancelable(false) // Disable cancel by tapping outside the dialog
                .setPositiveButton("Yes", (dialog, id) -> {
                    rejectOrder(view, order); // Reject the order if the user confirms
                    Toast.makeText(view.getContext(), "Order " + order.getName() + " has been rejected.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", (dialog, id) -> dialog.dismiss()) // Close dialog on 'No'
                .create()
                .show();
    }

    private void rejectOrder(View view, AdminOrder order) {
        // Get the reference to your Firebase database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference orderRef = database.getReference("orders")
                .child(order.getUserId())          // Reference to the specific user ID
                .child(order.getOrderId())         // Reference to the specific order by order ID
                .child("status");                  // Reference to the status field of the order

        // Update the status of the order to "Rejected"
        orderRef.setValue("Rejected")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Successfully updated the status to "Rejected"
                        Toast.makeText(view.getContext(), "Order status updated to Rejected", Toast.LENGTH_SHORT).show();

                        // Optionally, update the UI or notify the user here
                        order.setStatus("Rejected");
                        notifyItemChanged(orders.indexOf(order));  // Update the RecyclerView item
                    } else {
                        Toast.makeText(view.getContext(), "Failed to update order status", Toast.LENGTH_SHORT).show();
                    }
                });
    }





    private void showAcceptConfirmationDialog(View view, AdminOrder order) {
        // Inflate the custom delivery details dialog layout
        View dialogView = LayoutInflater.from(view.getContext()).inflate(R.layout.dialog_delivery_details, null);

        // Find the dialog elements by their IDs
        TextInputEditText riderNameEditText = dialogView.findViewById(R.id.riderNameEditText);
        TextInputEditText riderPhoneEditText = dialogView.findViewById(R.id.riderPhoneEditText);
        TimePicker estimatedTimePicker = dialogView.findViewById(R.id.estimatedTimePicker);
        MaterialButton confirmDeliveryButton = dialogView.findViewById(R.id.confirmDeliveryButton);

        // Set up the dialog
        AlertDialog dialog = new AlertDialog.Builder(view.getContext())
                .setView(dialogView)
                .create();
        dialog.show();

        // Configure the TimePicker for 12-hour format
        estimatedTimePicker.setIs24HourView(false);

        // Set the Confirm button click listener
        confirmDeliveryButton.setOnClickListener(v -> {
            String riderName = riderNameEditText.getText().toString().trim();
            String riderPhone = riderPhoneEditText.getText().toString().trim();

            int hour = estimatedTimePicker.getCurrentHour();
            int minute = estimatedTimePicker.getCurrentMinute();
            String period = (hour >= 12) ? "PM" : "AM";
            hour = (hour == 0 || hour == 12) ? 12 : hour % 12; // Convert 0/12 to 12 for AM/PM format
            String estimatedTime = String.format("%02d:%02d %s", hour, minute, period);

            // Validate inputs
            if (riderName.isEmpty() || riderPhone.isEmpty()) {
                Toast.makeText(view.getContext(), "Please fill out all fields.", Toast.LENGTH_SHORT).show();
            } else {
                // Pass data to acceptOrder method
                acceptOrder(view, order, riderName, riderPhone, estimatedTime);
                dialog.dismiss();
            }
        });




    }



    private void sendSms(String to, String message) {
        // Initialize the SmsSender
        SmsSender smsSender = new SmsSender();

        // Use the sendSms method from SmsSender class
        smsSender.sendSms(to, message, new SmsSender.SmsSendCallback() {
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "SMS sent successfully: " + response);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to send SMS: ", e);
            }
        });
    }

    private void acceptOrder(View view, AdminOrder order, String riderName, String riderPhone, String estimatedTime) {
        // Get the reference to your Firebase database
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        // Reference to the user's data in the 'users' node
        DatabaseReference userRef = database.getReference("users")
                .child(order.getUserId()); // Reference to the specific user by their UID

        // Retrieve the user's phone number from the 'users' node
        userRef.child("phoneNumber").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // If the phone number is retrieved successfully
                String userPhoneNumber = task.getResult().getValue(String.class);  // Assuming phoneNumber is a String field
                sendSms(userPhoneNumber, "Hello, your order has been accepted by GreenRed Winluck and it will be delivered shortly. Thank you for your patronage!");

                // Now, update the order in the 'orders' node
                DatabaseReference orderRef = database.getReference("orders")
                        .child(order.getUserId())  // Reference to the specific user ID
                        .child(order.getOrderId()); // Reference to the specific order by order ID

                // Create a map to update multiple fields in the order
                Map<String, Object> orderUpdates = new HashMap<>();
                orderUpdates.put("status", "Accepted");                // Update the status of the order to "Accepted"
                orderUpdates.put("riderName", riderName);              // Add rider's name
                orderUpdates.put("riderPhone", riderPhone);            // Add rider's phone number
                orderUpdates.put("estimatedDeliveryTime", estimatedTime); // Add estimated delivery time
                orderUpdates.put("userPhoneNumber", userPhoneNumber); // Add the user's phone number

                // Update the order in Firebase
                orderRef.updateChildren(orderUpdates)
                        .addOnCompleteListener(updateTask -> {
                            if (updateTask.isSuccessful()) {
                                // Successfully updated the order details
                                Toast.makeText(view.getContext(), "Order details updated", Toast.LENGTH_SHORT).show();
                                FCMNotificationHelper.sendNotification(view.getContext(), "Order Update", "An order has been processed");

                                // Optionally, update the UI or notify the user here
                                order.setStatus("Accepted");
                                order.setDeliveryPersonName(riderName);
                                order.setDeliveryPersonPhone(riderPhone);
                                order.setEstimatedDeliveryTime(estimatedTime);
                                notifyItemChanged(orders.indexOf(order));  // Update the RecyclerView item
                            } else {
                                Toast.makeText(view.getContext(), "Failed to update order details", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                // If the phone number retrieval failed
                Toast.makeText(view.getContext(), "Failed to retrieve user's phone number", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        // Declare the TextViews and RecyclerView for the nested items
        TextView orderNameTextView, orderDateTextView, orderStatusTextView, totalPriceTextView;
        RecyclerView itemsRecyclerView;
        MaterialButton acceptButton, rejectButton;

        public OrderViewHolder(View itemView) {
            super(itemView);
            // Initialize the views from the layout
            orderNameTextView = itemView.findViewById(R.id.orderName1);
            orderDateTextView = itemView.findViewById(R.id.orderDate1);
            orderStatusTextView = itemView.findViewById(R.id.orderStatus1);
            totalPriceTextView = itemView.findViewById(R.id.totalPrice);
            itemsRecyclerView = itemView.findViewById(R.id.itemsRecyclerView1);

            // Set the layout manager for the nested RecyclerView (items list)
            itemsRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            acceptButton = itemView.findViewById(R.id.btnAccept); // Replace with actual button ID in your XML
            rejectButton = itemView.findViewById(R.id.btnReject);




        }




    }
}

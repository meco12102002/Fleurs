package com.example.fleursonthego.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fleursonthego.Models.Transaction;
import com.example.fleursonthego.R;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactionList;

    public TransactionAdapter(List<Transaction> transactionList) {
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_item, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);

        // Set transaction details
        holder.orderIdTextView.setText("Order ID: " + transaction.getOrderId());
        holder.statusTextView.setText("Status: " + transaction.getStatus());
        holder.paymentMethodTextView.setText(transaction.getPaymentMethod());
        holder.totalPriceTextView.setText("Total: " + transaction.getTotalPrice());
        holder.deliveryDateTextView.setText("Delivery Date: " + transaction.getDeliveryDate());
        holder.deliveryRiderNameTextView.setText("Delivery Rider: " + transaction.getRiderName());
        holder.deliveryRiderPhoneTextView.setText("Phone: " + transaction.getRiderPhone());
        AdminItemAdapter itemsAdapter = new AdminItemAdapter(transaction.getItems());
        holder.orderItemsRecyclerView.setAdapter(itemsAdapter);
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {

        public TextView orderIdTextView;
        public TextView statusTextView;
        public TextView paymentMethodTextView;
        public TextView totalPriceTextView;
        public TextView deliveryDateTextView;
        public RecyclerView orderItemsRecyclerView;
        public TextView deliveryRiderNameTextView; // New field for delivery rider name
        public TextView deliveryRiderPhoneTextView;

        public TransactionViewHolder(View itemView) {
            super(itemView);
            orderIdTextView = itemView.findViewById(R.id.orderIdTextView);
            deliveryRiderNameTextView = itemView.findViewById(R.id.deliveryRiderNameTextView); // Binding the new view
            deliveryRiderPhoneTextView = itemView.findViewById(R.id.deliveryRiderPhoneTextView); //
            statusTextView = itemView.findViewById(R.id.statusTextView);
            paymentMethodTextView = itemView.findViewById(R.id.paymentMethodTextView);
            totalPriceTextView = itemView.findViewById(R.id.totalPriceTextView);
            deliveryDateTextView = itemView.findViewById(R.id.deliveryDateTextView);
            orderItemsRecyclerView = itemView.findViewById(R.id.itemsRecyclerView);
            orderItemsRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
        }
    }
}

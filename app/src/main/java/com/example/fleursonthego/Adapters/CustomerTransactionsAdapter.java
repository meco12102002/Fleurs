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

public class CustomerTransactionsAdapter extends RecyclerView.Adapter<CustomerTransactionsAdapter.CustomerTransactionViewHolder> {

    private List<Transaction> customerTransactionList;

    public CustomerTransactionsAdapter(List<Transaction> customerTransactionList) {
        this.customerTransactionList = customerTransactionList;
    }

    @NonNull
    @Override
    public CustomerTransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.customer_transaction_item, parent, false);
        return new CustomerTransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerTransactionViewHolder holder, int position) {
        Transaction customerTransaction = customerTransactionList.get(position);

        // Set transaction details
        holder.orderIdTextView.setText("Order ID: " + customerTransaction.getOrderId());
        holder.statusTextView.setText("Status: " + customerTransaction.getStatus());
        holder.paymentMethodTextView.setText(customerTransaction.getPaymentMethod());
        holder.totalPriceTextView.setText("Total: " + customerTransaction.getTotalPrice());
        holder.deliveryDateTextView.setText("Delivery Date: " + customerTransaction.getDeliveryDate());
        holder.deliveryRiderNameTextView.setText("Delivery Rider: " + customerTransaction.getRiderName());
        holder.deliveryRiderPhoneTextView.setText("Phone: " + customerTransaction.getRiderPhone());
        AdminItemAdapter itemsAdapter = new AdminItemAdapter(customerTransaction.getItems());
        holder.orderItemsRecyclerView.setAdapter(itemsAdapter);
    }

    @Override
    public int getItemCount() {
        return customerTransactionList.size();
    }

    public static class CustomerTransactionViewHolder extends RecyclerView.ViewHolder {

        public TextView orderIdTextView;
        public TextView statusTextView;
        public TextView paymentMethodTextView;
        public TextView totalPriceTextView;
        public TextView deliveryDateTextView;
        public RecyclerView orderItemsRecyclerView;
        public TextView deliveryRiderNameTextView;
        public TextView deliveryRiderPhoneTextView;

        public CustomerTransactionViewHolder(View itemView) {
            super(itemView);
            orderIdTextView = itemView.findViewById(R.id.orderIdTextView);
            deliveryRiderNameTextView = itemView.findViewById(R.id.deliveryRiderNameTextView);
            deliveryRiderPhoneTextView = itemView.findViewById(R.id.deliveryRiderPhoneTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            paymentMethodTextView = itemView.findViewById(R.id.paymentMethodTextView);
            totalPriceTextView = itemView.findViewById(R.id.totalPriceTextView);
            deliveryDateTextView = itemView.findViewById(R.id.deliveryDateTextView);
            orderItemsRecyclerView = itemView.findViewById(R.id.itemsRecyclerView);
            orderItemsRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
        }
    }
}

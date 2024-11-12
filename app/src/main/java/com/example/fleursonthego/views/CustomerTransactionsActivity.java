package com.example.fleursonthego.views;

import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fleursonthego.Adapters.CustomerTransactionsAdapter;
import com.example.fleursonthego.Models.Transaction;
import com.example.fleursonthego.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class CustomerTransactionsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewTransactions;
    private CustomerTransactionsAdapter customerTransactionsAdapter;
    private List<Transaction> customerTransactionList;
    private DatabaseReference transactionsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_transactions);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Enable the back button and set its action
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Initialize RecyclerView
        recyclerViewTransactions = findViewById(R.id.recyclerViewTransactions);

        // Initialize transaction list
        customerTransactionList = new ArrayList<>();

        // Get the current user from Firebase Authentication
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid(); // Get the logged-in user's UID

            // Set up Firebase reference to the user's transactions node
            transactionsRef = FirebaseDatabase.getInstance().getReference("transactions").child(uid);

            // Initialize and set up the adapter
            customerTransactionsAdapter = new CustomerTransactionsAdapter(customerTransactionList);
            recyclerViewTransactions.setAdapter(customerTransactionsAdapter);

            // Load transactions data from Firebase
            loadTransactionsData();
        } else {
            Log.e("CustomerTransactions", "No user is currently logged in.");
        }
    }

    private void loadTransactionsData() {
        transactionsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                customerTransactionList.clear(); // Clear the list to avoid duplicates
                for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                    // Assuming each order is of type CustomerTransaction
                    Transaction transaction = orderSnapshot.getValue(Transaction.class);
                    if (transaction != null) {
                        customerTransactionList.add(transaction);
                    }
                }
                customerTransactionsAdapter.notifyDataSetChanged(); // Notify adapter of data changes
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("CustomerTransactions", "Failed to read data", error.toException());
            }
        });
    }
}

package com.example.fleursonthego.views;

import android.os.Bundle;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fleursonthego.Adapters.ArchivedAdminProductAdapter;
import com.example.fleursonthego.Models.AdminProduct;
import com.example.fleursonthego.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class ArchivedProductsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ArchivedAdminProductAdapter adminProductAdapter;
    private List<AdminProduct> adminProductList;
    private FirebaseDatabase database;
    private DatabaseReference productRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Enables edge-to-edge support
        setContentView(R.layout.activity_archived_products);


        // Find the toolbar and set it up
        Toolbar toolbar = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);

        // Enable the back button functionality
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        // Initialize UI elements
        recyclerView = findViewById(R.id.recyclerViewArchived);

        // Set up Firebase references
        database = FirebaseDatabase.getInstance();
        productRef = database.getReference("archivedProducts");

        // Set up RecyclerView
        adminProductList = new ArrayList<>();
        adminProductAdapter = new ArchivedAdminProductAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adminProductAdapter);

        // Fetch archived product data from Firebase
        fetchAdminProductData();

        // Optional: Handle FAB button click (if needed)


        // Enable immersive mode if needed

    }

    private void fetchAdminProductData() {
        productRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                adminProductList.clear(); // Clear previous data
                for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
                    AdminProduct adminProduct = productSnapshot.getValue(AdminProduct.class);
                    if (adminProduct != null) {
                        adminProductList.add(adminProduct); // Add new data
                    }
                }
                adminProductAdapter.notifyDataSetChanged(); // Notify adapter to refresh the RecyclerView
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ArchivedProductsActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

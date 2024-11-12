package com.example.fleursonthego.views;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fleursonthego.Adapters.ProductsAdapter;
import com.example.fleursonthego.Models.Product;
import com.example.fleursonthego.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AllProductsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ImageButton backButton;
    private ImageView imageBouquet;
    private ImageView imageFlowerStem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_all_products_acitivty);

        // Initialize Views
        backButton = findViewById(R.id.backButtonTop);
        recyclerView = findViewById(R.id.recyclerViewAllProducts);
        imageBouquet = findViewById(R.id.imageBouquet);
        imageFlowerStem = findViewById(R.id.imageFlowerStem);

        // Set GridLayoutManager with 2 columns
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        // Set OnClickListener for the back button
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(AllProductsActivity.this, Dashboard.class);
            startActivity(intent);
            finish();
        });

        // Set click listener for imageBouquet
        imageBouquet.setOnClickListener(v -> {
            Toast.makeText(AllProductsActivity.this, "Bouquet clicked", Toast.LENGTH_SHORT).show();
            fetchProducts("NotStems"); // Fetch all products except "Stems"
        });

        // Set click listener for imageFlowerStem
        imageFlowerStem.setOnClickListener(v -> {
            Toast.makeText(AllProductsActivity.this, "Flower Stem clicked", Toast.LENGTH_SHORT).show();
            fetchProducts("Stems"); // Fetch only "Stems" products
        });

        // Load all products initially
        fetchProducts(null); // Load all categories initially
    }

    private void fetchProducts(String categoryFilter) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("products");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Product> productList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String productId = snapshot.getKey();
                    Product product = snapshot.getValue(Product.class);
                    if (product != null) {
                        product.setProductId(productId);

                        // Filter based on category
                        if (categoryFilter == null ||
                                (categoryFilter.equals("Stems") && "Stems".equals(product.getCategory())) ||
                                (categoryFilter.equals("NotStems") && !"Stems".equals(product.getCategory()))) {
                            productList.add(product);
                            Log.d("ProductsAdapter", "Binding Product: " + product.getProductName());
                        }
                    }
                }
                updateRecyclerView(productList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error if needed
            }
        });
    }

    private void updateRecyclerView(List<Product> productList) {
        ProductsAdapter adapter = new ProductsAdapter(this, productList);
        recyclerView.setAdapter(adapter);
    }
}

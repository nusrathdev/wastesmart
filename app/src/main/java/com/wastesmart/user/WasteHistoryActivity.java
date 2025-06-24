package com.wastesmart.user;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wastesmart.R;
import com.wastesmart.databinding.ActivityWasteHistoryBinding;
import com.google.firebase.auth.FirebaseAuth;

public class WasteHistoryActivity extends AppCompatActivity {

    private ActivityWasteHistoryBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWasteHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Waste Reports");
        }

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Set up RecyclerView (placeholder for now)
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // TODO: Implement actual adapter with data from Firebase
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

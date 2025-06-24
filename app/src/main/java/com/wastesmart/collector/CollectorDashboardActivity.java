package com.wastesmart.collector;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.wastesmart.MainActivity;
import com.wastesmart.R;
import com.wastesmart.databinding.ActivityCollectorDashboardBinding;
import com.google.firebase.auth.FirebaseAuth;

public class CollectorDashboardActivity extends AppCompatActivity {

    private ActivityCollectorDashboardBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCollectorDashboardBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Set up toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Collector Dashboard");
        }

        // Set up button click listeners
        setupClickListeners();
    }

    private void setupClickListeners() {
        // Today's Routes button
        binding.cardTodaysRoutes.setOnClickListener(v -> {
            Intent intent = new Intent(CollectorDashboardActivity.this, RouteMapActivity.class);
            startActivity(intent);
        });

        // Collection Tasks button
        binding.cardCollectionTasks.setOnClickListener(v -> {
            Intent intent = new Intent(CollectorDashboardActivity.this, CollectionTasksActivity.class);
            startActivity(intent);
        });

        // Route Optimization button
        binding.cardRouteOptimization.setOnClickListener(v -> {
            Toast.makeText(this, "Route optimization feature coming soon", Toast.LENGTH_SHORT).show();
        });

        // Reports button
        binding.cardReports.setOnClickListener(v -> {
            Toast.makeText(this, "Reports feature coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            mAuth.signOut();
            Intent intent = new Intent(CollectorDashboardActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

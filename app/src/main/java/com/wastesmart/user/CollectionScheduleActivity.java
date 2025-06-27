package com.wastesmart.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.wastesmart.R;
import com.wastesmart.databinding.ActivityCollectionScheduleBinding;

public class CollectionScheduleActivity extends BaseUserActivity {

    private ActivityCollectionScheduleBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCollectionScheduleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            // Remove back arrow
            // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("About");
        }

        // Setup bottom navigation
        setupBottomNavigation();

        setupClickListeners();
    }

    private void setupClickListeners() {
        // Submit Waste Report button
        binding.btnSubmitReport.setOnClickListener(v -> {
            Intent intent = new Intent(this, ReportWasteActivity.class);
            startActivity(intent);
        });

        // View My Reports button
        binding.btnViewReports.setOnClickListener(v -> {
            Intent intent = new Intent(this, MyReportsActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected String getActiveNavItem() {
        return "about";
    }
}

package com.wastesmart.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.wastesmart.MainActivity;
import com.wastesmart.R;
import com.wastesmart.databinding.ActivityUserAboutBinding;

public class CollectionScheduleActivity extends BaseUserActivity {

    private ActivityUserAboutBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Setup toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            // Remove back arrow
            // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("About");
        }

        // Set up logout button click listener
        binding.btnLogout.setOnClickListener(v -> showLogoutConfirmation());

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
    
    private void showLogoutConfirmation() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> logout())
                .setNegativeButton("No", null)
                .show();
    }
    
    private void logout() {
        mAuth.signOut();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected String getActiveNavItem() {
        return "about";
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}

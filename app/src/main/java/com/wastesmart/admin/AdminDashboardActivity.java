package com.wastesmart.admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.wastesmart.MainActivity;
import com.wastesmart.R;

public class AdminDashboardActivity extends AppCompatActivity {

    private static final String TAG = "AdminDashboard";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView tvWelcomeAdmin;
    private Button btnManageWasteReports, btnManageCollectors, btnViewAnalytics, btnSystemSettings, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Setup toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Admin Dashboard");
        }

        // Initialize views
        initViews();

        // Get admin info from intent
        String adminType = getIntent().getStringExtra("admin_type");
        String adminEmail = getIntent().getStringExtra("admin_email");

        // Display welcome message
        if (tvWelcomeAdmin != null) {
            tvWelcomeAdmin.setText("Welcome, " + (adminType != null ? adminType : "Admin"));
        }

        // Setup click listeners
        setupClickListeners();

        // Load dashboard data
        loadDashboardData();

        Toast.makeText(this, "Logged in as: " + adminEmail, Toast.LENGTH_SHORT).show();
    }

    private void initViews() {
        tvWelcomeAdmin = findViewById(R.id.tvWelcomeAdmin);
        btnManageWasteReports = findViewById(R.id.btnManageWasteReports);
        btnManageCollectors = findViewById(R.id.btnManageCollectors);
        btnViewAnalytics = findViewById(R.id.btnViewAnalytics);
        btnSystemSettings = findViewById(R.id.btnSystemSettings);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setupClickListeners() {
        btnManageWasteReports.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, ManageReportsActivity.class);
            startActivity(intent);
        });

        btnManageCollectors.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, ManageCollectorsActivity.class);
            startActivity(intent);
        });

        btnViewAnalytics.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, AnalyticsActivity.class);
            startActivity(intent);
        });

        btnSystemSettings.setOnClickListener(v -> {
            Toast.makeText(this, "System settings feature coming soon", Toast.LENGTH_SHORT).show();
        });

        btnLogout.setOnClickListener(v -> logout());
    }

    private void loadDashboardData() {
        // Load waste reports count
        db.collection("waste_reports")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int reportCount = queryDocumentSnapshots.size();
                    Log.d(TAG, "Total waste reports: " + reportCount);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading waste reports", e);
                });

        // Load collectors count
        db.collection("collectors")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int collectorCount = queryDocumentSnapshots.size();
                    Log.d(TAG, "Total collectors: " + collectorCount);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading collectors", e);
                });
    }

    private void logout() {
        mAuth.signOut();
        Intent intent = new Intent(AdminDashboardActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
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
            logout();
            return true;
        } else if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

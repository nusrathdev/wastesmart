package com.wastesmart.admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.wastesmart.MainActivity;
import com.wastesmart.R;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AdminDashboardActivity extends BaseAdminActivity {

    private static final String TAG = "AdminDashboard";
    private static final int MAX_REPORTS_TO_SHOW = 5;
    
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String actualCollectorId = "default_collector"; // fallback to default
    private TextView tvWelcomeAdmin;
    private TextView tvPendingCount;
    private TextView tvTodayCount;
    private TextView tvViewAll;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Hide default action bar - we use our custom toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize views
        initViews();

        // Get admin info from intent
        String adminType = getIntent().getStringExtra("admin_type");
        String adminEmail = getIntent().getStringExtra("admin_email");

        // Display admin type in the welcome card
        if (tvWelcomeAdmin != null) {
            tvWelcomeAdmin.setText(adminType != null ? adminType : "Administrator");
        }
        
        // Top right corner buttons have been removed per request

        // Setup click listeners
        setupClickListeners();

        // Set up bottom navigation
        setupBottomNavigation();
        
        // RecyclerView setup removed as we no longer need it
        Log.d(TAG, "Dashboard setup complete");

        // Load dashboard data
        loadDashboardData();
        
        // Load the actual collector ID from database
        loadActualCollectorId();
        
        if (adminEmail != null && !adminEmail.isEmpty()) {
            Toast.makeText(this, "Logged in as: " + adminEmail, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this screen
        loadDashboardData();
    }

    private void initViews() {
        tvWelcomeAdmin = findViewById(R.id.tvWelcomeAdmin);
        tvPendingCount = findViewById(R.id.tvPendingCount);
        tvTodayCount = findViewById(R.id.tvTodayCount);
        tvViewAll = findViewById(R.id.tvViewAll);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        // View All reports button in stats card
        tvViewAll.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, ManageReportsActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        
        // Logout button in toolbar
        ImageView btnLogout = findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> showLogoutConfirmation());
        }
    }

    private void loadDashboardData() {
        try {
            // Show progress bar
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            }
            
            // Set welcome message dynamically
            String[] welcomeMessages = {
                "Here's your waste management overview",
                "Manage pending reports efficiently",
                "Stay on top of waste collection",
                "Monitor community waste reports",
                "Keep your city clean and sustainable"
            };
            int randomIndex = (int) (Math.random() * welcomeMessages.length);
            TextView tvOverview = findViewById(R.id.tvOverview);
            if (tvOverview != null) {
                tvOverview.setText(welcomeMessages[randomIndex]);
            }
        
        // Load pending reports count
        db.collection("waste_reports")
                .whereEqualTo("status", "PENDING")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    try {
                        int pendingCount = queryDocumentSnapshots.size();
                        if (tvPendingCount != null) {
                            tvPendingCount.setText(String.valueOf(pendingCount));
                        }
                        Log.d(TAG, "Pending reports loaded: " + pendingCount);
                    } catch (Exception e) {
                        Log.e(TAG, "Error updating pending count", e);
                    }
                })
                .addOnFailureListener(e -> {
                    try {
                        Log.e(TAG, "Error loading pending reports", e);
                        if (tvPendingCount != null) {
                            tvPendingCount.setText("--");
                        }
                    } catch (Exception ex) {
                        Log.e(TAG, "Error handling pending reports failure", ex);
                    }
                });

        // Load today's reports count
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long startOfDay = calendar.getTimeInMillis();
        
        db.collection("waste_reports")
                .whereGreaterThan("timestamp", startOfDay)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    try {
                        int todayCount = queryDocumentSnapshots.size();
                        if (tvTodayCount != null) {
                            tvTodayCount.setText(String.valueOf(todayCount));
                        }
                        Log.d(TAG, "Today's reports: " + todayCount);
                    } catch (Exception e) {
                        Log.e(TAG, "Error updating today count", e);
                    }
                })
                .addOnFailureListener(e -> {
                    try {
                        Log.e(TAG, "Error loading today's reports", e);
                        if (tvTodayCount != null) {
                            tvTodayCount.setText("--");
                        }
                    } catch (Exception ex) {
                        Log.e(TAG, "Error handling today's reports failure", ex);
                    }
                });

        // Hide progress bar when all data is loaded
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        
        } catch (Exception e) {
            Log.e(TAG, "Error in loadDashboardData", e);
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            Toast.makeText(this, "Error loading dashboard data", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadActualCollectorId() {
        // Get the actual collector ID from the database
        db.collection("collectors")
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        actualCollectorId = queryDocumentSnapshots.iterator().next().getId();
                        Log.d(TAG, "Loaded actual collector ID: " + actualCollectorId);
                    } else {
                        Log.w(TAG, "No collector found, using default collector ID");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading collector ID", e);
                });
    }

    // Sample report creation methods removed as they're not needed in production

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> logout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void logout() {
        mAuth.signOut();
        Intent intent = new Intent(AdminDashboardActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    // Removed options menu methods as we now have a dedicated logout button in the toolbar
    
    @Override
    protected int getActiveItemIndex() {
        // Home tab is active
        return 2;
    }
    
    // Method to handle directly assigning reports to the collector
    public void assignReportToCollector(String reportId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "ASSIGNED");
        updates.put("assignedCollectorId", actualCollectorId); // Use actual collector ID
        updates.put("assignedCollectorName", "Waste Collector");
        updates.put("assignedTimestamp", System.currentTimeMillis());
        
        db.collection("waste_reports").document(reportId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Report assigned to collector", Toast.LENGTH_SHORT).show();
                    loadDashboardData(); // Refresh the dashboard data
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error assigning report", e);
                    Toast.makeText(this, "Error assigning report: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    
    // Removed forceUpdateUI method as it's no longer needed
    
    @Override
    protected void onPostResume() {
        super.onPostResume();
        // No need for forced UI updates since we removed the RecyclerView
    }
}

package com.wastesmart.admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.wastesmart.MainActivity;
import com.wastesmart.R;
import com.wastesmart.models.WasteReport;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminDashboardActivity extends BaseAdminActivity {

    private static final String TAG = "AdminDashboard";
    private static final int MAX_REPORTS_TO_SHOW = 5;
    
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView tvWelcomeAdmin;
    private TextView tvPendingCount;
    private TextView tvTodayCount;
    private TextView tvNoReports;
    private TextView tvViewAll;
    private ProgressBar progressBar;
    private RecyclerView recyclerViewRecentReports;
    private SimpleAdminReportsAdapter adapter;
    private List<WasteReport> reportsList;

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

        // Display welcome message
        if (tvWelcomeAdmin != null) {
            tvWelcomeAdmin.setText("Welcome, " + (adminType != null ? adminType : "Admin"));
        }

        // Setup click listeners
        setupClickListeners();

        // Set up bottom navigation
        setupBottomNavigation();
        
        // Set up RecyclerView
        reportsList = new ArrayList<>();
        adapter = new SimpleAdminReportsAdapter(reportsList, this);
        recyclerViewRecentReports.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewRecentReports.setAdapter(adapter);

        // Load dashboard data
        loadDashboardData();
        
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
        tvNoReports = findViewById(R.id.tvNoReports);
        tvViewAll = findViewById(R.id.tvViewAll);
        progressBar = findViewById(R.id.progressBar);
        recyclerViewRecentReports = findViewById(R.id.recyclerViewRecentReports);
    }

    private void setupClickListeners() {
        // View All reports button
        tvViewAll.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, ManageReportsActivity.class);
            startActivity(intent);
        });
    }

    private void loadDashboardData() {
        progressBar.setVisibility(View.VISIBLE);
        tvNoReports.setVisibility(View.GONE);
        
        // Load pending reports count
        db.collection("waste_reports")
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int pendingCount = queryDocumentSnapshots.size();
                    tvPendingCount.setText(String.valueOf(pendingCount));
                    Log.d(TAG, "Pending reports: " + pendingCount);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading pending reports", e);
                    tvPendingCount.setText("--");
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
                    int todayCount = queryDocumentSnapshots.size();
                    tvTodayCount.setText(String.valueOf(todayCount));
                    Log.d(TAG, "Today's reports: " + todayCount);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading today's reports", e);
                    tvTodayCount.setText("--");
                });

        // Load recent pending waste reports
        db.collection("waste_reports")
                .whereEqualTo("status", "pending")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(MAX_REPORTS_TO_SHOW)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
                    reportsList.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            WasteReport report = document.toObject(WasteReport.class);
                            report.setId(document.getId());
                            reportsList.add(report);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing report: " + document.getId(), e);
                        }
                    }

                    adapter.notifyDataSetChanged();

                    if (reportsList.isEmpty()) {
                        tvNoReports.setVisibility(View.VISIBLE);
                    } else {
                        tvNoReports.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    tvNoReports.setVisibility(View.VISIBLE);
                    Log.e(TAG, "Error loading recent pending reports", e);
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
        }

        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected int getActiveItemIndex() {
        // Home tab is active
        return 2;
    }
    
    // Method to handle directly assigning reports to the collector
    public void assignReportToCollector(String reportId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "assigned");
        updates.put("assignedCollectorId", "default_collector");
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
}

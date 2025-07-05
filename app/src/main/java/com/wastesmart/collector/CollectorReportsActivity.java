package com.wastesmart.collector;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.wastesmart.MainActivity;
import com.wastesmart.R;
import com.wastesmart.adapters.CollectorReportAdapter;
import com.wastesmart.databinding.ActivityCollectorReportsBinding;
import com.wastesmart.models.WasteReport;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CollectorReportsActivity extends BaseCollectorActivity {
    
    private static final String TAG = "CollectorReportsActivity";
    private ActivityCollectorReportsBinding binding;
    private CollectorReportAdapter reportAdapter;
    private List<WasteReport> reportsList;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCollectorReportsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Initialize Firebase components
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        
        // Set up toolbar
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("Waste Reports");
        
        // Setup bottom navigation
        setupBottomNavigation();
        
        // Initialize reports list and adapter
        reportsList = new ArrayList<>();
        reportAdapter = new CollectorReportAdapter(this, reportsList, new CollectorReportAdapter.OnReportClickListener() {
            @Override
            public void onReportClick(WasteReport report, int position) {
                // Handle report click - show details
                Toast.makeText(CollectorReportsActivity.this, "Viewing report details", Toast.LENGTH_SHORT).show();
                // You could navigate to a detail view here
            }
            
            @Override
            public void onUpdateStatusClick(WasteReport report, int position) {
                // Handle update status click
                showUpdateStatusDialog(report, position);
            }
        });
        
        // Set up RecyclerView
        binding.rvReports.setLayoutManager(new LinearLayoutManager(this));
        binding.rvReports.setAdapter(reportAdapter);
        
        // Set up filter spinner
        setupFilterSpinner();
        
        // Load reports
        loadReports();
    }
    
    private void setupFilterSpinner() {
        String[] filterOptions = {"All Reports", "Assigned", "In Progress", "Completed"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.spinner_item, filterOptions);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        binding.spinnerFilter.setAdapter(adapter);
        
        binding.spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedFilter = parent.getItemAtPosition(position).toString();
                filterReports(selectedFilter);
                
                // Log the selected filter for debugging
                Log.d(TAG, "Filter selected: " + selectedFilter);
                
                // Give visual feedback that the filter was applied
                Toast.makeText(CollectorReportsActivity.this, 
                    "Filtering by: " + selectedFilter, Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }
    
    private void filterReports(String filter) {
        binding.progressBar.setVisibility(View.VISIBLE);
        
        if (mAuth.getCurrentUser() == null) {
            binding.progressBar.setVisibility(View.GONE);
            return;
        }
        
        try {
            // Important: Clear the list before applying the filter to avoid duplicates
            reportsList.clear();
            reportAdapter.notifyDataSetChanged();
            
            // Load all waste reports and filter locally (simpler and avoids index issues)
            loadAllReportsAndFilterLocally(filter);
        } catch (Exception e) {
            Log.e(TAG, "Error filtering reports: " + e.getMessage());
            Toast.makeText(this, "Error filtering reports", Toast.LENGTH_SHORT).show();
            binding.progressBar.setVisibility(View.GONE);
            showNoReportsMessage(true);
        }
    }
    
    private void loadReports() {
        binding.progressBar.setVisibility(View.VISIBLE);
        
        if (mAuth.getCurrentUser() == null) {
            binding.progressBar.setVisibility(View.GONE);
            return;
        }
        
        try {
            // Use simple approach that loads all waste reports and sorts/filters locally
            // Default to showing "All Reports"
            loadAllReportsAndFilterLocally("All Reports");
        } catch (Exception e) {
            Log.e(TAG, "Error loading reports: " + e.getMessage());
            Toast.makeText(this, "Error loading reports", Toast.LENGTH_SHORT).show();
            binding.progressBar.setVisibility(View.GONE);
            showNoReportsMessage(true);
        }
    }
    
    /**
     * Modified method that loads only reports relevant to collectors
     */
    private void loadAllReportsAndFilterLocally(String filterOption) {
        // Make absolutely sure the list is cleared to prevent duplicates
        reportsList.clear();
        
        // Get current collector ID
        String collectorId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "default_collector";
        
        // Log the start of loading with specific filter
        Log.d(TAG, "Loading reports with filter: " + filterOption);
        
        // Query to get all reports and filter locally for better reliability
        db.collection("waste_reports")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                Log.d(TAG, "Retrieved " + queryDocumentSnapshots.size() + " waste reports from database");
                
                if (!queryDocumentSnapshots.isEmpty()) {
                    // Create a new list and map for this filter operation
                    List<WasteReport> allReports = new ArrayList<>();
                    // Use a map to track unique IDs and prevent duplicates
                    Map<String, WasteReport> uniqueReports = new HashMap<>();
                    
                    // Process each waste report
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            String reportId = document.getId();
                            // Only add if we haven't already processed this report ID and it's relevant to collectors
                            if (!uniqueReports.containsKey(reportId)) {
                                WasteReport report = document.toObject(WasteReport.class);
                                report.setId(reportId);
                                
                                // Include only relevant statuses for collectors (ASSIGNED, IN_PROGRESS, COMPLETED)
                                // Explicitly exclude PENDING status
                                String status = report.getStatus();
                                if (status != null) {
                                    status = status.toUpperCase(); // Normalize to uppercase
                                    // Skip PENDING reports
                                    if (!"PENDING".equals(status)) {
                                        uniqueReports.put(reportId, report);
                                        Log.d(TAG, "Adding collector report with ID: " + reportId + ", status: " + status);
                                    } else {
                                        Log.d(TAG, "Skipping PENDING report with ID: " + reportId);
                                    }
                                }
                            } else {
                                Log.w(TAG, "Duplicate report ID detected: " + reportId + ", skipping");
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing waste report: " + document.getId(), e);
                        }
                    }
                    
                    // Convert map to list
                    allReports.addAll(uniqueReports.values());
                    
                    // Sort by timestamp (descending)
                    allReports.sort((r1, r2) -> {
                        Long t1 = r1.getTimestamp();
                        Long t2 = r2.getTimestamp();
                        if (t1 == null && t2 == null) return 0;
                        if (t1 == null) return 1;
                        if (t2 == null) return -1;
                        return t2.compareTo(t1); // Descending order (newest first)
                    });
                    
                    // Make sure the reportsList is empty before applying filter
                    reportsList.clear();
                    
                    // Filter based on selected spinner option
                    if (filterOption != null && !filterOption.equals("All Reports")) {
                        String status = filterOption.toUpperCase();
                        // Handle special case for "IN PROGRESS"
                        if (status.equals("IN PROGRESS")) {
                            status = "IN_PROGRESS";
                        }
                        
                        // Create a set to track added report IDs during filtering
                        Set<String> addedReportIds = new HashSet<>();
                        
                        for (WasteReport report : allReports) {
                            if (status.equalsIgnoreCase(report.getStatus()) && !addedReportIds.contains(report.getId())) {
                                reportsList.add(report);
                                addedReportIds.add(report.getId());
                                Log.d(TAG, "Added filtered report with ID: " + report.getId() + ", status: " + report.getStatus());
                            }
                        }
                        Log.d(TAG, "Added " + reportsList.size() + " filtered reports with status: " + status);
                    } else {
                        // No filter - add all reports (already deduplicated via the uniqueReports map)
                        reportsList.addAll(allReports);
                        Log.d(TAG, "Added all " + allReports.size() + " reports (no filter applied)");
                    }
                    
                    // Always notify adapter after all data changes
                    reportAdapter.notifyDataSetChanged();
                    
                    if (reportsList.isEmpty()) {
                        // Check if we need to create a test report for debugging
                        if (queryDocumentSnapshots.isEmpty()) {
                            Log.d(TAG, "No reports found in database, creating test report");
                            createTestReport();
                        } else {
                            Log.d(TAG, "Reports found but none match collector criteria");
                        }
                        showNoReportsMessage(true);
                    } else {
                        showNoReportsMessage(false);
                        Log.d(TAG, "Displaying " + reportsList.size() + " waste reports after filtering");
                    }
                } else {
                    showNoReportsMessage(true);
                    Log.d(TAG, "No waste reports found");
                    // Create test report if database is empty
                    createTestReport();
                }
                
                binding.progressBar.setVisibility(View.GONE);
            })
            .addOnFailureListener(e -> {
                binding.progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error loading waste reports: " + e.getMessage(), e);
                Toast.makeText(CollectorReportsActivity.this, 
                    "Error loading reports: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                showNoReportsMessage(true);
            });
    }
    
    private void showNoReportsMessage(boolean show) {
        if (show) {
            binding.tvNoReports.setText("No waste reports found");
            binding.tvNoReports.setVisibility(View.VISIBLE);
            binding.rvReports.setVisibility(View.GONE);
        } else {
            binding.tvNoReports.setVisibility(View.GONE);
            binding.rvReports.setVisibility(View.VISIBLE);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_reports, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_refresh) {
            loadReports();
            return true;
        } else if (id == R.id.action_logout) {
            mAuth.signOut();
            Intent intent = new Intent(CollectorReportsActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected int getActiveNavItemIndex() {
        return 2; // Reports tab index (0-based)
    }
    

    
    /**
     * Shows a dialog to update the report status
     */
    private void showUpdateStatusDialog(WasteReport report, int position) {
        if (report == null) return;
        
        // Current status (normalized to uppercase)
        String currentStatus = report.getStatus().toUpperCase();
        String[] statusOptions;
        int preSelectedIndex = -1;
        
        // Determine available status options based on current status
        if ("COMPLETED".equals(currentStatus)) {
            // If already completed, only allow reverting to IN_PROGRESS
            statusOptions = new String[]{"IN_PROGRESS"};
        } else if ("IN_PROGRESS".equals(currentStatus)) {
            // If in progress, can complete or revert to assigned
            statusOptions = new String[]{"COMPLETED", "ASSIGNED"};
            preSelectedIndex = 0; // Suggest COMPLETED as next logical step
        } else {
            // Default options (from ASSIGNED or other states)
            statusOptions = new String[]{"IN_PROGRESS", "COMPLETED"};
            preSelectedIndex = 0; // Suggest IN_PROGRESS as next logical step
        }
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Update Report Status")
            .setSingleChoiceItems(statusOptions, preSelectedIndex, (dialog, which) -> {
                String newStatus = statusOptions[which];
                updateReportStatus(report, newStatus, position);
                dialog.dismiss();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    /**
     * Updates the waste report status in Firestore
     */
    private void updateReportStatus(WasteReport report, String newStatus, int position) {
        if (report == null || report.getId() == null) return;
        
        binding.progressBar.setVisibility(View.VISIBLE);
        
        db.collection("waste_reports")
            .document(report.getId())
            .update("status", newStatus)
            .addOnSuccessListener(aVoid -> {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Status updated successfully", Toast.LENGTH_SHORT).show();
                
                // Update local list and UI
                report.setStatus(newStatus);
                reportAdapter.notifyItemChanged(position);
            })
            .addOnFailureListener(e -> {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Failed to update status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // We don't need to refresh reports here as it's already called in onCreate
        // This prevents duplicate loading
    }
    
    // Create test reports with different statuses for debugging purposes
    private void createTestReport() {
        // Create 3 test reports with different statuses to demonstrate filtering
        createSingleTestReport("ASSIGNED", "Test Assigned Report", "Large", 
            "This is an assigned test waste report");
        
        createSingleTestReport("IN_PROGRESS", "Test In-Progress Report", "Medium", 
            "This is an in-progress test waste report");
        
        createSingleTestReport("COMPLETED", "Test Completed Report", "Small", 
            "This is a completed test waste report");
        
        // Reload after a delay to show all test data
        new android.os.Handler().postDelayed(() -> loadReports(), 2000);
    }
    
    // Helper method to create a single test report with specific status
    private void createSingleTestReport(String status, String wasteType, String wasteSize, String description) {
        String collectorId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "default_collector";
        
        Map<String, Object> testReport = new HashMap<>();
        testReport.put("wasteType", wasteType);
        testReport.put("wasteSize", wasteSize);
        testReport.put("description", description);
        testReport.put("latitude", 37.7749 + (Math.random() - 0.5) / 100); // Small random variation
        testReport.put("longitude", -122.4194 + (Math.random() - 0.5) / 100); // Small random variation
        testReport.put("status", status);
        testReport.put("timestamp", System.currentTimeMillis() - (long)(Math.random() * 86400000)); // Random time within last 24h
        testReport.put("assignedTimestamp", System.currentTimeMillis() - (long)(Math.random() * 43200000)); // Random time within last 12h
        testReport.put("assignedCollectorId", collectorId);
        testReport.put("assignedCollectorName", "Default Collector");
        testReport.put("userId", "test_user");
        
        // Add to Firestore
        db.collection("waste_reports")
            .add(testReport)
            .addOnSuccessListener(documentReference -> {
                Log.d(TAG, "Test report created with status " + status + " and ID: " + documentReference.getId());
                Toast.makeText(this, "Created test report with status: " + status, Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error creating test report with status " + status, e);
            });
    }
}

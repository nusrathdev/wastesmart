package com.wastesmart.admin;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.wastesmart.R;
import com.wastesmart.databinding.ActivityManageReportsBinding;
import com.wastesmart.models.WasteReport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ManageReportsActivity extends BaseAdminActivity {

    private static final String TAG = "ManageReportsActivity";
    private ActivityManageReportsBinding binding;
    private AdminReportAdapter reportAdapter;
    private List<WasteReport> reportsList;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageReportsBinding.inflate(getLayoutInflater());
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
        reportAdapter = new AdminReportAdapter(this, reportsList, new AdminReportAdapter.OnReportClickListener() {
            @Override
            public void onReportClick(WasteReport report, int position) {
                // Handle report click - show details
                Toast.makeText(ManageReportsActivity.this, "Viewing report details", Toast.LENGTH_SHORT).show();
                // You could navigate to a detail view here
            }
            
            @Override
            public void onAssignClick(WasteReport report, int position) {
                // Handle update status or assign click based on current status
                if ("PENDING".equalsIgnoreCase(report.getStatus())) {
                    assignReportToCollector(report, position);
                } else {
                    showUpdateStatusDialog(report, position);
                }
            }
        });
        
        // Set up RecyclerView
        binding.rvReports.setLayoutManager(new LinearLayoutManager(this));
        binding.rvReports.setAdapter(reportAdapter);
        
        // Set up swipe refresh listener
        binding.swipeRefreshLayout.setColorSchemeResources(R.color.primary, R.color.secondary);
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            loadReports();
        });
        
        // Set up filter spinner
        setupFilterSpinner();
        
        // Load reports
        loadReports();
    }
    
    private void setupFilterSpinner() {
        String[] filterOptions = {"All Reports", "Completed", "In Progress", "Pending", "Assigned"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.spinner_item, filterOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerFilter.setAdapter(adapter);
        
        binding.spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedFilter = parent.getItemAtPosition(position).toString();
                filterReports(selectedFilter);
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }
    
    private void filterReports(String filter) {
        binding.progressBar.setVisibility(View.VISIBLE);
        
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
        
        try {
            // Use simple approach that loads all waste reports and sorts/filters locally
            loadAllReportsAndFilterLocally("All Reports");
        } catch (Exception e) {
            Log.e(TAG, "Error loading reports: " + e.getMessage());
            Toast.makeText(this, "Error loading reports", Toast.LENGTH_SHORT).show();
            binding.progressBar.setVisibility(View.GONE);
            showNoReportsMessage(true);
        }
    }
    
    /**
     * Loads all waste reports and filters them locally
     */
    private void loadAllReportsAndFilterLocally(String filterOption) {
        // Make absolutely sure the list is cleared to prevent duplicates
        reportsList.clear();
        
        // Log the start of loading with specific filter
        Log.d(TAG, "Loading reports with filter: " + filterOption);
        
        // Simple query that loads all waste reports (no complex indexes needed)
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
                            // Only add if we haven't already processed this report ID
                            if (!uniqueReports.containsKey(reportId)) {
                                WasteReport report = document.toObject(WasteReport.class);
                                report.setId(reportId);
                                uniqueReports.put(reportId, report);
                                Log.d(TAG, "Adding report with ID: " + reportId);
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
                    showNoReportsMessage(reportsList.isEmpty());
                    
                    Log.d(TAG, "Displaying " + reportsList.size() + " waste reports after filtering");
                } else {
                    showNoReportsMessage(true);
                    Log.d(TAG, "No waste reports found");
                }
                
                binding.progressBar.setVisibility(View.GONE);
                // Stop the refresh animation if it's running
                if (binding.swipeRefreshLayout.isRefreshing()) {
                    binding.swipeRefreshLayout.setRefreshing(false);
                }
            })
            .addOnFailureListener(e -> {
                binding.progressBar.setVisibility(View.GONE);
                // Stop the refresh animation if it's running
                if (binding.swipeRefreshLayout.isRefreshing()) {
                    binding.swipeRefreshLayout.setRefreshing(false);
                }
                Log.e(TAG, "Error loading waste reports: " + e.getMessage(), e);
                Toast.makeText(ManageReportsActivity.this, 
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
    
    /**
     * Shows a dialog to update the report status
     */
    private void showUpdateStatusDialog(WasteReport report, int position) {
        if (report == null) return;
        
        String[] statusOptions = {"IN_PROGRESS", "COMPLETED", "ASSIGNED"};
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Update Report Status")
            .setSingleChoiceItems(statusOptions, -1, (dialog, which) -> {
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
    public void updateReportStatus(WasteReport report, String newStatus, int position) {
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
    
    /**
     * Assigns a report to the default collector
     */
    public void assignReportToCollector(WasteReport report, int position) {
        if (report == null || report.getId() == null) return;
        
        binding.progressBar.setVisibility(View.VISIBLE);
        
        // Use a single hardcoded collector for simplicity
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "ASSIGNED");
        updates.put("assignedCollectorId", "default_collector");
        updates.put("assignedCollectorName", "Waste Collector");
        updates.put("assignedTimestamp", System.currentTimeMillis());
        
        db.collection("waste_reports")
            .document(report.getId())
            .update(updates)
            .addOnSuccessListener(aVoid -> {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Report assigned to collector", Toast.LENGTH_SHORT).show();
                
                // Update local list and UI
                report.setStatus("ASSIGNED");
                report.setAssignedCollectorId("default_collector");
                report.setAssignedCollectorName("Waste Collector");
                report.setAssignedTimestamp(System.currentTimeMillis());
                reportAdapter.notifyItemChanged(position);
            })
            .addOnFailureListener(e -> {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Failed to assign report: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
    
    /**
     * Compatibility method for old ReportsAdapter using ID
     */
    public void assignReportToCollector(String reportId, WasteReport report) {
        // Find the position of this report in the list
        int position = -1;
        for (int i = 0; i < reportsList.size(); i++) {
            if (reportsList.get(i).getId().equals(reportId)) {
                position = i;
                break;
            }
        }
        
        if (position >= 0) {
            assignReportToCollector(report, position);
        }
    }
    
    /**
     * Compatibility method for old ReportsAdapter using ID
     */
    public void updateReportStatus(String reportId, String status) {
        // Find the report and its position
        int position = -1;
        WasteReport report = null;
        for (int i = 0; i < reportsList.size(); i++) {
            if (reportsList.get(i).getId().equals(reportId)) {
                position = i;
                report = reportsList.get(i);
                break;
            }
        }
        
        if (position >= 0 && report != null) {
            updateReportStatus(report, status, position);
        }
    }

    // Use the admin menu with sign out button from BaseAdminActivity
    // No need to override onCreateOptionsMenu since BaseAdminActivity handles it
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_logout) {
            // Let the parent class handle logout
            return super.onOptionsItemSelected(item);
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    // Add a method to refresh reports that can be called elsewhere if needed
    public void refreshReports() {
        loadReports();
    }
    
    @Override
    protected int getActiveItemIndex() {
        // Reports tab is active (index 0)
        return 0;
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // We don't need to refresh reports here as it's already called in onCreate
        // This prevents duplicate loading
    }
}

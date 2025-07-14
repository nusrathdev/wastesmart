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
        getSupportActionBar().setTitle("All Reports");
        
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
        
        // Load reports (no filtering)
        loadReports();
    }
    
    private void loadReports() {
        binding.progressBar.setVisibility(View.VISIBLE);
        
        // Get current collector ID (use default if not logged in)
        String collectorId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "default_collector";
        
        try {
            // Load all reports from Firestore
            loadAllCollectorReports();
        } catch (Exception e) {
            Log.e(TAG, "Error loading reports: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading reports", Toast.LENGTH_SHORT).show();
            binding.progressBar.setVisibility(View.GONE);
            showNoReportsMessage(true);
        }
    }
    
    /**
     * Simplified method to load all reports relevant to the current collector without filtering
     */
    private void loadAllCollectorReports() {
        // Clear the list to prevent duplicates
        reportsList.clear();
        
        // Get current collector ID
        String collectorId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "default_collector";
        
        Log.d(TAG, "Loading ALL reports for collector ID: " + collectorId);
        
        // Query to get all reports and filter locally for better reliability
        db.collection("waste_reports")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                Log.d(TAG, "Retrieved " + queryDocumentSnapshots.size() + " waste reports from database");
                
                // Use a map to track unique IDs and prevent duplicates
                Map<String, WasteReport> uniqueReports = new HashMap<>();
                
                // Simply log the number of reports retrieved
                Log.d(TAG, "Processing reports for collector ID: " + collectorId);
                
                // Process each waste report
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    try {
                        String reportId = document.getId();
                        // Only add if we haven't already processed this report ID
                        if (!uniqueReports.containsKey(reportId)) {
                            WasteReport report = document.toObject(WasteReport.class);
                            report.setId(reportId);
                            
                            // Ensure both photoUrl and imageUrl are set (critical fix)
                            if (report.getPhotoUrl() == null && report.getImageUrl() != null) {
                                report.setPhotoUrl(report.getImageUrl());
                            } else if (report.getImageUrl() == null && report.getPhotoUrl() != null) {
                                report.setImageUrl(report.getPhotoUrl());
                            }
                            
                            String status = report.getStatus();
                            String assignedCollectorId = report.getAssignedCollectorId();
                            
                            Log.d(TAG, "Processing report: ID=" + reportId + 
                                  ", status=" + status + ", assignedTo=" + assignedCollectorId +
                                  ", photoUrl=" + report.getPhotoUrl());
                            
                            // Include all reports except PENDING without any collector filtering
                            if (status != null && !status.equalsIgnoreCase("PENDING")) {
                                uniqueReports.put(reportId, report);
                                Log.d(TAG, "Including report with ID: " + reportId);
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing waste report: " + document.getId(), e);
                    }
                }
                
                // Convert map to list and add to reportsList
                reportsList.addAll(uniqueReports.values());
                
                // Sort by timestamp (newest first)
                reportsList.sort((r1, r2) -> {
                    Long t1 = r1.getTimestamp();
                    Long t2 = r2.getTimestamp();
                    if (t1 == null && t2 == null) return 0;
                    if (t1 == null) return 1;
                    if (t2 == null) return -1;
                    return t2.compareTo(t1);
                });
                
                // Update UI
                reportAdapter.notifyDataSetChanged();
                
                if (reportsList.isEmpty()) {
                    showNoReportsMessage(true);
                    Log.d(TAG, "No reports to display after processing.");
                } else {
                    showNoReportsMessage(false);
                    Log.d(TAG, "Displaying " + reportsList.size() + " waste reports");
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
        // Always refresh reports when returning to this screen
        // This ensures we always show the latest data
        Log.d(TAG, "onResume - refreshing reports");
        loadReports();
    }
    
    // All test report generation and filtering logic has been removed
    // The app now displays all non-PENDING reports from Firestore without any filtering
}

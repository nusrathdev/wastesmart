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
import java.util.List;

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
        
        if (mAuth.getCurrentUser() == null) {
            binding.progressBar.setVisibility(View.GONE);
            return;
        }
        
        try {
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
            loadAllReportsAndFilterLocally("All Reports");
        } catch (Exception e) {
            Log.e(TAG, "Error loading reports: " + e.getMessage());
            Toast.makeText(this, "Error loading reports", Toast.LENGTH_SHORT).show();
            binding.progressBar.setVisibility(View.GONE);
            showNoReportsMessage(true);
        }
    }
    
    /**
     * New method that loads all waste reports instead of collector reports
     */
    private void loadAllReportsAndFilterLocally(String filterOption) {
        reportsList.clear();
        
        // Simple query that loads all waste reports (no complex indexes needed)
        db.collection("waste_reports")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                Log.d(TAG, "Retrieved " + queryDocumentSnapshots.size() + " waste reports");
                
                if (!queryDocumentSnapshots.isEmpty()) {
                    List<WasteReport> allReports = new ArrayList<>();
                    
                    // Process each waste report
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            WasteReport report = document.toObject(WasteReport.class);
                            report.setId(document.getId());
                            allReports.add(report);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing waste report: " + document.getId(), e);
                        }
                    }
                    
                    // Sort by timestamp (descending)
                    allReports.sort((r1, r2) -> {
                        Long t1 = r1.getTimestamp();
                        Long t2 = r2.getTimestamp();
                        if (t1 == null && t2 == null) return 0;
                        if (t1 == null) return 1;
                        if (t2 == null) return -1;
                        return t2.compareTo(t1); // Descending order (newest first)
                    });
                    
                    // Filter based on selected spinner option
                    if (filterOption != null && !filterOption.equals("All Reports")) {
                        String status = filterOption.toUpperCase();
                        // Handle special case for "IN PROGRESS"
                        if (status.equals("IN PROGRESS")) {
                            status = "IN_PROGRESS";
                        }
                        
                        for (WasteReport report : allReports) {
                            if (status.equalsIgnoreCase(report.getStatus())) {
                                reportsList.add(report);
                            }
                        }
                    } else {
                        // No filter - add all
                        reportsList.addAll(allReports);
                    }
                    
                    reportAdapter.notifyDataSetChanged();
                    showNoReportsMessage(reportsList.isEmpty());
                    
                    Log.d(TAG, "Displaying " + reportsList.size() + " waste reports after filtering");
                } else {
                    showNoReportsMessage(true);
                    Log.d(TAG, "No waste reports found");
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
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected int getActiveNavItemIndex() {
        return 3; // Reports tab index
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
        // Refresh reports when returning to this activity
        loadReports();
    }
}

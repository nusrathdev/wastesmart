package com.wastesmart.collector;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.wastesmart.R;
import com.wastesmart.adapters.ReportAdapter;
import com.wastesmart.databinding.ActivityCollectorReportsBinding;
import com.wastesmart.models.Report;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class CollectorReportsActivity extends BaseCollectorActivity {
    
    private ActivityCollectorReportsBinding binding;
    private ReportAdapter reportAdapter;
    private List<Report> reportsList;
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
        getSupportActionBar().setTitle("Collection Reports");
        
        // Setup bottom navigation
        setupBottomNavigation();
        
        // Initialize reports list and adapter
        reportsList = new ArrayList<>();
        reportAdapter = new ReportAdapter(this, reportsList, (report, position) -> {
            // Handle report click
            Toast.makeText(this, "Report details coming soon", Toast.LENGTH_SHORT).show();
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
        String[] filterOptions = {"All Reports", "Completed", "In Progress", "Pending"};
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
        
        Query query;
        
        if (mAuth.getCurrentUser() == null) {
            binding.progressBar.setVisibility(View.GONE);
            return;
        }
        
        String collectorId = mAuth.getCurrentUser().getUid();
        
        if (filter.equals("All Reports")) {
            query = db.collection("reports")
                    .whereEqualTo("assignedCollectorId", collectorId)
                    .orderBy("reportDate", Query.Direction.DESCENDING);
        } else {
            String status = filter.toUpperCase();
            query = db.collection("reports")
                    .whereEqualTo("assignedCollectorId", collectorId)
                    .whereEqualTo("status", status)
                    .orderBy("reportDate", Query.Direction.DESCENDING);
        }
        
        fetchReports(query);
    }
    
    private void loadReports() {
        binding.progressBar.setVisibility(View.VISIBLE);
        
        if (mAuth.getCurrentUser() == null) {
            binding.progressBar.setVisibility(View.GONE);
            return;
        }
        
        String collectorId = mAuth.getCurrentUser().getUid();
        
        Query query = db.collection("reports")
                .whereEqualTo("assignedCollectorId", collectorId)
                .orderBy("reportDate", Query.Direction.DESCENDING);
        
        fetchReports(query);
    }
    
    private void fetchReports(Query query) {
        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            reportsList.clear();
            
            if (!queryDocumentSnapshots.isEmpty()) {
                for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                    Report report = queryDocumentSnapshots.getDocuments().get(i).toObject(Report.class);
                    report.setId(queryDocumentSnapshots.getDocuments().get(i).getId());
                    reportsList.add(report);
                }
                
                reportAdapter.notifyDataSetChanged();
                showNoReportsMessage(false);
            } else {
                showNoReportsMessage(true);
            }
            
            binding.progressBar.setVisibility(View.GONE);
        }).addOnFailureListener(e -> {
            binding.progressBar.setVisibility(View.GONE);
            
            // Check if error is about missing index
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("FAILED_PRECONDITION") && errorMessage.contains("index")) {
                // Extract the index creation URL if available
                String indexMessage = "This query requires a Firestore index. Please create it in the Firebase Console.";
                
                if (errorMessage.contains("https://console.firebase.google.com")) {
                    int startIndex = errorMessage.indexOf("https://");
                    if (startIndex > 0) {
                        String indexUrl = errorMessage.substring(startIndex).trim();
                        // For development purposes - show this message 
                        Toast.makeText(CollectorReportsActivity.this, 
                            "Index required. View logs for details.", Toast.LENGTH_LONG).show();
                        
                        // Log the URL for developers
                        android.util.Log.e("Firestore", "Create index at: " + indexUrl);
                        
                        // Fallback: Load all reports and filter in-memory
                        loadAllReportsAndFilterLocally();
                        return;
                    }
                }
                Toast.makeText(CollectorReportsActivity.this, indexMessage, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(CollectorReportsActivity.this, "Error loading reports: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
            
            // Attempt fallback loading
            loadAllReportsAndFilterLocally();
        });
    }
    
    private void showNoReportsMessage(boolean show) {
        if (show) {
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
     * Fallback method when Firestore indexes are not available
     * Loads all reports for the collector and filters them locally
     */
    private void loadAllReportsAndFilterLocally() {
        if (mAuth.getCurrentUser() == null) {
            binding.progressBar.setVisibility(View.GONE);
            return;
        }
        
        String collectorId = mAuth.getCurrentUser().getUid();
        
        // Simple query that doesn't require a complex index
        db.collection("reports")
            .whereEqualTo("assignedCollectorId", collectorId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                reportsList.clear();
                
                if (!queryDocumentSnapshots.isEmpty()) {
                    List<Report> allReports = new ArrayList<>();
                    
                    for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                        Report report = queryDocumentSnapshots.getDocuments().get(i).toObject(Report.class);
                        report.setId(queryDocumentSnapshots.getDocuments().get(i).getId());
                        allReports.add(report);
                    }
                    
                    // Sort by reportDate (descending)
                    allReports.sort((r1, r2) -> Long.compare(r2.getReportDate(), r1.getReportDate()));
                    
                    // Filter based on selected spinner option
                    String filterOption = (String) binding.spinnerFilter.getSelectedItem();
                    if (filterOption != null && !filterOption.equals("All Reports")) {
                        String status = filterOption.toUpperCase();
                        for (Report report : allReports) {
                            if (status.equals(report.getStatus())) {
                                reportsList.add(report);
                            }
                        }
                    } else {
                        // No filter - add all
                        reportsList.addAll(allReports);
                    }
                    
                    reportAdapter.notifyDataSetChanged();
                    showNoReportsMessage(reportsList.isEmpty());
                } else {
                    showNoReportsMessage(true);
                }
                
                binding.progressBar.setVisibility(View.GONE);
            })
            .addOnFailureListener(e -> {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(CollectorReportsActivity.this, 
                    "Error loading reports: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                showNoReportsMessage(true);
            });
    }
}

package com.wastesmart.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.wastesmart.R;
import com.wastesmart.models.WasteReport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageReportsActivity extends AppCompatActivity {

    private static final String TAG = "ManageReports";
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private ReportsAdapter adapter;
    private List<WasteReport> reportsList;
    private List<String> collectorsList;
    private Map<String, String> collectorsMap; // ID -> Name mapping

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_reports);

        // Setup toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Manage Waste Reports");
        }

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize collectors list
        collectorsList = new ArrayList<>();
        collectorsMap = new HashMap<>();
        loadCollectors();

        // Initialize views
        recyclerView = findViewById(R.id.recyclerViewReports);
        progressBar = findViewById(R.id.progressBar);

        // Setup RecyclerView
        reportsList = new ArrayList<>();
        adapter = new ReportsAdapter(reportsList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Load reports
        loadWasteReports();
    }

    private void loadCollectors() {
        // Use a single hardcoded collector for simplicity
        collectorsList.clear();
        collectorsMap.clear();
        
        // Add single default collector
        collectorsList.add("Waste Collector (All Areas)");
        collectorsMap.put("Waste Collector (All Areas)", "default_collector");
        
        Log.d(TAG, "Loaded 1 default collector");
    }

    private void loadWasteReports() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("waste_reports")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
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
                        }                    }

                    adapter.notifyDataSetChanged();

                    if (reportsList.isEmpty()) {
                        Toast.makeText(this, "No waste reports found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error loading waste reports", e);
                    Toast.makeText(this, "Error loading reports: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    public void updateReportStatus(String reportId, String status) {
        db.collection("waste_reports").document(reportId)
                .update("status", status)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Report status updated", Toast.LENGTH_SHORT).show();
                    loadWasteReports(); // Refresh the list
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating report status", e);
                    Toast.makeText(this, "Error updating status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public void assignReportToCollector(String reportId, WasteReport report) {
        if (collectorsList.isEmpty()) {
            Toast.makeText(this, "No collectors available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create collector selection dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Assign to Collector");
        
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_assign_collector, null);
        Spinner spinnerCollectors = dialogView.findViewById(R.id.spinnerCollectors);
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item, collectorsList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCollectors.setAdapter(adapter);
        
        builder.setView(dialogView);
        builder.setPositiveButton("Assign", (dialog, which) -> {
            String selectedCollector = (String) spinnerCollectors.getSelectedItem();
            String collectorId = collectorsMap.get(selectedCollector);
            
            // Update report with assigned collector
            Map<String, Object> updates = new HashMap<>();
            updates.put("status", "assigned");
            updates.put("assignedCollectorId", collectorId);
            updates.put("assignedCollectorName", selectedCollector);
            updates.put("assignedTimestamp", System.currentTimeMillis());
            
            db.collection("waste_reports").document(reportId)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Report assigned to " + selectedCollector, Toast.LENGTH_SHORT).show();
                        loadWasteReports(); // Refresh the list
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error assigning report", e);
                        Toast.makeText(this, "Error assigning report: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

package com.wastesmart.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.wastesmart.R;
import com.wastesmart.databinding.ActivityManageCollectorBinding;
import com.wastesmart.models.Collector;
import com.wastesmart.models.User;

import java.util.HashMap;
import java.util.Map;

public class ManageCollectorActivity extends BaseAdminActivity {

    private static final String TAG = "ManageCollectorActivity";
    private ActivityManageCollectorBinding binding;
    private FirebaseFirestore db;
    private Collector currentCollector;
    private String collectorUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageCollectorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup toolbar
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("Manage Collector");

        // Setup bottom navigation
        setupBottomNavigation();

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Set up edit button listener
        binding.btnEditCollector.setOnClickListener(v -> showEditCollectorDialog());

        // Load collector information (this will also load performance metrics)
        loadCollectorInfo();
    }

    private void loadCollectorInfo() {
        // Query for the default collector in the collectors collection
        db.collection("collectors")
                .limit(1) // Get just the first one since we're using a single collector model
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Get the first collector
                        QueryDocumentSnapshot document = queryDocumentSnapshots.iterator().next();
                        currentCollector = document.toObject(Collector.class);
                        currentCollector.setId(document.getId());
                        
                        Log.d(TAG, "Loaded collector with ID: " + currentCollector.getId());
                        Log.d(TAG, "Collector name: " + currentCollector.getName());
                        
                        // Update UI with collector info
                        updateCollectorUI();

                        // Load performance metrics after we have the collector
                        loadPerformanceMetrics();

                        // Also try to find the User record for this collector to get the user ID
                        findCollectorUser();
                    } else {
                        // No collector found - create default one
                        Log.w(TAG, "No collector found in database, creating default one");
                        createDefaultCollector();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading collector info", e);
                    Toast.makeText(this, "Failed to load collector information", Toast.LENGTH_SHORT).show();
                });
    }

    private void findCollectorUser() {
        // Look for user with type "collector"
        db.collection("users")
                .whereEqualTo("userType", "collector")
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        QueryDocumentSnapshot document = queryDocumentSnapshots.iterator().next();
                        collectorUserId = document.getId();
                        
                        // Now we have the user ID for this collector
                        Log.d(TAG, "Found collector user ID: " + collectorUserId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error finding collector user", e);
                });
    }

    private void createDefaultCollector() {
        // Create a default collector record
        Collector collector = new Collector();
        collector.setName("Waste Collector");
        collector.setEmail("collector@wastesmart.com");
        collector.setPhoneNumber("+1234567890");
        collector.setEmployeeId("WSC001");
        collector.setAssignedArea("All Areas");

        // Save to Firestore
        db.collection("collectors")
                .add(collector)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Default collector created", Toast.LENGTH_SHORT).show();
                    collector.setId(documentReference.getId());
                    currentCollector = collector;
                    updateCollectorUI();
                    
                    // Load performance metrics after creating the collector
                    loadPerformanceMetrics();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating default collector", e);
                    Toast.makeText(this, "Failed to create collector", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateCollectorUI() {
        if (currentCollector != null) {
            binding.tvCollectorName.setText(currentCollector.getName());
            binding.tvCollectorPhone.setText(currentCollector.getPhoneNumber());
            binding.tvCollectorEmail.setText(currentCollector.getEmail());
            
            // You could add more fields here if needed
        }
    }

    private void loadPerformanceMetrics() {
        if (currentCollector == null || currentCollector.getId() == null) {
            Log.w(TAG, "Cannot load performance metrics: collector is null or has no ID");
            return;
        }
        
        Log.d(TAG, "Loading performance metrics for collector ID: " + currentCollector.getId());
        
        // Get all reports and count them
        db.collection("waste_reports")
                .get()
                .addOnSuccessListener(allReports -> {
                    Log.d(TAG, "Total reports in database: " + allReports.size());
                    int completedCount = 0;
                    int assignedCount = 0;
                    int inProgressCount = 0;
                    
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : allReports) {
                        String status = doc.getString("status");
                        String assignedCollectorId = doc.getString("assignedCollectorId");
                        
                        Log.d(TAG, "Report ID: " + doc.getId() + ", Status: " + status + ", AssignedCollectorId: " + assignedCollectorId);
                        
                        // Count reports assigned to this collector or count all if no specific assignment
                        boolean isAssignedToThisCollector = (assignedCollectorId != null && currentCollector.getId().equals(assignedCollectorId));
                        boolean isAssignedToDefaultCollector = (assignedCollectorId != null && assignedCollectorId.equals("default_collector"));
                        boolean noAssignmentSpecified = (assignedCollectorId == null || assignedCollectorId.isEmpty());
                        
                        Log.d(TAG, "Collector matching - ThisCollector: " + isAssignedToThisCollector + 
                               ", DefaultCollector: " + isAssignedToDefaultCollector + 
                               ", NoAssignment: " + noAssignmentSpecified);
                        
                        if (isAssignedToThisCollector || isAssignedToDefaultCollector || noAssignmentSpecified) {
                            if ("COMPLETED".equals(status)) {
                                completedCount++;
                            } else if ("ASSIGNED".equals(status)) {
                                assignedCount++;
                            } else if ("IN_PROGRESS".equals(status)) {
                                inProgressCount++;
                                assignedCount++; // Count in-progress as part of assigned work
                            }
                        }
                    }
                    
                    Log.d(TAG, "Found " + completedCount + " completed reports and " + assignedCount + " assigned/in-progress reports");
                    
                    binding.tvCompletedCount.setText(String.valueOf(completedCount));
                    binding.tvAssignedCount.setText(String.valueOf(assignedCount));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading reports for performance metrics", e);
                    binding.tvCompletedCount.setText("0");
                    binding.tvAssignedCount.setText("0");
                });
    }

    private void showEditCollectorDialog() {
        if (currentCollector == null) {
            Toast.makeText(this, "Collector information not available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Inflate the dialog layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_collector, null);

        // Get references to the dialog fields
        TextInputEditText etName = dialogView.findViewById(R.id.etCollectorName);
        TextInputEditText etPhone = dialogView.findViewById(R.id.etCollectorPhone);
        TextInputEditText etEmail = dialogView.findViewById(R.id.etCollectorEmail);
        
        // Pre-fill the fields with current collector info
        etName.setText(currentCollector.getName());
        etPhone.setText(currentCollector.getPhoneNumber());
        etEmail.setText(currentCollector.getEmail());

        // Create and show the dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        // Set up button listeners
        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btnSave).setOnClickListener(v -> {
            // Update collector object
            currentCollector.setName(etName.getText().toString().trim());
            currentCollector.setPhoneNumber(etPhone.getText().toString().trim());
            currentCollector.setEmail(etEmail.getText().toString().trim());

            // Save to Firestore
            saveCollectorChanges();
            
            dialog.dismiss();
        });

        dialog.show();
    }

    private void saveCollectorChanges() {
        if (currentCollector == null || currentCollector.getId() == null) {
            Toast.makeText(this, "Cannot save: Invalid collector data", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update the collector document
        db.collection("collectors")
                .document(currentCollector.getId())
                .set(currentCollector)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Collector information updated", Toast.LENGTH_SHORT).show();
                    updateCollectorUI();
                    
                    // Also update the corresponding user document if we have its ID
                    updateCollectorUser();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating collector", e);
                    Toast.makeText(this, "Failed to update collector information", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateCollectorUser() {
        if (collectorUserId != null) {
            // Update user name, email, and phone to match collector
            Map<String, Object> updates = new HashMap<>();
            updates.put("name", currentCollector.getName());
            updates.put("email", currentCollector.getEmail());
            updates.put("phone", currentCollector.getPhoneNumber());
            
            db.collection("users")
                    .document(collectorUserId)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> 
                        Log.d(TAG, "Collector user information also updated"))
                    .addOnFailureListener(e -> 
                        Log.e(TAG, "Error updating collector user", e));
        }
    }

    @Override
    protected int getActiveItemIndex() {
        // Collector tab is active (index 1)
        return 1;
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadPerformanceMetrics(); // Refresh performance metrics when activity is resumed
    }
}

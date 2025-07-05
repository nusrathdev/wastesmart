package com.wastesmart.collector;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.wastesmart.MainActivity;
import com.wastesmart.R;
import com.wastesmart.databinding.ActivityCollectionTasksBinding;
import com.wastesmart.models.WasteReport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectionTasksActivity extends BaseCollectorActivity {

    private static final String TAG = "CollectionTasks";
    private ActivityCollectionTasksBinding binding;
    private FirebaseFirestore db;
    private TasksAdapter adapter;
    private List<WasteReport> tasksList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCollectionTasksBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Setup toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Collection Tasks");
        }
        
        // Setup bottom navigation
        setupBottomNavigation();

        // Setup RecyclerView
        tasksList = new ArrayList<>();
        adapter = new TasksAdapter(tasksList, this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        // Load assigned tasks
        loadAssignedTasks();
    }

    private void loadAssignedTasks() {
        binding.progressBar.setVisibility(View.VISIBLE);

        // Simple approach: Load all reports and filter client-side
        // This avoids any Firestore index requirements
        Log.d(TAG, "Starting to load waste_reports collection");
        db.collection("waste_reports")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    binding.progressBar.setVisibility(View.GONE);
                    tasksList.clear();
                    
                    Log.d(TAG, "Retrieved " + queryDocumentSnapshots.size() + " total reports from Firestore");

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            WasteReport task = document.toObject(WasteReport.class);
                            task.setId(document.getId());
                            
                            // Filter for assigned and in_progress tasks only
                            String status = task.getStatus();
                            if (status != null) {
                                status = status.toUpperCase(); // Normalize to uppercase for comparison
                                if ("ASSIGNED".equals(status) || "IN_PROGRESS".equals(status)) {
                                    tasksList.add(task);
                                    Log.d(TAG, "Added task with ID: " + task.getId() + ", status: " + status);
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing task: " + document.getId(), e);
                        }
                    }

                    // Sort by assigned timestamp (newest first)
                    tasksList.sort((t1, t2) -> {
                        Long ts1 = t1.getAssignedTimestamp();
                        Long ts2 = t2.getAssignedTimestamp();
                        if (ts1 == null && ts2 == null) return 0;
                        if (ts1 == null) return 1;
                        if (ts2 == null) return -1;
                        return ts2.compareTo(ts1); // Descending order (newest first)
                    });

                    adapter.notifyDataSetChanged();

                    if (tasksList.isEmpty()) {
                        // Check if we need to create a test report for debugging
                        if (queryDocumentSnapshots.isEmpty()) {
                            Log.d(TAG, "No reports found in database, creating test report");
                            createTestReport();
                        } else {
                            Log.d(TAG, "Reports found but none match collector criteria");
                        }
                        
                        binding.tvNoTasks.setVisibility(View.VISIBLE);
                        binding.recyclerView.setVisibility(View.GONE);
                        binding.tvNoTasks.setText("No collection tasks assigned yet");
                    } else {
                        binding.tvNoTasks.setVisibility(View.GONE);
                        binding.recyclerView.setVisibility(View.VISIBLE);
                        Log.d(TAG, "Showing " + tasksList.size() + " tasks to collector");
                    }
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error loading tasks", e);
                    Toast.makeText(this, "Error loading tasks: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    public void updateTaskStatus(String taskId, String status) {
        // Always use uppercase for status to maintain consistency across the app
        String normalizedStatus = status.toUpperCase();
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", normalizedStatus);
        if ("COMPLETED".equals(normalizedStatus)) {
            updates.put("completedTimestamp", System.currentTimeMillis());
        }

        db.collection("waste_reports").document(taskId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Task status updated to " + status, Toast.LENGTH_SHORT).show();
                    loadAssignedTasks(); // Refresh the list
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating task status", e);
                    Toast.makeText(this, "Error updating task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_logout) {
            // Sign out and go to main activity
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(CollectionTasksActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected int getActiveNavItemIndex() {
        return 0; // Tasks tab index (0-based)
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tasks, menu);
        return true;
    }
    
    // Create a test report with ASSIGNED status for debugging purposes
    private void createTestReport() {
        Map<String, Object> testReport = new HashMap<>();
        testReport.put("wasteType", "Test Waste");
        testReport.put("wasteSize", "Medium");
        testReport.put("description", "This is a test waste report for collector view");
        testReport.put("latitude", 37.7749);
        testReport.put("longitude", -122.4194);
        testReport.put("status", "ASSIGNED");
        testReport.put("timestamp", System.currentTimeMillis());
        testReport.put("assignedTimestamp", System.currentTimeMillis());
        testReport.put("assignedCollectorId", "default_collector");
        testReport.put("assignedCollectorName", "Default Collector");
        testReport.put("userId", "test_user");
        
        // Add realistic image URL for better testing
        String imageUrl = "https://firebasestorage.googleapis.com/v0/b/wastesmart-app.appspot.com/o/waste_images%2Fgeneral_waste.jpg?alt=media";
        testReport.put("photoUrl", imageUrl);
        testReport.put("imageUrl", imageUrl);
        
        Log.d(TAG, "Creating test report with image URL: " + imageUrl);
        
        // Add to Firestore
        db.collection("waste_reports")
            .add(testReport)
            .addOnSuccessListener(documentReference -> {
                Log.d(TAG, "Test report created with ID: " + documentReference.getId());
                Toast.makeText(this, "Created test report", Toast.LENGTH_SHORT).show();
                
                // Reload after a delay to show the test data
                new android.os.Handler().postDelayed(this::loadAssignedTasks, 1500);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error creating test report", e);
                Toast.makeText(this, "Failed to create test report", Toast.LENGTH_SHORT).show();
            });
    }
}

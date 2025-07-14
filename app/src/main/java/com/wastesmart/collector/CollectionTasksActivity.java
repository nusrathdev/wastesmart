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
    private FirebaseAuth mAuth;
    private TasksAdapter adapter;
    private List<WasteReport> tasksList;
    private String actualCollectorId = "default_collector"; // fallback

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCollectionTasksBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

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

        // Load actual collector ID
        loadActualCollectorId();

        // Load assigned tasks
        loadAssignedTasks();
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

    private void loadAssignedTasks() {
        binding.progressBar.setVisibility(View.VISIBLE);
        
        // First get the collector document ID using the Firebase Auth user ID
        if (mAuth.getCurrentUser() == null) {
            Log.d(TAG, "No authenticated user");
            binding.progressBar.setVisibility(View.GONE);
            binding.tvNoTasks.setVisibility(View.VISIBLE);
            binding.tvNoTasks.setText("Please log in to view tasks");
            return;
        }
        
        String authUserId = mAuth.getCurrentUser().getUid();
        Log.d(TAG, "Loading tasks for auth user ID: " + authUserId);
        
        // First find the collector document using the auth user ID
        db.collection("collectors")
                .whereEqualTo("userId", authUserId)
                .limit(1)
                .get()
                .addOnSuccessListener(collectorQuery -> {
                    if (!collectorQuery.isEmpty()) {
                        String collectorId = collectorQuery.getDocuments().get(0).getId();
                        Log.d(TAG, "Found collector document ID: " + collectorId);
                        
                        // Now query for tasks assigned to this collector
                        db.collection("waste_reports")
                                .whereEqualTo("assignedCollectorId", collectorId)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    binding.progressBar.setVisibility(View.GONE);
                                    tasksList.clear();
                                    
                                    Log.d(TAG, "Retrieved " + queryDocumentSnapshots.size() + " reports for collector ID: " + collectorId);

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
                    } else {
                        Log.w(TAG, "No collector found for auth user ID: " + authUserId);
                        binding.progressBar.setVisibility(View.GONE);
                        binding.tvNoTasks.setVisibility(View.VISIBLE);
                        binding.tvNoTasks.setText("Collector account not found");
                    }
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error finding collector: " + e.getMessage());
                    Toast.makeText(this, "Error finding collector: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
            // Show confirmation dialog before logging out
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(CollectionTasksActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
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
        testReport.put("assignedCollectorId", actualCollectorId); // Use actual collector ID
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}

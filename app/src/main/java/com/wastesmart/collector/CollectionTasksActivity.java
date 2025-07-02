package com.wastesmart.collector;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
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
        db.collection("waste_reports")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    binding.progressBar.setVisibility(View.GONE);
                    tasksList.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            WasteReport task = document.toObject(WasteReport.class);
                            task.setId(document.getId());
                            
                            // Filter for assigned and in_progress tasks only
                            String status = task.getStatus();
                            if ("assigned".equals(status) || "in_progress".equals(status)) {
                                tasksList.add(task);
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
                    }
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error loading tasks", e);
                    Toast.makeText(this, "Error loading tasks: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    public void updateTaskStatus(String taskId, String status) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);
        if ("completed".equals(status)) {
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
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected int getActiveNavItemIndex() {
        return 0; // Tasks tab index (0-based)
    }
}

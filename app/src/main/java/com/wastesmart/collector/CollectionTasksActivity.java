package com.wastesmart.collector;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.wastesmart.R;
import com.wastesmart.databinding.ActivityCollectionTasksBinding;
import com.wastesmart.models.WasteReport;

import java.util.ArrayList;
import java.util.List;

public class CollectionTasksActivity extends AppCompatActivity {

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
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Collection Tasks");
        }

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

        // Load tasks that are assigned but not completed
        db.collection("waste_reports")
                .whereIn("status", java.util.Arrays.asList("assigned", "in_progress"))
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    binding.progressBar.setVisibility(View.GONE);
                    tasksList.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            WasteReport task = document.toObject(WasteReport.class);
                            task.setId(document.getId());
                            tasksList.add(task);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing task: " + document.getId(), e);
                        }
                    }

                    adapter.notifyDataSetChanged();

                    if (tasksList.isEmpty()) {
                        binding.tvNoTasks.setVisibility(View.VISIBLE);
                        binding.recyclerView.setVisibility(View.GONE);
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

    public void markTaskInProgress(String taskId) {
        updateTaskStatus(taskId, "in_progress");
    }

    public void markTaskCompleted(String taskId) {
        updateTaskStatus(taskId, "completed");
    }

    private void updateTaskStatus(String taskId, String status) {
        db.collection("waste_reports").document(taskId)
                .update("status", status)
                .addOnSuccessListener(aVoid -> {
                    String message = "completed".equals(status) ? "Task completed!" : "Task started!";
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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
}

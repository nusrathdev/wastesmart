package com.wastesmart.collector;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.wastesmart.MainActivity;
import com.wastesmart.R;
import com.wastesmart.databinding.ActivityCollectorDashboardBinding;
import com.wastesmart.models.Collector;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class CollectorDashboardActivity extends BaseCollectorActivity {

    private ActivityCollectorDashboardBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Collector currentCollector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCollectorDashboardBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // Initialize Firebase components
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Set up toolbar
        setSupportActionBar(binding.toolbar);
        
        // Setup bottom navigation
        setupBottomNavigation();
        
        // Load collector data
        loadCollectorData();
    }
    
    @Override
    protected int getActiveNavItemIndex() {
        return 1; // Home tab index (0-based)
    }

    private void loadCollectorData() {
        // Check if we have collector data from the intent (default credentials)
        String collectorType = getIntent().getStringExtra("collector_type");
        String collectorEmail = getIntent().getStringExtra("collector_email");
        String collectorName = getIntent().getStringExtra("collector_name");
        
        if (collectorType != null && collectorEmail != null) {
            // This is a default collector login
            currentCollector = new Collector();
            currentCollector.setId("default_collector");
            currentCollector.setName(collectorName != null ? collectorName : collectorType);
            currentCollector.setEmail(collectorEmail);
            
            // For supervisor, set different values
            if (collectorEmail.equals("supervisor@wastesmart.com")) {
                currentCollector.setEmployeeId("SUP-001");
                currentCollector.setAssignedArea("All Areas");
            } else {
                currentCollector.setEmployeeId("COL-001");
                currentCollector.setAssignedArea("Downtown");
            }
            
            updateUI();
            return;
        }
        
        // If not using default credentials, check Firebase Auth
        if (mAuth.getCurrentUser() == null) {
            // Not logged in, go back to login
            Intent intent = new Intent(CollectorDashboardActivity.this, CollectorLoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        String collectorId = mAuth.getCurrentUser().getUid();
        db.collection("collectors").document(collectorId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentCollector = documentSnapshot.toObject(Collector.class);
                        updateUI();
                    } else {
                        // Create a fallback collector if document doesn't exist
                        currentCollector = new Collector();
                        currentCollector.setId(collectorId);
                        currentCollector.setName("Waste Collector");
                        currentCollector.setEmail(mAuth.getCurrentUser().getEmail());
                        currentCollector.setEmployeeId("NEW-001");
                        currentCollector.setAssignedArea("Not Assigned");
                        updateUI();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CollectorDashboardActivity.this, "Error loading collector data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUI() {
        if (currentCollector != null && binding != null) {
            try {
                // Set collector name
                if (binding.tvCollectorName != null) {
                    binding.tvCollectorName.setText(currentCollector.getName());
                }
                
                // Set dynamic welcome message
                String[] welcomeMessages = {
                    "Ready to collect and clean up today?",
                    "Your work keeps our city clean!",
                    "Today's routes are ready for you!",
                    "Thank you for your dedication!",
                    "Making our environment better!"
                };
                int randomIndex = (int) (Math.random() * welcomeMessages.length);
                if (binding.tvWelcomeMessage != null) {
                    binding.tvWelcomeMessage.setText(welcomeMessages[randomIndex]);
                }
                
                // Load assigned task count
                loadAssignedTaskCount();
                
                // Initially set a placeholder value that will be updated
                if (binding.tvTasksCount != null) {
                    binding.tvTasksCount.setText("0");
                }
                
                // Setup route view button
                setupViewRoutesButton();
            } catch (Exception e) {
                Toast.makeText(this, "Error updating UI: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else if (currentCollector == null) {
            Toast.makeText(this, "No collector data available. Please log in again.", Toast.LENGTH_LONG).show();
            // Redirect to login
            Intent intent = new Intent(CollectorDashboardActivity.this, CollectorLoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
    
    // Setup task view button
    private void setupViewRoutesButton() {
        try {
            if (binding != null && binding.btnViewTasks != null) {
                binding.btnViewTasks.setOnClickListener(v -> {
                    // Navigate to Tasks Activity using the navigation method
                    navigateToTasks();
                });
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error setting up task button: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Load the count of assigned waste reports for this collector
     */
    private void loadAssignedTaskCount() {
        if (mAuth.getCurrentUser() == null) {
            // If not logged in, just use placeholder
            if (binding.tvTasksCount != null) {
                binding.tvTasksCount.setText("0");
            }
            return;
        }

        // Get collector ID
        String collectorId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "default_collector";

        // Query Firestore for assigned waste reports
        db.collection("waste_reports")
            .whereEqualTo("assignedCollectorId", collectorId)
            .whereEqualTo("status", "ASSIGNED")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                // Set the count to the TextView
                int assignedCount = queryDocumentSnapshots.size();
                if (binding.tvTasksCount != null) {
                    binding.tvTasksCount.setText(String.valueOf(assignedCount));
                }
            })
            .addOnFailureListener(e -> {
                // On failure, log error but don't show to user
                Log.e("CollectorDashboard", "Error getting assigned task count: " + e.getMessage());
                if (binding.tvTasksCount != null) {
                    binding.tvTasksCount.setText("?");
                }
            });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_logout_only, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            mAuth.signOut();
            Intent intent = new Intent(CollectorDashboardActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

package com.wastesmart.collector;

import android.content.Intent;
import android.content.SharedPreferences;
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

    private static final String TAG = "CollectorDashboard";
    private ActivityCollectorDashboardBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Collector currentCollector;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "WasteSmartPrefs";
    private static final String COLLECTOR_KEY = "currentCollector";
    private static final String TASKS_COUNT_KEY = "assignedTasksCount";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCollectorDashboardBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // Initialize Firebase components
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        
        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Set up toolbar
        setSupportActionBar(binding.toolbar);
        
        // Setup bottom navigation
        setupBottomNavigation();
        
        // Load collector data
        loadCollectorData();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // When resuming, refresh assigned task count
        if (currentCollector != null) {
            loadAssignedTaskCount();
        }
        
        // Restore state if available
        restoreState();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Save state when pausing
        saveState();
    }
    
    @Override
    protected int getActiveNavItemIndex() {
        return 1; // Home tab index (0-based)
    }

    private void loadCollectorData() {
        // First try to restore collector from SharedPreferences
        String collectorId = sharedPreferences.getString(COLLECTOR_KEY + "_id", null);
        String collectorName = sharedPreferences.getString(COLLECTOR_KEY + "_name", null);
        String collectorEmail = sharedPreferences.getString(COLLECTOR_KEY + "_email", null);
        String employeeId = sharedPreferences.getString(COLLECTOR_KEY + "_employeeId", null);
        String assignedArea = sharedPreferences.getString(COLLECTOR_KEY + "_area", null);
        
        if (collectorId != null && collectorName != null) {
            try {
                currentCollector = new Collector();
                currentCollector.setId(collectorId);
                currentCollector.setName(collectorName);
                currentCollector.setEmail(collectorEmail);
                currentCollector.setEmployeeId(employeeId);
                currentCollector.setAssignedArea(assignedArea);
                
                Log.d(TAG, "Loaded collector from SharedPreferences: " + currentCollector.getName());
                updateUI();
                return;
            } catch (Exception e) {
                Log.e(TAG, "Error loading collector from SharedPreferences", e);
                // Continue with normal loading
            }
        }
        
        // Check if we have collector data from the intent (default credentials)
        String collectorType = getIntent().getStringExtra("collector_type");
        String intentEmail = getIntent().getStringExtra("collector_email");
        String intentName = getIntent().getStringExtra("collector_name");
        
        // Add debug logging to track login flow
        Log.d(TAG, "Loading collector data. Intent has collector type: " + 
              (collectorType != null) + ", email: " + intentEmail);
        
        if (collectorType != null && intentEmail != null) {
            // This is a default collector login
            currentCollector = new Collector();
            currentCollector.setId("default_collector");
            currentCollector.setName(intentName != null ? intentName : collectorType);
            currentCollector.setEmail(intentEmail);
            
            // For supervisor, set different values
            if (intentEmail.equals("supervisor@wastesmart.com")) {
                currentCollector.setEmployeeId("SUP-001");
                currentCollector.setAssignedArea("All Areas");
            } else {
                currentCollector.setEmployeeId("COL-001");
                currentCollector.setAssignedArea("Downtown");
            }
            
            updateUI();
            saveState(); // Save to SharedPreferences
            return;
        }
        
        // If not using default credentials, check Firebase Auth
        if (mAuth.getCurrentUser() == null) {
            // Check if this is a navigation action within the app (don't redirect to login)
            boolean isNavigationAction = getIntent().getBooleanExtra("isNavigationAction", false);
            
            // Only redirect to login if this is not a navigation action
            if (!isNavigationAction) {
                Log.d(TAG, "User not logged in, redirecting to login");
                Intent intent = new Intent(CollectorDashboardActivity.this, CollectorLoginActivity.class);
                startActivity(intent);
                finish();
                return;
            } else {
                Log.d(TAG, "Navigation action detected, not redirecting to login");
                // Create a default collector for navigation
                currentCollector = new Collector();
                currentCollector.setId("navigation_collector");
                currentCollector.setName("Collector");
                currentCollector.setEmail("collector@wastesmart.com");
                currentCollector.setEmployeeId("NAV-001");
                currentCollector.setAssignedArea("Navigation Area");
                updateUI();
                saveState(); // Save to SharedPreferences
                return;
            }
        }

        String currentUserId = mAuth.getCurrentUser().getUid();
        db.collection("collectors").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentCollector = documentSnapshot.toObject(Collector.class);
                        updateUI();
                        saveState(); // Save to SharedPreferences
                    } else {
                        // Create a fallback collector if document doesn't exist
                        currentCollector = new Collector();
                        currentCollector.setId(currentUserId);
                        currentCollector.setName("Waste Collector");
                        currentCollector.setEmail(mAuth.getCurrentUser().getEmail());
                        currentCollector.setEmployeeId("NEW-001");
                        currentCollector.setAssignedArea("Not Assigned");
                        updateUI();
                        saveState(); // Save to SharedPreferences
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
                
                // Restore task count if available
                String savedCount = sharedPreferences.getString(TASKS_COUNT_KEY, null);
                if (savedCount != null && binding.tvTasksCount != null) {
                    binding.tvTasksCount.setText(savedCount);
                } else {
                    // Load assigned task count
                    loadAssignedTaskCount();
                    
                    // Initially set a placeholder value that will be updated
                    if (binding.tvTasksCount != null) {
                        binding.tvTasksCount.setText("0");
                    }
                }
                
                // Setup route view button
                setupViewRoutesButton();
            } catch (Exception e) {
                Toast.makeText(this, "Error updating UI: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else if (currentCollector == null) {
            // Check if this is a navigation action within the app
            boolean isNavigationAction = getIntent().getBooleanExtra("isNavigationAction", false);
            
            if (isNavigationAction) {
                Log.d(TAG, "Navigation action detected but collector is null, creating default");
                // Create a default collector for navigation to prevent login redirect
                currentCollector = new Collector();
                currentCollector.setId("navigation_collector");
                currentCollector.setName("Waste Collector");
                currentCollector.setEmail("collector@wastesmart.com");
                currentCollector.setEmployeeId("NAV-001");
                currentCollector.setAssignedArea("Navigation Area");
                saveState(); // Save to SharedPreferences
                // Try updating UI again
                updateUI();
                return;
            } else {
                Toast.makeText(this, "No collector data available. Please log in again.", Toast.LENGTH_LONG).show();
                // Redirect to login
                Intent intent = new Intent(CollectorDashboardActivity.this, CollectorLoginActivity.class);
                startActivity(intent);
                finish();
            }
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
        // Get collector ID - use currentCollector if available, otherwise use Firebase Auth
        String collectorId;
        
        if (currentCollector != null && currentCollector.getId() != null) {
            collectorId = currentCollector.getId();
        } else if (mAuth.getCurrentUser() != null) {
            collectorId = mAuth.getCurrentUser().getUid();
        } else {
            collectorId = "default_collector";
            Log.d(TAG, "Using default collector ID for assigned count");
        }
        
        Log.d(TAG, "Loading assigned task count for collector ID: " + collectorId);
        
        // Query Firestore for assigned waste reports (use uppercase for consistent comparison)
        db.collection("waste_reports")
            .whereEqualTo("assignedCollectorId", collectorId)
            .whereEqualTo("status", "ASSIGNED")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                // Set the count to the TextView
                int assignedCount = queryDocumentSnapshots.size();
                Log.d(TAG, "Found " + assignedCount + " assigned tasks for collector ID: " + collectorId);
                
                if (binding.tvTasksCount != null) {
                    String countStr = String.valueOf(assignedCount);
                    binding.tvTasksCount.setText(countStr);
                    
                    // Save the count to SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(TASKS_COUNT_KEY, countStr);
                    editor.apply();
                }
            })
            .addOnFailureListener(e -> {
                // On failure, log error but don't show to user
                Log.e(TAG, "Error getting assigned task count: " + e.getMessage());
                if (binding.tvTasksCount != null) {
                    binding.tvTasksCount.setText("?");
                }
            });
    }

    /**
     * Save current state to SharedPreferences
     */
    private void saveState() {
        if (currentCollector != null && sharedPreferences != null) {
            try {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                
                // Save collector fields individually
                editor.putString(COLLECTOR_KEY + "_id", currentCollector.getId());
                editor.putString(COLLECTOR_KEY + "_name", currentCollector.getName());
                editor.putString(COLLECTOR_KEY + "_email", currentCollector.getEmail());
                editor.putString(COLLECTOR_KEY + "_employeeId", currentCollector.getEmployeeId());
                editor.putString(COLLECTOR_KEY + "_area", currentCollector.getAssignedArea());
                
                // Also save the current tasks count if available
                if (binding != null && binding.tvTasksCount != null) {
                    String count = binding.tvTasksCount.getText().toString();
                    editor.putString(TASKS_COUNT_KEY, count);
                }
                
                editor.apply();
                Log.d(TAG, "Saved collector state to SharedPreferences");
            } catch (Exception e) {
                Log.e(TAG, "Error saving state", e);
            }
        }
    }
    
    /**
     * Restore state from SharedPreferences
     */
    private void restoreState() {
        // Already handled in loadCollectorData for collector object
        // Just need to restore task count if not already done
        if (binding != null && binding.tvTasksCount != null) {
            String savedCount = sharedPreferences.getString(TASKS_COUNT_KEY, null);
            if (savedCount != null) {
                binding.tvTasksCount.setText(savedCount);
                Log.d(TAG, "Restored tasks count: " + savedCount);
            }
        }
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
            // Clear SharedPreferences when logging out
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
            
            mAuth.signOut();
            Intent intent = new Intent(CollectorDashboardActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}

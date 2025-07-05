package com.wastesmart.collector;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import com.wastesmart.MainActivity;
import com.wastesmart.R;
import com.wastesmart.databinding.ActivityCollectorDashboardBinding;
import com.wastesmart.models.Collector;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CollectorDashboardActivity extends BaseCollectorActivity {

    private static final String TAG = "CollectorDashboard";
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
        
        // Add debug logging to track login flow
        Log.d(TAG, "Loading collector data. Intent has collector type: " + 
              (collectorType != null) + ", email: " + collectorEmail);
        
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
                return;
            }
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
        
        // First check if we should create some test data for empty cases
        db.collection("waste_reports")
            .whereEqualTo("assignedCollectorId", collectorId)
            .limit(1)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                if (querySnapshot.isEmpty() && 
                    ("default_collector".equals(collectorId) || 
                     "navigation_collector".equals(collectorId))) {
                    Log.d(TAG, "No assigned reports found, creating test reports");
                    createTestReports(collectorId);
                }
            });

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
                    binding.tvTasksCount.setText(String.valueOf(assignedCount));
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
     * Creates test reports for demonstration purposes
     */
    private void createTestReports(String collectorId) {
        // Notify user that we're creating test data
        Toast.makeText(this, "Creating test reports for demonstration", Toast.LENGTH_SHORT).show();
        
        Log.d(TAG, "Creating test reports for collector ID: " + collectorId);
        
        // Create test reports with different statuses
        createTestReport("ASSIGNED", "Household Waste", "Large", "Test household waste collection near downtown area", collectorId);
        createTestReport("ASSIGNED", "Recyclable Items", "Medium", "Recyclable materials ready for pickup", collectorId);
        createTestReport("IN_PROGRESS", "Electronic Waste", "Medium", "Used electronics waiting for collection", collectorId);
        createTestReport("COMPLETED", "Garden Waste", "Large", "Garden waste including branches, leaves and grass clippings", collectorId);
        
        Log.d(TAG, "Created test reports, will reload after 2 seconds");
        
        // Reload the dashboard after a delay
        new android.os.Handler().postDelayed(() -> {
            Log.d(TAG, "Reloading dashboard data after creating test reports");
            loadAssignedTaskCount();
        }, 2000);
    }
    
    /**
     * Creates a single test waste report
     */
    private void createTestReport(String status, String wasteType, String wasteSize, String description, String collectorId) {
        Map<String, Object> testReport = new HashMap<>();
        testReport.put("wasteType", wasteType);
        testReport.put("wasteSize", wasteSize);
        testReport.put("description", description);
        testReport.put("latitude", 37.7749 + (Math.random() - 0.5) / 100); // San Francisco with small random variation
        testReport.put("longitude", -122.4194 + (Math.random() - 0.5) / 100);
        testReport.put("status", status);
        testReport.put("timestamp", System.currentTimeMillis() - (long)(Math.random() * 86400000)); // Within last 24h
        testReport.put("assignedTimestamp", System.currentTimeMillis() - (long)(Math.random() * 43200000)); // Within last 12h
        testReport.put("assignedCollectorId", collectorId);
        testReport.put("assignedCollectorName", currentCollector != null ? currentCollector.getName() : "Default Collector");
        testReport.put("userId", "test_user");
        
        // Add realistic image URL based on waste type
        String imageUrl = "https://firebasestorage.googleapis.com/v0/b/wastesmart-app.appspot.com/o/waste_images%2F";
        
        if (wasteType.contains("Household")) {
            imageUrl += "household_waste.jpg";
        } else if (wasteType.contains("Recyclable")) {
            imageUrl += "recyclable_waste.jpg";
        } else if (wasteType.contains("Electronic")) {
            imageUrl += "electronic_waste.jpg";
        } else if (wasteType.contains("Garden")) {
            imageUrl += "garden_waste.jpg";
        } else {
            imageUrl += "general_waste.jpg";
        }
        
        imageUrl += "?alt=media";
        
        // Set both imageUrl and photoUrl for maximum compatibility with different parts of the app
        testReport.put("imageUrl", imageUrl);
        testReport.put("photoUrl", imageUrl);
        
        // Log the report details we're creating for better debugging
        Log.d(TAG, "Creating test report: status=" + status + ", wasteType=" + wasteType + 
              ", assignedTo=" + collectorId + ", imageUrl=" + imageUrl);
        
        // Add to Firestore
        db.collection("waste_reports")
            .add(testReport)
            .addOnSuccessListener(documentReference -> {
                Log.d(TAG, "Test report created with status " + status + " and ID: " + documentReference.getId());
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error creating test report with status " + status, e);
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

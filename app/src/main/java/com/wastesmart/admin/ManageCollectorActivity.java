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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.wastesmart.R;
import com.wastesmart.adapters.CollectorAdapter;
import com.wastesmart.databinding.ActivityManageCollectorBinding;
import com.wastesmart.models.Collector;
import com.wastesmart.models.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageCollectorActivity extends BaseAdminActivity implements CollectorAdapter.OnCollectorClickListener {

    private static final String TAG = "ManageCollectorActivity";
    private ActivityManageCollectorBinding binding;
    private FirebaseFirestore db;
    private CollectorAdapter collectorAdapter;
    private List<Collector> collectorList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageCollectorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup toolbar
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("Manage Collectors");

        // Setup bottom navigation
        setupBottomNavigation();

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize collectors list and adapter
        collectorList = new ArrayList<>();
        collectorAdapter = new CollectorAdapter(this, collectorList);
        collectorAdapter.setOnCollectorClickListener(this);

        // Setup RecyclerView
        binding.recyclerViewCollectors.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewCollectors.setAdapter(collectorAdapter);

        // Set up create collector button listener
        binding.btnCreateCollector.setOnClickListener(v -> showCreateCollectorDialog());

        // Load all collectors
        loadAllCollectors();
    }

    private void loadAllCollectors() {
        db.collection("collectors")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    collectorList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Collector collector = document.toObject(Collector.class);
                        collector.setId(document.getId());
                        collectorList.add(collector);
                    }
                    
                    if (collectorList.isEmpty()) {
                        binding.tvNoCollectors.setVisibility(View.VISIBLE);
                        binding.recyclerViewCollectors.setVisibility(View.GONE);
                    } else {
                        binding.tvNoCollectors.setVisibility(View.GONE);
                        binding.recyclerViewCollectors.setVisibility(View.VISIBLE);
                        collectorAdapter.updateCollectors(collectorList);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading collectors", e);
                    Toast.makeText(this, "Error loading collectors", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onEditCollector(Collector collector) {
        showEditCollectorDialog(collector);
    }

    private void showEditCollectorDialog(Collector collector) {
        if (collector == null) {
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
        etName.setText(collector.getName());
        etPhone.setText(collector.getPhoneNumber());
        etEmail.setText(collector.getEmail());

        // Create and show the dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        // Set up button listeners
        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btnSave).setOnClickListener(v -> {
            // Update collector object
            collector.setName(etName.getText().toString().trim());
            collector.setPhoneNumber(etPhone.getText().toString().trim());
            collector.setEmail(etEmail.getText().toString().trim());

            // Save to Firestore
            saveCollectorChanges(collector);
            
            dialog.dismiss();
        });

        dialog.show();
    }

    private void saveCollectorChanges(Collector collector) {
        if (collector == null || collector.getId() == null) {
            Toast.makeText(this, "Cannot save: Invalid collector data", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update the collector document
        db.collection("collectors")
                .document(collector.getId())
                .set(collector)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Collector information updated", Toast.LENGTH_SHORT).show();
                    loadAllCollectors(); // Refresh the collector list
                    
                    // Also update the corresponding user document if we have its ID
                    updateCollectorUser(collector);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating collector", e);
                    Toast.makeText(this, "Failed to update collector information", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateCollectorUser(Collector collector) {
        if (collector != null && collector.getUserId() != null) {
            // Update user name, email, and phone to match collector
            Map<String, Object> updates = new HashMap<>();
            updates.put("name", collector.getName());
            updates.put("email", collector.getEmail());
            updates.put("phone", collector.getPhoneNumber());
            
            db.collection("users")
                    .document(collector.getUserId())
                    .update(updates)
                    .addOnSuccessListener(aVoid -> 
                        Log.d(TAG, "Collector user information also updated"))
                    .addOnFailureListener(e -> 
                        Log.e(TAG, "Error updating collector user", e));
        }
    }

    private void showCreateCollectorDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_collector, null);
        
        TextInputEditText etName = dialogView.findViewById(R.id.etCollectorName);
        TextInputEditText etEmail = dialogView.findViewById(R.id.etCollectorEmail);
        TextInputEditText etPhone = dialogView.findViewById(R.id.etCollectorPhone);
        TextInputEditText etPassword = dialogView.findViewById(R.id.etCollectorPassword);
        TextInputEditText etArea = dialogView.findViewById(R.id.etCollectorArea);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnCreate = dialogView.findViewById(R.id.btnCreate);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        btnCreate.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String area = etArea.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || area.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            createNewCollector(name, email, phone, password, area, dialog);
        });

        dialog.show();
    }

    private void createNewCollector(String name, String email, String phone, String password, String area, AlertDialog dialog) {
        // Validate phone number length
        if (phone.length() != 10) {
            Toast.makeText(this, "Phone number must be exactly 10 digits", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // First, get the count of existing collectors to generate the next ID
        db.collection("collectors")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int collectorCount = querySnapshot.size();
                    String employeeId = String.format("WSC%03d", collectorCount + 1); // WSC001, WSC002, etc.
                    
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    
                    // Create user account first
                    auth.createUserWithEmailAndPassword(email, password)
                            .addOnSuccessListener(authResult -> {
                                String userId = authResult.getUser().getUid();
                                
                                // Create collector object
                                Collector collector = new Collector();
                                collector.setName(name);
                                collector.setEmail(email);
                                collector.setPhoneNumber(phone);
                                collector.setAssignedArea(area);
                                collector.setEmployeeId(employeeId);
                                collector.setUserId(userId);
                                
                                // Create user object
                                User user = new User();
                                user.setUserId(userId);
                                user.setName(name);
                                user.setEmail(email);
                                user.setPhone(phone);
                                user.setUserType("collector");
                                
                                Log.d(TAG, "Creating collector document in Firestore...");
                                
                                // Save to collectors collection
                                db.collection("collectors")
                                        .add(collector)
                                        .addOnSuccessListener(collectorDoc -> {
                                            // Update collector with its document ID
                                            collector.setId(collectorDoc.getId());
                                            collectorDoc.update("id", collectorDoc.getId());
                                            
                                            // Save to users collection
                                            db.collection("users")
                                                    .document(userId)
                                                    .set(user)
                                                    .addOnSuccessListener(userDoc -> {
                                                        Toast.makeText(this, "Collector " + employeeId + " created successfully!", Toast.LENGTH_SHORT).show();
                                                        dialog.dismiss();
                                                        loadAllCollectors(); // Refresh the list
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Log.e(TAG, "Error creating user document", e);
                                                        Toast.makeText(this, "Error creating collector profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                                    });
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Error creating collector document", e);
                                            Toast.makeText(this, "Error creating collector: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error creating collector account", e);
                                String errorMessage = e.getMessage();
                                if (errorMessage.contains("email address is already in use")) {
                                    errorMessage = "Email address is already registered";
                                } else if (errorMessage.contains("weak password")) {
                                    errorMessage = "Password must be at least 6 characters";
                                }
                                Toast.makeText(this, "Error creating collector account: " + errorMessage, Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error counting collectors", e);
                    Toast.makeText(this, "Error generating collector ID: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected int getActiveItemIndex() {
        // Collector tab is active (index 1)
        return 1;
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadAllCollectors(); // Refresh collector list when activity is resumed
    }
}

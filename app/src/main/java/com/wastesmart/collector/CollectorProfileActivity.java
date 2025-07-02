package com.wastesmart.collector;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.wastesmart.collector.CollectorLoginActivity;
import com.wastesmart.R;
import com.wastesmart.databinding.ActivityCollectorProfileBinding;
import com.wastesmart.models.Collector;

public class CollectorProfileActivity extends BaseCollectorActivity {

    private ActivityCollectorProfileBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private String collectorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCollectorProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Set up toolbar
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("My Profile");

        // Setup bottom navigation
        setupBottomNavigation();

        // Load collector profile data
        loadCollectorProfile();

        // Set up update button
        binding.btnUpdateProfile.setOnClickListener(v -> updateProfile());

        // Set up logout button
        binding.btnLogout.setOnClickListener(v -> logout());
    }

    private void loadCollectorProfile() {
        // Show progress
        binding.progressBar.setVisibility(View.VISIBLE);

        // Get current user
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            // Not logged in, redirect to login
            Toast.makeText(this, "Please log in to view profile", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, CollectorLoginActivity.class));
            finish();
            return;
        }

        // Get collector ID from current user email
        String userEmail = currentUser.getEmail();
        
        // Query Firestore for collector data
        firestore.collection("collectors")
            .whereEqualTo("email", userEmail)
            .limit(1)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                    collectorId = document.getId();
                    Collector collector = document.toObject(Collector.class);
                    if (collector != null) {
                        // Set profile data
                        binding.etName.setText(collector.getName());
                        binding.etEmail.setText(collector.getEmail());
                        binding.etPhone.setText(collector.getPhoneNumber());
                        binding.etAssignedArea.setText(collector.getAssignedArea());
                        
                        // Set statistics
                        int completedTasks = 0;
                        if (document.contains("completedTasks")) {
                            completedTasks = document.getLong("completedTasks").intValue();
                        }
                        binding.tvTasksCompleted.setText(String.valueOf(completedTasks));
                        
                        // Load ranking
                        loadCollectorRanking();
                    }
                } else {
                    Toast.makeText(this, "Collector profile not found", Toast.LENGTH_SHORT).show();
                }
                binding.progressBar.setVisibility(View.GONE);
            })
            .addOnFailureListener(e -> {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Error loading profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
    
    private void loadCollectorRanking() {
        if (collectorId == null) return;
        
        // Query collectors by completed tasks to determine ranking
        firestore.collection("collectors")
            .orderBy("completedTasks", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                int rank = 1;
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    if (doc.getId().equals(collectorId)) {
                        binding.tvRanking.setText(String.valueOf(rank));
                        break;
                    }
                    rank++;
                }
            })
            .addOnFailureListener(e -> {
                binding.tvRanking.setText("--");
            });
    }

    private void updateProfile() {
        if (collectorId == null) {
            Toast.makeText(this, "Profile not loaded correctly", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show progress
        binding.progressBar.setVisibility(View.VISIBLE);
        
        // Get updated data
        String name = binding.etName.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        
        // Validate fields
        if (name.isEmpty()) {
            binding.etName.setError("Name required");
            binding.progressBar.setVisibility(View.GONE);
            return;
        }
        
        // Update in Firestore
        firestore.collection("collectors").document(collectorId)
            .update(
                "name", name,
                "phoneNumber", phone
            )
            .addOnSuccessListener(aVoid -> {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(CollectorProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(CollectorProfileActivity.this, "Failed to update profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, CollectorLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected int getActiveNavItemIndex() {
        return 4; // Profile tab index (0-based index, 5th position)
    }
}

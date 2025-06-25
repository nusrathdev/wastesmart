package com.wastesmart.collector;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.wastesmart.MainActivity;
import com.wastesmart.R;
import com.wastesmart.databinding.ActivityCollectorRegistrationBinding;

import java.util.HashMap;
import java.util.Map;

public class CollectorRegistrationActivity extends AppCompatActivity {

    private static final String TAG = "CollectorRegistration";
    private ActivityCollectorRegistrationBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCollectorRegistrationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Setup toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Collector Registration");
        }

        // Setup zone spinner
        setupZoneSpinner();

        // Setup click listeners
        setupClickListeners();
    }

    private void setupZoneSpinner() {
        String[] zones = {
            "Select Zone",
            "Zone A - North District", 
            "Zone B - South District",
            "Zone C - East District", 
            "Zone D - West District",
            "Zone E - Central District",
            "Zone F - Industrial Area",
            "Zone G - Residential Area"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item, zones);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerZone.setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.btnRegister.setOnClickListener(v -> registerCollector());
        
        binding.btnBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(CollectorRegistrationActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void registerCollector() {
        // Get input values
        String fullName = binding.etFullName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String vehicleNumber = binding.etVehicleNumber.getText().toString().trim();
        String licenseNumber = binding.etLicenseNumber.getText().toString().trim();
        String selectedZone = binding.spinnerZone.getSelectedItem().toString();

        // Validate inputs
        if (!validateInputs(fullName, email, password, confirmPassword, phone, 
                          vehicleNumber, licenseNumber, selectedZone)) {
            return;
        }

        // Show progress
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnRegister.setEnabled(false);

        // Create Firebase Auth account
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Registration successful
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveCollectorToFirestore(user.getUid(), fullName, email, phone, 
                                                   vehicleNumber, licenseNumber, selectedZone);
                        }
                    } else {
                        // Registration failed
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnRegister.setEnabled(true);
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(CollectorRegistrationActivity.this, 
                                "Registration failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean validateInputs(String fullName, String email, String password, 
                                 String confirmPassword, String phone, String vehicleNumber,
                                 String licenseNumber, String selectedZone) {
        
        if (TextUtils.isEmpty(fullName)) {
            binding.etFullName.setError("Full name is required");
            binding.etFullName.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(email)) {
            binding.etEmail.setError("Email is required");
            binding.etEmail.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.setError("Please enter a valid email");
            binding.etEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            binding.etPassword.setError("Password is required");
            binding.etPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            binding.etPassword.setError("Password must be at least 6 characters");
            binding.etPassword.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            binding.etConfirmPassword.setError("Passwords do not match");
            binding.etConfirmPassword.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(phone)) {
            binding.etPhone.setError("Phone number is required");
            binding.etPhone.requestFocus();
            return false;
        }

        if (phone.length() < 10) {
            binding.etPhone.setError("Please enter a valid phone number");
            binding.etPhone.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(vehicleNumber)) {
            binding.etVehicleNumber.setError("Vehicle number is required");
            binding.etVehicleNumber.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(licenseNumber)) {
            binding.etLicenseNumber.setError("License number is required");
            binding.etLicenseNumber.requestFocus();
            return false;
        }

        if ("Select Zone".equals(selectedZone)) {
            Toast.makeText(this, "Please select a zone", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void saveCollectorToFirestore(String userId, String fullName, String email, 
                                        String phone, String vehicleNumber, String licenseNumber, 
                                        String selectedZone) {
        
        Map<String, Object> collector = new HashMap<>();
        collector.put("name", fullName);
        collector.put("email", email);
        collector.put("phone", phone);
        collector.put("vehicleNumber", vehicleNumber);
        collector.put("licenseNumber", licenseNumber);
        collector.put("zone", selectedZone);
        collector.put("role", "collector");
        collector.put("status", "pending"); // Admin approval required
        collector.put("registrationTimestamp", System.currentTimeMillis());
        collector.put("approvedBy", null);
        collector.put("approvedTimestamp", null);

        db.collection("users").document(userId)
                .set(collector)
                .addOnSuccessListener(aVoid -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(CollectorRegistrationActivity.this, 
                            "Registration successful! Please wait for admin approval.", 
                            Toast.LENGTH_LONG).show();
                    
                    // Sign out user until approved
                    mAuth.signOut();
                    
                    // Go back to main screen
                    Intent intent = new Intent(CollectorRegistrationActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnRegister.setEnabled(true);
                    Log.w(TAG, "Error saving collector data", e);
                    Toast.makeText(CollectorRegistrationActivity.this, 
                            "Error saving data: " + e.getMessage(), 
                            Toast.LENGTH_LONG).show();
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

package com.wastesmart.collector;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.wastesmart.MainActivity;
import com.wastesmart.R;
import com.wastesmart.databinding.ActivityCollectorSetupBinding;

import java.util.HashMap;
import java.util.Map;

public class CollectorSetupActivity extends AppCompatActivity {

    private static final String TAG = "CollectorSetup";
    private ActivityCollectorSetupBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCollectorSetupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Setup toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Collector Setup");
        }

        // Setup click listeners
        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.btnCreateCollector.setOnClickListener(v -> createCollectorAccount());
        
        binding.btnBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(CollectorSetupActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void createCollectorAccount() {
        // Get input values safely
        String collectorName = getTextSafely(binding.etCollectorName);
        String email = getTextSafely(binding.etEmail);
        String password = getTextSafely(binding.etPassword);

        // Use default values if not provided
        if (TextUtils.isEmpty(collectorName)) {
            collectorName = getString(R.string.default_collector_name);
        }
        if (TextUtils.isEmpty(email)) {
            email = getString(R.string.default_collector_email);
        }
        if (TextUtils.isEmpty(password)) {
            password = getString(R.string.default_collector_password);
        }

        // Show final values to user
        binding.tvCollectorInfo.setVisibility(View.VISIBLE);
        binding.tvCollectorInfo.setText(getString(R.string.creating_collector_info, 
                collectorName, email, password));

        // Show progress
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnCreateCollector.setEnabled(false);

        // Create the collector account
        final String finalName = collectorName;
        final String finalEmail = email;
        final String finalPassword = password;

        mAuth.createUserWithEmailAndPassword(finalEmail, finalPassword)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Registration successful
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveCollectorToFirestore(user.getUid(), finalName, finalEmail);
                        }
                    } else {
                        // Try to sign in if account already exists
                        mAuth.signInWithEmailAndPassword(finalEmail, finalPassword)
                                .addOnCompleteListener(signInTask -> {
                                    if (signInTask.isSuccessful()) {
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        if (user != null) {
                                            saveCollectorToFirestore(user.getUid(), finalName, finalEmail);
                                        }
                                    } else {
                                        binding.progressBar.setVisibility(View.GONE);
                                        binding.btnCreateCollector.setEnabled(true);
                                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                        String errorMessage = task.getException() != null ? 
                                                task.getException().getMessage() : "Unknown error";
                                        Toast.makeText(CollectorSetupActivity.this, 
                                                getString(R.string.setup_failed, errorMessage),
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                    }
                });
    }

    private void saveCollectorToFirestore(String userId, String collectorName, String email) {
        Map<String, Object> collector = new HashMap<>();
        collector.put("name", collectorName);
        collector.put("email", email);
        collector.put("role", "collector");
        collector.put("status", "active");
        collector.put("zone", "All Areas");
        collector.put("setupTimestamp", System.currentTimeMillis());

        db.collection("users").document(userId)
                .set(collector)
                .addOnSuccessListener(aVoid -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(CollectorSetupActivity.this, 
                            getString(R.string.collector_created_success), 
                            Toast.LENGTH_LONG).show();
                    
                    // Sign out and go back to main screen
                    mAuth.signOut();
                    
                    Intent intent = new Intent(CollectorSetupActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnCreateCollector.setEnabled(true);
                    Log.w(TAG, "Error saving collector data", e);
                    String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error";
                    Toast.makeText(CollectorSetupActivity.this, 
                            getString(R.string.error_saving_data, errorMessage), 
                            Toast.LENGTH_LONG).show();
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

    /**
     * Safely extracts text from EditText, handling potential null values
     * @param editText The EditText to extract text from
     * @return Trimmed text or empty string if null
     */
    private String getTextSafely(com.google.android.material.textfield.TextInputEditText editText) {
        if (editText == null || editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }
}

package com.wastesmart.collector;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.wastesmart.R;
import com.wastesmart.databinding.ActivityCollectorLoginBinding;
import com.google.firebase.auth.FirebaseAuth;

public class CollectorLoginActivity extends AppCompatActivity {

    private ActivityCollectorLoginBinding binding;
    private FirebaseAuth mAuth;

    // Default collector credentials - In production, these should be securely managed
    private static final String COLLECTOR_EMAIL = "collector@wastesmart.com";
    private static final String COLLECTOR_PASSWORD = "collector123";
    private static final String SUPERVISOR_EMAIL = "supervisor@wastesmart.com";
    private static final String SUPERVISOR_PASSWORD = "supervisor123";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCollectorLoginBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Setup toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Collector Login");
        }

        // Login button click listener
        binding.btnCollectorLogin.setOnClickListener(v -> attemptLogin());

        // Add a hint for default credentials
        showDefaultCredentials();
    }

    private void showDefaultCredentials() {
        Toast.makeText(this, 
            "Default Collector: collector@wastesmart.com / collector123\n" +
            "Supervisor: supervisor@wastesmart.com / supervisor123", 
            Toast.LENGTH_LONG).show();
    }

    private void attemptLogin() {
        // Reset errors
        binding.etCollectorEmail.setError(null);
        binding.etCollectorPassword.setError(null);

        // Store values
        String email = binding.etCollectorEmail.getText().toString().trim();
        String password = binding.etCollectorPassword.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password
        if (TextUtils.isEmpty(password)) {
            binding.etCollectorPassword.setError("Password is required");
            focusView = binding.etCollectorPassword;
            cancel = true;
        }

        // Check for a valid email address
        if (TextUtils.isEmpty(email)) {
            binding.etCollectorEmail.setError("Email is required");
            focusView = binding.etCollectorEmail;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first form field with an error
            focusView.requestFocus();
        } else {
            // Check if it's a default collector credential first
            if (isDefaultCollectorCredential(email, password)) {
                loginWithDefaultCredentials(email, password);
            } else {
                // Try Firebase authentication
                loginWithFirebase(email, password);
            }
        }
    }

    private boolean isDefaultCollectorCredential(String email, String password) {
        return (COLLECTOR_EMAIL.equals(email) && COLLECTOR_PASSWORD.equals(password)) ||
               (SUPERVISOR_EMAIL.equals(email) && SUPERVISOR_PASSWORD.equals(password));
    }

    private void loginWithDefaultCredentials(String email, String password) {
        binding.progressBar.setVisibility(View.VISIBLE);

        // Simulate a brief loading time
        binding.getRoot().postDelayed(() -> {
            binding.progressBar.setVisibility(View.GONE);
            
            // Determine collector type and navigate accordingly
            String collectorType = COLLECTOR_EMAIL.equals(email) ? "Waste Collector" : "Supervisor";
            String collectorName = COLLECTOR_EMAIL.equals(email) ? "John Collector" : "Sarah Supervisor";
            
            Toast.makeText(CollectorLoginActivity.this, 
                "Welcome " + collectorName + "!", Toast.LENGTH_SHORT).show();
            
            // Sign in with Firebase Auth first to maintain session consistency
            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Navigate to collector dashboard
                        Intent intent = new Intent(CollectorLoginActivity.this, CollectorDashboardActivity.class);
                        intent.putExtra("collector_type", collectorType);
                        intent.putExtra("collector_email", email);
                        intent.putExtra("collector_name", collectorName);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        // If Firebase Auth fails, continue anyway with default login
                        Intent intent = new Intent(CollectorLoginActivity.this, CollectorDashboardActivity.class);
                        intent.putExtra("collector_type", collectorType);
                        intent.putExtra("collector_email", email);
                        intent.putExtra("collector_name", collectorName);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                });
        }, 1000);
    }

    private void loginWithFirebase(String email, String password) {
        // Show a progress spinner, and perform the login attempt
        binding.progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        // Login success, navigate to collector dashboard
                        Intent intent = new Intent(CollectorLoginActivity.this, CollectorDashboardActivity.class);
                        intent.putExtra("collector_type", "Firebase Collector");
                        intent.putExtra("collector_email", email);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        // Login failed
                        Toast.makeText(CollectorLoginActivity.this, "Authentication failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

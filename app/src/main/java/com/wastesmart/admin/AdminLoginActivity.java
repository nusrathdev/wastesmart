package com.wastesmart.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.wastesmart.R;
import com.wastesmart.databinding.ActivityAdminLoginBinding;
import com.google.firebase.auth.FirebaseAuth;

public class AdminLoginActivity extends AppCompatActivity {

    private ActivityAdminLoginBinding binding;
    private FirebaseAuth mAuth;

    // Default admin credentials - In production, these should be securely managed
    private static final String ADMIN_EMAIL = "admin@wastesmart.com";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final String MUNICIPAL_EMAIL = "municipal@wastesmart.com";
    private static final String MUNICIPAL_PASSWORD = "municipal123";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminLoginBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Setup toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Admin Login");
        }

        // Login button click listener
        binding.btnLogin.setOnClickListener(v -> attemptLogin());

        // Back button click listener
        binding.btnBack.setOnClickListener(v -> {
            finish(); // This will close the current activity and return to MainActivity
        });

        // Add a hint for default credentials
        showDefaultCredentials();
    }

    private void showDefaultCredentials() {
        Toast.makeText(this, 
            "Default Admin: admin@wastesmart.com / admin123\n" +
            "Municipal: municipal@wastesmart.com / municipal123", 
            Toast.LENGTH_LONG).show();
    }

    private void attemptLogin() {
        // Reset errors
        binding.etEmail.setError(null);
        binding.etPassword.setError(null);

        // Store values
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password
        if (TextUtils.isEmpty(password)) {
            binding.etPassword.setError("Password is required");
            focusView = binding.etPassword;
            cancel = true;
        }

        // Check for a valid email address
        if (TextUtils.isEmpty(email)) {
            binding.etEmail.setError("Email is required");
            focusView = binding.etEmail;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first form field with an error
            focusView.requestFocus();
        } else {
            // Check if it's a default admin credential first
            if (isDefaultAdminCredential(email, password)) {
                loginWithDefaultCredentials(email, password);
            } else {
                // Try Firebase authentication
                loginWithFirebase(email, password);
            }
        }
    }

    private boolean isDefaultAdminCredential(String email, String password) {
        return (ADMIN_EMAIL.equals(email) && ADMIN_PASSWORD.equals(password)) ||
               (MUNICIPAL_EMAIL.equals(email) && MUNICIPAL_PASSWORD.equals(password));
    }

    private void loginWithDefaultCredentials(String email, String password) {
        binding.progressBar.setVisibility(View.VISIBLE);

        // Simulate a brief loading time
        binding.getRoot().postDelayed(() -> {
            binding.progressBar.setVisibility(View.GONE);
            
            // Determine admin type and navigate accordingly
            String adminType = ADMIN_EMAIL.equals(email) ? "Super Admin" : "Municipal Admin";
            
            Toast.makeText(AdminLoginActivity.this, 
                "Welcome " + adminType + "!", Toast.LENGTH_SHORT).show();
            
            // Navigate to admin dashboard
            Intent intent = new Intent(AdminLoginActivity.this, AdminDashboardActivity.class);
            intent.putExtra("admin_type", adminType);
            intent.putExtra("admin_email", email);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }, 1000);
    }

    private void loginWithFirebase(String email, String password) {
        binding.progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        // Login success, navigate to admin dashboard
                        Intent intent = new Intent(AdminLoginActivity.this, AdminDashboardActivity.class);
                        intent.putExtra("admin_type", "Firebase Admin");
                        intent.putExtra("admin_email", email);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        // Login failed
                        Toast.makeText(AdminLoginActivity.this, 
                            "Authentication failed: " + task.getException().getMessage(),
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

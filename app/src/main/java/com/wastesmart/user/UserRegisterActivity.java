package com.wastesmart.user;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.wastesmart.R;
import com.wastesmart.databinding.ActivityUserRegisterBinding;
import com.wastesmart.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UserRegisterActivity extends AppCompatActivity {

    private ActivityUserRegisterBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private boolean isRegistering = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserRegisterBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // Initialize Firebase components
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Register button click listener
        binding.btnRegister.setOnClickListener(v -> attemptRegistration());

        // Back button click listener
        binding.btnBack.setOnClickListener(v -> {
            finish(); // This will close the current activity and return to UserLoginActivity
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });
    }

    private void attemptRegistration() {
        // Prevent multiple registration attempts
        if (isRegistering) {
            return;
        }
        
        // Reset errors
        binding.etName.setError(null);
        binding.etEmail.setError(null);
        binding.etPassword.setError(null);
        binding.etConfirmPassword.setError(null);
        binding.etPhone.setError(null);
        binding.etAddress.setError(null);

        // Store values
        String name = binding.etName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String address = binding.etAddress.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        // Check for valid name
        if (TextUtils.isEmpty(name)) {
            binding.etName.setError("Name is required");
            focusView = binding.etName;
            cancel = true;
        }

        // Check for valid email
        if (TextUtils.isEmpty(email)) {
            binding.etEmail.setError("Email is required");
            focusView = binding.etEmail;
            cancel = true;
        } else if (!isEmailValid(email)) {
            binding.etEmail.setError("Enter a valid email address");
            focusView = binding.etEmail;
            cancel = true;
        }

        // Check for valid password
        if (TextUtils.isEmpty(password)) {
            binding.etPassword.setError("Password is required");
            focusView = binding.etPassword;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            binding.etPassword.setError("Password must be at least 6 characters");
            focusView = binding.etPassword;
            cancel = true;
        }

        // Check for password confirmation match
        if (!password.equals(confirmPassword)) {
            binding.etConfirmPassword.setError("Passwords do not match");
            focusView = binding.etConfirmPassword;
            cancel = true;
        }

        // Check for valid phone
        if (TextUtils.isEmpty(phone)) {
            binding.etPhone.setError("Phone number is required");
            focusView = binding.etPhone;
            cancel = true;
        }

        // Check for valid address
        if (TextUtils.isEmpty(address)) {
            binding.etAddress.setError("Address is required");
            focusView = binding.etAddress;
            cancel = true;
        }

        if (cancel) {
            // There was an error; focus the first form field with an error
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and start the registration attempt
            isRegistering = true;
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.btnRegister.setEnabled(false);

            // Create new user with email and password
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        isRegistering = false;
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnRegister.setEnabled(true);
                        
                        if (task.isSuccessful()) {
                            // Registration successful
                            String userId = mAuth.getCurrentUser().getUid();

                            // Create user object
                            User user = new User(userId, name, email, phone, address, "user");

                            // Show success message first
                            Toast.makeText(UserRegisterActivity.this, "Account created successfully!", 
                                    Toast.LENGTH_SHORT).show();

                            // Save additional user data to Firestore
                            db.collection("users").document(userId).set(user)
                                    .addOnSuccessListener(aVoid -> {
                                        // Go to user dashboard
                                        Intent intent = new Intent(UserRegisterActivity.this, UserDashboardActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(UserRegisterActivity.this, "Error saving user data: " + e.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            // Registration fails, display a message to the user
                            String errorMessage = "Registration failed";
                            if (task.getException() != null) {
                                errorMessage += ": " + task.getException().getMessage();
                            }
                            Toast.makeText(UserRegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@") && email.contains(".");
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 6;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}

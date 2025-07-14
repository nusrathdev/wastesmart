package com.wastesmart.user;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.wastesmart.R;
import com.wastesmart.databinding.ActivityUserLoginBinding;
import com.google.firebase.auth.FirebaseAuth;

public class UserLoginActivity extends AppCompatActivity {

    private ActivityUserLoginBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserLoginBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Login button click listener
        binding.btnLogin.setOnClickListener(v -> attemptLogin());

        // Back button click listener
        binding.btnBack.setOnClickListener(v -> {
            finish(); // This will close the current activity and return to MainActivity
        });

        // Forgot password click listener
        binding.tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(UserLoginActivity.this, UserForgotPasswordActivity.class);
            startActivity(intent);
        });

        // Register click listener
        binding.tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(UserLoginActivity.this, UserRegisterActivity.class);
            startActivity(intent);
        });
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
            // Show a progress spinner, and perform the login attempt
            binding.progressBar.setVisibility(View.VISIBLE);

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        binding.progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            // Login success, update UI with the signed-in user's information
                            Intent intent = new Intent(UserLoginActivity.this, UserDashboardActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user
                            Toast.makeText(UserLoginActivity.this, "Authentication failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}

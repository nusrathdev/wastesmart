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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCollectorLoginBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();        // Login button click listener
        binding.btnCollectorLogin.setOnClickListener(v -> attemptLogin());
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
            // Show a progress spinner, and perform the login attempt
            binding.progressBar.setVisibility(View.VISIBLE);

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        binding.progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            // Login success, navigate to collector dashboard
                            Intent intent = new Intent(CollectorLoginActivity.this, CollectorDashboardActivity.class);
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
    }
}

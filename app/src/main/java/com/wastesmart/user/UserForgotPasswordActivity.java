package com.wastesmart.user;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.wastesmart.R;
import com.wastesmart.databinding.ActivityUserForgotPasswordBinding;
import com.google.firebase.auth.FirebaseAuth;

public class UserForgotPasswordActivity extends AppCompatActivity {

    private ActivityUserForgotPasswordBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserForgotPasswordBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Setup toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Reset Password");
        }

        // Reset password button click listener
        binding.btnResetPassword.setOnClickListener(v -> attemptPasswordReset());
    }

    private void attemptPasswordReset() {
        // Reset errors
        binding.etEmail.setError(null);

        // Store value
        String email = binding.etEmail.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        // Check for valid email address
        if (TextUtils.isEmpty(email)) {
            binding.etEmail.setError("Email is required");
            focusView = binding.etEmail;
            cancel = true;
        } else if (!isEmailValid(email)) {
            binding.etEmail.setError("Enter a valid email address");
            focusView = binding.etEmail;
            cancel = true;
        }

        if (cancel) {
            // There was an error; focus the first form field with an error
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and attempt to reset password
            binding.progressBar.setVisibility(View.VISIBLE);

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        binding.progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Toast.makeText(UserForgotPasswordActivity.this,
                                    "Password reset email sent. Please check your inbox.",
                                    Toast.LENGTH_LONG).show();
                            finish();
                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                        } else {
                            Toast.makeText(UserForgotPasswordActivity.this,
                                    "Failed to send reset email: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@") && email.contains(".");
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}

package com.wastesmart.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.wastesmart.MainActivity;
import com.wastesmart.R;
import com.wastesmart.databinding.ActivityUserDashboardBinding;
import com.wastesmart.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserDashboardActivity extends BaseUserActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ActivityUserDashboardBinding binding;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserDashboardBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // Initialize Firebase components
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Set up toolbar
        setSupportActionBar(binding.toolbar);

        // Set up logout button click listener
        binding.btnLogout.setOnClickListener(v -> logout());

        // Setup bottom navigation
        setupBottomNavigation();

        // Set up quick submit button click
        binding.btnQuickSubmit.setOnClickListener(v -> {
            if (!getActiveNavItem().equals("submit")) {
                Intent intent = new Intent(UserDashboardActivity.this, ReportWasteActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        // Load user data
        loadUserData();
    }

    @Override
    protected String getActiveNavItem() {
        return "home";
    }

    private void loadUserData() {
        if (mAuth.getCurrentUser() == null) {
            // Not logged in, go back to login
            Intent intent = new Intent(UserDashboardActivity.this, UserLoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentUser = documentSnapshot.toObject(User.class);
                        updateUI();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(UserDashboardActivity.this, "Error loading user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUI() {
        if (currentUser != null) {
            binding.tvUserName.setText(currentUser.getName());
            
            // Set dynamic welcome message
            String[] welcomeMessages = {
                "Ready to make a difference today?",
                "Your community thanks you!",
                "Every report counts!",
                "Together we keep our city clean!",
                "Thank you for being a responsible citizen!"
            };
            int randomIndex = (int) (Math.random() * welcomeMessages.length);
            binding.tvWelcomeMessage.setText(welcomeMessages[randomIndex]);
            
            // TODO: Load actual user stats from database
            // For now, using placeholder values
            binding.tvReportsCount.setText("12");
            // binding.tvPointsEarned.setText("240"); // Removed: view no longer exists in layout
        }
    }

    private void logout() {
        mAuth.signOut();
        // Go back to main screen
        Intent intent = new Intent(UserDashboardActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}

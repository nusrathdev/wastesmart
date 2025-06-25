package com.wastesmart.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.wastesmart.MainActivity;
import com.wastesmart.R;
import com.wastesmart.databinding.ActivityUserDashboardBinding;
import com.wastesmart.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserDashboardActivity extends AppCompatActivity {

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

        // Set up button click listeners
        binding.cardReportWaste.setOnClickListener(v -> {
            Intent intent = new Intent(UserDashboardActivity.this, ReportWasteActivity.class);
            startActivity(intent);
        });        binding.cardViewSchedule.setOnClickListener(v -> {
            Intent intent = new Intent(UserDashboardActivity.this, CollectionScheduleActivity.class);
            startActivity(intent);
        });

        binding.cardWasteHistory.setOnClickListener(v -> {
            Intent intent = new Intent(UserDashboardActivity.this, MyReportsActivity.class);
            startActivity(intent);
        });

        binding.cardMyProfile.setOnClickListener(v -> {
            Intent intent = new Intent(UserDashboardActivity.this, UserProfileActivity.class);
            startActivity(intent);
        });

        // Load user data
        loadUserData();
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
            // User email is not displayed in this layout, so we'll remove this line
            // binding.tvUserEmail.setText(currentUser.getEmail());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            mAuth.signOut();
            // Go back to main screen
            Intent intent = new Intent(UserDashboardActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

package com.wastesmart.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.wastesmart.R;
import com.wastesmart.databinding.ActivityManageUsersBinding;
import com.wastesmart.models.User;
import com.wastesmart.models.WasteReport;

import java.util.ArrayList;
import java.util.List;

public class ManageUsersActivity extends BaseAdminActivity {

    private static final String TAG = "ManageUsersActivity";
    private ActivityManageUsersBinding binding;
    private FirebaseFirestore db;
    private UserAdapter adapter;
    private List<User> usersList;
    private List<User> filteredUsersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup toolbar
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("Manage Users");

        // Setup bottom navigation
        setupBottomNavigation();

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        
        // Initialize lists
        usersList = new ArrayList<>();
        filteredUsersList = new ArrayList<>();
        
        // Setup RecyclerView
        adapter = new UserAdapter(filteredUsersList, this, new UserAdapter.UserActionListener() {
            @Override
            public void onViewProfileClicked(User user, int position) {
                showUserProfileDialog(user);
            }

            @Override
            public void onBlockUserClicked(User user, int position) {
                toggleUserBlockStatus(user, position);
            }
        });
        
        binding.recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewUsers.setAdapter(adapter);
        
        // Setup search functionality
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString().toLowerCase().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Load users
        loadUsers();
    }
    
    private void loadUsers() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.tvNoUsers.setVisibility(View.GONE);
        
        db.collection("users")
            .whereEqualTo("userType", "user") // Only regular users, not admin or collector
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                usersList.clear();
                filteredUsersList.clear();
                
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    try {
                        User user = document.toObject(User.class);
                        user.setUserId(document.getId());
                        usersList.add(user);
                        filteredUsersList.add(user);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing user data: " + document.getId(), e);
                    }
                }
                
                adapter.notifyDataSetChanged();
                binding.progressBar.setVisibility(View.GONE);
                
                if (filteredUsersList.isEmpty()) {
                    binding.tvNoUsers.setVisibility(View.VISIBLE);
                } else {
                    binding.tvNoUsers.setVisibility(View.GONE);
                }
                
                Log.d(TAG, "Loaded " + usersList.size() + " users");
            })
            .addOnFailureListener(e -> {
                binding.progressBar.setVisibility(View.GONE);
                binding.tvNoUsers.setVisibility(View.VISIBLE);
                Log.e(TAG, "Error loading users", e);
                Toast.makeText(this, "Error loading users: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
    
    private void filterUsers(String query) {
        filteredUsersList.clear();
        
        if (query.isEmpty()) {
            filteredUsersList.addAll(usersList);
        } else {
            for (User user : usersList) {
                if ((user.getName() != null && user.getName().toLowerCase().contains(query)) ||
                    (user.getEmail() != null && user.getEmail().toLowerCase().contains(query)) ||
                    (user.getPhone() != null && user.getPhone().contains(query))) {
                    filteredUsersList.add(user);
                }
            }
        }
        
        adapter.notifyDataSetChanged();
        
        if (filteredUsersList.isEmpty()) {
            binding.tvNoUsers.setVisibility(View.VISIBLE);
            binding.tvNoUsers.setText("No users found matching '" + query + "'");
        } else {
            binding.tvNoUsers.setVisibility(View.GONE);
        }
    }
    
    private void showUserProfileDialog(User user) {
        // Create dialog to show detailed user profile
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_user_profile, null);
        
        // Set user details in dialog view
        TextView tvProfileName = dialogView.findViewById(R.id.tvProfileName);
        TextView tvProfileEmail = dialogView.findViewById(R.id.tvProfileEmail);
        TextView tvProfilePhone = dialogView.findViewById(R.id.tvProfilePhone);
        TextView tvProfileAddress = dialogView.findViewById(R.id.tvProfileAddress);
        TextView tvProfileStatus = dialogView.findViewById(R.id.tvProfileStatus);
        TextView tvTotalSubmissions = dialogView.findViewById(R.id.tvTotalSubmissions);
        TextView tvCompletedSubmissions = dialogView.findViewById(R.id.tvCompletedSubmissions);
        TextView tvPendingSubmissions = dialogView.findViewById(R.id.tvPendingSubmissions);
        
        // Set basic profile information
        tvProfileName.setText(user.getName() != null ? user.getName() : "User");
        tvProfileEmail.setText(user.getEmail() != null ? user.getEmail() : "No email provided");
        tvProfilePhone.setText(user.getPhone() != null ? user.getPhone() : "No phone provided");
        tvProfileAddress.setText(user.getAddress() != null ? user.getAddress() : "No address provided");
        
        // Set user status
        boolean isBlocked = "blocked".equals(user.getUserType());
        if (isBlocked) {
            tvProfileStatus.setText("BLOCKED");
            tvProfileStatus.setTextColor(getResources().getColor(R.color.error, null));
        } else {
            tvProfileStatus.setText("ACTIVE");
            tvProfileStatus.setTextColor(getResources().getColor(R.color.success, null));
        }
        
        // Get reports statistics for this user
        fetchUserReportsStatistics(user.getUserId(), tvTotalSubmissions, tvCompletedSubmissions, tvPendingSubmissions);
        
        // Show the dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("User Profile")
            .setView(dialogView)
            .setPositiveButton("Close", null)
            .create();
            
        dialog.show();
    }
    
    private void fetchUserReportsStatistics(String userId, TextView tvTotal, TextView tvCompleted, TextView tvPending) {
        // Show loading state
        tvTotal.setText("...");
        tvCompleted.setText("...");
        tvPending.setText("...");
        
        // Query Firestore for user's reports
        db.collection("waste_reports")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                int totalReports = queryDocumentSnapshots.size();
                int completedReports = 0;
                int pendingReports = 0;
                
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    try {
                        WasteReport report = document.toObject(WasteReport.class);
                        String status = report.getStatus();
                        
                        if ("COLLECTED".equals(status)) {
                            completedReports++;
                        } else if ("PENDING".equals(status) || "ASSIGNED".equals(status)) {
                            pendingReports++;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing report data: " + document.getId(), e);
                    }
                }
                
                // Update UI with statistics
                tvTotal.setText(String.valueOf(totalReports));
                tvCompleted.setText(String.valueOf(completedReports));
                tvPending.setText(String.valueOf(pendingReports));
                
                Log.d(TAG, "Fetched user statistics: Total=" + totalReports + 
                      ", Completed=" + completedReports + ", Pending=" + pendingReports);
            })
            .addOnFailureListener(e -> {
                // Show error state
                tvTotal.setText("0");
                tvCompleted.setText("0");
                tvPending.setText("0");
                
                Log.e(TAG, "Error fetching user reports", e);
                Toast.makeText(this, "Failed to load user statistics", Toast.LENGTH_SHORT).show();
            });
    }
    
    private void toggleUserBlockStatus(User user, int position) {
        // Check current status and toggle it
        boolean isCurrentlyBlocked = "blocked".equals(user.getUserType());
        String newStatus = isCurrentlyBlocked ? "user" : "blocked";
        
        // Create confirmation message based on the action
        String actionMessage = isCurrentlyBlocked ? 
                "unblock " + user.getName() + "?" : 
                "block " + user.getName() + "?";
                
        String actionDescription = isCurrentlyBlocked ?
                "This will restore the user's access to the application." :
                "This will prevent the user from accessing the application until they are unblocked.";
        
        // Show confirmation dialog
        new AlertDialog.Builder(this)
            .setTitle("Confirm Action")
            .setMessage("Are you sure you want to " + actionMessage + "\n\n" + actionDescription)
            .setPositiveButton("Yes", (dialog, which) -> {
                // Proceed with the update
                updateUserStatus(user, position, newStatus);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void updateUserStatus(User user, int position, String newStatus) {
        binding.progressBar.setVisibility(View.VISIBLE);
        
        // Update in Firestore
        db.collection("users")
            .document(user.getUserId())
            .update("userType", newStatus)
            .addOnSuccessListener(aVoid -> {
                // Update local data
                user.setUserType(newStatus);
                adapter.notifyItemChanged(position);
                
                String message = "blocked".equals(newStatus) ? 
                        "User has been blocked" :
                        "User has been unblocked";
                        
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                binding.progressBar.setVisibility(View.GONE);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error updating user status", e);
                Toast.makeText(this, "Failed to update user status: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                binding.progressBar.setVisibility(View.GONE);
            });
    }

    @Override
    protected int getActiveItemIndex() {
        // Users tab is active (index 4)
        return 4;
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadUsers(); // Refresh user data when activity is resumed
    }
}

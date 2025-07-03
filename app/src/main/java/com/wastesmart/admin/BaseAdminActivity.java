package com.wastesmart.admin;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import com.wastesmart.R;
import com.wastesmart.utils.BottomNavigationHelper;

/**
 * Base class for all admin activities to include common functionality like the bottom navigation bar
 */
public abstract class BaseAdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Child activities should call setContentView() before calling setupBottomNavigation()
    }

    /**
     * Set up the bottom navigation with the appropriate active tab
     */
    protected void setupBottomNavigation() {
        // Use the BottomNavigationHelper for admin navigation
        BottomNavigationHelper.setupAdminBottomNavigation(this, getActiveItemIndex());
        
        // Add click listeners for navigation items
        setupNavItemClickListener(R.id.navItem1, ManageReportsActivity.class);
        setupNavItemClickListener(R.id.navItem2, ManageCollectorActivity.class);
        setupNavItemClickListener(R.id.navItem3, AdminDashboardActivity.class);
        setupNavItemClickListener(R.id.navItem4, AnalyticsActivity.class);
        setupNavItemClickListener(R.id.navItem5, ManageUsersActivity.class);
    }
    
    /**
     * Set up click listener for a navigation item
     * @param navItemId The navigation item resource ID
     * @param activityClass The activity class to start
     */
    private void setupNavItemClickListener(int navItemId, final Class<?> activityClass) {
        LinearLayout navItem = findViewById(navItemId);
        if (navItem != null) {
            navItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Don't navigate if we're already on this page
                    if (BaseAdminActivity.this.getClass() != activityClass) {
                        Intent intent = new Intent(BaseAdminActivity.this, activityClass);
                        startActivity(intent);
                        // Optional: Add flags to control navigation stack
                        // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    }
                }
            });
        }
    }
    
    /**
     * Get the index of the active bottom navigation item for this activity
     * @return The index (0-4) of the active item
     */
    protected abstract int getActiveItemIndex();
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_admin, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_sign_out) {
            signOut();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * Sign out the current admin user
     */
    protected void signOut() {
        // Show confirmation dialog
        new AlertDialog.Builder(this)
            .setTitle("Sign Out")
            .setMessage("Are you sure you want to sign out?")
            .setPositiveButton("Yes", (dialog, which) -> {
                // Sign out from Firebase Auth
                FirebaseAuth.getInstance().signOut();
                
                // Clear activity stack and go to login
                Intent intent = new Intent(this, AdminLoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            })
            .setNegativeButton("No", null)
            .show();
    }
}

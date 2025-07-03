package com.wastesmart.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

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
}

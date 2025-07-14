package com.wastesmart.collector;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.wastesmart.R;
import com.wastesmart.utils.BottomNavigationHelper;

/**
 * Base activity for all collector pages that includes the bottom navigation bar
 */
public abstract class BaseCollectorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Child activities should call setContentView() before calling setupBottomNavigation()
    }

    /**
     * Call this method after setContentView() in child activities to setup navigation
     */
    protected void setupBottomNavigation() {
        // Find navigation views
        LinearLayout navTasks = findViewById(R.id.navTasks);
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navReports = findViewById(R.id.navReports);
        
        // Reset all to inactive
        resetNavigationItem(navTasks);
        resetNavigationItem(navHome);
        resetNavigationItem(navReports);
        
        // Set active based on index
        int activeIndex = getActiveNavItemIndex();
        switch (activeIndex) {
            case 0:
                setActiveNavigationItem(navTasks);
                break;
            case 1:
                setActiveNavigationItem(navHome);
                break;
            case 2:
                setActiveNavigationItem(navReports);
                break;
        }

        // Set click listeners
        setupNavigationClickListeners();
    }
    
    private void resetNavigationItem(LinearLayout navItem) {
        if (navItem != null) {
            // Find the image and text views
            if (navItem.getChildCount() >= 2) {
                View iconView = navItem.getChildAt(0);
                View textView = navItem.getChildAt(1);
                
                if (iconView instanceof ImageView && textView instanceof TextView) {
                    // Reset to inactive state
                    ((ImageView) iconView).setColorFilter(ContextCompat.getColor(this, R.color.dark_gray));
                    ((TextView) textView).setTextColor(ContextCompat.getColor(this, R.color.dark_gray));
                    ((TextView) textView).setTypeface(null, android.graphics.Typeface.NORMAL);
                }
            }
        }
    }
    
    private void setActiveNavigationItem(LinearLayout navItem) {
        if (navItem != null) {
            // Find the image and text views
            if (navItem.getChildCount() >= 2) {
                View iconView = navItem.getChildAt(0);
                View textView = navItem.getChildAt(1);
                
                if (iconView instanceof ImageView && textView instanceof TextView) {
                    // Set to active state
                    ((ImageView) iconView).setColorFilter(ContextCompat.getColor(this, R.color.primary));
                    ((TextView) textView).setTextColor(ContextCompat.getColor(this, R.color.primary));
                    ((TextView) textView).setTypeface(null, android.graphics.Typeface.BOLD);
                }
            }
        }
    }

    private void setupNavigationClickListeners() {
        // Find navigation views
        LinearLayout navTasks = findViewById(R.id.navTasks);
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navReports = findViewById(R.id.navReports);

        // Set click listeners
        if (navTasks != null) {
            navTasks.setOnClickListener(v -> navigateToTasks());
        }
        if (navHome != null) {
            navHome.setOnClickListener(v -> navigateToHome());
        }
        if (navReports != null) {
            navReports.setOnClickListener(v -> navigateToReports());
        }
    }

    /**
     * Override this method in child activities to specify which nav item index should be active (0-2)
     * 0: Tasks, 1: Home, 2: Reports
     */
    protected abstract int getActiveNavItemIndex();

    // Navigation methods
    protected void navigateToTasks() {
        if (getActiveNavItemIndex() != 0) {
            Intent intent = new Intent(this, CollectionTasksActivity.class);
            // Add a flag to indicate this is a navigation action to prevent login redirect
            intent.putExtra("isNavigationAction", true);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }

    protected void navigateToHome() {
        if (getActiveNavItemIndex() != 1) {
            Intent intent = new Intent(this, CollectorDashboardActivity.class);
            // Add a flag to indicate this is a navigation action to prevent login redirect
            intent.putExtra("isNavigationAction", true);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }

    protected void navigateToReports() {
        if (getActiveNavItemIndex() != 2) {
            Intent intent = new Intent(this, CollectorReportsActivity.class);
            // Add a flag to indicate this is a navigation action to prevent login redirect
            intent.putExtra("isNavigationAction", true);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }
}

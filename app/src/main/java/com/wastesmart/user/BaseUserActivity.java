package com.wastesmart.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.wastesmart.R;

/**
 * Base activity for all user pages that includes the bottom navigation bar
 */
public abstract class BaseUserActivity extends AppCompatActivity {

    private LinearLayout navSubmit;
    private LinearLayout navReports;
    private LinearLayout navHome;
    private LinearLayout navAbout;
    private LinearLayout navProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Child activities should call setContentView() before calling super.onCreate()
    }

    /**
     * Call this method after setContentView() in child activities to setup navigation
     */
    protected void setupBottomNavigation() {
        // Find navigation views
        navSubmit = findViewById(R.id.navSubmit);
        navReports = findViewById(R.id.navReports);
        navHome = findViewById(R.id.navHome);
        navAbout = findViewById(R.id.navAbout);
        navProfile = findViewById(R.id.navProfile);

        // Set click listeners
        if (navSubmit != null) {
            navSubmit.setOnClickListener(v -> navigateToSubmit());
        }
        
        if (navReports != null) {
            navReports.setOnClickListener(v -> navigateToReports());
        }
        
        if (navHome != null) {
            navHome.setOnClickListener(v -> navigateToHome());
        }
        
        if (navAbout != null) {
            navAbout.setOnClickListener(v -> navigateToAbout());
        }
        
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> navigateToProfile());
        }

        // Update active state
        updateNavigationState();
    }

    /**
     * Override this method in child activities to specify which nav item should be active
     */
    protected abstract String getActiveNavItem();

    private void updateNavigationState() {
        String activeItem = getActiveNavItem();
        
        // Reset all navigation items to inactive state
        resetNavigationState(navSubmit);
        resetNavigationState(navReports);
        resetNavigationState(navHome);
        resetNavigationState(navAbout);
        resetNavigationState(navProfile);

        // Set active state based on current page
        switch (activeItem) {
            case "submit":
                setActiveNavigationState(navSubmit);
                break;
            case "reports":
                setActiveNavigationState(navReports);
                break;
            case "home":
                setActiveNavigationState(navHome);
                break;
            case "about":
                setActiveNavigationState(navAbout);
                break;
            case "profile":
                setActiveNavigationState(navProfile);
                break;
        }
    }

    private void resetNavigationState(LinearLayout navItem) {
        if (navItem != null && navItem.getChildCount() >= 2) {
            // Reset icon and text colors to inactive state
            View icon = navItem.getChildAt(0);
            View text = navItem.getChildAt(1);
            
            if (icon instanceof android.widget.ImageView) {
                ((android.widget.ImageView) icon).setColorFilter(getResources().getColor(R.color.dark_gray));
            }
            
            if (text instanceof android.widget.TextView) {
                ((android.widget.TextView) text).setTextColor(getResources().getColor(R.color.dark_gray));
                ((android.widget.TextView) text).setTypeface(null, android.graphics.Typeface.NORMAL);
            }
        }
    }

    private void setActiveNavigationState(LinearLayout navItem) {
        if (navItem != null && navItem.getChildCount() >= 2) {
            // Set icon and text colors to active state
            View icon = navItem.getChildAt(0);
            View text = navItem.getChildAt(1);
            
            if (icon instanceof android.widget.ImageView) {
                ((android.widget.ImageView) icon).setColorFilter(getResources().getColor(R.color.primary));
            }
            
            if (text instanceof android.widget.TextView) {
                ((android.widget.TextView) text).setTextColor(getResources().getColor(R.color.primary));
                ((android.widget.TextView) text).setTypeface(null, android.graphics.Typeface.BOLD);
            }
        }
    }

    private void navigateToSubmit() {
        if (!getActiveNavItem().equals("submit")) {
            Intent intent = new Intent(this, ReportWasteActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }

    private void navigateToReports() {
        if (!getActiveNavItem().equals("reports")) {
            Intent intent = new Intent(this, MyReportsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }

    private void navigateToHome() {
        if (!getActiveNavItem().equals("home")) {
            Intent intent = new Intent(this, UserDashboardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }

    private void navigateToAbout() {
        if (!getActiveNavItem().equals("about")) {
            Intent intent = new Intent(this, CollectionScheduleActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }

    private void navigateToProfile() {
        if (!getActiveNavItem().equals("profile")) {
            Intent intent = new Intent(this, UserProfileActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }
}

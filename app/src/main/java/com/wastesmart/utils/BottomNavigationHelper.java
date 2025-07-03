package com.wastesmart.utils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

import com.wastesmart.R;

/**
 * Helper class to manage the shared bottom navigation component across different user types
 */
public class BottomNavigationHelper {

    /**
     * Set up the bottom navigation for the user role
     * @param activity The current activity
     * @param activeItemIndex The index of the currently active item (0-4)
     */
    public static void setupUserBottomNavigation(Activity activity, int activeItemIndex) {
        // Define navigation items for user
        NavigationItem[] items = {
                new NavigationItem(R.drawable.baseline_assignment_add_24, "Submit", null),
                new NavigationItem(R.drawable.baseline_assignment_24, "Reports", null),
                new NavigationItem(R.drawable.baseline_home24, "Home", null),
                new NavigationItem(R.drawable.baseline_info_24, "About", null),
                new NavigationItem(R.drawable.baseline_person24, "Profile", null)
        };

        setupBottomNavigation(activity, items, activeItemIndex);
    }

    /**
     * Set up the bottom navigation for the collector role
     * @param activity The current activity
     * @param activeItemIndex The index of the currently active item (0-4)
     */
    public static void setupCollectorBottomNavigation(Activity activity, int activeItemIndex) {
        // Define navigation items for collector
        NavigationItem[] items = {
                new NavigationItem(R.drawable.baseline_assignment_24, "Tasks", null),
                new NavigationItem(R.drawable.ic_location_on, "Routes", null),
                new NavigationItem(R.drawable.baseline_home24, "Home", null),
                new NavigationItem(R.drawable.ic_reports, "Reports", null),
                new NavigationItem(R.drawable.baseline_person24, "Profile", null)
        };

        setupBottomNavigation(activity, items, activeItemIndex);
    }
    
    /**
     * Set up the bottom navigation for the admin role
     * @param activity The current activity
     * @param activeItemIndex The index of the currently active item (0-4)
     */
    public static void setupAdminBottomNavigation(Activity activity, int activeItemIndex) {
        // Define navigation items for admin
        NavigationItem[] items = {
                new NavigationItem(R.drawable.baseline_assignment_24, "Reports", null),
                new NavigationItem(R.drawable.baseline_person_24, "Collector", null),
                new NavigationItem(R.drawable.baseline_home24, "Home", null),
                new NavigationItem(R.drawable.ic_reports, "Analytics", null),
                new NavigationItem(R.drawable.baseline_person24, "Users", null)
        };

        setupBottomNavigation(activity, items, activeItemIndex);
    }

    /**
     * Configure the shared bottom navigation with the provided items
     * @param activity The current activity
     * @param items The navigation items to display
     * @param activeItemIndex The index of the currently active item
     */
    private static void setupBottomNavigation(Activity activity, NavigationItem[] items, int activeItemIndex) {
        if (items.length != 5) {
            throw new IllegalArgumentException("Navigation must have exactly 5 items");
        }

        // Find views
        int[] itemIds = {
                R.id.navItem1, R.id.navItem2, R.id.navItem3, R.id.navItem4, R.id.navItem5
        };
        int[] iconIds = {
                R.id.navItem1Icon, R.id.navItem2Icon, R.id.navItem3Icon, R.id.navItem4Icon, R.id.navItem5Icon
        };
        int[] textIds = {
                R.id.navItem1Text, R.id.navItem2Text, R.id.navItem3Text, R.id.navItem4Text, R.id.navItem5Text
        };

        // Configure each item
        for (int i = 0; i < items.length; i++) {
            LinearLayout itemView = activity.findViewById(itemIds[i]);
            ImageView iconView = activity.findViewById(iconIds[i]);
            TextView textView = activity.findViewById(textIds[i]);

            // Set icon and text
            iconView.setImageResource(items[i].iconResId);
            textView.setText(items[i].text);

            // Set active state
            boolean isActive = (i == activeItemIndex);
            int colorRes = isActive ? R.color.primary : R.color.dark_gray;
            int color = ContextCompat.getColor(activity, colorRes);
            
            iconView.setColorFilter(color);
            textView.setTextColor(color);
            if (isActive) {
                textView.setTextSize(12);
                textView.setTypeface(textView.getTypeface(), android.graphics.Typeface.BOLD);
            }

            // Set click listener if provided
            final Intent intent = items[i].intent;
            if (intent != null) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        activity.startActivity(intent);
                    }
                });
            }
        }
    }

    /**
     * Data class for navigation items
     */
    private static class NavigationItem {
        @DrawableRes int iconResId;
        String text;
        Intent intent;

        NavigationItem(@DrawableRes int iconResId, String text, Intent intent) {
            this.iconResId = iconResId;
            this.text = text;
            this.intent = intent;
        }
    }
}

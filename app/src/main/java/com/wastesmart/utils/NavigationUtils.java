package com.wastesmart.utils;

import android.app.Activity;
import android.content.Intent;
import com.wastesmart.R;

public class NavigationUtils {
    
    /**
     * Start activity with slide-in-right transition (for forward navigation)
     */
    public static void navigateForward(Activity fromActivity, Intent intent) {
        fromActivity.startActivity(intent);
        fromActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
    
    /**
     * Start activity with fade transition (for dashboard/main pages)
     */
    public static void navigateToDashboard(Activity fromActivity, Intent intent) {
        fromActivity.startActivity(intent);
        fromActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
    
    /**
     * Finish activity with slide-in-left transition (for back navigation)
     */
    public static void navigateBack(Activity activity) {
        activity.finish();
        activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
    
    /**
     * Override back pressed with smooth transition
     */
    public static void handleBackPressed(Activity activity) {
        activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
    
    /**
     * Start activity with custom transitions
     */
    public static void navigateWithCustomTransition(Activity fromActivity, Intent intent, 
                                                   int enterAnim, int exitAnim) {
        fromActivity.startActivity(intent);
        fromActivity.overridePendingTransition(enterAnim, exitAnim);
    }
}

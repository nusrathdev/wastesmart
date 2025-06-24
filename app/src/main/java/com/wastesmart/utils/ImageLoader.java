package com.wastesmart.utils;

import android.content.Context;
import android.widget.ImageView;

import com.wastesmart.R;

/**
 * Simple image loading utility without external dependencies
 */
public class ImageLoader {

    /**
     * Load image using simple placeholder approach
     */
    public static void loadImage(Context context, String imageUrl, ImageView imageView) {
        loadImage(context, imageUrl, imageView, R.drawable.ic_photo_placeholder, R.drawable.ic_photo_error);
    }

    /**
     * Load image with custom placeholders
     */
    public static void loadImage(Context context, String imageUrl, ImageView imageView, 
                               int placeholderRes, int errorRes) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            // For now, just show placeholder
            // TODO: Implement actual image loading if needed
            imageView.setImageResource(placeholderRes);
        } else {
            imageView.setImageResource(errorRes);
        }
    }

    /**
     * Load local resource images
     */
    public static void loadResource(Context context, int resourceId, ImageView imageView) {
        imageView.setImageResource(resourceId);
    }
}

package com.wastesmart.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;
import com.wastesmart.R;

/**
 * Activity to display a fullscreen image - can be used by both admin and collector
 */
public class FullscreenImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fullscreen_image_view);

        // Get image URL from intent
        String imageUrl = getIntent().getStringExtra("imageUrl");
        
        ImageView fullscreenImageView = findViewById(R.id.fullscreenImageView);
        ImageButton closeButton = findViewById(R.id.btnCloseFullscreen);
        
        // Load image with Picasso
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.ic_photo_placeholder)
                .error(R.drawable.ic_photo_error)
                .into(fullscreenImageView);
        }
        
        // Set close button action
        closeButton.setOnClickListener(v -> finish());
    }
}

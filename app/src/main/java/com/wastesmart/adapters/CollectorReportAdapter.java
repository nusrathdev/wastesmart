package com.wastesmart.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import com.wastesmart.R;
import com.wastesmart.models.WasteReport;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter specifically for collector reports.
 * Provides a better visual presentation of waste collection reports for collectors.
 */
public class CollectorReportAdapter extends RecyclerView.Adapter<CollectorReportAdapter.ReportViewHolder> {

    private static final String TAG = "CollectorReportAdapter";
    
    private Context context;
    private List<WasteReport> reportsList;
    private OnReportClickListener listener;
    private SimpleDateFormat dateFormat;

    public interface OnReportClickListener {
        void onReportClick(WasteReport report, int position);
        void onUpdateStatusClick(WasteReport report, int position);
    }

    public CollectorReportAdapter(Context context, List<WasteReport> reportsList, OnReportClickListener listener) {
        this.context = context;
        this.reportsList = reportsList;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        
        Log.d(TAG, "CollectorReportAdapter initialized with " + 
            (reportsList != null ? reportsList.size() : 0) + " reports");
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Use the same layout as user reports for consistency
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_collector_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        WasteReport report = reportsList.get(position);
        
        // Set waste type if available
        if (report.getWasteType() != null && !report.getWasteType().isEmpty()) {
            holder.tvWasteType.setText(report.getWasteType());
            holder.tvWasteType.setVisibility(View.VISIBLE);
        } else {
            holder.tvWasteType.setVisibility(View.GONE);
        }
        
        // Set waste size if available
        if (report.getWasteSize() != null && !report.getWasteSize().isEmpty()) {
            holder.tvSize.setText(report.getWasteSize());
            holder.tvSize.setVisibility(View.VISIBLE);
        } else {
            holder.tvSize.setText("Unknown size");
        }
        
        // Set report description
        if (report.getDescription() != null && !report.getDescription().isEmpty()) {
            holder.tvDescription.setText(report.getDescription());
            holder.tvDescription.setVisibility(View.VISIBLE);
        } else {
            holder.tvDescription.setVisibility(View.GONE);
        }
        
        // Set location from coordinates
        String locationText = String.format(Locale.getDefault(), 
            "📍 Lat: %.4f, Long: %.4f", report.getLatitude(), report.getLongitude());
        holder.tvLocation.setText(locationText);
        holder.tvLocation.setVisibility(View.VISIBLE);
        
        // Set date
        if (report.getTimestamp() != null && report.getTimestamp() > 0) {
            Date date = new Date(report.getTimestamp());
            holder.tvTimestamp.setText(dateFormat.format(date));
        } else {
            holder.tvTimestamp.setText("Date not available");
        }
        
        // User info not displayed in new layout
        // setUserInfo(holder, report);
        
        // Set status with appropriate styling
        String status = report.getStatus();
        if (status != null) {
            holder.tvStatus.setText(formatStatus(status));
            
            int statusBackground;
            int statusColor;
            
            switch (status.toUpperCase()) {
                case "COMPLETED":
                    statusBackground = R.drawable.status_completed_circle_bg;
                    statusColor = context.getResources().getColor(R.color.status_completed, null);
                    break;
                case "IN_PROGRESS":
                    statusBackground = R.drawable.status_in_progress_circle_bg;
                    statusColor = context.getResources().getColor(R.color.status_in_progress, null);
                    break;
                case "PENDING":
                    statusBackground = R.drawable.status_pending_circle_bg;
                    statusColor = context.getResources().getColor(R.color.status_pending, null);
                    break;
                case "ASSIGNED":
                    statusBackground = R.drawable.status_assigned_circle_bg;
                    statusColor = context.getResources().getColor(R.color.status_assigned, null);
                    break;
                default:
                    statusBackground = R.drawable.status_circle_bg;
                    statusColor = context.getResources().getColor(R.color.primary, null);
                    break;
            }
            
            holder.tvStatus.setBackgroundResource(statusBackground);
            holder.tvStatus.setTextColor(statusColor);
        }
        
        // Load image if available - try both photoUrl and imageUrl
        // Determine the image URL to use
        String tempImageUrl = null;
        if (report.getPhotoUrl() != null && !report.getPhotoUrl().isEmpty()) {
            tempImageUrl = report.getPhotoUrl();
        } else if (report.getImageUrl() != null && !report.getImageUrl().isEmpty()) {
            tempImageUrl = report.getImageUrl();
        }
        
        // Make the URL final so it can be used in the inner class callback
        final String imageUrl = tempImageUrl;
        
        if (imageUrl != null && !imageUrl.isEmpty()) {
            // Make the image visible
            holder.ivReportImage.setVisibility(View.VISIBLE);
            
            Log.d(TAG, "Loading image from URL: " + imageUrl + " for report ID: " + report.getId());
            
            // Use a fallback image based on waste type if the URL fails
            // Make this variable final so it can be accessed from the inner class
            final String fallbackUrl = getFallbackImageUrl(report.getWasteType());
            
            // Try to load the image with Picasso, with extra reliability features
            try {
                Picasso.get()
                    .load(imageUrl)
                    .into(holder.ivReportImage, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "Successfully loaded image for report ID: " + report.getId());
                        }
                        
                        @Override
                        public void onError(Exception e) {
                            Log.e(TAG, "Error loading image for report ID: " + report.getId() + 
                                ", trying fallback URL: " + fallbackUrl, e);
                            
                            // Try fallback URL if primary URL failed
                            if (fallbackUrl != null && !fallbackUrl.equals(imageUrl)) {
                                Picasso.get()
                                    .load(fallbackUrl)
                                    .into(holder.ivReportImage);
                            } else {
                                // Hide the image if both main and fallback URLs fail
                                holder.ivReportImage.setVisibility(View.GONE);
                            }
                        }
                    });
                
                // Set image size to match the layout - convert dp to pixels
                int sizeInDp = 120;
                float scale = context.getResources().getDisplayMetrics().density;
                int sizeInPixels = (int) (sizeInDp * scale + 0.5f);
                
                holder.ivReportImage.getLayoutParams().width = sizeInPixels; // match the layout size of 120dp
                holder.ivReportImage.getLayoutParams().height = sizeInPixels; // match the layout size of 120dp
            } catch (Exception e) {
                Log.e(TAG, "Exception while setting up Picasso image load", e);
                // Hide the image if there's an error
                holder.ivReportImage.setVisibility(View.GONE);
            }
        } else {
            Log.d(TAG, "No image URL available for report ID: " + report.getId());
            
            // Hide the image view when there's no image
            holder.ivReportImage.setVisibility(View.GONE);
        }
        
        // Set button visibility based on status
        if ("COMPLETED".equalsIgnoreCase(status) || "CANCELED".equalsIgnoreCase(status)) {
            holder.btnUpdateStatus.setVisibility(View.GONE);
        } else {
            holder.btnUpdateStatus.setVisibility(View.VISIBLE);
        }
        
        // Always ensure the View Details button is visible regardless of status
        holder.btnViewDetails.setVisibility(View.VISIBLE);
        
        // Set click listeners
        holder.btnViewDetails.setOnClickListener(v -> {
            // Launch Map Activity to show waste location
            Intent mapIntent = new Intent(context, com.wastesmart.admin.WasteLocationMapActivity.class);
            mapIntent.putExtra("latitude", report.getLatitude());
            mapIntent.putExtra("longitude", report.getLongitude());
            
            // Add waste type directly
            mapIntent.putExtra("wasteType", report.getWasteType());
            
            // Add title for map marker (just for the marker, not for display in UI)
            mapIntent.putExtra("title", "Waste Location");
            
            // Add description
            String description = report.getDescription() != null ? report.getDescription() : "";
            mapIntent.putExtra("description", description);
            
            // Add status
            mapIntent.putExtra("status", report.getStatus());
            
            context.startActivity(mapIntent);
        });
        
        holder.btnUpdateStatus.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUpdateStatusClick(report, position);
            }
        });
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReportClick(report, position);
            }
        });
        
        // Add click listener for the image to open fullscreen view
        holder.ivReportImage.setOnClickListener(v -> {
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Intent fullscreenIntent = new Intent(context, com.wastesmart.ui.FullscreenImageActivity.class);
                fullscreenIntent.putExtra("imageUrl", imageUrl);
                context.startActivity(fullscreenIntent);
            }
        });
        
        // User info not displayed in new layout
        // setUserInfo(holder, report);
    }

    @Override
    public int getItemCount() {
        return reportsList != null ? reportsList.size() : 0;
    }
    
    // Helper method to format status text for display
    private String formatStatus(String status) {
        if (status == null || status.isEmpty()) return "";
        
        switch (status.toUpperCase()) {
            case "IN_PROGRESS":
                return "IN PROGRESS";
            default:
                return status.toUpperCase();
        }
    }
    
    // Helper method to get item safely
    public WasteReport getItem(int position) {
        if (reportsList != null && position >= 0 && position < reportsList.size()) {
            return reportsList.get(position);
        }
        return null;
    }
    
    // Helper method to get user info and display it
    // Note: This method is commented out as tvUserInfo is no longer part of the new layout
    // Keeping the implementation for reference if user info needs to be added back
    private void setUserInfo(ReportViewHolder holder, WasteReport report) {
        // Skip this method as tvUserInfo is not in the new layout
        if (true) return;
        
        /* 
        // Get user ID
        String userId = report.getUserId();
        
        if (userId != null && !userId.isEmpty()) {
            // Default user info (if can't fetch the actual name)
            holder.tvUserInfo.setText("👤 Reported by: User");
            holder.tvUserInfo.setVisibility(View.VISIBLE);
            
            // Try to fetch the user's name from Firestore
            FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String userName = documentSnapshot.getString("name");
                        if (userName != null && !userName.isEmpty()) {
                            holder.tvUserInfo.setText("👤 Reported by: " + userName);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching user info: " + e.getMessage());
                });
        } else {
            holder.tvUserInfo.setVisibility(View.GONE);
        }
        */
    }

    /**
     * Get a fallback image URL based on waste type
     */
    private String getFallbackImageUrl(String wasteType) {
        String baseUrl = "https://firebasestorage.googleapis.com/v0/b/wastesmart-app.appspot.com/o/waste_images%2F";
        
        if (wasteType == null) {
            return baseUrl + "general_waste.jpg?alt=media";
        }
        
        wasteType = wasteType.toLowerCase();
        
        if (wasteType.contains("household")) {
            return baseUrl + "household_waste.jpg?alt=media";
        } else if (wasteType.contains("recycl")) {
            return baseUrl + "recyclable_waste.jpg?alt=media";
        } else if (wasteType.contains("electronic")) {
            return baseUrl + "electronic_waste.jpg?alt=media";
        } else if (wasteType.contains("garden") || wasteType.contains("yard")) {
            return baseUrl + "garden_waste.jpg?alt=media";
        } else if (wasteType.contains("commercial")) {
            return baseUrl + "commercial_waste.jpg?alt=media";
        } else if (wasteType.contains("construction") || wasteType.contains("debris")) {
            return baseUrl + "construction_waste.jpg?alt=media";
        } else if (wasteType.contains("hazard")) {
            return baseUrl + "hazardous_waste.jpg?alt=media";
        } else {
            return baseUrl + "general_waste.jpg?alt=media";
        }
    }
    
    /**
     * Set a default image on the ImageView based on waste type
     */
    private void setDefaultWasteImage(ImageView imageView, String wasteType) {
        try {
            if (wasteType == null) {
                imageView.setImageResource(R.drawable.ic_recycling);
                return;
            }
            
            wasteType = wasteType.toLowerCase();
            
            // Use appropriate drawable based on waste type - only use drawables we know exist
            if (wasteType.contains("household")) {
                imageView.setImageResource(R.drawable.baseline_home24);
            } else if (wasteType.contains("recycl")) {
                imageView.setImageResource(R.drawable.ic_recycling);
            } else if (wasteType.contains("electronic")) {
                imageView.setImageResource(R.drawable.ic_description);
            } else if (wasteType.contains("garden") || wasteType.contains("yard")) {
                imageView.setImageResource(R.drawable.ic_assignment);
            } else if (wasteType.contains("construction") || wasteType.contains("debris")) {
                imageView.setImageResource(R.drawable.ic_straighten);
            } else {
                imageView.setImageResource(R.drawable.ic_recycling);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting default waste image", e);
            try {
                // Last resort - use a placeholder icon we know must exist
                imageView.setImageResource(R.drawable.ic_photo_placeholder);
            } catch (Exception ex) {
                Log.e(TAG, "Failed to set even placeholder image", ex);
            }
        }
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView tvWasteType, tvDescription, tvLocation, tvStatus, tvTimestamp, tvSize;
        ImageView ivReportImage;
        MaterialButton btnViewDetails, btnUpdateStatus;
        
        ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWasteType = itemView.findViewById(R.id.tvWasteType);
            tvSize = itemView.findViewById(R.id.tvSize);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            ivReportImage = itemView.findViewById(R.id.ivReportImage);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            btnUpdateStatus = itemView.findViewById(R.id.btnUpdateStatus);
        }
    }
}
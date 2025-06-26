package com.wastesmart.user;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.app.Dialog;
import android.view.Window;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AlphaAnimation;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wastesmart.R;
import com.wastesmart.models.WasteReport;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying user's own waste reports in a user-friendly format
 */
public class UserReportsAdapter extends RecyclerView.Adapter<UserReportsAdapter.UserReportViewHolder> {

    private List<WasteReport> reports;
    private Context context;
    private SimpleDateFormat dateFormat;

    public UserReportsAdapter(List<WasteReport> reports, Context context) {
        this.reports = reports;
        this.context = context;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public UserReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_report, parent, false);
        return new UserReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserReportViewHolder holder, int position) {
        WasteReport report = reports.get(position);

        // Set basic report info
        holder.tvWasteType.setText(report.getWasteType());
        holder.tvSize.setText(report.getSize());
        holder.tvDescription.setText(report.getDescription());
        holder.tvLocation.setText(String.format(Locale.getDefault(), "%.6f, %.6f", 
                report.getLatitude(), report.getLongitude()));

        // Format and set timestamp
        if (report.getTimestamp() != null) {
            holder.tvTimestamp.setText(dateFormat.format(new Date(report.getTimestamp())));
        }

        // Set status and update UI based on status
        String status = report.getStatus() != null ? report.getStatus().toLowerCase() : "pending";
        
        // Check if photo should be displayed (show for all statuses when available)
        boolean hasPhoto = report.getImageUrl() != null && !report.getImageUrl().isEmpty();
        
        // Update status badge with emoji and background color
        switch (status) {
            case "completed":
                holder.tvStatus.setText("COMPLETED");
                holder.tvStatus.setBackgroundResource(R.drawable.status_completed_circle_bg);
                holder.tvStatus.setTextColor(context.getResources().getColor(R.color.status_completed, null));
                break;
            case "in_progress":
                holder.tvStatus.setText("IN PROGRESS");
                holder.tvStatus.setBackgroundResource(R.drawable.status_in_progress_circle_bg);
                holder.tvStatus.setTextColor(context.getResources().getColor(R.color.status_in_progress, null));
                break;
            case "assigned":
                holder.tvStatus.setText("ASSIGNED");
                holder.tvStatus.setBackgroundResource(R.drawable.status_assigned_circle_bg);
                holder.tvStatus.setTextColor(context.getResources().getColor(R.color.status_assigned, null));
                break;
            default: // pending
                holder.tvStatus.setText("PENDING");
                holder.tvStatus.setBackgroundResource(R.drawable.status_pending_circle_bg);
                holder.tvStatus.setTextColor(context.getResources().getColor(R.color.status_pending, null));
                break;
        }

        // Show photo thumbnail in corner when photo is available
        if (hasPhoto) {
            holder.ivPhotoCorner.setVisibility(View.VISIBLE);
            
            // Load actual image from URL using Picasso
            Picasso.get()
                    .load(report.getImageUrl())
                    .fit()
                    .centerCrop()
                    .into(holder.ivPhotoCorner);

            // Set click listener for full-screen image view
            holder.ivPhotoCorner.setOnClickListener(v -> showFullScreenImage(report.getImageUrl()));
        } else {
            holder.ivPhotoCorner.setVisibility(View.GONE);
            holder.ivPhotoCorner.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    /**
     * Show full-screen image viewer with modern slide-in animation
     */
    private void showFullScreenImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) return;

        // Create dialog for full-screen image
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_fullscreen_image);
        
        // Make dialog full-screen and transparent
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, 
                           WindowManager.LayoutParams.MATCH_PARENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                          WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        // Get views from dialog
        ImageView fullScreenImage = dialog.findViewById(R.id.ivFullScreenImage);
        View closeArea = dialog.findViewById(R.id.closeArea);

        // Load the full-resolution image
        Picasso.get()
                .load(imageUrl)
                .into(fullScreenImage);

        // Close dialog when clicking outside the image
        closeArea.setOnClickListener(v -> {
            // Add fade-out animation
            Animation fadeOut = new AlphaAnimation(1.0f, 0.0f);
            fadeOut.setDuration(200);
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}
                
                @Override
                public void onAnimationEnd(Animation animation) {
                    dialog.dismiss();
                }
                
                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            closeArea.startAnimation(fadeOut);
        });
        
        // Close dialog when clicking the image itself (alternative)
        fullScreenImage.setOnClickListener(v -> {
            Animation fadeOut = new AlphaAnimation(1.0f, 0.0f);
            fadeOut.setDuration(200);
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}
                
                @Override
                public void onAnimationEnd(Animation animation) {
                    dialog.dismiss();
                }
                
                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            closeArea.startAnimation(fadeOut);
        });

        // Show dialog with fade-in animation
        dialog.show();
        
        // Add fade-in animation
        Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(250);
        closeArea.startAnimation(fadeIn);
    }

    static class UserReportViewHolder extends RecyclerView.ViewHolder {
        TextView tvWasteType, tvSize, tvDescription, tvLocation, tvTimestamp, tvStatus;
        ImageView ivPhotoCorner;

        public UserReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWasteType = itemView.findViewById(R.id.tvWasteType);
            tvSize = itemView.findViewById(R.id.tvSize);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            ivPhotoCorner = itemView.findViewById(R.id.ivPhotoCorner);
        }
    }
}

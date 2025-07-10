package com.wastesmart.admin;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wastesmart.R;
import com.wastesmart.models.WasteReport;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying a simplified version of waste reports in the admin dashboard
 */
public class SimpleAdminReportsAdapter extends RecyclerView.Adapter<SimpleAdminReportsAdapter.ReportViewHolder> {

    private List<WasteReport> reports;
    private AdminDashboardActivity context;
    private SimpleDateFormat dateFormat;

    public SimpleAdminReportsAdapter(List<WasteReport> reports, AdminDashboardActivity context) {
        this.reports = reports;
        this.context = context;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        try {
            WasteReport report = reports.get(position);
            
            Log.d("SimpleAdminReportsAdapter", "Binding report at position " + position + ": " + report.getId());

            // Set the waste type (bold and prominent like in the screenshot)
            holder.tvWasteType.setText(report.getWasteType());
            
            // Always set title to "Report Details"
            holder.tvTitle.setText("Report Details");
            
            // Hide location information
            holder.tvLocation.setVisibility(View.GONE);
            
            // Format the date with emoji for better readability
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            if (report.getTimestamp() != null) {
                holder.tvDate.setText("🕒 " + dateFormat.format(new Date(report.getTimestamp())));
            }
            
            // For the description, just use the raw description field
            holder.tvDescription.setText(report.getDescription());
            
            // Set reported by exactly like in the screenshot
            if (report.getSubmitterName() != null && !report.getSubmitterName().isEmpty()) {
                holder.tvUserInfo.setText("Reported by: " + report.getSubmitterName());
                holder.tvUserInfo.setVisibility(View.VISIBLE);
            } else {
                holder.tvUserInfo.setVisibility(View.GONE);
            }
            
            // Set status - getStatus() already returns uppercase from WasteReport
            String status = report.getStatus();
            holder.tvStatus.setText(status);
            Log.d("SimpleAdminReportsAdapter", "Status for report " + report.getId() + ": " + status);
            
            // Set background drawable and badge style to match user dashboard
            int backgroundRes;
            int textColor;
            switch(status.toUpperCase()) {
                case "ASSIGNED":
                    backgroundRes = R.drawable.status_assigned_bg;
                    textColor = context.getResources().getColor(R.color.white);
                    break;
                case "IN_PROGRESS":
                    backgroundRes = R.drawable.status_in_progress_bg;
                    textColor = context.getResources().getColor(R.color.white);
                    break;
                case "COMPLETED":
                    backgroundRes = R.drawable.status_completed_bg;
                    textColor = context.getResources().getColor(R.color.white);
                    break;
                case "PENDING":
                default:
                    // Use a styled badge for pending
                    backgroundRes = R.drawable.status_background;
                    textColor = context.getResources().getColor(R.color.white);
                    break;
            }
            holder.tvStatus.setBackground(context.getDrawable(backgroundRes));
            holder.tvStatus.setTextColor(textColor);
            
            // Make the status badge more prominent
            holder.tvStatus.setPadding(
                context.getResources().getDimensionPixelSize(R.dimen.status_padding_horizontal),
                context.getResources().getDimensionPixelSize(R.dimen.status_padding_vertical),
                context.getResources().getDimensionPixelSize(R.dimen.status_padding_horizontal),
                context.getResources().getDimensionPixelSize(R.dimen.status_padding_vertical)
            );
            
            // Determine the image URL to use - try both imageUrl and photoUrl
            String imageUrl = null;
            if (report.getImageUrl() != null && !report.getImageUrl().isEmpty()) {
                imageUrl = report.getImageUrl();
            } else if (report.getPhotoUrl() != null && !report.getPhotoUrl().isEmpty()) {
                imageUrl = report.getPhotoUrl();
            }
            
            // Show image if available, otherwise hide
            if (imageUrl != null && !imageUrl.isEmpty()) {
                // Make the image visible
                holder.ivReportImage.setVisibility(View.VISIBLE);
                
                // Use Picasso to load the image
                try {
                    com.squareup.picasso.Picasso.get()
                        .load(imageUrl)
                        .into(holder.ivReportImage);
                    
                    // Convert dp to pixels to ensure consistent size across all devices
                    int sizeInDp = 120;
                    float scale = context.getResources().getDisplayMetrics().density;
                    int sizeInPixels = (int) (sizeInDp * scale + 0.5f);
                    
                    holder.ivReportImage.getLayoutParams().width = sizeInPixels;
                    holder.ivReportImage.getLayoutParams().height = sizeInPixels;
                    
                    // Set up click listener for fullscreen image viewing
                    final String finalImageUrl = imageUrl;
                    holder.ivReportImage.setOnClickListener(v -> {
                        Intent fullscreenIntent = new Intent(context, com.wastesmart.ui.FullscreenImageActivity.class);
                        fullscreenIntent.putExtra("imageUrl", finalImageUrl);
                        context.startActivity(fullscreenIntent);
                    });
                } catch (Exception e) {
                    Log.e("SimpleAdminReportsAdapter", "Error loading image: " + e.getMessage());
                    holder.ivReportImage.setVisibility(View.GONE);
                }
            } else {
                // Hide the image if there's no URL available
                holder.ivReportImage.setVisibility(View.GONE);
            }

            // Setup view details button (left button in screenshot)
            holder.btnViewDetails.setText("View Details");
            holder.btnViewDetails.setOnClickListener(v -> {
                // This could be implemented to show full report details
                Toast.makeText(context, "Viewing details for report: " + report.getId(), Toast.LENGTH_SHORT).show();
            });
            
            // Setup assign button (right button in screenshot)
            holder.btnAssign.setText("Assign");
            holder.btnAssign.setOnClickListener(v -> {
                try {
                    // First remove this item from the adapter
                    int adapterPosition = holder.getAdapterPosition();
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        reports.remove(adapterPosition);
                        notifyItemRemoved(adapterPosition);
                        
                        // Then update the database
                        context.assignReportToCollector(report.getId());
                        
                        // If list is now empty, update UI
                        if (reports.isEmpty()) {
                            // Update the UI to reflect that all reports have been assigned
                            Toast.makeText(context, "All reports have been assigned", Toast.LENGTH_SHORT).show();
                            
                            // Update the pending count
                            TextView pendingCountView = context.findViewById(R.id.tvPendingCount);
                            if (pendingCountView != null) {
                                pendingCountView.setText("0");
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e("SimpleAdminReportsAdapter", "Error assigning report", e);
                    Toast.makeText(context, "Error assigning report: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            
        } catch (Exception e) {
            Log.e("SimpleAdminReportsAdapter", "Error in onBindViewHolder", e);
        }
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView tvWasteType, tvLocation, tvDescription, tvDate, tvStatus, tvTitle, tvUserInfo;
        ImageView ivReportImage;
        Button btnAssign, btnViewDetails;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWasteType = itemView.findViewById(R.id.tvWasteType);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvUserInfo = itemView.findViewById(R.id.tvUserInfo);
            ivReportImage = itemView.findViewById(R.id.ivReportImage);
            btnAssign = itemView.findViewById(R.id.btnAssign);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
        }
    }
}

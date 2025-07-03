package com.wastesmart.admin;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
 * Adapter for admin reports that matches the collector report adapter functionality
 */
public class AdminReportAdapter extends RecyclerView.Adapter<AdminReportAdapter.ReportViewHolder> {

    private static final String TAG = "AdminReportAdapter";
    
    private Context context;
    private List<WasteReport> reportsList;
    private OnReportClickListener listener;
    private SimpleDateFormat dateFormat;

    public interface OnReportClickListener {
        void onReportClick(WasteReport report, int position);
        void onAssignClick(WasteReport report, int position);
    }

    public AdminReportAdapter(Context context, List<WasteReport> reportsList, OnReportClickListener listener) {
        this.context = context;
        this.reportsList = reportsList;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        
        Log.d(TAG, "AdminReportAdapter initialized with " + 
            (reportsList != null ? reportsList.size() : 0) + " reports");
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_report, parent, false);
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
        
        // Set title with waste size
        String title = "Waste Report";
        if (report.getWasteSize() != null && !report.getWasteSize().isEmpty()) {
            title = report.getWasteSize() + " Size Waste";
        }
        holder.tvTitle.setText(title);
        
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
            holder.tvDate.setText("🕒 " + dateFormat.format(date));
        } else {
            holder.tvDate.setText("🕒 Date not available");
        }
        
        // Set user info
        setUserInfo(holder, report);
        
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
        
        // Load image if available
        if (report.getPhotoUrl() != null && !report.getPhotoUrl().isEmpty()) {
            holder.ivReportImage.setVisibility(View.VISIBLE);
            Picasso.get()
                .load(report.getPhotoUrl())
                .placeholder(R.drawable.ic_photo_placeholder)
                .error(R.drawable.ic_photo_error)
                .into(holder.ivReportImage);
                
            // Make image clickable to view fullscreen
            final String photoUrl = report.getPhotoUrl();
            holder.ivReportImage.setOnClickListener(v -> {
                Intent fullscreenIntent = new Intent(context, FullscreenImageActivity.class);
                fullscreenIntent.putExtra("imageUrl", photoUrl);
                context.startActivity(fullscreenIntent);
            });
        } else {
            holder.ivReportImage.setVisibility(View.GONE);
        }
        
        // Set button visibility based on status
        if ("COMPLETED".equalsIgnoreCase(status)) {
            holder.btnAssign.setVisibility(View.GONE);
        } else {
            holder.btnAssign.setVisibility(View.VISIBLE);
            
            // Set appropriate button text based on status
            if ("PENDING".equalsIgnoreCase(status)) {
                holder.btnAssign.setText("Assign");
            } else if ("ASSIGNED".equalsIgnoreCase(status) || "IN_PROGRESS".equalsIgnoreCase(status)) {
                holder.btnAssign.setText("Update Status");
            }
        }
        
        // Set click listeners
        holder.btnViewDetails.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReportClick(report, position);
            }
        });
        
        holder.btnAssign.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAssignClick(report, position);
            }
        });
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReportClick(report, position);
            }
        });
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
    
    // Helper method to get user info and display it
    private void setUserInfo(ReportViewHolder holder, WasteReport report) {
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
    }
    
    // Helper method to get item at position safely
    public WasteReport getItem(int position) {
        if (reportsList != null && position >= 0 && position < reportsList.size()) {
            return reportsList.get(position);
        }
        return null;
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView tvWasteType, tvTitle, tvDescription, tvLocation, tvDate, tvStatus, tvUserInfo;
        ImageView ivReportImage;
        MaterialButton btnViewDetails, btnAssign;
        
        ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWasteType = itemView.findViewById(R.id.tvWasteType);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvUserInfo = itemView.findViewById(R.id.tvUserInfo);
            ivReportImage = itemView.findViewById(R.id.ivReportImage);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            btnAssign = itemView.findViewById(R.id.btnAssign);
        }
    }
}

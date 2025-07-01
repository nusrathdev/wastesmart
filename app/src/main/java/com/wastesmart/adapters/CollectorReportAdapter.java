package com.wastesmart.adapters;

import android.content.Context;
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
            
            int statusColor;
            
            switch (status.toUpperCase()) {
                case "COMPLETED":
                    statusColor = ContextCompat.getColor(context, R.color.success);
                    break;
                case "IN_PROGRESS":
                    statusColor = ContextCompat.getColor(context, R.color.status_in_progress);
                    break;
                case "PENDING":
                    statusColor = ContextCompat.getColor(context, R.color.status_pending);
                    break;
                case "ASSIGNED":
                    statusColor = ContextCompat.getColor(context, R.color.status_assigned);
                    break;
                default:
                    statusColor = ContextCompat.getColor(context, R.color.primary);
                    break;
            }
            
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
        } else {
            holder.ivReportImage.setVisibility(View.GONE);
        }
        
        // Set button visibility based on status
        if ("COMPLETED".equalsIgnoreCase(status) || "CANCELED".equalsIgnoreCase(status)) {
            holder.btnUpdateStatus.setVisibility(View.GONE);
        } else {
            holder.btnUpdateStatus.setVisibility(View.VISIBLE);
        }
        
        // Set click listeners
        holder.btnViewDetails.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReportClick(report, position);
            }
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
        
        // Set user info
        setUserInfo(holder, report);
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

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView tvWasteType, tvTitle, tvDescription, tvLocation, tvDate, tvStatus, tvUserInfo;
        ImageView ivReportImage;
        MaterialButton btnViewDetails, btnUpdateStatus;
        
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
            btnUpdateStatus = itemView.findViewById(R.id.btnUpdateStatus);
        }
    }
}
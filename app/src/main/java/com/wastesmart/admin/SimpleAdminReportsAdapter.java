package com.wastesmart.admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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
        WasteReport report = reports.get(position);

        holder.tvWasteType.setText(report.getWasteType());
        holder.tvSize.setText(report.getSize());
        
        // Set location using coordinates
        String location = String.format(Locale.getDefault(), "%.6f, %.6f", 
                report.getLatitude(), report.getLongitude());
        holder.tvLocation.setText(location);
        
        // Use shorter description display for dashboard
        String description = report.getDescription();
        if (description != null && description.length() > 50) {
            description = description.substring(0, 47) + "...";
        }
        holder.tvDescription.setText(description);
        
        // Format timestamp
        if (report.getTimestamp() != null) {
            holder.tvTimestamp.setText(dateFormat.format(new Date(report.getTimestamp())));
        }

        // Set status
        String status = report.getStatus() != null ? report.getStatus() : "pending";
        holder.tvStatus.setText(status.toUpperCase());
        holder.tvStatus.setTextColor(context.getResources().getColor(R.color.error, null));
        
        // Hide assigned collector info
        holder.tvAssignedTo.setVisibility(View.GONE);
        
        // Show image indicator if available
        if (report.getImageUrl() != null && !report.getImageUrl().isEmpty()) {
            holder.ivPhoto.setVisibility(View.VISIBLE);
            holder.ivPhoto.setImageResource(R.drawable.ic_photo_placeholder);
        } else {
            holder.ivPhoto.setVisibility(View.GONE);
        }

        // Setup button listeners for quick assignment
        holder.btnAssign.setOnClickListener(v -> {
            context.assignReportToCollector(report.getId());
        });
        
        // Only show assign button for pending reports
        holder.btnAssign.setVisibility(View.VISIBLE);
        holder.btnComplete.setVisibility(View.GONE);
        holder.btnAssign.setText("ASSIGN");
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView tvWasteType, tvSize, tvLocation, tvDescription, tvTimestamp, tvStatus, tvAssignedTo;
        ImageView ivPhoto;
        Button btnAssign, btnComplete;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWasteType = itemView.findViewById(R.id.tvWasteType);
            tvSize = itemView.findViewById(R.id.tvSize);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvAssignedTo = itemView.findViewById(R.id.tvAssignedTo);
            ivPhoto = itemView.findViewById(R.id.ivPhoto);
            btnAssign = itemView.findViewById(R.id.btnAssign);
            btnComplete = itemView.findViewById(R.id.btnComplete);
        }
    }
}

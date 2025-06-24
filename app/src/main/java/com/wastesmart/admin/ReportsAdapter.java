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

public class ReportsAdapter extends RecyclerView.Adapter<ReportsAdapter.ReportViewHolder> {

    private List<WasteReport> reports;
    private ManageReportsActivity context;
    private SimpleDateFormat dateFormat;

    public ReportsAdapter(List<WasteReport> reports, ManageReportsActivity context) {
        this.reports = reports;
        this.context = context;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_waste_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        WasteReport report = reports.get(position);

        holder.tvWasteType.setText(report.getWasteType());
        holder.tvSize.setText(report.getSize());
        holder.tvLocation.setText(String.format(Locale.getDefault(), "%.6f, %.6f", 
                report.getLatitude(), report.getLongitude()));
        holder.tvDescription.setText(report.getDescription());
        
        // Format timestamp
        if (report.getTimestamp() != null) {
            holder.tvTimestamp.setText(dateFormat.format(new Date(report.getTimestamp())));
        }

        // Set status
        String status = report.getStatus() != null ? report.getStatus() : "Pending";
        holder.tvStatus.setText(status);
        
        // Set status color
        int statusColor;
        switch (status.toLowerCase()) {
            case "completed":
                statusColor = context.getResources().getColor(R.color.success, null);
                break;
            case "in_progress":
                statusColor = context.getResources().getColor(R.color.warning, null);
                break;
            case "assigned":
                statusColor = context.getResources().getColor(R.color.primary, null);
                break;
            default:
                statusColor = context.getResources().getColor(R.color.error, null);
                break;
        }
        holder.tvStatus.setTextColor(statusColor);        // Show image indicator if available
        if (report.getImageUrl() != null && !report.getImageUrl().isEmpty()) {
            holder.ivPhoto.setVisibility(View.VISIBLE);
            // Simple placeholder - actual image loading can be implemented later
            holder.ivPhoto.setImageResource(R.drawable.ic_photo_placeholder);
        } else {
            holder.ivPhoto.setVisibility(View.GONE);
        }

        // Setup button listeners
        holder.btnAssign.setOnClickListener(v -> {
            context.updateReportStatus(report.getId(), "assigned");
        });

        holder.btnComplete.setOnClickListener(v -> {
            context.updateReportStatus(report.getId(), "completed");
        });

        // Hide buttons based on current status
        if ("completed".equals(status.toLowerCase())) {
            holder.btnAssign.setVisibility(View.GONE);
            holder.btnComplete.setVisibility(View.GONE);
        } else if ("assigned".equals(status.toLowerCase()) || "in_progress".equals(status.toLowerCase())) {
            holder.btnAssign.setVisibility(View.GONE);
            holder.btnComplete.setVisibility(View.VISIBLE);
        } else {
            holder.btnAssign.setVisibility(View.VISIBLE);
            holder.btnComplete.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView tvWasteType, tvSize, tvLocation, tvDescription, tvTimestamp, tvStatus;
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
            ivPhoto = itemView.findViewById(R.id.ivPhoto);
            btnAssign = itemView.findViewById(R.id.btnAssign);
            btnComplete = itemView.findViewById(R.id.btnComplete);
        }
    }
}

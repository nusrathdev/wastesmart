package com.wastesmart.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.wastesmart.R;
import com.wastesmart.models.Report;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    private Context context;
    private List<Report> reportsList;
    private OnReportClickListener listener;
    private SimpleDateFormat dateFormat;

    public interface OnReportClickListener {
        void onReportClick(Report report, int position);
    }

    public ReportAdapter(Context context, List<Report> reportsList, OnReportClickListener listener) {
        this.context = context;
        this.reportsList = reportsList;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        Report report = reportsList.get(position);
        
        // Set waste type
        holder.tvReportType.setText(report.getWasteType());
        
        // Set title
        holder.tvReportTitle.setText(report.getTitle());
        
        // Set location
        holder.tvReportLocation.setText("📍 " + report.getLocation());
        
        // Set date
        if (report.getReportDate() > 0) {
            Date date = new Date(report.getReportDate());
            holder.tvReportDate.setText("🕒 " + dateFormat.format(date));
        } else {
            holder.tvReportDate.setText("🕒 Date not available");
        }
        
        // Set status with appropriate styling
        String status = report.getStatus();
        holder.tvReportStatus.setText(status);
        
        int statusColor;
        int statusBgResource;
        
        switch(status.toLowerCase()) {
            case "completed":
                statusColor = ContextCompat.getColor(context, R.color.status_completed);
                statusBgResource = R.drawable.status_completed_bg;
                break;
            case "in_progress":
                statusColor = ContextCompat.getColor(context, R.color.status_in_progress);
                statusBgResource = R.drawable.status_in_progress_bg;
                break;
            default: // pending or other
                statusColor = ContextCompat.getColor(context, R.color.status_assigned);
                statusBgResource = R.drawable.status_assigned_bg;
                break;
        }
        
        holder.tvReportStatus.setTextColor(statusColor);
        holder.tvReportStatus.setBackground(ContextCompat.getDrawable(context, statusBgResource));
        
        // Set button visibility based on status
        if ("completed".equalsIgnoreCase(status)) {
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
            // For now, just inform of the action
            if (listener != null) {
                listener.onReportClick(report, position);
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
        return reportsList.size();
    }

    public static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView tvReportType, tvReportTitle, tvReportLocation, tvReportDate, tvReportStatus;
        MaterialButton btnViewDetails, btnUpdateStatus;
        
        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReportType = itemView.findViewById(R.id.tvReportType);
            tvReportTitle = itemView.findViewById(R.id.tvReportTitle);
            tvReportLocation = itemView.findViewById(R.id.tvReportLocation);
            tvReportDate = itemView.findViewById(R.id.tvReportDate);
            tvReportStatus = itemView.findViewById(R.id.tvReportStatus);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            btnUpdateStatus = itemView.findViewById(R.id.btnUpdateStatus);
        }
    }
}

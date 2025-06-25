package com.wastesmart.user;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
        
        // Update status badge with emoji and background color
        switch (status) {
            case "completed":
                holder.tvStatus.setText("COMPLETED");
                holder.tvStatus.setBackgroundResource(R.drawable.status_completed_circle_bg);
                holder.tvStatus.setTextColor(context.getResources().getColor(R.color.status_completed, null));
                holder.layoutCollectorInfo.setVisibility(View.VISIBLE);
                holder.layoutProgress.setVisibility(View.GONE);
                holder.layoutCompletionInfo.setVisibility(View.VISIBLE);
                break;
            case "in_progress":
                holder.tvStatus.setText("IN PROGRESS");
                holder.tvStatus.setBackgroundResource(R.drawable.status_in_progress_circle_bg);
                holder.tvStatus.setTextColor(context.getResources().getColor(R.color.status_in_progress, null));
                holder.layoutCollectorInfo.setVisibility(View.VISIBLE);
                holder.layoutProgress.setVisibility(View.VISIBLE);
                holder.layoutCompletionInfo.setVisibility(View.GONE);
                break;
            case "assigned":
                holder.tvStatus.setText("ASSIGNED");
                holder.tvStatus.setBackgroundResource(R.drawable.status_assigned_circle_bg);
                holder.tvStatus.setTextColor(context.getResources().getColor(R.color.status_assigned, null));
                holder.layoutCollectorInfo.setVisibility(View.VISIBLE);
                holder.layoutProgress.setVisibility(View.GONE);
                holder.layoutCompletionInfo.setVisibility(View.GONE);
                break;
            default: // pending
                holder.tvStatus.setText("PENDING");
                holder.tvStatus.setBackgroundResource(R.drawable.status_pending_circle_bg);
                holder.tvStatus.setTextColor(context.getResources().getColor(R.color.status_pending, null));
                holder.layoutCollectorInfo.setVisibility(View.GONE);
                holder.layoutProgress.setVisibility(View.GONE);
                holder.layoutCompletionInfo.setVisibility(View.GONE);
                break;
        }

        // Show assigned collector info if available
        if (report.getAssignedCollectorName() != null && !report.getAssignedCollectorName().isEmpty()) {
            holder.tvAssignedCollector.setText(report.getAssignedCollectorName());
        } else {
            holder.layoutCollectorInfo.setVisibility(View.GONE);
        }

        // Photo indicator (always visible since photo is required)
        holder.tvPhotoIndicator.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    static class UserReportViewHolder extends RecyclerView.ViewHolder {
        TextView tvWasteType, tvSize, tvDescription, tvLocation, tvTimestamp, tvStatus;
        TextView tvPhotoIndicator, tvAssignedCollector;
        LinearLayout layoutCollectorInfo, layoutProgress, layoutCompletionInfo;

        public UserReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWasteType = itemView.findViewById(R.id.tvWasteType);
            tvSize = itemView.findViewById(R.id.tvSize);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvPhotoIndicator = itemView.findViewById(R.id.tvPhotoIndicator);
            tvAssignedCollector = itemView.findViewById(R.id.tvAssignedCollector);
            layoutCollectorInfo = itemView.findViewById(R.id.layoutCollectorInfo);
            layoutProgress = itemView.findViewById(R.id.layoutProgress);
            layoutCompletionInfo = itemView.findViewById(R.id.layoutCompletionInfo);
        }
    }
}

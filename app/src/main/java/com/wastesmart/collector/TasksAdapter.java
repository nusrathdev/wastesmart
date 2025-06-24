package com.wastesmart.collector;

import android.content.Intent;
import android.net.Uri;
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

public class TasksAdapter extends RecyclerView.Adapter<TasksAdapter.TaskViewHolder> {

    private List<WasteReport> tasks;
    private CollectionTasksActivity context;
    private SimpleDateFormat dateFormat;

    public TasksAdapter(List<WasteReport> tasks, CollectionTasksActivity context) {
        this.tasks = tasks;
        this.context = context;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_collection_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        WasteReport task = tasks.get(position);

        holder.tvWasteType.setText(task.getWasteType());
        holder.tvSize.setText(task.getSize());
        holder.tvLocation.setText(String.format(Locale.getDefault(), "%.6f, %.6f", 
                task.getLatitude(), task.getLongitude()));
        holder.tvDescription.setText(task.getDescription());
        
        // Format timestamp
        if (task.getTimestamp() != null) {
            holder.tvTimestamp.setText(dateFormat.format(new Date(task.getTimestamp())));
        }

        // Set status
        String status = task.getStatus() != null ? task.getStatus() : "assigned";
        holder.tvStatus.setText(status.replace("_", " ").toUpperCase());
        
        // Set status color
        int statusColor;
        if ("in_progress".equals(status)) {
            statusColor = context.getResources().getColor(R.color.warning, null);
        } else {
            statusColor = context.getResources().getColor(R.color.primary, null);
        }
        holder.tvStatus.setTextColor(statusColor);        // Show image indicator if available
        if (task.getImageUrl() != null && !task.getImageUrl().isEmpty()) {
            holder.ivPhoto.setVisibility(View.VISIBLE);
            // Simple placeholder - actual image loading can be implemented later
            holder.ivPhoto.setImageResource(R.drawable.ic_photo_placeholder);
        } else {
            holder.ivPhoto.setVisibility(View.GONE);
        }

        // Setup button listeners
        holder.btnNavigate.setOnClickListener(v -> {
            // Open Google Maps for navigation
            String uri = String.format(Locale.getDefault(), 
                    "geo:%f,%f?q=%f,%f(Waste Collection Point)", 
                    task.getLatitude(), task.getLongitude(),
                    task.getLatitude(), task.getLongitude());
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            mapIntent.setPackage("com.google.android.apps.maps");
            
            if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(mapIntent);
            } else {
                // Fallback to browser
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, 
                        Uri.parse("https://maps.google.com/maps?q=" + task.getLatitude() + "," + task.getLongitude()));
                context.startActivity(browserIntent);
            }
        });

        if ("assigned".equals(status)) {
            holder.btnStart.setVisibility(View.VISIBLE);
            holder.btnComplete.setVisibility(View.GONE);
            holder.btnStart.setOnClickListener(v -> {
                context.markTaskInProgress(task.getId());
            });
        } else if ("in_progress".equals(status)) {
            holder.btnStart.setVisibility(View.GONE);
            holder.btnComplete.setVisibility(View.VISIBLE);
            holder.btnComplete.setOnClickListener(v -> {
                context.markTaskCompleted(task.getId());
            });
        }
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvWasteType, tvSize, tvLocation, tvDescription, tvTimestamp, tvStatus;
        ImageView ivPhoto;
        Button btnNavigate, btnStart, btnComplete;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWasteType = itemView.findViewById(R.id.tvWasteType);
            tvSize = itemView.findViewById(R.id.tvSize);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            ivPhoto = itemView.findViewById(R.id.ivPhoto);
            btnNavigate = itemView.findViewById(R.id.btnNavigate);
            btnStart = itemView.findViewById(R.id.btnStart);
            btnComplete = itemView.findViewById(R.id.btnComplete);
        }
    }
}

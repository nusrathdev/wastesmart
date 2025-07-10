package com.wastesmart.collector;

import android.content.Intent;
import android.content.Context;
import android.net.Uri;
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

public class TasksAdapter extends RecyclerView.Adapter<TasksAdapter.TaskViewHolder> {

    private List<WasteReport> tasksList;
    private Context context;
    private SimpleDateFormat dateFormat;

    public TasksAdapter(List<WasteReport> tasksList, Context context) {
        this.tasksList = tasksList;
        this.context = context;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_collection_task_new, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        WasteReport task = tasksList.get(position);

        // Set basic task information
        holder.tvWasteType.setText(task.getWasteType() != null ? task.getWasteType() : "Unknown");
        holder.tvSize.setText(task.getSize() != null ? task.getSize() : "Not specified");
        
        // Format location with emoji
        if (task.getLatitude() != 0.0 && task.getLongitude() != 0.0) {
            holder.tvLocation.setText("📍 " + task.getLatitude() + ", " + task.getLongitude());
        } else {
            holder.tvLocation.setText("📍 Location not available");
        }

        // Format timestamp with emoji
        if (task.getTimestamp() != null) {
            holder.tvTimestamp.setText("🕒 " + dateFormat.format(new Date(task.getTimestamp())));
        } else {
            holder.tvTimestamp.setText("🕒 Time not available");
        }
        
        // Set description if available
        if (task.getDescription() != null && !task.getDescription().trim().isEmpty()) {
            holder.tvDescription.setVisibility(View.VISIBLE);
            holder.tvDescription.setText(task.getDescription());
        } else {
            holder.tvDescription.setVisibility(View.GONE);
        }

        // Set status with emoji and color
        String status = task.getStatus() != null ? task.getStatus() : "assigned";
        int statusBackground;
        int statusColor;
        
        // Update status badge with emoji and background color
        String statusUpperCase = status.toUpperCase();
        switch (statusUpperCase) {
            case "COMPLETED":
                holder.tvStatus.setText("COMPLETED");
                statusBackground = R.drawable.status_completed_circle_bg;
                statusColor = context.getResources().getColor(R.color.status_completed, null);
                break;
            case "IN_PROGRESS":
                holder.tvStatus.setText("IN PROGRESS");
                statusBackground = R.drawable.status_in_progress_circle_bg;
                statusColor = context.getResources().getColor(R.color.status_in_progress, null);
                break;
            default: // ASSIGNED
                holder.tvStatus.setText("ASSIGNED");
                statusBackground = R.drawable.status_assigned_circle_bg;
                statusColor = context.getResources().getColor(R.color.status_assigned, null);
                break;
        }
        
        // Hide assigned collector info (no need to show collector name)
        holder.tvAssignedInfo.setVisibility(View.GONE);
        
        // Show assigned time if available with better formatting
        if (task.getAssignedTimestamp() != null) {
            holder.tvAssignedTime.setVisibility(View.VISIBLE);
            holder.tvAssignedTime.setText("📅 Assigned: " + dateFormat.format(new Date(task.getAssignedTimestamp())));
        } else {
            holder.tvAssignedTime.setVisibility(View.GONE);
        }
        
        // Apply status styling
        holder.tvStatus.setTextColor(statusColor);
        holder.tvStatus.setBackgroundResource(statusBackground);

        // Show image if available - try both imageUrl and photoUrl properties
        String imageUrl = null;
        if (task.getImageUrl() != null && !task.getImageUrl().isEmpty()) {
            imageUrl = task.getImageUrl();
        } else if (task.getPhotoUrl() != null && !task.getPhotoUrl().isEmpty()) {
            imageUrl = task.getPhotoUrl();
        }
            
        holder.ivPhoto.setVisibility(View.VISIBLE);
            
        if (imageUrl != null && !imageUrl.isEmpty()) {
            // Make the image visible
            holder.ivPhoto.setVisibility(View.VISIBLE);
            
            // Use Glide to load and display the image
            try {
                android.util.Log.d("TasksAdapter", "Loading image from URL: " + imageUrl + " for task ID: " + task.getId());
                com.bumptech.glide.Glide.with(context)
                    .load(imageUrl)
                    .centerCrop()
                    .into(holder.ivPhoto);
                
                // Convert dp to pixels to ensure consistent size across all devices
                int sizeInDp = 120;
                float scale = context.getResources().getDisplayMetrics().density;
                int sizeInPixels = (int) (sizeInDp * scale + 0.5f);
                
                holder.ivPhoto.getLayoutParams().width = sizeInPixels; // match the layout size of 200dp
                holder.ivPhoto.getLayoutParams().height = sizeInPixels; // match the layout size of 200dp
            } catch (Exception e) {
                // If Glide fails, fall back to placeholder
                android.util.Log.e("TasksAdapter", "Error loading image: " + e.getMessage());
                // Don't set a placeholder - just hide the ImageView
                holder.ivPhoto.setVisibility(View.GONE);
            }
        } else {
            android.util.Log.d("TasksAdapter", "No image URL available for task ID: " + task.getId());
            // Hide the ImageView when there's no image
            holder.ivPhoto.setVisibility(View.GONE);
        }

        // Set up image click listener to view full image
        final String finalImageUrl = imageUrl;
        holder.ivPhoto.setOnClickListener(v -> {
            if (finalImageUrl != null && !finalImageUrl.isEmpty()) {
                // Open image in fullscreen activity
                Intent fullscreenIntent = new Intent(context, com.wastesmart.ui.FullscreenImageActivity.class);
                fullscreenIntent.putExtra("imageUrl", finalImageUrl);
                context.startActivity(fullscreenIntent);
            } else {
                Toast.makeText(context, "No image available", Toast.LENGTH_SHORT).show();
            }
        });

        // Setup button listeners
        holder.btnNavigate.setOnClickListener(v -> {
            // Open Google Maps for navigation
            if (task.getLatitude() != 0.0 && task.getLongitude() != 0.0) {
                String uri = "geo:" + task.getLatitude() + "," + task.getLongitude() + "?q=" + 
                           task.getLatitude() + "," + task.getLongitude() + "(Waste Collection Point)";
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                mapIntent.setPackage("com.google.android.apps.maps");
                
                if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(mapIntent);
                } else {
                    // Fallback to web browser
                    String webUri = "https://www.google.com/maps?q=" + task.getLatitude() + "," + task.getLongitude();
                    Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webUri));
                    context.startActivity(webIntent);
                }
            } else {
                Toast.makeText(context, "Location not available for navigation", Toast.LENGTH_SHORT).show();
            }
        });

        holder.btnStart.setOnClickListener(v -> {
            // Update task status to in_progress (using consistent uppercase)
            if (context instanceof CollectionTasksActivity) {
                ((CollectionTasksActivity) context).updateTaskStatus(task.getId(), "IN_PROGRESS");
            }
        });

        holder.btnComplete.setOnClickListener(v -> {
            // Update task status to completed (using consistent uppercase)
            if (context instanceof CollectionTasksActivity) {
                ((CollectionTasksActivity) context).updateTaskStatus(task.getId(), "COMPLETED");
            }
        });

        // Show/hide complete button based on status (case-insensitive comparison)
        if (status.equalsIgnoreCase("in_progress")) {
            holder.btnComplete.setVisibility(View.VISIBLE);
            holder.btnStart.setVisibility(View.GONE);
        } else {
            holder.btnComplete.setVisibility(View.GONE);
            holder.btnStart.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return tasksList.size();
    }

    public void updateTasks(List<WasteReport> newTasks) {
        this.tasksList = newTasks;
        notifyDataSetChanged();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvWasteType, tvSize, tvLocation, tvTimestamp, tvDescription;
        TextView tvStatus, tvAssignedInfo, tvAssignedTime;
        ImageView ivPhoto;
        Button btnNavigate, btnStart, btnComplete;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWasteType = itemView.findViewById(R.id.tvWasteType);
            tvSize = itemView.findViewById(R.id.tvSize);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvAssignedInfo = itemView.findViewById(R.id.tvAssignedInfo);
            tvAssignedTime = itemView.findViewById(R.id.tvAssignedTime);
            ivPhoto = itemView.findViewById(R.id.ivPhoto);
            btnNavigate = itemView.findViewById(R.id.btnNavigate);
            btnStart = itemView.findViewById(R.id.btnStart);
            btnComplete = itemView.findViewById(R.id.btnComplete);
        }
    }
}
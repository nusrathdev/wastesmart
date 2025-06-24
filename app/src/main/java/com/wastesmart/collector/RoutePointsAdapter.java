package com.wastesmart.collector;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wastesmart.R;
import com.wastesmart.models.WasteReport;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RoutePointsAdapter extends RecyclerView.Adapter<RoutePointsAdapter.RoutePointViewHolder> {

    private List<WasteReport> routePoints;
    private RouteMapActivity context;
    private SimpleDateFormat dateFormat;

    public RoutePointsAdapter(List<WasteReport> routePoints, RouteMapActivity context) {
        this.routePoints = routePoints;
        this.context = context;
        this.dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public RoutePointViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_route_point, parent, false);
        return new RoutePointViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoutePointViewHolder holder, int position) {
        WasteReport routePoint = routePoints.get(position);

        holder.tvRouteNumber.setText(String.valueOf(position + 1));
        holder.tvWasteType.setText(routePoint.getWasteType());
        holder.tvSize.setText(routePoint.getSize());
        holder.tvLocation.setText(String.format(Locale.getDefault(), "%.6f, %.6f", 
                routePoint.getLatitude(), routePoint.getLongitude()));
        
        // Format timestamp
        if (routePoint.getTimestamp() != null) {
            holder.tvTime.setText(dateFormat.format(new Date(routePoint.getTimestamp())));
        }

        // Set status
        String status = routePoint.getStatus() != null ? routePoint.getStatus() : "assigned";
        holder.tvStatus.setText(status.replace("_", " ").toUpperCase());
        
        // Set status color
        int statusColor;
        if ("in_progress".equals(status)) {
            statusColor = context.getResources().getColor(R.color.warning, null);
        } else if ("completed".equals(status)) {
            statusColor = context.getResources().getColor(R.color.success, null);
        } else {
            statusColor = context.getResources().getColor(R.color.primary, null);
        }
        holder.tvStatus.setTextColor(statusColor);

        // Setup navigate button
        holder.btnNavigate.setOnClickListener(v -> {
            // Open Google Maps for navigation
            String uri = String.format(Locale.getDefault(), 
                    "google.navigation:q=%f,%f", 
                    routePoint.getLatitude(), routePoint.getLongitude());
            Intent navIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            navIntent.setPackage("com.google.android.apps.maps");
            
            if (navIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(navIntent);
            } else {
                // Fallback to directions
                String fallbackUri = String.format(Locale.getDefault(), 
                        "https://www.google.com/maps/dir/?api=1&destination=%f,%f", 
                        routePoint.getLatitude(), routePoint.getLongitude());
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUri));
                context.startActivity(browserIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return routePoints.size();
    }

    static class RoutePointViewHolder extends RecyclerView.ViewHolder {
        TextView tvRouteNumber, tvWasteType, tvSize, tvLocation, tvTime, tvStatus;
        Button btnNavigate;

        public RoutePointViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRouteNumber = itemView.findViewById(R.id.tvRouteNumber);
            tvWasteType = itemView.findViewById(R.id.tvWasteType);
            tvSize = itemView.findViewById(R.id.tvSize);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnNavigate = itemView.findViewById(R.id.btnNavigate);
        }
    }
}

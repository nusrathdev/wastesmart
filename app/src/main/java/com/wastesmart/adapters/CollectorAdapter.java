package com.wastesmart.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.wastesmart.R;
import com.wastesmart.models.Collector;

import java.util.List;

public class CollectorAdapter extends RecyclerView.Adapter<CollectorAdapter.CollectorViewHolder> {

    private List<Collector> collectors;
    private Context context;
    private OnCollectorClickListener listener;

    public interface OnCollectorClickListener {
        void onEditCollector(Collector collector);
        // Removed onViewTasks method
    }

    public CollectorAdapter(Context context, List<Collector> collectors) {
        this.context = context;
        this.collectors = collectors;
    }

    public void setOnCollectorClickListener(OnCollectorClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CollectorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_collector, parent, false);
        return new CollectorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CollectorViewHolder holder, int position) {
        Collector collector = collectors.get(position);
        holder.bind(collector);
    }

    @Override
    public int getItemCount() {
        return collectors.size();
    }

    public void updateCollectors(List<Collector> newCollectors) {
        this.collectors = newCollectors;
        notifyDataSetChanged();
    }

    class CollectorViewHolder extends RecyclerView.ViewHolder {
        TextView tvCollectorName, tvCollectorEmail, tvCollectorPhone, tvCollectorArea, 
                 tvCompletedTasks, tvAssignedTasks, tvInProgressTasks;
        MaterialButton btnEditCollector;

        public CollectorViewHolder(@NonNull View itemView) {
            super(itemView);
            
            tvCollectorName = itemView.findViewById(R.id.tvCollectorName);
            tvCollectorEmail = itemView.findViewById(R.id.tvCollectorEmail);
            tvCollectorPhone = itemView.findViewById(R.id.tvCollectorPhone);
            tvCollectorArea = itemView.findViewById(R.id.tvCollectorArea);
            tvCompletedTasks = itemView.findViewById(R.id.tvCompletedTasks);
            tvAssignedTasks = itemView.findViewById(R.id.tvAssignedTasks);
            tvInProgressTasks = itemView.findViewById(R.id.tvInProgressTasks);
            btnEditCollector = itemView.findViewById(R.id.btnEditCollector);
        }

        public void bind(Collector collector) {
            tvCollectorName.setText(collector.getName());
            tvCollectorEmail.setText(collector.getEmail());
            tvCollectorPhone.setText(collector.getPhoneNumber());
            tvCollectorArea.setText(collector.getAssignedArea());
            
            // Load and display performance stats from database
            loadCollectorStats(collector.getId());
            
            // Set click listeners
            btnEditCollector.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditCollector(collector);
                }
            });
        }
        
        private void loadCollectorStats(String collectorId) {
            if (collectorId == null) {
                setDefaultStats();
                return;
            }
            
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("waste_reports")
                    .whereEqualTo("assignedCollectorId", collectorId)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        int completed = 0;
                        int assigned = 0;
                        int inProgress = 0;
                        
                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            String status = doc.getString("status");
                            if ("COMPLETED".equals(status)) {
                                completed++;
                            } else if ("ASSIGNED".equals(status)) {
                                assigned++;
                            } else if ("IN_PROGRESS".equals(status)) {
                                inProgress++;
                            }
                        }
                        
                        tvCompletedTasks.setText(String.valueOf(completed));
                        tvAssignedTasks.setText(String.valueOf(assigned));
                        tvInProgressTasks.setText(String.valueOf(inProgress));
                    })
                    .addOnFailureListener(e -> setDefaultStats());
        }
        
        private void setDefaultStats() {
            tvCompletedTasks.setText("0");
            tvAssignedTasks.setText("0");
            tvInProgressTasks.setText("0");
        }
    }
}

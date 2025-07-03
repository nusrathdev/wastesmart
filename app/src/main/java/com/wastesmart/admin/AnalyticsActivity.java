package com.wastesmart.admin;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.firestore.FirebaseFirestore;
import com.wastesmart.R;
import com.wastesmart.databinding.ActivityAnalyticsBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class AnalyticsActivity extends BaseAdminActivity {

    private static final String TAG = "AnalyticsActivity";
    private ActivityAnalyticsBinding binding;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAnalyticsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup toolbar
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("Analytics");

        // Setup bottom navigation
        setupBottomNavigation();

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        
        // Show loading state
        showLoading(true);
        
        // Load analytics data
        loadAnalyticsData();
    }
    
    private void showLoading(boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.scrollContent.setVisibility(View.GONE);
        } else {
            binding.progressBar.setVisibility(View.GONE);
            binding.scrollContent.setVisibility(View.VISIBLE);
        }
    }
    
    private void loadAnalyticsData() {
        AtomicInteger pendingCount = new AtomicInteger(0);
        AtomicInteger inProgressCount = new AtomicInteger(0);
        AtomicInteger completedCount = new AtomicInteger(0);
        AtomicInteger assignedCount = new AtomicInteger(0);
        
        // Load reports by status for pie chart
        db.collection("waste_reports")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Process report data
                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                        String status = doc.getString("status");
                        if (status != null) {
                            status = status.toUpperCase();
                            if (status.equals("PENDING")) {
                                pendingCount.incrementAndGet();
                            } else if (status.equals("IN_PROGRESS")) {
                                inProgressCount.incrementAndGet();
                            } else if (status.equals("COMPLETED")) {
                                completedCount.incrementAndGet();
                            } else if (status.equals("ASSIGNED")) {
                                assignedCount.incrementAndGet();
                            }
                        }
                    }
                    
                    // Update counters
                    binding.tvTotalReports.setText(String.valueOf(queryDocumentSnapshots.size()));
                    binding.tvPendingCount.setText(String.valueOf(pendingCount.get()));
                    binding.tvCompletedCount.setText(String.valueOf(completedCount.get()));
                    binding.tvAssignedCount.setText(String.valueOf(assignedCount.get()));
                    binding.tvInProgressCount.setText(String.valueOf(inProgressCount.get()));
                    
                    // Set up pie chart
                    setupStatusPieChart(pendingCount.get(), inProgressCount.get(), 
                            completedCount.get(), assignedCount.get());
                    
                    // Load daily reports for bar chart
                    loadDailyReportsData();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading reports data", e);
                    Toast.makeText(this, "Failed to load analytics data", Toast.LENGTH_SHORT).show();
                    showLoading(false);
                });
    }
    
    private void loadDailyReportsData() {
        // Get data for the last 7 days
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        
        // Go back 6 days to get 7 days total including today
        calendar.add(Calendar.DAY_OF_YEAR, -6);
        
        // Create a map to store counts by day
        Map<String, Integer> dailyCounts = new HashMap<>();
        List<String> dateLabels = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
        
        // Initialize the map with 0 counts for each day
        for (int i = 0; i < 7; i++) {
            String dateStr = dateFormat.format(calendar.getTime());
            dailyCounts.put(dateStr, 0);
            dateLabels.add(dateStr);
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        
        // Reset calendar to 7 days ago for the query
        calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        long sevenDaysAgo = calendar.getTimeInMillis();
        
        // Query reports in the last 7 days
        db.collection("waste_reports")
                .whereGreaterThan("timestamp", sevenDaysAgo)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Process each report
                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                        Long timestamp = doc.getLong("timestamp");
                        if (timestamp != null) {
                            Date reportDate = new Date(timestamp);
                            String dateStr = dateFormat.format(reportDate);
                            
                            // Increment count for this date
                            if (dailyCounts.containsKey(dateStr)) {
                                dailyCounts.put(dateStr, dailyCounts.get(dateStr) + 1);
                            }
                        }
                    }
                    
                    // Set up bar chart
                    setupDailyReportsBarChart(dateLabels, dailyCounts);
                    
                    // Loading complete
                    showLoading(false);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading daily reports data", e);
                    showLoading(false);
                });
    }
    
    private void setupStatusPieChart(int pending, int inProgress, int completed, int assigned) {
        PieChart pieChart = binding.chartReportStatus;
        
        // Create entries
        List<PieEntry> entries = new ArrayList<>();
        if (pending > 0) entries.add(new PieEntry(pending, "Pending"));
        if (inProgress > 0) entries.add(new PieEntry(inProgress, "In Progress"));
        if (completed > 0) entries.add(new PieEntry(completed, "Completed"));
        if (assigned > 0) entries.add(new PieEntry(assigned, "Assigned"));
        
        // If no data, add a placeholder
        if (entries.isEmpty()) {
            entries.add(new PieEntry(1, "No Data"));
        }
        
        // Create dataset
        PieDataSet dataSet = new PieDataSet(entries, "Report Status");
        dataSet.setColors(
            Color.parseColor("#FF9800"), // Orange for Pending
            Color.parseColor("#2196F3"), // Blue for In Progress
            Color.parseColor("#4CAF50"), // Green for Completed
            Color.parseColor("#673AB7")  // Purple for Assigned
        );
        
        // Format chart
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);
        
        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);
        pieChart.animateY(1000);
        pieChart.invalidate(); // refresh
    }
    
    private void setupDailyReportsBarChart(List<String> dateLabels, Map<String, Integer> dailyCounts) {
        BarChart barChart = binding.chartDailyReports;
        
        // Create entries
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < dateLabels.size(); i++) {
            String dateLabel = dateLabels.get(i);
            int count = dailyCounts.getOrDefault(dateLabel, 0);
            entries.add(new BarEntry(i, count));
        }
        
        // Create dataset
        BarDataSet dataSet = new BarDataSet(entries, "Daily Reports");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(10f);
        
        // Format chart
        BarData data = new BarData(dataSet);
        barChart.setData(data);
        barChart.getDescription().setEnabled(false);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(dateLabels));
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setGranularity(1f);
        barChart.getXAxis().setLabelRotationAngle(45);
        barChart.setDrawGridBackground(false);
        barChart.animateY(1000);
        barChart.invalidate(); // refresh
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected int getActiveItemIndex() {
        // Analytics tab is active (index 3)
        return 3;
    }
}

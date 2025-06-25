package com.wastesmart.collector;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.wastesmart.R;
import com.wastesmart.databinding.ActivityRouteMapBinding;
import com.wastesmart.models.WasteReport;

import java.util.ArrayList;
import java.util.List;

public class RouteMapActivity extends AppCompatActivity {

    private static final String TAG = "RouteMap";
    private ActivityRouteMapBinding binding;
    private FirebaseFirestore db;
    private RoutePointsAdapter adapter;
    private List<WasteReport> routePoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRouteMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Setup toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Today's Routes");
        }

        // Setup RecyclerView for route points
        routePoints = new ArrayList<>();
        adapter = new RoutePointsAdapter(routePoints, this);
        binding.recyclerViewRoutes.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewRoutes.setAdapter(adapter);

        // Load today's collection routes
        loadTodaysRoutes();

        // TODO: Initialize Google Maps
        // TODO: Display routes on map
        
        binding.tvMapPlaceholder.setText("📍 Map Integration Coming Soon\n\nBelow are today's collection points:");
    }

    private void loadTodaysRoutes() {
        binding.progressBar.setVisibility(View.VISIBLE);

        // Simple approach: Load all reports and filter client-side
        // This avoids any Firestore index requirements
        db.collection("waste_reports")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    binding.progressBar.setVisibility(View.GONE);
                    routePoints.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            WasteReport route = document.toObject(WasteReport.class);
                            route.setId(document.getId());
                            
                            // Filter for assigned and in_progress tasks only
                            String status = route.getStatus();
                            if ("assigned".equals(status) || "in_progress".equals(status)) {
                                routePoints.add(route);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing route: " + document.getId(), e);
                        }
                    }

                    // Sort by timestamp (oldest first for route planning)
                    routePoints.sort((r1, r2) -> {
                        Long ts1 = r1.getTimestamp();
                        Long ts2 = r2.getTimestamp();
                        if (ts1 == null && ts2 == null) return 0;
                        if (ts1 == null) return 1;
                        if (ts2 == null) return -1;
                        return ts1.compareTo(ts2); // Ascending order (oldest first)
                    });

                    adapter.notifyDataSetChanged();

                    if (routePoints.isEmpty()) {
                        binding.tvNoRoutes.setVisibility(View.VISIBLE);
                        binding.recyclerViewRoutes.setVisibility(View.GONE);
                    } else {
                        binding.tvNoRoutes.setVisibility(View.GONE);
                        binding.recyclerViewRoutes.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error loading routes", e);
                    Toast.makeText(this, "Error loading routes: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

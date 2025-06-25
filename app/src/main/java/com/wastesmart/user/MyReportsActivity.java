package com.wastesmart.user;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.wastesmart.R;
import com.wastesmart.models.WasteReport;

import java.util.ArrayList;
import java.util.List;

public class MyReportsActivity extends AppCompatActivity {

    private static final String TAG = "MyReports";
    private RecyclerView recyclerViewReports;
    private ProgressBar progressBar;
    private LinearLayout layoutEmptyState;
    private MaterialButton btnSubmitFirstReport;
    
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private UserReportsAdapter adapter;
    private List<WasteReport> reportsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reports);

        // Setup toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Reports");
        }

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Check if user is authenticated
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in to view your reports", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        recyclerViewReports = findViewById(R.id.recyclerViewMyReports);
        progressBar = findViewById(R.id.progressBar);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        btnSubmitFirstReport = findViewById(R.id.btnSubmitFirstReport);

        // Setup RecyclerView
        reportsList = new ArrayList<>();
        adapter = new UserReportsAdapter(reportsList, this);
        recyclerViewReports.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewReports.setAdapter(adapter);

        // Setup button click listener for first report
        btnSubmitFirstReport.setOnClickListener(v -> {
            Intent intent = new Intent(MyReportsActivity.this, ReportWasteActivity.class);
            startActivity(intent);
        });

        // Load user's reports
        loadUserReports();
    }

    private void loadUserReports() {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmptyState.setVisibility(View.GONE);
        recyclerViewReports.setVisibility(View.GONE);

        String currentUserId = mAuth.getCurrentUser().getUid();
        Log.d(TAG, "Loading reports for user: " + currentUserId);

        // Use a simpler query without orderBy to avoid index requirement
        db.collection("waste_reports")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
                    reportsList.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            WasteReport report = document.toObject(WasteReport.class);
                            report.setId(document.getId());
                            reportsList.add(report);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing report: " + document.getId(), e);
                        }
                    }

                    // Sort the list manually by timestamp (newest first)
                    reportsList.sort((r1, r2) -> {
                        Long t1 = r1.getTimestamp();
                        Long t2 = r2.getTimestamp();
                        if (t1 == null && t2 == null) return 0;
                        if (t1 == null) return 1;
                        if (t2 == null) return -1;
                        return t2.compareTo(t1); // Descending order (newest first)
                    });

                    adapter.notifyDataSetChanged();

                    if (reportsList.isEmpty()) {
                        layoutEmptyState.setVisibility(View.VISIBLE);
                        recyclerViewReports.setVisibility(View.GONE);
                        Log.d(TAG, "No reports found for user");
                    } else {
                        layoutEmptyState.setVisibility(View.GONE);
                        recyclerViewReports.setVisibility(View.VISIBLE);
                        Log.d(TAG, "Loaded " + reportsList.size() + " reports for user");
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    layoutEmptyState.setVisibility(View.VISIBLE);
                    recyclerViewReports.setVisibility(View.GONE);
                    Log.e(TAG, "Error loading user reports", e);
                    
                    // Show user-friendly error message
                    String errorMessage = "Unable to load your reports. Please check your connection and try again.";
                    if (e.getMessage() != null && e.getMessage().contains("index")) {
                        errorMessage = "Setting up database... Please try again in a moment.";
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh reports when returning to this activity (e.g., after submitting a new report)
        loadUserReports();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

package com.wastesmart.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.wastesmart.R;

public class WasteLocationMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "WasteLocationMap";
    private GoogleMap mMap;
    private double latitude = 0.0;
    private double longitude = 0.0;
    private String wasteTitle = "Waste Location";
    private String wasteDescription = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waste_location_map);

        // Get location data from intent
        Intent intent = getIntent();
        if (intent != null) {
            latitude = intent.getDoubleExtra("latitude", 0.0);
            longitude = intent.getDoubleExtra("longitude", 0.0);
            wasteTitle = intent.getStringExtra("title") != null ? 
                intent.getStringExtra("title") : "Waste Location";
            wasteDescription = intent.getStringExtra("description") != null ?
                intent.getStringExtra("description") : "";
        }

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Waste Location");
        }
        
        // Setup report details
        setupReportDetails(intent);

        // Get the SupportMapFragment and request notification when the map is ready
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Check if we have valid coordinates
        if (latitude == 0.0 && longitude == 0.0) {
            Toast.makeText(this, "Invalid location coordinates", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add marker at the waste location
        LatLng wasteLocation = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions()
                .position(wasteLocation)
                .title(wasteTitle)
                .snippet(wasteDescription));
        
        // Move camera to the location with appropriate zoom
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(wasteLocation, 15));
    }

    /**
     * Sets up the report details card with information from the intent
     */
    private void setupReportDetails(Intent intent) {
        if (intent == null) return;
        
        TextView tvReportTitle = findViewById(R.id.tvReportTitle);
        TextView tvWasteType = findViewById(R.id.tvWasteType);
        TextView tvDescription = findViewById(R.id.tvDescription);
        TextView tvStatus = findViewById(R.id.tvStatus);
        
        // Set report title
        tvReportTitle.setText("Waste Report Details");
        
        // Get waste type directly from intent
        String wasteType = intent.getStringExtra("wasteType");
        
        String description = intent.getStringExtra("description");
        String status = intent.getStringExtra("status");
        
        // Set waste type
        if (wasteType != null && !wasteType.isEmpty()) {
            tvWasteType.setText("Waste Type: " + wasteType);
        } else {
            // Extract wasteType directly if passed separately
            String directWasteType = intent.getStringExtra("wasteType");
            if (directWasteType != null && !directWasteType.isEmpty()) {
                tvWasteType.setText("Waste Type: " + directWasteType);
            } else {
                tvWasteType.setText("Waste Type");
            }
        }
        
        // Set description
        if (description != null && !description.isEmpty()) {
            tvDescription.setText(description);
        } else {
            tvDescription.setText("No description available");
        }
        
        // Set status with appropriate background
        if (status != null && !status.isEmpty()) {
            tvStatus.setText(status.toUpperCase());
            
            // Set appropriate background color
            int backgroundResId;
            if (status.equalsIgnoreCase("completed")) {
                backgroundResId = R.drawable.status_completed_bg;
            } else if (status.equalsIgnoreCase("in_progress")) {
                backgroundResId = R.drawable.status_in_progress_bg;
            } else if (status.equalsIgnoreCase("assigned")) {
                backgroundResId = R.drawable.status_assigned_bg;
            } else {
                backgroundResId = R.drawable.status_pending_circle_bg;
            }
            tvStatus.setBackgroundResource(backgroundResId);
        } else {
            tvStatus.setVisibility(android.view.View.GONE); // Hide the status if not available
        }
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

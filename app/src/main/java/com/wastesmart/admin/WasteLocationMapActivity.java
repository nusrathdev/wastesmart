package com.wastesmart.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

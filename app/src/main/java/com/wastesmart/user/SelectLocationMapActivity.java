package com.wastesmart.user;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.wastesmart.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class SelectLocationMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "SelectLocationMap";
    private static final int REQUEST_LOCATION_PERMISSION = 101;
    
    private GoogleMap mMap;
    private LatLng selectedLocation;
    private Button btnConfirmLocation;
    private FusedLocationProviderClient fusedLocationClient;
    private double currentLatitude = 0.0;
    private double currentLongitude = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_location_map);

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        
        // Get current location from intent if passed
        Intent intent = getIntent();
        if (intent.hasExtra("current_latitude") && intent.hasExtra("current_longitude")) {
            currentLatitude = intent.getDoubleExtra("current_latitude", 0.0);
            currentLongitude = intent.getDoubleExtra("current_longitude", 0.0);
            Log.d(TAG, "Received current location: " + currentLatitude + ", " + currentLongitude);
        }

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Select Location");
        }

        // Get the SupportMapFragment and request notification when the map is ready to be used
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnConfirmLocation = findViewById(R.id.btnConfirmLocation);
        btnConfirmLocation.setOnClickListener(v -> {
            if (selectedLocation != null) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("latitude", selectedLocation.latitude);
                resultIntent.putExtra("longitude", selectedLocation.longitude);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng initialLocation;
        
        // Use current location if available, otherwise use default
        if (currentLatitude != 0.0 && currentLongitude != 0.0) {
            initialLocation = new LatLng(currentLatitude, currentLongitude);
            Log.d(TAG, "Using passed current location: " + currentLatitude + ", " + currentLongitude);
        } else {
            // Try to get current location if not passed
            getCurrentLocationForMap();
            // Use a default location (you can change this to your city's coordinates)
            initialLocation = new LatLng(-1.2921, 36.8219); // Nairobi, Kenya as default
            Log.d(TAG, "Using default location, will try to get current location");
        }
        
        selectedLocation = initialLocation;
        mMap.addMarker(new MarkerOptions()
                .position(initialLocation)
                .title("Selected Location")
                .draggable(true));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 15));

        // Handle map click to update marker
        mMap.setOnMapClickListener(latLng -> {
            mMap.clear();
            selectedLocation = latLng;
            mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Selected Location"));
        });

        // Handle marker drag
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(com.google.android.gms.maps.model.Marker marker) {}

            @Override
            public void onMarkerDrag(com.google.android.gms.maps.model.Marker marker) {}

            @Override
            public void onMarkerDragEnd(com.google.android.gms.maps.model.Marker marker) {
                selectedLocation = marker.getPosition();
            }
        });
    }

    private void getCurrentLocationForMap() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
            return;
        }

        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null && mMap != null) {
                            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.clear();
                            selectedLocation = currentLocation;
                            mMap.addMarker(new MarkerOptions()
                                    .position(currentLocation)
                                    .title("Current Location")
                                    .draggable(true));
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                            Log.d(TAG, "Updated map with current location: " + location.getLatitude() + ", " + location.getLongitude());
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to get current location for map", e);
                        Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                    });
        } catch (SecurityException e) {
            Log.e(TAG, "Location permission denied", e);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocationForMap();
            } else {
                Toast.makeText(this, "Location permission is required to show your current location", Toast.LENGTH_LONG).show();
            }
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

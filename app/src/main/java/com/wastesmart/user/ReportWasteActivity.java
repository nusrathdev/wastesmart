package com.wastesmart.user;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.wastesmart.R;
import com.wastesmart.databinding.ActivityReportWasteBinding;
import com.wastesmart.models.WasteReport;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReportWasteActivity extends AppCompatActivity {

    private static final String TAG = "ReportWasteActivity";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_LOCATION_PERMISSION = 2;
    private static final int REQUEST_MAP_LOCATION = 3;
    private static final int REQUEST_CAMERA_PERMISSION = 4;
    private static final int REQUEST_GALLERY_PERMISSION = 5;
    private static final int REQUEST_PICK_IMAGE = 6;

    private ActivityReportWasteBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private FusedLocationProviderClient fusedLocationClient;

    private Uri photoUri;
    private String currentPhotoPath;
    private double latitude;
    private double longitude;
    private boolean hasLocation = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReportWasteBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // Initialize Firebase components
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // Validate Firebase initialization
        if (mAuth == null || db == null || storage == null || storageRef == null) {
            Log.e(TAG, "Firebase components not properly initialized");
            Toast.makeText(this, "Firebase initialization error", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Setup toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.report_waste);
        }

        // Setup waste type spinner
        ArrayAdapter<CharSequence> wasteTypeAdapter = ArrayAdapter.createFromResource(
                this, R.array.waste_types, android.R.layout.simple_spinner_item);
        wasteTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerWasteType.setAdapter(wasteTypeAdapter);

        // Setup waste size spinner
        ArrayAdapter<CharSequence> wasteSizeAdapter = ArrayAdapter.createFromResource(
                this, R.array.waste_sizes, android.R.layout.simple_spinner_item);
        wasteSizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerWasteSize.setAdapter(wasteSizeAdapter);

        // Button listeners
        binding.btnCurrentLocation.setOnClickListener(v -> getCurrentLocation());

        binding.btnSelectOnMap.setOnClickListener(v -> {
            Intent intent = new Intent(ReportWasteActivity.this, SelectLocationMapActivity.class);
            startActivityForResult(intent, REQUEST_MAP_LOCATION);
        });

        binding.btnAddPhoto.setOnClickListener(v -> showPhotoOptionsDialog());

        binding.btnSubmitReport.setOnClickListener(v -> validateAndSubmitReport());
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.tvLocationStatus.setText(R.string.loading);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        hasLocation = true;
                        binding.tvLocationStatus.setText(getString(R.string.location_obtained,
                                latitude, longitude));
                        binding.tvLocationStatus.setTextColor(getResources().getColor(R.color.success));
                    } else {
                        binding.tvLocationStatus.setText(R.string.unable_to_get_location);
                        binding.tvLocationStatus.setTextColor(getResources().getColor(R.color.error));
                    }
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.tvLocationStatus.setText(getString(R.string.error_location, e.getMessage()));
                    binding.tvLocationStatus.setTextColor(getResources().getColor(R.color.error));
                });
    }

    private void dispatchTakePictureIntent() {
        Log.d(TAG, "dispatchTakePictureIntent called");
        
        // Check camera permission first
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission not granted, requesting permission");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
            return;
        }

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            Log.d(TAG, "Camera app available, creating image file");
            // Create the file where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
                Log.d(TAG, "Image file created: " + photoFile.getAbsolutePath());
            } catch (IOException ex) {
                Log.e(TAG, "Error creating image file", ex);
                Toast.makeText(this, "Error creating image file: " + ex.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }

            // Continue only if the file was successfully created
            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(this,
                        "com.wastesmart.fileprovider",
                        photoFile);
                Log.d(TAG, "Photo URI: " + photoUri.toString());
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        } else {
            Log.e(TAG, "No camera app available");
            Toast.makeText(this, "No camera app available", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        
        // Use app-specific directory which doesn't require permissions
        File storageDir = new File(getExternalFilesDir(null), "WastePhotos");
        if (!storageDir.exists()) {
            boolean created = storageDir.mkdirs();
            Log.d(TAG, "Storage directory created: " + created + " at " + storageDir.getAbsolutePath());
        }
        
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        Log.d(TAG, "Created image file: " + currentPhotoPath);
        return image;
    }

    private void validateAndSubmitReport() {
        // Check if user is authenticated
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in to submit a report", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if a location has been set
        if (!hasLocation) {
            Toast.makeText(this, "Please select a location for the waste report", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if a photo has been taken or selected
        if (photoUri == null) {
            Toast.makeText(this, "Please add a photo of the waste", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Additional validation for camera photos
        if (currentPhotoPath != null) {
            File photoFile = new File(currentPhotoPath);
            if (!photoFile.exists() || photoFile.length() == 0) {
                Toast.makeText(this, "Photo file is invalid. Please take another photo.", Toast.LENGTH_SHORT).show();
                photoUri = null;
                binding.imgPreview.setVisibility(View.GONE);
                binding.tvPhotoStatus.setText("No photo added");
                binding.tvPhotoStatus.setTextColor(getResources().getColor(R.color.medium_gray));
                return;
            }
        }

        // Validate spinner selections
        if (binding.spinnerWasteType.getSelectedItem() == null || 
            binding.spinnerWasteSize.getSelectedItem() == null) {
            Toast.makeText(this, "Please select waste type and size", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get other report details
        String wasteType = binding.spinnerWasteType.getSelectedItem().toString();
        String wasteSize = binding.spinnerWasteSize.getSelectedItem().toString();
        String description = binding.etDescription.getText().toString().trim();

        // Show progress
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSubmitReport.setEnabled(false); // Prevent multiple submissions

        // First upload the image to Firebase Storage
        String userId = mAuth.getCurrentUser().getUid();
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imagePath = "waste_reports/" + userId + "/" + timestamp + ".jpg";

        Log.d(TAG, "Starting image upload to: " + imagePath);
        Log.d(TAG, "Photo URI: " + photoUri.toString());

        StorageReference imageRef = storageRef.child(imagePath);
        imageRef.putFile(photoUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d(TAG, "Image uploaded successfully");
                    // Get the download URL
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        Log.d(TAG, "Download URL obtained: " + uri.toString());
                        // Create the waste report object with the image URL
                        WasteReport report = new WasteReport(
                                null, // ID will be set by Firestore
                                userId,
                                wasteType,
                                wasteSize,
                                description,
                                latitude,
                                longitude,
                                uri.toString(),
                                "PENDING",
                                new Date());

                        Log.d(TAG, "Saving report to Firestore");
                        // Save to Firestore
                        db.collection("waste_reports").add(report)
                                .addOnSuccessListener(documentReference -> {
                                    Log.d(TAG, "Report saved successfully with ID: " + documentReference.getId());
                                    binding.progressBar.setVisibility(View.GONE);
                                    binding.btnSubmitReport.setEnabled(true);
                                    Toast.makeText(ReportWasteActivity.this,
                                            "Report submitted successfully", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error saving report to Firestore", e);
                                    binding.progressBar.setVisibility(View.GONE);
                                    binding.btnSubmitReport.setEnabled(true);
                                    Toast.makeText(ReportWasteActivity.this,
                                            "Error submitting report: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }).addOnFailureListener(e -> {
                        Log.e(TAG, "Error getting download URL", e);
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnSubmitReport.setEnabled(true);
                        Toast.makeText(ReportWasteActivity.this,
                                "Error getting image URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error uploading image to Storage", e);
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnSubmitReport.setEnabled(true);
                    Toast.makeText(ReportWasteActivity.this,
                            "Error uploading image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);
        
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Handle camera capture result
            if (photoUri != null && currentPhotoPath != null) {
                File photoFile = new File(currentPhotoPath);
                Log.d(TAG, "Photo file path: " + currentPhotoPath);
                Log.d(TAG, "Photo file exists: " + photoFile.exists());
                Log.d(TAG, "Photo file size: " + photoFile.length());
                
                if (photoFile.exists() && photoFile.length() > 0) {
                    binding.imgPreview.setVisibility(View.VISIBLE);
                    binding.imgPreview.setImageURI(photoUri);
                    binding.tvPhotoStatus.setText(R.string.photo_added);
                    binding.tvPhotoStatus.setTextColor(getResources().getColor(R.color.success));
                    Log.d(TAG, "Photo successfully captured and displayed");
                } else {
                    Log.e(TAG, "Photo file not found or empty");
                    Toast.makeText(this, "Photo file not found or empty", Toast.LENGTH_SHORT).show();
                    photoUri = null; // Reset if failed
                }
            } else {
                Log.e(TAG, "Photo URI or path is null");
                Toast.makeText(this, "Failed to capture photo", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_CANCELED) {
            Log.d(TAG, "Camera capture was cancelled");
            Toast.makeText(this, "Photo capture cancelled", Toast.LENGTH_SHORT).show();
        } else if (requestCode == REQUEST_MAP_LOCATION && resultCode == RESULT_OK && data != null) {
            latitude = data.getDoubleExtra("latitude", 0);
            longitude = data.getDoubleExtra("longitude", 0);
            hasLocation = true;
            binding.tvLocationStatus.setText(getString(R.string.location_obtained, latitude, longitude));
            binding.tvLocationStatus.setTextColor(getResources().getColor(R.color.success));
        } else if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                try {
                    // Validate that we can access the URI
                    getContentResolver().openInputStream(selectedImageUri).close();
                    photoUri = selectedImageUri;
                    currentPhotoPath = null; // Clear camera path since we're using gallery image
                    binding.imgPreview.setVisibility(View.VISIBLE);
                    binding.imgPreview.setImageURI(photoUri);
                    binding.tvPhotoStatus.setText(R.string.photo_added);
                    binding.tvPhotoStatus.setTextColor(getResources().getColor(R.color.success));
                    Log.d(TAG, "Photo selected from gallery: " + photoUri.toString());
                } catch (Exception e) {
                    Log.e(TAG, "Error accessing selected image", e);
                    Toast.makeText(this, "Cannot access selected image. Please try another image.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(TAG, "Failed to get image from gallery");
                Toast.makeText(this, "Failed to select image from gallery", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showPhotoOptionsDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Add Photo")
                .setMessage("Choose an option to add a photo")
                .setPositiveButton("Take Photo", (dialog, which) -> dispatchTakePictureIntent())
                .setNegativeButton("Choose from Gallery", (dialog, which) -> chooseFromGallery())
                .setNeutralButton("Cancel", null)
                .show();
    }

    private void chooseFromGallery() {
        // Check if we need READ_MEDIA_IMAGES permission (Android 13+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        REQUEST_GALLERY_PERMISSION);
                return;
            }
        }
        
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        if (intent.resolveActivity(getPackageManager()) != null) {
            Log.d(TAG, "Opening gallery for image selection");
            startActivityForResult(intent, REQUEST_PICK_IMAGE);
        } else {
            Log.e(TAG, "No gallery app available");
            Toast.makeText(this, "No gallery app available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult: requestCode=" + requestCode);
        
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Location permission granted");
                getCurrentLocation();
            } else {
                Log.d(TAG, "Location permission denied");
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Camera permission granted");
                dispatchTakePictureIntent();
            } else {
                Log.d(TAG, "Camera permission denied");
                Toast.makeText(this, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_GALLERY_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Gallery permission granted");
                chooseFromGallery();
            } else {
                Log.d(TAG, "Gallery permission denied");
                Toast.makeText(this, "Gallery permission is required to select photos", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

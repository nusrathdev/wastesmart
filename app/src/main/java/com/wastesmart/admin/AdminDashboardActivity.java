package com.wastesmart.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.wastesmart.R;

public class AdminDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Setup toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Admin Dashboard");
        }

        // Get admin info from intent
        String adminType = getIntent().getStringExtra("admin_type");
        String adminEmail = getIntent().getStringExtra("admin_email");

        // Display welcome message
        TextView welcomeText = findViewById(R.id.tvWelcomeAdmin);
        if (welcomeText != null) {
            welcomeText.setText("Welcome, " + (adminType != null ? adminType : "Admin"));
        }

        Toast.makeText(this, "Logged in as: " + adminEmail, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

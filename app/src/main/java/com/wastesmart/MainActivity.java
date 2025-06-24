package com.wastesmart;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.wastesmart.databinding.ActivityMainBinding;
import com.wastesmart.user.UserLoginActivity;
import com.wastesmart.user.UserRegisterActivity;
import com.wastesmart.collector.CollectorLoginActivity;
import com.wastesmart.admin.AdminLoginActivity;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // User login button
        binding.btnUserLogin.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UserLoginActivity.class);
            startActivity(intent);
        });

        // User register button
        binding.btnUserRegister.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UserRegisterActivity.class);
            startActivity(intent);
        });

        // Collector login button
        binding.btnCollectorLogin.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CollectorLoginActivity.class);
            startActivity(intent);
        });

        // Admin login button
        binding.btnAdminLogin.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AdminLoginActivity.class);
            startActivity(intent);
        });
    }
}

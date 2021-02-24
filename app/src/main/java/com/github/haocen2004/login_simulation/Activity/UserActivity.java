package com.github.haocen2004.login_simulation.Activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.github.haocen2004.login_simulation.databinding.ActivityUserBinding;

public class UserActivity extends AppCompatActivity {
    private ActivityUserBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}
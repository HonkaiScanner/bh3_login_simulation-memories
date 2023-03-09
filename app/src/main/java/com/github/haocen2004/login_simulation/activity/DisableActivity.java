package com.github.haocen2004.login_simulation.activity;

import android.os.Bundle;

import com.github.haocen2004.login_simulation.databinding.ActivityDisableBinding;

public class DisableActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityDisableBinding binding = ActivityDisableBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        activityManager.clearActivity();
    }
}
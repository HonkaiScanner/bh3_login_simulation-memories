package com.github.haocen2004.login_simulation.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.github.haocen2004.login_simulation.databinding.ActivityDisableBinding;

public class DisableActivity extends BaseActivity {
    private final Handler disableDelay = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityDisableBinding binding = ActivityDisableBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        disableDelay.postDelayed(() -> ActivityManager.getInstance().clearActivity(), 30000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        activityManager.clearActivity();
    }
}
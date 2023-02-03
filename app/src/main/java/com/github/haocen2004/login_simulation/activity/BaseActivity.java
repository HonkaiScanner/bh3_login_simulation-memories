package com.github.haocen2004.login_simulation.activity;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.haocen2004.login_simulation.utils.Logger;
import com.github.haocen2004.login_simulation.utils.PmsHooker;

public class BaseActivity extends AppCompatActivity {

    ActivityManager activityManager = ActivityManager.getInstance();
    private final String TAG = "activityManager";

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityManager.addActivity(this);
        Logger.d(TAG, "onCreate: " + getClass().getName());
    }

    @Override
    protected void onPause() {
        Logger.d(TAG, "onPause: " + getClass().getName());
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logger.d(TAG, "onResume: " + getClass().getName());
        activityManager.addActivity(this);
    }

    @Override
    protected void onStop() {
        Logger.d(TAG, "onStop: " + getClass().getName());
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        activityManager.addActivity(this);
        Logger.d(TAG, "onStart: " + getClass().getName());
    }

    @Override
    protected void onDestroy() {
        Logger.d(TAG, "onDestroy: " + getClass().getName());
        activityManager.removeActivity(this);
        super.onDestroy();
    }

    @NonNull
    @Override
    public String getOpPackageName() {
        return PmsHooker.getPackageNameFilter(super.getOpPackageName());
    }

    @Override
    public String getPackageName() {
        return PmsHooker.getPackageNameFilter(super.getPackageName());
    }

}

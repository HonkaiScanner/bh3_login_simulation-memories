package com.github.haocen2004.login_simulation.activity;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.haocen2004.login_simulation.util.PmsHooker;

public class BaseActivity extends AppCompatActivity {

    ActivityManager activityManager = ActivityManager.getInstance();

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityManager.addActivity(this);
    }

    @Override
    protected void onPause() {
        activityManager.removeActivity(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        activityManager.addActivity(this);
    }

    @Override
    protected void onStop() {
        activityManager.removeActivity(this);
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        activityManager.addActivity(this);
    }

    @Override
    protected void onDestroy() {
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

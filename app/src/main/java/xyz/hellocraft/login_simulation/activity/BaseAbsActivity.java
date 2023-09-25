package xyz.hellocraft.login_simulation.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.drakeet.about.AbsAboutActivity;

import java.util.List;

import xyz.hellocraft.login_simulation.utils.PmsHooker;

public class BaseAbsActivity extends AbsAboutActivity {


    ActivityManager activityManager = ActivityManager.getInstance();
    Activity activity;

    @Override
    protected void onCreateHeader(@NonNull ImageView icon, @NonNull TextView slogan, @NonNull TextView version) {

    }

    @Override
    protected void onItemsCreated(@NonNull List<Object> items) {

    }

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityManager.addActivity(this);
        activity = this;
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

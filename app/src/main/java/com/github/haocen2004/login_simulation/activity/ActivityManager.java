package com.github.haocen2004.login_simulation.activity;

import android.app.Activity;

import com.github.haocen2004.login_simulation.util.Logger;

import java.util.ArrayList;
import java.util.List;

public class ActivityManager {

    private static ActivityManager INSTANCE;

    private final List<Activity> arrayStack;

    public ActivityManager() {
        arrayStack = new ArrayList<>();
    }

    public static ActivityManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ActivityManager();
        }
        return INSTANCE;
    }

    public void addActivity(Activity activity) {
        if (!arrayStack.contains(activity)) {
            arrayStack.add(activity);
        }
    }

    public void removeActivity(Activity activity) {
        arrayStack.remove(activity);
    }

    public void clearActivity() {
        for (Activity activity : arrayStack) {
            activity.finish();
        }
        Logger.d("activityManager", "all exit.");
    }
}

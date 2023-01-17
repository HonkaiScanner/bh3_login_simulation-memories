package com.github.haocen2004.login_simulation.activity;

import android.app.Activity;

import com.github.haocen2004.login_simulation.util.Logger;

import java.util.ArrayList;
import java.util.List;

public class ActivityManager {

    private volatile static ActivityManager INSTANCE;

    private final List<Activity> arrayStack;

    private int topPos = -1;

    private boolean stopState;

    public ActivityManager() {
        arrayStack = new ArrayList<>();
    }

    public static ActivityManager getInstance() {
        if (INSTANCE == null) {
            synchronized (ActivityManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ActivityManager();
                }
            }
        }
        return INSTANCE;
    }

    public void addActivity(Activity activity) {
        if (stopState) {
            activity.finish();
            return;
        }
        if (!arrayStack.contains(activity)) {
            arrayStack.add(activity);
            topPos = arrayStack.size() - 1;
        }
    }

    public Activity getTopActivity() {
        if (topPos == -1) {
            if (arrayStack.size() > 0) {
                topPos = arrayStack.size() - 1;
            } else {
                return null;
            }
        }
        return arrayStack.get(topPos);
    }

    public void removeActivity(Activity activity) {
        arrayStack.remove(activity);
        topPos = arrayStack.size() - 1;
    }

    public void clearActivity() {
        stopState = true;
        for (Activity activity : arrayStack) {
            activity.finish();
        }
        Logger.d("activityManager", "all exit.");
    }
}

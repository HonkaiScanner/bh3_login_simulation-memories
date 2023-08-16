package com.github.haocen2004.login_simulation.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import com.github.haocen2004.login_simulation.utils.Logger;

import java.util.ArrayList;
import java.util.List;

public class ActivityManager extends BroadcastReceiver {

    private volatile static ActivityManager INSTANCE;

    private final List<Activity> arrayStack;

    private int topPos = -1;

    private boolean stopState = false;

    private boolean receiverLocked = false;

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

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_SCREEN_OFF.equals(action)) {
            receiverLocked = true;
        } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
            receiverLocked = false;
            if (stopState) {
                exit();
            }
        }
    }

    public void clearActivity() {
        stopState = true;
        boolean isScreenOn = true;
        try {
            PowerManager pm = (PowerManager) getTopActivity().getSystemService(Context.POWER_SERVICE);
            isScreenOn = pm.isInteractive();
        } catch (Exception ignore) {
        }

        if (!receiverLocked || isScreenOn) {
            exit();
        }
    }

    private void exit() {
        for (Activity activity : arrayStack) {
            activity.finish();
        }
        Logger.d("activityManager", "all exit.");
        System.exit(0);
    }
}

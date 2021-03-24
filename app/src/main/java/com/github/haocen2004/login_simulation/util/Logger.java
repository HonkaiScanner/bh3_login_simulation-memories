package com.github.haocen2004.login_simulation.util;

import android.content.Context;
import android.widget.Toast;

import com.tencent.bugly.crashreport.BuglyLog;

public class Logger {
    private static Logger INSTANCE;
    private final Context context;

    public static Logger getLogger(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new Logger(context);
        }
        return INSTANCE;
    }

    public Logger(Context context) {
        this.context = context;
    }

    public static void e(String TAG, String msg) {
        BuglyLog.e(TAG, msg);
    }

    public static void d(String TAG, String msg) {
        BuglyLog.d(TAG, msg);
    }

    public static void i(String TAG, String msg) {
        BuglyLog.i(TAG, msg);
    }

    public static void w(String TAG, String msg) {
        BuglyLog.w(TAG, msg);
    }

    public void makeToast(String msg) {
        makeToast(context, msg, Toast.LENGTH_LONG);
    }

    public void makeToast(Integer id) {
        makeToast(context.getString(id));
    }

    public void makeToast(Context context, String msg) {
        makeToast(context, msg, Toast.LENGTH_LONG);
    }

    public static void makeToast(Context context, String msg, Integer length) {
        Toast.makeText(context, msg, length).show();
    }
}

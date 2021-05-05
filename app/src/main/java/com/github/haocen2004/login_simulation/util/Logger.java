package com.github.haocen2004.login_simulation.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.hjq.toast.ToastUtils;
import com.tencent.bugly.crashreport.BuglyLog;

@SuppressLint("StaticFieldLeak")
public class Logger {
    private static Logger INSTANCE;
    private final Context context;
    private static View view;
    private static boolean useSnackbar;

    public static Logger getLogger(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new Logger(context);
        }
        return INSTANCE;
    }

    public Logger(Context context) {
        this.context = context;
        useSnackbar = false;
//        ToastUtils.init();
    }

    public static void setView(View view) {
        Logger.view = view;
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
        makeToast(context, msg, Toast.LENGTH_SHORT);
    }

    public void makeToast(Integer id) {
        makeToast(context.getString(id));
    }

    public void makeToast(Context context, String msg) {
        makeToast(context, msg, Toast.LENGTH_SHORT);
    }

    public static void makeToast(Context context, String msg, Integer length) {
        if (useSnackbar) {
            if (length == Toast.LENGTH_SHORT) {
                length = Snackbar.LENGTH_SHORT;
            } else if (length == Toast.LENGTH_LONG) {
                length = Snackbar.LENGTH_LONG;
            }
            d("Logger", "Transfer Toast Length to SnackBar Length: " + length);
            Snackbar.make(view, msg, length).show();
        } else {
            ToastUtils.show(msg);
        }
    }
}

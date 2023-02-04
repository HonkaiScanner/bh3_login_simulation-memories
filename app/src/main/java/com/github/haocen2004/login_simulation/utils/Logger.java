package com.github.haocen2004.login_simulation.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.github.haocen2004.login_simulation.data.LogLiveData;
import com.github.haocen2004.login_simulation.data.dialog.DialogLiveData;
import com.google.android.material.snackbar.Snackbar;
import com.hjq.toast.ToastUtils;
import com.tencent.bugly.crashreport.BuglyLog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressLint("StaticFieldLeak")
public class Logger implements Serializable {
    private volatile static Logger INSTANCE;
    private static Context context;
    private static View view;
    private static boolean useSnackBar;
    private static List<String> logBlackList;
    private static LogLiveData logLiveData;
    private static boolean fabMode;
//    private static String blackListString;

    public Logger(Context context) {
        Logger.context = context;
        useSnackBar = false;
        logBlackList = new ArrayList();
        logLiveData = LogLiveData.getINSTANCE();
        DialogLiveData.getINSTANCE(context);
    }

    public static Logger getLogger(Context context) {
        if (INSTANCE == null) {
            synchronized (Logger.class) {
                if (INSTANCE == null) {
                    INSTANCE = new Logger(context);
                }
            }
        }
        return INSTANCE;
    }

    public static void addBlacklist(String blackMsg) {
        if (logBlackList.contains(blackMsg)) return;
        if (blackMsg.length() < 2) {
            d("BlackList", "blackMsg is too short.");
            return;
        }
        if (blackMsg.contains("/")) {
            for (String s : blackMsg.split("/")) {
                if (logBlackList.contains(s)) continue;
                if (s.length() < 2) {
                    d("BlackList", "blackMsg is too short.");
                    continue;
                }
                logBlackList.add(s);
            }
        } else {
            logBlackList.add(blackMsg);
        }
    }

    public static void setView(View view) {
        Logger.view = view;
    }

    public static String processWithBlackList(String msg) {

        for (String b : logBlackList) {
            try {
                msg = msg.replace(b, "******");
            } catch (Exception ignore) {
            }
        }
        return msg;
    }

    public static void e(String TAG, String msg) {
        msg = processWithBlackList(msg);
        BuglyLog.e(TAG, msg);
        logLiveData.addNewLog("ERROR", TAG, msg);
    }

    public static void d(String TAG, String msg) {
        msg = processWithBlackList(msg);
        BuglyLog.d(TAG, msg);
        logLiveData.addNewLog("DEBUG", TAG, msg);
    }

    public static void i(String TAG, String msg) {
        msg = processWithBlackList(msg);
        BuglyLog.i(TAG, msg);
        logLiveData.addNewLog("INFO", TAG, msg);
    }

    public static void w(String TAG, String msg) {
        msg = processWithBlackList(msg);
        BuglyLog.w(TAG, msg);
        logLiveData.addNewLog("WARNING", TAG, msg);
    }

    public static void makeToast(Context context, String msg, Integer length) {
        msg = processWithBlackList(msg);
        d("TOAST", "show Toast " + msg);
        if (useSnackBar) {
            if (length == Toast.LENGTH_SHORT) {
                length = Snackbar.LENGTH_SHORT;
            } else if (length == Toast.LENGTH_LONG) {
                length = Snackbar.LENGTH_LONG;
            }
            d("Logger", "Transfer Toast Length to SnackBar Length: " + length);
            Snackbar.make(view, msg, length).show();
        } else if (fabMode) {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        } else {
            ToastUtils.show(msg);
        }
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

    public static void setFabMode(boolean b) {
        fabMode = b;
    }
}

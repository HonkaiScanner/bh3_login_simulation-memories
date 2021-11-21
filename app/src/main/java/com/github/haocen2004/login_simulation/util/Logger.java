package com.github.haocen2004.login_simulation.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.hjq.toast.ToastUtils;
import com.tencent.bugly.crashreport.BuglyLog;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("StaticFieldLeak")
public class Logger {
    private static Logger INSTANCE;
    private static Context context;
    private static View view;
    private static boolean useSnackBar;
    private static List<String> logBlackList;
//    private static String blackListString;

    public Logger(Context context) {
        Logger.context = context;
        useSnackBar = false;
        logBlackList = new ArrayList();
//        blackListString = getString(context, "logBlackLists");
//        if (!blackListString.equals("")) {
//            for (String blackItem : blackListString.split(";")) {
//                if (blackItem.length() < 4) w("BlackList", "blackMsg is too short: " + blackItem);
//                if (!blackItem.equals("")) {
//                    logBlackList.add(blackItem);
//                }
//            }
//            d("BlackList", "Total " + logBlackList.size());
////            logBlackList.addAll(Arrays.asList(blackListString.split(";")));
//        }
//        ToastUtils.init();
    }

    public static Logger getLogger(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new Logger(context);
        }
        return INSTANCE;
    }

    public static void addBlacklist(String blackMsg) {
//        d("addBlackList",blackMsg);
//        d("addBlackList",blackListString);
//        d("addBlackList",logBlackList.toString());
        if (logBlackList.contains(blackMsg)) return;
        logBlackList.add(blackMsg);
//        if (blackMsg.length() < 4) d("BlackList", "blackMsg is too short: " + blackMsg);
//        if (blackListString.equals("")) {
//            blackListString = blackMsg;
//        } else {
//            blackListString = blackListString + ";" + blackMsg;
//        }
//        saveString(context, "logBlackLists", blackListString);
    }

    public static void setView(View view) {
        Logger.view = view;
    }

    public static void e(String TAG, String msg) {
        for (String b : logBlackList) {
            msg = msg.replace(b, "******");
        }
        BuglyLog.e(TAG, msg);
    }

    public static void d(String TAG, String msg) {
        for (String b : logBlackList) {
//            BuglyLog.d("logBlacklist","try to replace "+b);
            msg = msg.replace(b, "******");
        }
        BuglyLog.d(TAG, msg);
    }

    public static void i(String TAG, String msg) {
        for (String b : logBlackList) {
            msg = msg.replace(b, "******");
        }
        BuglyLog.i(TAG, msg);
    }

    public static void w(String TAG, String msg) {
        for (String b : logBlackList) {
            msg = msg.replace(b, "******");
        }
        BuglyLog.w(TAG, msg);
    }

    public static void makeToast(Context context, String msg, Integer length) {
        if (useSnackBar) {
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

    public void makeToast(String msg) {
        makeToast(context, msg, Toast.LENGTH_SHORT);
    }

    public void makeToast(Integer id) {
        makeToast(context.getString(id));
    }

    public void makeToast(Context context, String msg) {
        makeToast(context, msg, Toast.LENGTH_SHORT);
    }
}

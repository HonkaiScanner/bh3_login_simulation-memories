package com.github.haocen2004.login_simulation.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.github.haocen2004.login_simulation.activity.ActivityManager;
import com.github.haocen2004.login_simulation.data.LogData;
import com.github.haocen2004.login_simulation.data.LogLiveData;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class CrashHandler extends CrashReport.CrashHandleCallback {
    private static final String TAG = "CrashHandler";
    @SuppressLint("StaticFieldLeak")
    private static final CrashHandler instance = new CrashHandler();
    private String PATH = "";
    private static final String FILE_NAME = "crash";
    private static final String FILE_NAME_SUFFIX = ".txt";
    private Context mContext;

    @Override
    public synchronized Map<String, String> onCrashHandleStart(int i, String s, String s1, String s2) {

        Log.e(TAG, "DETECT CRASH");
        String crashInfo = convert2StackDump(s, s1, s2);
        try {
            Log.d(TAG, "DUMP TO SDCARD");
//            Log.d(TAG,"PATH:" + PATH);
            dumpExceptionToSDCard(crashInfo);
            Log.d(TAG, "DUMP TO SDCARD SUCCEED");

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Log.d(TAG, "CRASH DETAIL");
        Log.d(TAG, crashInfo);

        Log.d(TAG, "MARK CRASH ACTIVITY");

        Tools.saveBoolean(mContext, "has_crash", true);
        Tools.saveBoolean(mContext, "last_login_succeed", false);

        ActivityManager.getInstance().clearActivity();

        return super.onCrashHandleStart(i, s, s1, s2);
    }

    private String convert2StackDump(String errorType,
                                     String errorMessage, String errorStack) {
        StringBuilder output = new StringBuilder();
        output.append(errorType)
                .append(": ")
                .append(errorMessage);
        for (String s : errorStack.split("\n")) {
            if (s.length() > 1) {
                output.append("\n\tat ")
                        .append(s);
            }

        }
        return output.toString();
    }

    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        return instance;
    }

    public void init(Context context) {
        mContext = context;
        PATH = mContext.getExternalFilesDir(null) + "/crash-report/";
    }

    private void dumpExceptionToSDCard(String errorStack) {

        File dir = new File(PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        long current = System.currentTimeMillis();
        String time = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.CHINA).format(new Date(current));
        time = time.replace(":", "-");
        File file = new File(PATH + FILE_NAME + "-" + time + FILE_NAME_SUFFIX);
        Tools.saveString(mContext, "crash-report-name", FILE_NAME + "-" + time + FILE_NAME_SUFFIX);
        Log.d(TAG, "crash-report-name: " + file.getName());

        try {
            file.createNewFile();
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            pw.println(time);
            dumpPhoneInfo(pw);
            pw.println();
            pw.println(errorStack);
//            ex.printStackTrace(pw);
            pw.println();
            pw.println("Logs: ");
            dumpLogs(pw);
            pw.println();
            pw.println("SharedPref: ");
            dumpSharedPref(pw);
            pw.close();
        } catch (Exception e) {
            Log.e(TAG, "dump crash info failed");
            e.printStackTrace();
        }
    }

    private void dumpLogs(PrintWriter pw) {
        try {
            for (LogData logData : LogLiveData.getINSTANCE().getDebugLogList()) {
                if (logData.getTAG().equals("复制日志")) continue;
                pw.print(logData.getLevel());
                pw.print("/");
                pw.print(logData.getTAG());
                pw.print(": ");
                pw.println(logData.getMessage());
            }
        } catch (NullPointerException e) {
            pw.println("NO LOGS.");
        }
    }

    private void dumpSharedPref(PrintWriter pw) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        Map<String, ?> map = sharedPreferences.getAll();
        for (String s : map.keySet()) {
            pw.print(s);
            pw.print(":");
            pw.println(map.get(s));
        }
    }

    private void dumpPhoneInfo(PrintWriter pw) throws PackageManager.NameNotFoundException {
        PackageManager pm = mContext.getPackageManager();
        PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
        pw.print("App Version: ");
        pw.print(pi.versionName);
        pw.print(" (");
        pw.print(pi.versionCode);
        pw.println(")");

        //android版本号
        pw.print("OS Version: ");
        pw.print(Build.VERSION.RELEASE);
        pw.print("(");
        pw.print(Build.VERSION.SDK_INT);
        pw.println(")");

        //手机制造商
        pw.print("Vendor: ");
        pw.println(Build.MANUFACTURER);

        //手机型号
        pw.print("Model: ");
        pw.println(Build.MODEL);

        //cpu架构
        pw.print("Support ABI: ");
        StringBuilder supportedABI = new StringBuilder();
        for (String abi : Build.SUPPORTED_ABIS) {
            supportedABI.append(abi);
            supportedABI.append(',');
        }
        pw.println(supportedABI);
    }
}

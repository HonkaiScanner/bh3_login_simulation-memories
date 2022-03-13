package com.github.haocen2004.login_simulation.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Process;
import android.util.Log;

import androidx.annotation.NonNull;

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
import java.util.Objects;

public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "CrashHandler";
    private static final CrashHandler instance = new CrashHandler();
    private Thread.UncaughtExceptionHandler mDefaultCrashHandler;
    private String PATH = "";
    private static final String FILE_NAME = "crash";
    private static final String FILE_NAME_SUFFIX = ".txt";
    private static Context mContext;

    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        Log.e(TAG, "DETECT CRASH");
        try {
            Log.d(TAG, "DUMP TO SDCARD");
//            Log.d(TAG,"PATH:" + PATH);
            dumpExceptionToSDCard(e);
            Log.d(TAG, "DUMP TO SDCARD SUCCEED");
            CrashReport.postCatchedException(e, t);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        e.printStackTrace();
        if (mDefaultCrashHandler != null) {
            mDefaultCrashHandler.uncaughtException(t, e);
        } else {
            try {
                Thread.sleep(2000); // 延迟2秒杀进程
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            android.os.Process.killProcess(Process.myPid());
        }
    }

    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        return instance;
    }

    public void init(Context context) {
        mDefaultCrashHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
        mContext = context;
        PATH = mContext.getExternalFilesDir(null) + "/crash-report/";
    }

    private void dumpExceptionToSDCard(Throwable ex) {

        File dir = new File(PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        long current = System.currentTimeMillis();
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(new Date(current));
        time = time.replace(":", "-");
        File file = new File(PATH + FILE_NAME + "-" + time + FILE_NAME_SUFFIX);

        try {
            file.createNewFile();
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            pw.println(time);
            dumpPhoneInfo(pw);
            pw.println();
            ex.printStackTrace(pw);
            pw.println();
            pw.println("Logs: ");
            dumpLogs(pw);
            pw.close();
        } catch (Exception e) {
            Log.e(TAG, "dump crash info failed");
            e.printStackTrace();
        }
    }

    private void dumpLogs(PrintWriter pw) {
        for (LogData logData : Objects.requireNonNull(LogLiveData.getINSTANCE(mContext).getValue())) {
            if (logData.getTAG().equals("复制日志")) continue;
            pw.print(logData.getLevel());
            pw.print("/");
            pw.print(logData.getTAG());
            pw.print(": ");
            pw.println(logData.getMessage());
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
        pw.print("SUPPORTED ABI: ");
        StringBuilder supportedABI = new StringBuilder();
        for (String abi : Build.SUPPORTED_ABIS) {
            supportedABI.append(abi);
            supportedABI.append(',');
        }
        pw.println(supportedABI);
    }
}

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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@SuppressLint("StaticFieldLeak")
public class Logger implements Serializable {
    private volatile static Logger INSTANCE;
    private static Context context;
    private static View view;
    private static boolean useSnackBar;
    private static List<String> logBlackList;
    private static LogLiveData logLiveData;
    private static boolean fabMode;
    private static FileWriter fileWriter;
    private static BufferedWriter bufferedWriter;
    private static File logFile;
    private static boolean logToFile = true;
//    private static String blackListString;

    public Logger(Context context) {
        Logger.context = context;
        useSnackBar = false;
        logBlackList = new ArrayList();
        createOutputStream();
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
        logToFile("ERROR", TAG, msg);
    }

    public static void d(String TAG, String msg) {
        msg = processWithBlackList(msg);
        BuglyLog.d(TAG, msg);
        logLiveData.addNewLog("DEBUG", TAG, msg);
        logToFile("DEBUG", TAG, msg);
    }

    public static void i(String TAG, String msg) {
        msg = processWithBlackList(msg);
        BuglyLog.i(TAG, msg);
        logLiveData.addNewLog("INFO", TAG, msg);
        logToFile("INFO", TAG, msg);
    }

    public static void w(String TAG, String msg) {
        msg = processWithBlackList(msg);
        BuglyLog.w(TAG, msg);
        logLiveData.addNewLog("WARNING", TAG, msg);
        logToFile("WARNING", TAG, msg);
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

    private static void logToFile(String level, String TAG, String msg) {
        if (bufferedWriter == null) {
            createOutputStream();
        }
        if (logToFile) {
            long current = System.currentTimeMillis();
            SimpleDateFormat logFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
            String outputLog = logFormat.format(new Date(current)) + " " + level + "/" + TAG + ": " + msg;
            try {
                bufferedWriter.write(outputLog);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            } catch (IOException ignore) {
                logToFile = false;
            }
        }

    }

    private static int daysBetween(String date1str, String date2str) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        try {
            Date date1 = format.parse(date1str);
            Date date2 = format.parse(date2str);
            int a = (int) ((date1.getTime() - date2.getTime()) / (1000 * 3600 * 24));
            return a;
        } catch (Exception ignore) {
            return 0;
        }
    }

    private static void removeOldLogFiles() {
        String dirPath = context.getExternalFilesDir(null) + "/logs/";
        File dir = new File(dirPath);
        if (dir.exists()) {
            long current = System.currentTimeMillis();
            String nowDate = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(new Date(current));
            File[] logs = dir.listFiles();
            for (File log : logs) {
                String oldDate = log.getName().replace("logs-", "").replace(".log", "");
                if (daysBetween(nowDate, oldDate) > 14) {
                    log.delete();
                }
            }
        }
    }

    private static void checkLogFile() {

        removeOldLogFiles();
        long current = System.currentTimeMillis();
        String time = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(new Date(current));
        String dirPath = context.getExternalFilesDir(null) + "/logs/";
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        logFile = new File(dirPath + "logs-" + time + ".log");
        if (!logFile.exists()) {
            try {
                //在指定的文件夹中创建文件
                logFile.createNewFile();
            } catch (Exception ignored) {
                logToFile = false;
            }
        }
    }

    private static void createOutputStream() {
        try {
            if (fileWriter == null) {
                if (!(logFile != null && logFile.exists())) {
                    checkLogFile();
                }
                fileWriter = new FileWriter(logFile, true);
            }

            bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write("========== " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(new Date(System.currentTimeMillis())) + " ==========");
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            logToFile = false;
        }
    }
}

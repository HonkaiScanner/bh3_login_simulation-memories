package xyz.hellocraft.login_simulation.utils;

import static xyz.hellocraft.login_simulation.data.Constant.SAVE_ALL_LOGS;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Keep;

import com.google.android.material.snackbar.Snackbar;
import com.hjq.toast.Toaster;
import com.tencent.bugly.crashreport.BuglyLog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import xyz.hellocraft.login_simulation.data.LogLiveData;
import xyz.hellocraft.login_simulation.data.dialog.DialogLiveData;

@SuppressLint("StaticFieldLeak")
public class Logger implements Serializable {
    private volatile static Logger INSTANCE;
    private static Context context;
    private static View view;
    private static boolean useSnackBar;
    private static ArrayList<String> logBlackList;
    private static LogLiveData logLiveData;
    private static FileWriter fileWriter;
    private static FileWriter DEBUGLOGWRITER;
    private static BufferedWriter bufferedWriter;
    private static BufferedWriter DEBUGLOGBUFFERWRITER;
    private static File logFile;
    private static File DEBUGLOGFILE;
    private static boolean logToFile = true;
    private static boolean DEBUGLOG2FILE = true;
    private static final SimpleDateFormat logFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
//    private static String blackListString;

    public Logger(Context context) {
        Logger.context = context;
        useSnackBar = false;
        logBlackList = new ArrayList<>();
        createOutputStream();
        logLiveData = LogLiveData.getINSTANCE();
        DialogLiveData.getINSTANCE();
        if (SAVE_ALL_LOGS && DEBUGLOG2FILE) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        Runtime.getRuntime().exec("logcat -c");
                        Process process = Runtime.getRuntime().exec("logcat *:V -b all --pid=" + android.os.Process.myPid());
                        BufferedReader bufferedReader = new BufferedReader(
                                new InputStreamReader(process.getInputStream()));

                        d("DEBUG_LOG_WRITER",
                                "LOGCAT INSTANCE CREATED");
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            DEBUGLOGBUFFERWRITER.write(line);
                            DEBUGLOGBUFFERWRITER.write("\n");
                            DEBUGLOGBUFFERWRITER.flush();
                        }
                    } catch (Exception e) {
                        d("DEBUG_LOG_WRITER",
                                "read logcat process failed. message: "
                                        + e.getMessage());
                    } finally {
                        if (null != DEBUGLOGBUFFERWRITER) {
                            try {
                                DEBUGLOGBUFFERWRITER.close();
                                DEBUGLOGBUFFERWRITER = null;
                            } catch (IOException e) {
                                // Do nothing
                            }
                        }
                        DEBUGLOG2FILE = false;
                    }
                }
            }.start();

        }
    }

    public static String getDebugLogName() {
        if (DEBUGLOGFILE.exists()) return DEBUGLOGFILE.getName();
        return "NONE";
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
        if (blackMsg.length() < 4) {
            d("BlackList", "blackMsg is too short.");
            return;
        }
        if (blackMsg.contains("/")) {
            for (String s : blackMsg.split("/")) {
                if (logBlackList.contains(s)) continue;
                if (s.length() < 4) {
                    d("BlackList", "blackMsg is too short.");
                    continue;
                }
                logBlackList.add(s.strip());
            }
        } else {
            logBlackList.add(blackMsg.strip());
        }
    }

    public static String processWithBlackList(String msg) {

        for (String b : logBlackList) {
            if (b.length() < 3) {
                logBlackList.remove(b);
                continue;
            }
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
        logLiveData.addNewLog("ERROR", TAG, msg, getCurrentTimeStr());
        logToFile("ERROR", TAG, msg);
    }

    @Keep
    public static int fakeE(String TAG, String msg) {
        if (SAVE_ALL_LOGS) {
            msg = processWithBlackList(msg);
            BuglyLog.e(TAG, msg);
            if (msg.length() > 200) {
                msg = msg.substring(0, 200) + "...";
            }
            logLiveData.addNewLog("ERROR", TAG, msg, getCurrentTimeStr());
            logToFile("ERROR", TAG, msg);
        } else {
            return Log.e(TAG, msg);
        }
        return 0;
    }

    public static void d(String TAG, String msg) {
        msg = processWithBlackList(msg);
        BuglyLog.d(TAG, msg);
        logLiveData.addNewLog("DEBUG", TAG, msg, getCurrentTimeStr());
        logToFile("DEBUG", TAG, msg);
    }

    @Keep
    public static int fakeD(String TAG, String msg) {
        if (SAVE_ALL_LOGS) {
            msg = processWithBlackList(msg);
            BuglyLog.d(TAG, msg);
            if (msg.length() > 200) {
                msg = msg.substring(0, 200) + "...";
            }
            logLiveData.addNewLog("DEBUG", TAG, msg, getCurrentTimeStr());
            logToFile("DEBUG", TAG, msg);
        } else {
            return Log.d(TAG, msg);
        }
        return 0;
    }

    public static void i(String TAG, String msg) {
        msg = processWithBlackList(msg);
        BuglyLog.i(TAG, msg);
        logLiveData.addNewLog("INFO", TAG, msg, getCurrentTimeStr());
        logToFile("INFO", TAG, msg);
    }

    @Keep
    public static int fakeI(String TAG, String msg) {
        if (SAVE_ALL_LOGS) {
            msg = processWithBlackList(msg);
            BuglyLog.i(TAG, msg);
            if (msg.length() > 200) {
                msg = msg.substring(0, 200) + "...";
            }
            logLiveData.addNewLog("INFO", TAG, msg, getCurrentTimeStr());
            logToFile("INFO", TAG, msg);
        } else {
            return Log.i(TAG, msg);
        }
        return 0;
    }

    public static void w(String TAG, String msg) {
        msg = processWithBlackList(msg);
        BuglyLog.w(TAG, msg);
        logLiveData.addNewLog("WARNING", TAG, msg, getCurrentTimeStr());
        logToFile("WARNING", TAG, msg);
    }

    @Keep
    public static int fakeW(String TAG, String msg) {
        if (SAVE_ALL_LOGS) {
            msg = processWithBlackList(msg);
            BuglyLog.w(TAG, msg);
            if (msg.length() > 200) {
                msg = msg.substring(0, 200) + "...";
            }
            logLiveData.addNewLog("WARNING", TAG, msg, getCurrentTimeStr());
            logToFile("WARNING", TAG, msg);
        } else {
            return Log.w(TAG, msg);
        }
        return 0;
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
//        } else if (fabMode) {
//            try {
//                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
//            } catch (NullPointerException ignore) {
//                Looper.prepare();
//                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
//                Looper.loop();
//            }
        } else {
            Toaster.show(msg);
        }
    }

    private static String getTimeStr(long timeMillis) {
        return logFormat.format(new Date(timeMillis));
    }

    private static String getCurrentTimeStr() {
        return getTimeStr(System.currentTimeMillis());
    }

    private static void logToFile(String level, String TAG, String msg) {
        try {
            if (bufferedWriter == null) {
                createOutputStream();
            }
            if (logToFile) {
                String outputLog = getCurrentTimeStr() + " " + level + "/" + TAG + ": " + msg.replaceAll("\\t", "{%&t%}").replaceAll("\\n", "{%&n%}").replaceAll("\\r", "{%&r%}");
                try {
                    bufferedWriter.write(outputLog);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                } catch (IOException ignore) {
                    logToFile = false;
                }
            }
        } catch (Exception ignore) {
        }

    }

    private static int daysBetween(String date1str, String date2str) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        try {
            Date date1 = format.parse(date1str);
            Date date2 = format.parse(date2str);
            if (date1 != null && date2 != null) {
                return (int) ((date1.getTime() - date2.getTime()) / (1000 * 3600 * 24));
            }
            return 0;
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
            if (logs != null) {
                for (File log : logs) {
                    String oldDate = log.getName().replace("logs-", "").replace("debug-", "").replace(".log", "");
                    if (daysBetween(nowDate, oldDate) > 14) {
                        log.delete();
                    }
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
        SAVE_ALL_LOGS = new File(dirPath + ".saveAllLogs").exists();
        logFile = new File(dirPath + "logs-" + time + ".log");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (Exception ignored) {
                logToFile = false;
            }
        }
        DEBUGLOGFILE = new File(dirPath + "logs-debug-" + time + ".log");
        if (!DEBUGLOGFILE.exists()) {
            try {
                DEBUGLOGFILE.createNewFile();
            } catch (Exception ignore) {
                DEBUGLOG2FILE = false;
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
        try {
            if (DEBUGLOGWRITER == null) {
                if (!(DEBUGLOGFILE != null && DEBUGLOGFILE.exists())) {
                    checkLogFile();
                }
                DEBUGLOGWRITER = new FileWriter(DEBUGLOGFILE, true);
            }
            DEBUGLOGBUFFERWRITER = new BufferedWriter(DEBUGLOGWRITER);
            DEBUGLOGBUFFERWRITER.write("========== " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(new Date(System.currentTimeMillis())) + " ==========");
            DEBUGLOGBUFFERWRITER.newLine();
            DEBUGLOGBUFFERWRITER.flush();
        } catch (Exception ignore) {
            DEBUGLOG2FILE = false;
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

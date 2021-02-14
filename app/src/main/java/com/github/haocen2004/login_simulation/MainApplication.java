package com.github.haocen2004.login_simulation;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.github.haocen2004.login_simulation.Database.SponsorRepo;
import com.github.haocen2004.login_simulation.util.Network;
import com.tencent.bugly.crashreport.BuglyLog;
import com.tencent.bugly.crashreport.CrashReport;

import org.json.JSONObject;

import cn.leancloud.AVLogger;
import cn.leancloud.AVOSCloud;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.github.haocen2004.login_simulation.BuildConfig.DEBUG;
import static com.github.haocen2004.login_simulation.BuildConfig.VERSION_CODE;
import static com.github.haocen2004.login_simulation.util.Constant.BH_VER;
import static com.github.haocen2004.login_simulation.util.Tools.openUrl;

public class MainApplication extends Application {
    private SharedPreferences app_pref;
    @Override
    public void onCreate() {
        super.onCreate();
        CrashReport.initCrashReport(getApplicationContext(), "4bfa7b722e", true);
        AVOSCloud.initialize(this, "VMh6lRyykuNDyhXxoi996cGI-gzGzoHsz", "RWvHCY9qXzX1BH4L72J9RI1I", "https://vmh6lryy.lc-cn-n1-shared.com");
        if(DEBUG) {
            AVOSCloud.setLogLevel(AVLogger.Level.DEBUG);
        }
        app_pref = getDefaultSharedPreferences(this);
        if (app_pref.getBoolean("is_first_run", true) || app_pref.getInt("version", 1) < VERSION_CODE) {
            app_pref.edit()
                    .putBoolean("is_first_run", false)
                    .putInt("version", VERSION_CODE)
                    .apply();
            if (!app_pref.contains("auto_confirm")) {
                app_pref.edit()
                        .putBoolean("auto_confirm", false)
                        .apply();
            }
            if (!app_pref.contains("enable_ad")) {
                app_pref.edit()
                        .putBoolean("enable_ad", true)
                        .apply();
            }
            if (!app_pref.contains("server_type")) {
                app_pref.edit()
                        .putString("server_type", "Official")
                        .apply();
            }

            if (!app_pref.contains("showBetaInfo")) {
                app_pref.edit()
                        .putBoolean("showBetaInfo", DEBUG)
                        .apply();
            }
            if (!app_pref.contains("custom_username")) {
                app_pref.edit()
                        .putString("custom_username", "崩坏3扫码器用户")
                        .apply();
            }
            if (!app_pref.contains("check_update")) {
                app_pref.edit()
                        .putBoolean("check_update", !getPackageName().contains("dev"))
                        .apply();
            }

        }
        if (app_pref.getBoolean("showBetaInfo", DEBUG)) {
            showBetaInfoDialog();
        }
        if (app_pref.getBoolean("check_update", true)) {
            new Thread(update_rb).start();
        }

    }
    @SuppressLint("HandlerLeak")
    Handler update_check_hd = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String feedback = data.getString("value");
            feedback = feedback.substring(1, feedback.length() - 1).replaceAll("\\\\", "");
            BuglyLog.i("Update", "handleMessage: " + feedback);
            try {
                JSONObject json = new JSONObject(feedback);
                app_pref.edit().putString("bh_ver", json.getString("bh_ver")).apply();
                int sp_ver = app_pref.getInt("sponsors_db_ver", 0);
                if (sp_ver < json.getInt("sponsors_db_ver")) {
                    new SponsorRepo(getApplicationContext()).refreshSponsors();
                    app_pref.edit().putInt("sponsors_db_ver", json.getInt("sponsors_db_ver")).apply();
                }

                if (!getPackageName().contains("dev") && app_pref.getInt("version", VERSION_CODE) < json.getInt("ver")) {
                    showUpdateDialog(
                            json.getString("ver_name"),
                            json.getString("update_url"),
                            json.getString("logs").replaceAll("&n", "\n")
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
                BuglyLog.d("Update", "Check Update Failed");

                app_pref.edit().putString("bh_ver", BH_VER).apply();
            }
            BH_VER = app_pref.getString("bh_ver", BH_VER);


        }
    };
    private void showBetaInfoDialog() {

        final AlertDialog.Builder normalDialog = new AlertDialog.Builder(this);
        normalDialog.setTitle("Beta使用须知");
        normalDialog.setMessage("你现在使用的是内部测试版本\n请及时通过左边侧滑栏反馈bug\n此消息只会出现一次");
        normalDialog.setPositiveButton("我已知晓",
                (dialog, which) -> {
                    getDefaultSharedPreferences(this).edit().putBoolean("showBetaInfo", false).apply();
                    dialog.dismiss();
                });
        normalDialog.setCancelable(false);
        normalDialog.show();
    }

    private void showUpdateDialog(String ver, String url, String logs) {
        final AlertDialog.Builder normalDialog = new AlertDialog.Builder(this);
        normalDialog.setTitle("获取到新版本: " + ver);
        normalDialog.setMessage("更新日志：\n" + logs);
        normalDialog.setPositiveButton("打开更新链接",
                (dialog, which) -> {
                    openUrl(url, this);
                    dialog.dismiss();
                });
        normalDialog.setCancelable(false);
        normalDialog.show();
    }
    Runnable update_rb = () -> {
        String feedback = Network.sendPost("https://service-beurmroh-1256541670.sh.apigw.tencentcs.com/version", "");
        if (feedback.isEmpty()) {
            return;
        }
        Message msg = new Message();
        Bundle data = new Bundle();
        data.putString("value", feedback);
        msg.setData(data);
        update_check_hd.sendMessage(msg);
    };
}

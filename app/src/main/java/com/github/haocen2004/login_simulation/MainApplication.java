package com.github.haocen2004.login_simulation;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;

import com.github.haocen2004.login_simulation.Database.SponsorRepo;
import com.github.haocen2004.login_simulation.util.Logger;
import com.github.haocen2004.login_simulation.util.Network;
import com.tencent.bugly.crashreport.CrashReport;

import org.json.JSONObject;

import java.util.concurrent.Executors;

import cn.leancloud.AVLogger;
import cn.leancloud.AVOSCloud;
import cn.leancloud.AVUser;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.github.haocen2004.login_simulation.BuildConfig.DEBUG;
import static com.github.haocen2004.login_simulation.BuildConfig.VERSION_CODE;
import static com.github.haocen2004.login_simulation.util.Constant.BH_VER;
import static com.github.haocen2004.login_simulation.util.Constant.CHECK_VER;
import static com.github.haocen2004.login_simulation.util.Constant.HAS_ACCOUNT;
import static com.github.haocen2004.login_simulation.util.Constant.MDK_VERSION;
import static com.github.haocen2004.login_simulation.util.Constant.SP_URL;
import static com.github.haocen2004.login_simulation.util.Tools.openUrl;

public class MainApplication extends Application {
    private SharedPreferences app_pref;
    private Logger Log;
    @Override
    public void onCreate() {
        super.onCreate();
        Log = Logger.getLogger(this);
        CrashReport.initCrashReport(getApplicationContext(), "4bfa7b722e", true);
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
//            if (!app_pref.contains("enable_ad")) {
//                app_pref.edit()
//                        .putBoolean("enable_ad", true)
//                        .apply();
//            }
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
            if (!app_pref.contains("official_type")) {
                app_pref.edit()
                        .putInt("official_type", 0)
                        .apply();
            }
            if (!app_pref.contains("dark_type")) {
                app_pref.edit()
                        .putString("dark_type", "-1")
                        .apply();
            }
            if (!app_pref.contains("mdk_ver")) {
                app_pref.edit()
                        .putString("mdk_ver", MDK_VERSION)
                        .apply();
            }

        }
        CHECK_VER = app_pref.getBoolean("check_update", true);

        if (CHECK_VER) {
            new Thread(update_rb).start();
        } else {
            BH_VER = app_pref.getString("bh_ver", BH_VER);
            MDK_VERSION = app_pref.getString("mdk_ver", MDK_VERSION);
            AVOSCloud.initialize(this, "VMh6lRyykuNDyhXxoi996cGI-gzGzoHsz", "RWvHCY9qXzX1BH4L72J9RI1I", SP_URL);
            if (DEBUG) {
                AVOSCloud.setLogLevel(AVLogger.Level.DEBUG);
            }
        }

        if (app_pref.getBoolean("has_account", false)) {
            String TAG = "login check";

            Logger.d(TAG, "Start.");
            Executors.newSingleThreadExecutor().execute(() -> {
                AVUser.becomeWithSessionTokenInBackground(app_pref.getString("account_token", "")).subscribe(new Observer<AVUser>() {
                    public void onSubscribe(Disposable disposable) {
                    }

                    public void onNext(AVUser user) {
                        AVUser.changeCurrentUser(user, true);
                        HAS_ACCOUNT = true;
                        app_pref.edit().putString("custom_username", user.getString("custom_username")).apply();
                        Logger.d(TAG, "Succeed.");

                    }

                    public void onError(Throwable throwable) {
                        AVUser.changeCurrentUser(null, true);
                        app_pref.edit().putBoolean("has_account", false)
                                .putString("custom_username", "崩坏3扫码器用户").apply();
                        throwable.printStackTrace();
                        Logger.d(TAG, "Failed.");
                        Log.makeToast("赞助者身份验证已过期...");
                    }

                    public void onComplete() {
                    }
                });
            });

        }
        switch (app_pref.getString("dark_type", "-1")) {
            case "-1":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case "1":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "2":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
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
            Logger.i("Update", "handleMessage: " + feedback);
            try {
                JSONObject json = new JSONObject(feedback);
                app_pref.edit().putString("bh_ver", json.getString("bh_ver"))
                        .putString("mdk_ver", json.getString("mdk_ver"))
                        .putString("sp_url", json.getString("sp_url")).apply();

                if (!getPackageName().contains("dev") && app_pref.getInt("version", VERSION_CODE) < json.getInt("ver")) {
                    showUpdateDialog(
                            json.getString("ver_name"),
                            json.getString("update_url"),
                            json.getString("logs").replaceAll("&n", "\n")
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
                Logger.d("Update", "Check Update Failed");

                //app_pref.edit().putString("bh_ver", BH_VER).apply();
            }
            BH_VER = app_pref.getString("bh_ver", BH_VER);
            MDK_VERSION = app_pref.getString("mdk_ver", MDK_VERSION);
            SP_URL = app_pref.getString("sp_url", SP_URL);
            AVOSCloud.initialize(getApplicationContext(), "VMh6lRyykuNDyhXxoi996cGI-gzGzoHsz", "RWvHCY9qXzX1BH4L72J9RI1I", SP_URL);
            if (DEBUG) {
                AVOSCloud.setLogLevel(AVLogger.Level.DEBUG);
            }

            Executors.newSingleThreadExecutor().execute(() -> new SponsorRepo(getApplicationContext()).refreshSponsors());

        }
    };


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
        Message msg = new Message();
        Bundle data = new Bundle();
        data.putString("value", feedback);
        msg.setData(data);
        update_check_hd.sendMessage(msg);
    };
}

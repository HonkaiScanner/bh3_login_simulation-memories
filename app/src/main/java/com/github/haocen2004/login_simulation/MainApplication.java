package com.github.haocen2004.login_simulation;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.github.haocen2004.login_simulation.BuildConfig.DEBUG;
import static com.github.haocen2004.login_simulation.BuildConfig.VERSION_CODE;
import static com.github.haocen2004.login_simulation.BuildConfig.VERSION_NAME;
import static com.github.haocen2004.login_simulation.data.Constant.BETA_VER;
import static com.github.haocen2004.login_simulation.data.Constant.BH_VER;
import static com.github.haocen2004.login_simulation.data.Constant.CHECK_VER;
import static com.github.haocen2004.login_simulation.data.Constant.DEBUG_MODE;
import static com.github.haocen2004.login_simulation.data.Constant.MDK_VERSION;
import static com.github.haocen2004.login_simulation.data.Constant.MI_ADV_MODE;
import static com.github.haocen2004.login_simulation.data.Constant.SAVE_ALL_LOGS;
import static com.github.haocen2004.login_simulation.data.Constant.SP_APP_KEY;
import static com.github.haocen2004.login_simulation.data.Constant.SP_URL;
import static com.github.haocen2004.login_simulation.data.Constant.VISITOR_COUNT;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.view.Gravity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

import com.github.haocen2004.login_simulation.activity.ActivityManager;
import com.github.haocen2004.login_simulation.activity.NotificationActivity;
import com.github.haocen2004.login_simulation.data.LogLiveData;
import com.github.haocen2004.login_simulation.data.dialog.ButtonData;
import com.github.haocen2004.login_simulation.data.dialog.DialogData;
import com.github.haocen2004.login_simulation.data.dialog.DialogLiveData;
import com.github.haocen2004.login_simulation.data.sponsor.database.SponsorRepo;
import com.github.haocen2004.login_simulation.utils.CrashHandler;
import com.github.haocen2004.login_simulation.utils.ForegroundCallbacks;
import com.github.haocen2004.login_simulation.utils.Logger;
import com.github.haocen2004.login_simulation.utils.Network;
import com.github.haocen2004.login_simulation.utils.PmsHooker;
import com.github.haocen2004.login_simulation.utils.Tools;
import com.hjq.toast.Toaster;
import com.tencent.bugly.crashreport.CrashReport;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import cn.leancloud.LCInstallation;
import cn.leancloud.LCObject;
import cn.leancloud.LeanCloud;
import cn.leancloud.push.PushService;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
//import com.tencent.ysdk.api.YSDKApi;

public class MainApplication extends Application implements LifecycleOwner {
    private SharedPreferences app_pref;


    @Override
    public String getPackageName() {
        return PmsHooker.getPackageNameFilter(super.getPackageName());
    }

    @NonNull
    @Override
    public String getOpPackageName() {
        return PmsHooker.getPackageNameFilter(super.getOpPackageName());
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Logger.getLogger(this);
        PmsHooker.startHook(base);
    }

    private final LifecycleRegistry mLifecycle = new LifecycleRegistry(this);

    @Override
    public void onCreate() {
        super.onCreate();
        app_pref = getDefaultSharedPreferences(this);
        mLifecycle.setCurrentState(Lifecycle.State.CREATED);
        LogLiveData.getINSTANCE(); //init live data
        Logger.getLogger(this);
        ForegroundCallbacks.init(this);
//        YSDKApi.setMainActivity("com.github.haocen2004.login_simulation.activity.MainActivity");
        Toaster.init(this);
        Toaster.setGravity(Gravity.BOTTOM, 0, 50);
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(this);
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(getApplicationContext());
        if (app_pref.contains("server_type")) {
            String appChannel = app_pref.getString("server_type", "noLogin");
            strategy.setAppChannel(appChannel);
            CrashReport.setAppChannel(this, appChannel);
        }
        strategy.setAppChannel("noLogin");
        CrashReport.setAppChannel(this, "noLogin");
        strategy.setDeviceID(Tools.getUUID(this));
        CrashReport.setDeviceId(this, Tools.getUUID(this));
        strategy.setDeviceModel(Tools.getDeviceModel());
        strategy.setCrashHandleCallback(crashHandler);
        CrashReport.setIsDevelopmentDevice(getApplicationContext(), DEBUG);
        CrashReport.initCrashReport(getApplicationContext(), "4bfa7b722e", DEBUG, strategy);
        DEBUG_MODE = app_pref.getBoolean("debug_mode", false) || DEBUG;
        String dirPath = getExternalFilesDir(null) + "/logs/";
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File saveAllLogs = new File(dirPath + ".saveAllLogs");
        if (DEBUG_MODE) {
            if (!saveAllLogs.exists()) {
                try {
                    saveAllLogs.createNewFile();
                    SAVE_ALL_LOGS = true;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            saveAllLogs.delete();
            SAVE_ALL_LOGS = false;
        }
        if (app_pref.getBoolean("is_first_run", true) || app_pref.getInt("version", 1) < VERSION_CODE) {
            Logger.d("prefSetup", "first run or version update detect.");
            app_pref.edit()
                    .putBoolean("is_first_run", false)
                    .putInt("version", VERSION_CODE)
                    .apply();
            try {
                new SponsorRepo(getApplicationContext()).reset();
            } catch (Exception ignore) {
            }
            if (!app_pref.contains("auto_confirm")) {
                app_pref.edit()
                        .putBoolean("auto_confirm", false)
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
                        .putBoolean("check_update", true)
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
            if (!app_pref.contains("bh_ver")) {
                app_pref.edit()
                        .putString("bh_ver", BH_VER)
                        .apply();
            }
            if (!app_pref.contains("sp_url")) {
                app_pref.edit()
                        .putString("sp_url", SP_URL)
                        .apply();
            }
            if (!app_pref.contains("use_socket")) {
                app_pref.edit()
                        .putBoolean("use_socket", false)
                        .apply();
            }
            if (!app_pref.contains("fab_save_img")) {
                app_pref.edit()
                        .putBoolean("fab_save_img", false)
                        .apply();
            }
            if (!app_pref.contains("keep_capture")) {
                app_pref.edit()
                        .putBoolean("keep_capture", false)
                        .apply();
            }
            if (!app_pref.contains("capture_continue_before_result")) {
                app_pref.edit()
                        .putBoolean("capture_continue_before_result", false)
                        .apply();
            }
            if (!DEBUG_MODE) {
                app_pref.edit()
                        .putBoolean("no_crash_page", false)
                        .apply();
            }
            if (!app_pref.contains("adv_setting")) {
                app_pref.edit()
                        .putBoolean("adv_setting", false)
                        .putBoolean("beta_update", false)
                        .putBoolean("bh_ver_overwrite", false)
                        .putBoolean("keep_capture_no_cooling_down", false)
                        .putBoolean("no_server_tip", false)
                        .apply();
            }

        }
        if (app_pref.contains("sp_url") || app_pref.getString("sp_url", SP_URL).strip().length() > 3) {
            SP_URL = app_pref.getString("sp_url", SP_URL);
        }
        MI_ADV_MODE = app_pref.getBoolean("mi_adv_mode", MI_ADV_MODE);
        try {
            LeanCloud.initializeSecurely(getApplicationContext(), SP_APP_KEY, SP_URL);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !Tools.getDeviceModel().toLowerCase(Locale.ROOT).contains("mumu")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    NotificationChannel channel = new NotificationChannel("scanner_post_channel", "扫码器消息推送服务", NotificationManager.IMPORTANCE_DEFAULT);
                    notificationManager.createNotificationChannel(channel);
                    PushService.setDefaultChannelId(this, channel.getId());
                }
                if (DEBUG_MODE) {
                    PushService.subscribe(this, "debug", NotificationActivity.class);
                    PushService.unsubscribe(this, "release");
                } else {
                    PushService.subscribe(this, "release", NotificationActivity.class);
                    PushService.unsubscribe(this, "debug");
                }
                if (VERSION_NAME.contains("snapshot")) {
                    BETA_VER = true;
                    app_pref.edit().putBoolean("beta_update", true).apply();
                    PushService.subscribe(this, "snapshot", NotificationActivity.class);
                } else {
                    BETA_VER = app_pref.getBoolean("beta_update", false);
                    PushService.unsubscribe(this, "snapshot");
                }

                LCInstallation.getCurrentInstallation().saveInBackground().subscribe(new Observer<LCObject>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull LCObject lcObject) {
                        String installationId = LCInstallation.getCurrentInstallation().getInstallationId();
                        Tools.saveString(getApplicationContext(), "installationId", installationId);
                        Logger.d("LCPush", "init success, installationId: " + installationId);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Logger.d("LCPush", "init Failed, " + e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
            } else {
                Logger.d("PUSH Service", "temp disable push service." + Build.VERSION.SDK_INT + ":" + Tools.getDeviceModel());
            }
        } catch (UnsatisfiedLinkError error) {
            Logger.d("LeanCloud", "ABI ERROR " + error.getMessage());
            error.printStackTrace();

            StringBuilder supportedABI = new StringBuilder();
            for (String abi : Build.SUPPORTED_ABIS) {
                supportedABI.append(abi);
                supportedABI.append('\n');
            }
            DialogData dialogData = new DialogData("错误", "你所下载的版本可能不支持在当前设备上运行\n请下载正确的版本\n\n参考数据:\n" + supportedABI);
            dialogData.setPositiveButtonData(new ButtonData("我已知晓"));
            DialogLiveData.getINSTANCE().addNewDialog(dialogData);
        }

        CHECK_VER = app_pref.getBoolean("check_update", true);

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

        new Thread(() -> {

            String feedback = Network.sendGet("https://api.z-notify.zxlee.cn/v1/public/statistics/8316326835763216384?tag=" + (BETA_VER ? "snapshot" : "release") + "&from=" + VERSION_NAME, false);
            try {
                JSONObject object = new JSONObject(feedback);
                if (object.has("data")) {
                    JSONObject data = object.getJSONObject("data");
                    VISITOR_COUNT = data.getInt("visitor_count");
                    Logger.d("z-notify", "get statistics succ");
                }
            } catch (Exception ignore) {
                Logger.d("z-notify", "get statistics failed");
            }

        }).start();


        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(ActivityManager.getInstance(), filter);

        mLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START);
        mLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME);
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return mLifecycle;
    }
}

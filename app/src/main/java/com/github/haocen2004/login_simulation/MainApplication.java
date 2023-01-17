package com.github.haocen2004.login_simulation;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.github.haocen2004.login_simulation.BuildConfig.DEBUG;
import static com.github.haocen2004.login_simulation.BuildConfig.VERSION_CODE;
import static com.github.haocen2004.login_simulation.BuildConfig.VERSION_NAME;
import static com.github.haocen2004.login_simulation.util.Constant.BH_VER;
import static com.github.haocen2004.login_simulation.util.Constant.CHECK_VER;
import static com.github.haocen2004.login_simulation.util.Constant.DEBUG_MODE;
import static com.github.haocen2004.login_simulation.util.Constant.MDK_VERSION;
import static com.github.haocen2004.login_simulation.util.Constant.SP_URL;

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
import com.github.haocen2004.login_simulation.activity.MainActivity;
import com.github.haocen2004.login_simulation.data.LogLiveData;
import com.github.haocen2004.login_simulation.data.database.sponsor.SponsorRepo;
import com.github.haocen2004.login_simulation.util.CrashHandler;
import com.github.haocen2004.login_simulation.util.Logger;
import com.github.haocen2004.login_simulation.util.PmsHooker;
import com.github.haocen2004.login_simulation.util.Tools;
import com.hjq.toast.ToastUtils;
import com.tencent.bugly.crashreport.CrashReport;

import cn.leancloud.LCInstallation;
import cn.leancloud.LCObject;
import cn.leancloud.LeanCloud;
import cn.leancloud.push.PushService;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
//import com.tencent.ysdk.api.YSDKApi;

public class MainApplication extends Application implements LifecycleOwner {
    private SharedPreferences app_pref;
    private Logger Log;


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
        PmsHooker.startHook(base);
    }

    private final LifecycleRegistry mLifecycle = new LifecycleRegistry(this);

    @Override
    public void onCreate() {
        super.onCreate();
        mLifecycle.setCurrentState(Lifecycle.State.CREATED);
        LogLiveData.getINSTANCE(); //init live data
        Log = Logger.getLogger(this);
//        YSDKApi.setMainActivity("com.github.haocen2004.login_simulation.activity.MainActivity");
        ToastUtils.init(this);
        ToastUtils.setGravity(Gravity.BOTTOM, 0, 50);
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(this);
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(getApplicationContext());
        strategy.setDeviceID(Tools.getUUID(this));
        strategy.setDeviceModel(Tools.getDeviceModel());
        strategy.setCrashHandleCallback(crashHandler);
        CrashReport.setIsDevelopmentDevice(getApplicationContext(), DEBUG);
        CrashReport.initCrashReport(getApplicationContext(), "4bfa7b722e", DEBUG, strategy);
        LeanCloud.initializeSecurely(getApplicationContext(), "VMh6lRyykuNDyhXxoi996cGI-gzGzoHsz", SP_URL);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel("scanner_post_channel", "扫码器消息推送服务", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
            PushService.setDefaultChannelId(this, channel.getId());
        }
        app_pref = getDefaultSharedPreferences(this);
        DEBUG_MODE = app_pref.getBoolean("debug_mode", false) || DEBUG;
        if (DEBUG_MODE) {
            PushService.subscribe(this, "debug", MainActivity.class);
            PushService.unsubscribe(this, "release");
        } else {
            PushService.subscribe(this, "release", MainActivity.class);
            PushService.unsubscribe(this, "debug");
        }
        if (VERSION_NAME.contains("snapshot")) {
            PushService.subscribe(this, "snapshot", MainActivity.class);
        } else {
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
        if (app_pref.getBoolean("is_first_run", true) || app_pref.getInt("version", 1) < VERSION_CODE) {
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
            if (!app_pref.contains("use_socket")) {
                app_pref.edit()
                        .putBoolean("use_socket", false)
                        .apply();
            }
            if (!DEBUG_MODE) {
                app_pref.edit()
                        .putBoolean("no_crash_page", false)
                        .apply();
            }

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

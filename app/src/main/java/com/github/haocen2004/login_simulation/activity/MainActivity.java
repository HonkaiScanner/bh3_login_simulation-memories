package com.github.haocen2004.login_simulation.activity;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.github.haocen2004.login_simulation.BuildConfig.DEBUG;
import static com.github.haocen2004.login_simulation.BuildConfig.VERSION_CODE;
import static com.github.haocen2004.login_simulation.BuildConfig.VERSION_NAME;
import static com.github.haocen2004.login_simulation.data.Constant.AFD_URL;
import static com.github.haocen2004.login_simulation.data.Constant.BETA_VER;
import static com.github.haocen2004.login_simulation.data.Constant.BH_VER;
import static com.github.haocen2004.login_simulation.data.Constant.CHECK_VER;
import static com.github.haocen2004.login_simulation.data.Constant.DEBUG_MODE;
import static com.github.haocen2004.login_simulation.data.Constant.HAS_ACCOUNT;
import static com.github.haocen2004.login_simulation.data.Constant.HAS_UPDATE_THREAD;
import static com.github.haocen2004.login_simulation.data.Constant.MDK_VERSION;
import static com.github.haocen2004.login_simulation.data.Constant.QQ_GROUP_URL;
import static com.github.haocen2004.login_simulation.data.Constant.QUICK_MODE;
import static com.github.haocen2004.login_simulation.data.Constant.SP_CHECKED;
import static com.github.haocen2004.login_simulation.data.Constant.SP_URL;
import static com.github.haocen2004.login_simulation.utils.Tools.openUrl;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.github.haocen2004.login_simulation.R;
import com.github.haocen2004.login_simulation.data.dialog.ButtonData;
import com.github.haocen2004.login_simulation.data.dialog.DialogData;
import com.github.haocen2004.login_simulation.data.dialog.DialogLiveData;
import com.github.haocen2004.login_simulation.data.sponsor.database.SponsorRepo;
import com.github.haocen2004.login_simulation.databinding.ActivityMainBinding;
import com.github.haocen2004.login_simulation.utils.DialogHelper;
import com.github.haocen2004.login_simulation.utils.ForegroundCallbacks;
import com.github.haocen2004.login_simulation.utils.Logger;
import com.github.haocen2004.login_simulation.utils.Network;
import com.github.haocen2004.login_simulation.utils.Tools;
import com.king.wechat.qrcode.WeChatQRCodeDetector;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.opencv.OpenCV;

import java.util.Objects;
import java.util.concurrent.Executors;

import cn.leancloud.LCInstallation;
import cn.leancloud.LCLogger;
import cn.leancloud.LCObject;
import cn.leancloud.LCUser;
import cn.leancloud.LeanCloud;
import cn.leancloud.push.PushService;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class MainActivity extends BaseActivity implements ForegroundCallbacks.Listener {
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private SharedPreferences app_pref;
    private Logger Log;
    Handler bh_update_check_hd = new Handler(Objects.requireNonNull(Looper.myLooper())) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String feedback = data.getString("value");
            if (feedback != null) {
                try {
                    JSONObject feedback_json = new JSONObject(feedback);
                    if (feedback_json.getInt("retcode") == 0) {
                        String new_bh_ver = feedback_json.getJSONObject("data").getJSONObject("game").getJSONObject("latest").getString("version");
                        Logger.d("VersionCheck", "cloud bh ver: " + new_bh_ver);
                        app_pref.edit().putString("bh_ver", new_bh_ver).apply();
                        BH_VER = new_bh_ver;
                        return;
                    } else {
                        Log.makeToast("崩坏3版本更新失败\n" + feedback_json.getString("message"));
                    }
                } catch (Exception ignore) {
                }
            }
            BH_VER = app_pref.getString("cloud_bh_ver", BH_VER);
        }
    };
    Runnable bh_update_rb = () -> {
        String feedback = Network.sendGet("https://bh3-launcher-static.mihoyo.com/bh3_cn/mdk/launcher/api/resource?launcher_id=4", false);
        Message msg = new Message();
        Bundle data = new Bundle();
        data.putString("value", feedback);
        msg.setData(data);
        bh_update_check_hd.sendMessage(msg);
    };
    private long backTime = 0;
    private boolean catchBackAction = false;
    private boolean closeOnBackground = false;
    private Activity activity;
    @SuppressLint("HandlerLeak")
    Handler update_check_hd = new Handler(Objects.requireNonNull(Looper.myLooper())) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String feedback = data.getString("value");
            try {
                Logger.i("Update", "handleMessage: " + feedback);
                JSONObject json;
                if (feedback != null) {
                    json = new JSONObject(feedback);
                    if (json.has("disable_scanner") && json.getBoolean("disable_scanner")) {
                        Intent disableIntent = new Intent(getApplicationContext(), DisableActivity.class);
                        startActivity(disableIntent);
                        return;
                    }
                    app_pref.edit().putString("cloud_bh_ver", json.getString("bh_ver"))
                            .putString("mdk_ver", json.getString("mdk_ver"))
                            .putString("sp_url", json.getString("sp_url"))
                            .putString("afd_url", json.getString("afd_url"))
                            .putString("qq_group_url", json.getString("qq_group_url"))
                            .putString("custom_username", json.getString("default_name"))
//                            .putLong("update_time", json.getLong("update_time"))
                            .apply();
                    Logger.d("Update", "cloud ver:" + json.getInt("ver"));
                    Logger.d("Update", "local ver:" + VERSION_CODE);
//                    Logger.d("Update", "pack name contains dev:" + getPackageName().contains("dev"));
                    if (BETA_VER && json.has("beta_ver") && VERSION_CODE < json.getInt("beta_ver")) {
                        Logger.i("Update", "Open Beta Update Window");
                        DialogData dialogData = new DialogData("获取到新测试版: " + json.getString("beta_ver_name"), "更新日志：\n" +
                                json.getString("beta_logs").replaceAll("&n", "\n"));
                        dialogData.setPositiveButtonData(new ButtonData("打开更新链接") {
                            @Override
                            public void callback(DialogHelper dialogHelper) {
                                try {
                                    openUrl(json.getString("beta_update_url"), getApplicationContext());
                                } catch (Exception ignore) {
                                }
                                closeOnBackground = true;
                            }
                        });
                        dialogData.setCancelable(false);
                        DialogLiveData.getINSTANCE().addNewDialog(dialogData);
                    } else if (!getPackageName().contains("dev") && (VERSION_CODE < json.getInt("ver")) && CHECK_VER && json.getInt("ver") > app_pref.getInt("ignore_ver", 0)) {
                        Logger.i("Update", "Open Update Window");
                        showUpdateDialog(
                                json.getString("ver_name"),
                                json.getString("update_url"),
                                json.getString("logs").replaceAll("&n", "\n")
                        );
                    }

                } else {
                    Logger.d("Update", "Check Update Failed");
                    Log.makeToast("检查更新失败...");
                }
            } catch (Exception e) {
                e.printStackTrace();
                Logger.d("Update", "Check Update Failed");
                Log.makeToast("检查更新失败...");
                //app_pref.edit().putString("bh_ver", BH_VER).apply();
            }
            if (app_pref.getBoolean("bh_ver_overwrite", false)) {
                BH_VER = app_pref.getString("custom_bh_ver", BH_VER);
            } else {
                BH_VER = app_pref.getString("cloud_bh_ver", BH_VER);
            }
            MDK_VERSION = app_pref.getString("mdk_ver", MDK_VERSION);
            SP_URL = app_pref.getString("sp_url", SP_URL);
            AFD_URL = app_pref.getString("afd_url", AFD_URL);
            QQ_GROUP_URL = app_pref.getString("qq_group_url", AFD_URL);
//            UPDATE_TIME = app_pref.getLong("update_time", 0);
            if (CHECK_VER) {
                if (DEBUG_MODE) {
                    LeanCloud.setLogLevel(LCLogger.Level.DEBUG);
                }

                Executors.newSingleThreadExecutor().execute(() -> {
                    new SponsorRepo(getApplicationContext()).refreshSponsors();
                    if (app_pref.getBoolean("has_account", false)) {
                        String TAG = "sponsor login check";

                        Logger.d(TAG, "Start.");
                        LCUser.becomeWithSessionTokenInBackground(app_pref.getString("account_token", "")).subscribe(new Observer<LCUser>() {
                            public void onSubscribe(@NotNull Disposable disposable) {
                            }

                            public void onNext(@NotNull LCUser user) {
                                LCUser.changeCurrentUser(user, true);
                                HAS_ACCOUNT = true;
                                if (user.getUsername().equals("Hao_cen")) {
                                    Logger.d("PUSH", "admin registering");
                                    PushService.subscribe(activity, "admin", NotificationActivity.class);
                                } else {
                                    PushService.unsubscribe(activity, "admin");
                                }

                                LCInstallation.getCurrentInstallation().saveInBackground().subscribe(new Observer<LCObject>() {
                                    @Override
                                    public void onSubscribe(@NonNull Disposable d) {
                                    }

                                    @Override
                                    public void onNext(@NonNull LCObject lcObject) {
                                        String installationId = LCInstallation.getCurrentInstallation().getInstallationId();
                                        Tools.saveString(getApplicationContext(), "installationId", installationId);
                                        Logger.d("PUSH", "admin registered");
                                    }

                                    @Override
                                    public void onError(@NonNull Throwable e) {
                                    }

                                    @Override
                                    public void onComplete() {
                                    }
                                });
                                app_pref.edit().putBoolean("has_account", true).putString("custom_username", user.getString("custom_username")).apply();
                                Logger.d(TAG, "Succeed.");
                                SP_CHECKED = true;

                            }

                            public void onError(@NotNull Throwable throwable) {
                                LCUser.changeCurrentUser(null, true);
                                app_pref.edit().putBoolean("has_account", false)
                                        .putString("custom_username", "崩坏3扫码器用户").apply();
                                throwable.printStackTrace();
                                Logger.d(TAG, "Failed. Reset custom username");
                                Log.makeToast("赞助者身份验证已过期...");
                                SP_CHECKED = true;
                            }

                            public void onComplete() {
                            }
                        });

                    }
                });

            }
        }
    };
    Runnable update_rb = () -> {
        if (HAS_UPDATE_THREAD) return;
        HAS_UPDATE_THREAD = true;
        String feedback = Network.sendGet("https://api.scanner.hellocraft.xyz/update");
        Message msg = new Message();
        Bundle data = new Bundle();
        data.putString("value", feedback);
        msg.setData(data);
        update_check_hd.sendMessage(msg);
    };

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (YYB_INIT) {
//            YSDKApi.onActivityResult(requestCode, resultCode, data);
//        }
//        if (requestCode == REQ_TENCENT_WEB_LOGIN_CALLBACK) {
//            .onActivityResult(requestCode,resultCode,data);
//        }
//    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Logger.d("ORIENTATION", "SWITCH");
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Logger.d("ORIENTATION", "LANDSCAPE");
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Logger.d("ORIENTATION", "PORTRAIT");
        }
    }

    @Override
    public void onBackPressed() {
        if (catchBackAction) {
            long currTime = System.currentTimeMillis();
            if (System.currentTimeMillis() - backTime < 2000) {
                activityManager.clearActivity();
                super.onBackPressed();
            } else {
                Log.makeToast("再次返回来退出扫码器");
                backTime = currTime;
            }
        } else {
            super.onBackPressed();
        }
        //super.onBackPressed();
    }

    private void showUpdateDialog(String ver, String url, String logs) {
        DialogData dialogData = new DialogData("获取到新版本: " + ver, "更新日志：\n" + logs);
        dialogData.setPositiveButtonData(new ButtonData("打开更新链接") {
            @Override
            public void callback(DialogHelper dialogHelper) {
                super.callback(dialogHelper);
                openUrl("https://www.coolapk.com/apk/com.github.haocen2004.bh3_login_simulation", getApplicationContext());
            }
        });
        dialogData.setNegativeButtonData(new ButtonData("蓝奏云") {
            @Override
            public void callback(DialogHelper dialogHelper) {
                super.callback(dialogHelper);
                openUrl(url, getApplicationContext());
            }
        });
        dialogData.setNeutralButtonData(new ButtonData(getString(R.string.btn_cancel)) {
            @Override
            public void callback(DialogHelper dialogHelper) {
                final AlertDialog.Builder normalDialog = new AlertDialog.Builder(getApplicationContext());
                normalDialog.setTitle("是否关闭更新提示？");
                normalDialog.setMessage("将无法获取扫码器最新更新\n\n赞助者相关功能将同时不可用\n\n崩坏3版本号将保持更新");
                normalDialog.setPositiveButton(R.string.btn_close_update, (dialog, which) -> {
                    app_pref.edit().putBoolean("check_update", false).apply();
                    dialog.dismiss();
                    super.callback(dialogHelper);
                });
                normalDialog.setNegativeButton(R.string.btn_cancel, (dialog, which) -> {
                    dialog.dismiss();
                });

            }
        });

        DialogLiveData.getINSTANCE().addNewDialog(dialogData);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        app_pref = getDefaultSharedPreferences(this);
        Log = Logger.getLogger(this);
        ForegroundCallbacks.get(this).addListener(this);
        DialogHelper.getDialogHelper(this);
//        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(binding.mainInclude.toolbar);
        navController = Navigation.findNavController(this, R.id.hostFragment);
        appBarConfiguration = new AppBarConfiguration
                .Builder(R.id.mainFragment)
                .setOpenableLayout(binding.drawerLayout)
                .build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navigationView, navController);
        binding.textView2.setText(VERSION_NAME);
        binding.textView3.setOnClickListener(v -> openUrl("https://www.pixiv.net/artworks/89418903", this));
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            String toolbarTitle = "DEBUG WRONG TITLE";
            catchBackAction = false;
            if (destination.getId() == R.id.mainFragment) {
                toolbarTitle = getString(R.string.page_main);
                catchBackAction = true;
            }
            if (destination.getId() == R.id.reportFragment) {
                toolbarTitle = getString(R.string.list_report);
            }
            if (destination.getId() == R.id.supportFragment) {
                toolbarTitle = getString(R.string.list_pay);
            }
            if (destination.getId() == R.id.settingsFragment) {
                toolbarTitle = getString(R.string.list_settings);
            }
            binding.mainInclude.collapsingToolbarLayout.setTitle(toolbarTitle);
        });
        try {
            if (!HAS_UPDATE_THREAD) {
                OpenCV.initAsync(this);
                WeChatQRCodeDetector.init(this);
            }
        } catch (UnsatisfiedLinkError e) {
            Logger.e("ABI", "Wrong ABI");
            e.printStackTrace();
            showWrongABIDialog();
            return;
        }
        if (!HAS_UPDATE_THREAD && (VERSION_NAME.contains("dev") || DEBUG)) {
            showBetaInfoDialog();
        }

        // 新版本功能介绍
//        if (VERSION_CODE >= 24 && !app_pref.getBoolean("show150NewFeature", false)) {
//            show15NewFeatureDialog();
//        }


        // 优先读取本地数据
        BH_VER = app_pref.getBoolean("bh_ver_overwrite", false) ? app_pref.getString("custom_bh_ver", BH_VER) : app_pref.getString("bh_ver", BH_VER);
        MDK_VERSION = app_pref.getString("mdk_ver", MDK_VERSION);

        if (!HAS_UPDATE_THREAD) {
            new Thread(update_rb).start();

            if (!app_pref.getBoolean("bh_ver_overwrite", false)) {
                new Thread(bh_update_rb).start();
            }
        }

        if (Tools.getBoolean(this, "has_crash") && !app_pref.getBoolean("no_crash_page", false)) {
            Logger.d("CRASH", "has crash before");
            Intent intent = new Intent(this, CrashActivity.class);
            startActivity(intent);
        }

        Intent dataIntent = getIntent();
        if (dataIntent.hasExtra("scanner.quick")) {
            Logger.d("Shortcut", "start From ShortCut");
            boolean autoLogin = app_pref.getBoolean("auto_login", false);
            boolean lastLoginState = Tools.getBoolean(this, "last_login_succeed", false);
            if (autoLogin) {
                Logger.d("Shortcut", "auto login checked.");
                if (lastLoginState) {
                    Logger.d("Shortcut", "last login checked.");
                    QUICK_MODE = true;
                    return;
                }
            }
            Logger.d("Shortcut", "pre login check failed");
            DialogData dialogData = new DialogData("快速扫码失败", "由于某些原因 快速扫码初始化失败\n\n快速扫码快捷方式：" + (ShortcutManagerCompat.getDynamicShortcuts(this).size() > 0) + "\n自动登陆：" + autoLogin + "\n上次登陆情况：" + lastLoginState);
            dialogData.setPositiveButtonData("确认");
            DialogLiveData.getINSTANCE().addNewDialog(dialogData);
            ShortcutManagerCompat.removeAllDynamicShortcuts(this);
        }
//        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void showBetaInfoDialog() {

        DialogData dialogData = new DialogData("自动构建使用须知", "你现在使用的是自动构建版本\n请及时通过左边侧滑栏反馈bug\n每次启动该消息都会显示");
        dialogData.setPositiveButtonData("我已知晓");
        DialogLiveData.getINSTANCE().addNewDialog(dialogData);
    }

    private void showWrongABIDialog() {
        StringBuilder supportedABI = new StringBuilder();
        for (String abi : Build.SUPPORTED_ABIS) {
            supportedABI.append(abi);
            supportedABI.append('\n');
        }
        DialogData dialogData = new DialogData("错误", "你所下载的版本可能不支持在当前设备上运行\n请下载正确的版本\n\n参考数据:\n" + supportedABI);
        dialogData.setPositiveButtonData("我已知晓");
        DialogLiveData.getINSTANCE().addNewDialog(dialogData);
    }


    @Override
    public void onBecameForeground() {
    }

    @Override
    public void onBecameBackground() {
        String iconPos = app_pref.getString("icon_pos", "0");

        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(this, "com.github.haocen2004.login_simulation.activity.icon.main1"),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        if (!iconPos.equals("0") && app_pref.getBoolean("enable_icon_pos", false)) {
            pm.setComponentEnabledSetting(new ComponentName(this, "com.github.haocen2004.login_simulation.activity.icon.main" + iconPos),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        }

        if (closeOnBackground) {
            activityManager.clearActivity();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ForegroundCallbacks.get(this).removeListener(this);
    }
}

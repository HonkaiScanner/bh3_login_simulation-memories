package com.github.haocen2004.login_simulation.activity;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.github.haocen2004.login_simulation.BuildConfig.DEBUG;
import static com.github.haocen2004.login_simulation.BuildConfig.VERSION_CODE;
import static com.github.haocen2004.login_simulation.BuildConfig.VERSION_NAME;
import static com.github.haocen2004.login_simulation.util.Constant.AFD_URL;
import static com.github.haocen2004.login_simulation.util.Constant.BH_VER;
import static com.github.haocen2004.login_simulation.util.Constant.CHECK_VER;
import static com.github.haocen2004.login_simulation.util.Constant.DEBUG_MODE;
import static com.github.haocen2004.login_simulation.util.Constant.HAS_ACCOUNT;
import static com.github.haocen2004.login_simulation.util.Constant.MDK_VERSION;
import static com.github.haocen2004.login_simulation.util.Constant.QQ_GROUP_URL;
import static com.github.haocen2004.login_simulation.util.Constant.SP_CHECKED;
import static com.github.haocen2004.login_simulation.util.Constant.SP_URL;
import static com.github.haocen2004.login_simulation.util.Tools.openUrl;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.github.haocen2004.login_simulation.R;
import com.github.haocen2004.login_simulation.data.database.sponsor.SponsorRepo;
import com.github.haocen2004.login_simulation.data.dialog.ButtonData;
import com.github.haocen2004.login_simulation.data.dialog.DialogData;
import com.github.haocen2004.login_simulation.data.dialog.DialogLiveData;
import com.github.haocen2004.login_simulation.databinding.ActivityMainBinding;
import com.github.haocen2004.login_simulation.util.DialogHelper;
import com.github.haocen2004.login_simulation.util.Logger;
import com.github.haocen2004.login_simulation.util.Network;
import com.github.haocen2004.login_simulation.util.Tools;
import com.king.wechat.qrcode.WeChatQRCodeDetector;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.opencv.OpenCV;

import java.util.Objects;
import java.util.concurrent.Executors;

import cn.leancloud.LCLogger;
import cn.leancloud.LCUser;
import cn.leancloud.LeanCloud;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class MainActivity extends BaseActivity {
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private SharedPreferences app_pref;
    private Logger Log;
    private long backTime = 0;
    private boolean catchBackAction = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
//        Logger.setView(binding.getRoot());
        app_pref = getDefaultSharedPreferences(this);
        Log = Logger.getLogger(this);
        DialogHelper.getDialogHelper(this);
        Logger.d("dialogHelper", "loaded.");
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
            OpenCV.initAsync(this);
            WeChatQRCodeDetector.init(this);
        } catch (UnsatisfiedLinkError e) {
            Logger.e("ABI", "Wrong ABI");
            e.printStackTrace();
            showWrongABIDialog();
            return;
        }
        if (VERSION_NAME.contains("dev") || DEBUG) {
            showBetaInfoDialog();
        }

        // 新版本功能介绍
//        if (VERSION_CODE >= 24 && !app_pref.getBoolean("show150NewFeature", false)) {
//            show15NewFeatureDialog();
//        }


        // 优先读取本地数据
        BH_VER = app_pref.getBoolean("bh_ver_overwrite", false) ? app_pref.getString("custom_bh_ver", BH_VER) : app_pref.getString("bh_ver", BH_VER);
        MDK_VERSION = app_pref.getString("mdk_ver", MDK_VERSION);

//        if (CHECK_VER) {
        new Thread(update_rb).start();
//        }


        if (Tools.getBoolean(this, "has_crash")) {
            Logger.d("CRASH", "has crash before");
            Intent intent = new Intent(this, CrashActivity.class);
            startActivity(intent);
        }


//        }
    }

    @Override
    public void onBackPressed() {
        if (catchBackAction) {
            long currTime = System.currentTimeMillis();
            if (System.currentTimeMillis() - backTime < 2000) {
                System.exit(0);
                finish();
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
                    app_pref.edit().putString("bh_ver", json.getString("bh_ver"))
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
                    if (!getPackageName().contains("dev") && (VERSION_CODE < json.getInt("ver")) && CHECK_VER && json.getInt("ver") > app_pref.getInt("ignore_ver", 0)) {
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
                BH_VER = app_pref.getString("bh_ver", BH_VER);
            }
            MDK_VERSION = app_pref.getString("mdk_ver", MDK_VERSION);
            SP_URL = app_pref.getString("sp_url", SP_URL);
            AFD_URL = app_pref.getString("afd_url", AFD_URL);
            QQ_GROUP_URL = app_pref.getString("qq_group_url", AFD_URL);
            DEBUG_MODE = app_pref.getBoolean("debug_mode", false) || DEBUG;
//            UPDATE_TIME = app_pref.getLong("update_time", 0);
            if (CHECK_VER) {
                LeanCloud.initializeSecurely(getApplicationContext(), "VMh6lRyykuNDyhXxoi996cGI-gzGzoHsz", SP_URL);
                if (DEBUG) {
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
                                app_pref.edit().putString("custom_username", user.getString("custom_username")).apply();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if (YYB_INIT) {
//            YSDKApi.onActivityResult(requestCode, resultCode, data);
//        }
//        if (requestCode == REQ_TENCENT_WEB_LOGIN_CALLBACK) {
//            .onActivityResult(requestCode,resultCode,data);
//        }
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
                normalDialog.setTitle("是否关闭更新检查？");
                normalDialog.setMessage("将无法获取扫码器最新更新\n\n赞助者相关功能将同时不可用\n\n崩坏3版本号将保持更新");
                normalDialog.setNeutralButton("忽略本次更新", (dialog, which) -> {
                    app_pref.edit().putInt("ignore_ver", VERSION_CODE).apply();
                    dialog.dismiss();
                    super.callback(dialogHelper);
                });
                normalDialog.setPositiveButton(R.string.btn_close_update, (dialog, which) -> {
                    app_pref.edit().putBoolean("check_update", false).apply();
                    dialog.dismiss();
                    super.callback(dialogHelper);
                });
                normalDialog.setNegativeButton(R.string.btn_cancel, (dialog, which) -> {
                    dialog.dismiss();
                });
//                DialogLiveData.getINSTANCE(null).insertNewDialog(dialogData, getCloseUpdateDialog());

            }
        });

        DialogLiveData.getINSTANCE(this).addNewDialog(dialogData);
    }


    Runnable update_rb = () -> {
        String feedback = Network.sendGet("https://api.scanner.hellocraft.xyz/update", false);
        Message msg = new Message();
        Bundle data = new Bundle();
        data.putString("value", feedback);
        msg.setData(data);
        update_check_hd.sendMessage(msg);
    };

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void showBetaInfoDialog() {

        DialogData dialogData = new DialogData("自动构建使用须知", "你现在使用的是自动构建版本\n请及时通过左边侧滑栏反馈bug\n每次启动该消息都会显示");
        dialogData.setPositiveButtonData(new ButtonData("我已知晓"));
        DialogLiveData.getINSTANCE(this).addNewDialog(dialogData);
    }

    private void showWrongABIDialog() {
        StringBuilder supportedABI = new StringBuilder();
        for (String abi : Build.SUPPORTED_ABIS) {
            supportedABI.append(abi);
            supportedABI.append('\n');
        }
        DialogData dialogData = new DialogData("错误", "你所下载的版本可能不支持在当前设备上运行\n请下载正确的版本\n\n参考数据:\n" + supportedABI);
        dialogData.setPositiveButtonData(new ButtonData("我已知晓"));
        DialogLiveData.getINSTANCE(this).addNewDialog(dialogData);
    }


}

package xyz.hellocraft.login_simulation.activity;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.github.haocen2004.login_simulation.BuildConfig.DEBUG;
import static com.github.haocen2004.login_simulation.BuildConfig.VERSION_CODE;
import static com.github.haocen2004.login_simulation.BuildConfig.VERSION_NAME;
import static xyz.hellocraft.login_simulation.data.Constant.API_ENDPOINT;
import static xyz.hellocraft.login_simulation.data.Constant.BH_VER;
import static xyz.hellocraft.login_simulation.data.Constant.DISABLE_TIPS;
import static xyz.hellocraft.login_simulation.data.Constant.HAS_UPDATE_THREAD;
import static xyz.hellocraft.login_simulation.data.Constant.MDK_VERSION;
import static xyz.hellocraft.login_simulation.data.Constant.QUICK_MODE;
import static xyz.hellocraft.login_simulation.utils.Tools.openUrl;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.github.haocen2004.login_simulation.R;
import com.github.haocen2004.login_simulation.databinding.ActivityMainBinding;

import org.json.JSONObject;

import xyz.hellocraft.login_simulation.data.dialog.DialogData;
import xyz.hellocraft.login_simulation.data.dialog.DialogLiveData;
import xyz.hellocraft.login_simulation.utils.DialogHelper;
import xyz.hellocraft.login_simulation.utils.ForegroundCallbacks;
import xyz.hellocraft.login_simulation.utils.Logger;
import xyz.hellocraft.login_simulation.utils.Network;
import xyz.hellocraft.login_simulation.utils.Tools;

public class MainActivity extends BaseActivity implements ForegroundCallbacks.Listener {
    private final Handler scannerCheckerHandle = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
        }
    };
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private SharedPreferences app_pref;
    private Logger Log;
    private long backTime = 0;
    private boolean catchBackAction = false;
    @SuppressLint("HandlerLeak")

    private int lossCount = 0;
    Runnable update_rb = () -> {
        if (HAS_UPDATE_THREAD) return;
        HAS_UPDATE_THREAD = true;
        scannerCheckerHandle.postDelayed(this::delayScannerChecker, 1500);
    };

    private void delayScannerChecker() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                String ret = Network.sendGet(API_ENDPOINT + "/update", false);
                if (ret != null) {
                    try {
                        JSONObject json = new JSONObject(ret);
                        lossCount = 0;
                        if ((json.has("disable_scanner") && json.getBoolean("disable_scanner")) || (json.has("disable_version") && json.getInt("disable_version") > VERSION_CODE)) {
                            Intent disableIntent = new Intent(getApplicationContext(), DisableActivity.class);
                            startActivity(disableIntent);
                            lossCount = 5;
                        }
                        return;
                    } catch (Exception ignore) {
                    }
                }
                if (ForegroundCallbacks.get().isForeground()) {
                    lossCount++;
                    if (lossCount >= 3) {
                        DISABLE_TIPS = "无法链接验证服务\n扫码器已暂停使用\n请检查网络后重试";
                        Intent disableIntent = new Intent(getApplicationContext(), DisableActivity.class);
                        startActivity(disableIntent);
                    }
                    Logger.d("verifyCheck", String.valueOf(lossCount));
                } else {
                    Logger.d("verifyCheck", "background, stop check");
                    scannerCheckerHandle.removeCallbacks(MainActivity.this::delayScannerChecker);
                }
            }
        }.start();
        scannerCheckerHandle.removeCallbacks(this::delayScannerChecker);
        if (lossCount < 3) {
            scannerCheckerHandle.postDelayed(this::delayScannerChecker, 30000);
        }
    }
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        app_pref = getDefaultSharedPreferences(this);
        Log = Logger.getLogger(this);
        ForegroundCallbacks.get(this).addListener(this);
        DialogHelper.getDialogHelper(this);
//        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(binding.mainInclude.toolbar);
        navController = Navigation.findNavController(this, R.id.hostFragment);
        appBarConfiguration = new AppBarConfiguration.Builder(R.id.mainFragment).setOpenableLayout(binding.drawerLayout).build();
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
        if (!HAS_UPDATE_THREAD && (VERSION_NAME.contains("dev") || DEBUG)) {
            showBetaInfoDialog();
        }


        // 优先读取本地数据
//        BH_VER = app_pref.getBoolean("bh_ver_overwrite", false) ? app_pref.getString("custom_bh_ver", BH_VER) : app_pref.getString("bh_ver", BH_VER);
        BH_VER = app_pref.getString("bh_ver", BH_VER);
        MDK_VERSION = app_pref.getString("mdk_ver", MDK_VERSION);

        if (!HAS_UPDATE_THREAD) {
            new Thread(update_rb).start();
        }

        if (Tools.getBoolean(this, "has_crash") && !app_pref.getBoolean("no_crash_page", false)) {
            Logger.d("CRASH", "has crash before");
            Intent intent = new Intent(this, CrashActivity.class);
            startActivity(intent);
        }

        Intent dataIntent = getIntent();
        ComponentName component = dataIntent.getComponent();
        if (component != null) {
            String aliasClassName = component.getClassName();
            if (aliasClassName.contains("main2")) {
                if (System.currentTimeMillis() - app_pref.getLong("ft", 0) < 1000 * 60 * 30) {
                    Intent intent = new Intent(this, SbeamLoginActivity.class);
                    intent.putExtra("youlookthecode", "whyskipit");
                    startActivity(intent);
                    return;
                }
            }
        }
        if (dataIntent.hasExtra("scanner.quick")) {
            Logger.d("Shortcut", "start From ShortCut");
            boolean autoLogin = app_pref.getBoolean("auto_login", false);
            boolean lastLoginState = Tools.getBoolean(this, "last_login_succeed", false);
            if (autoLogin) {
                Logger.d("Shortcut", "auto login checked.");
                if (lastLoginState) {
                    Logger.d("Shortcut", "last login checked.");
                    QUICK_MODE = true;
                    Intent intent = new Intent(this, SplashActivity.class);
                    intent.putExtra("scanner.quick", true);
                    startActivity(intent);
                    return;
                }
            }
            Logger.d("Shortcut", "pre login check failed");
            DialogData dialogData = new DialogData("快速扫码失败", "由于某些原因 快速扫码初始化失败\n\n快速扫码快捷方式：" + (!ShortcutManagerCompat.getDynamicShortcuts(this).isEmpty()) + "\n自动登陆：" + autoLogin + "\n上次登陆情况：" + lastLoginState);
            dialogData.setPositiveButtonData("确认");
            DialogLiveData.getINSTANCE().addNewDialog(dialogData);
            ShortcutManagerCompat.removeAllDynamicShortcuts(this);
        } else {
            startActivity(new Intent(this, SplashActivity.class));
        }
//        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }

    private void showBetaInfoDialog() {

        DialogData dialogData = new DialogData("自动构建使用须知", "你现在使用的是自动构建版本\n可能会存在一些影响使用的bug或其他问题\n如遇到请及时带上崩溃日志进行反馈\n\n每次启动该消息都会显示");
        dialogData.setPositiveButtonData("我已知晓");
        DialogLiveData.getINSTANCE().addNewDialog(dialogData);
    }

    @Override
    public void onBecameForeground() {
        Logger.d("verifyCheck", "foreground, force check");
        scannerCheckerHandle.removeCallbacks(this::delayScannerChecker);
        scannerCheckerHandle.postDelayed(this::delayScannerChecker, 0);
    }

    @Override
    public void onBecameBackground() {
        Logger.d("verifyCheck", "background, stop check");
        scannerCheckerHandle.removeCallbacks(this::delayScannerChecker);
        String iconPos = app_pref.getString("icon_pos", "0");

        PackageManager pm = getPackageManager();
        for (String s : getResources().getStringArray(R.array.icon_type_values_full)) {
            if (s.equals(iconPos) && app_pref.getBoolean("enable_icon_pos", false)) continue;
            if (s.equals("0")) continue;
            pm.setComponentEnabledSetting(new ComponentName(this, "com.github.haocen2004.login_simulation.activity.icon.main" + s), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
        if (app_pref.getBoolean("enable_icon_pos", false) && !iconPos.equals("0")) {
            Logger.d("ICON", "Enable " + iconPos);
            pm.setComponentEnabledSetting(new ComponentName(this, "com.github.haocen2004.login_simulation.activity.icon.main" + iconPos), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ForegroundCallbacks.get(this).removeListener(this);
    }
}

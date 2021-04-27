package com.github.haocen2004.login_simulation.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.github.haocen2004.login_simulation.Database.Announcement.AnnouncementRepo;
import com.github.haocen2004.login_simulation.Database.Sponsor.SponsorRepo;
import com.github.haocen2004.login_simulation.R;
import com.github.haocen2004.login_simulation.databinding.ActivityMainBinding;
import com.github.haocen2004.login_simulation.util.Logger;
import com.github.haocen2004.login_simulation.util.Network;

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
import static com.github.haocen2004.login_simulation.BuildConfig.VERSION_NAME;
import static com.github.haocen2004.login_simulation.util.Constant.BH_VER;
import static com.github.haocen2004.login_simulation.util.Constant.CHECK_VER;
import static com.github.haocen2004.login_simulation.util.Constant.HAS_ACCOUNT;
import static com.github.haocen2004.login_simulation.util.Constant.MDK_VERSION;
import static com.github.haocen2004.login_simulation.util.Constant.SP_URL;
import static com.github.haocen2004.login_simulation.util.Tools.openUrl;

public class MainActivity extends AppCompatActivity {
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private SharedPreferences app_pref;
    private Logger Log;
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Logger.setView(binding.getRoot());
        app_pref = getDefaultSharedPreferences(this);
        Log = Logger.getLogger(this);
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
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            String toolbarTitle = "DEBUG WRONG TITLE";
            if (destination.getId() == R.id.mainFragment) {
                toolbarTitle = getString(R.string.page_main);
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
        if (getDefaultSharedPreferences(this).getBoolean("showBetaInfo", DEBUG)) {
            showBetaInfoDialog();
        }

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
                Logger.d("Update", "cloud ver:" + json.getInt("ver"));
                Logger.d("Update", "local ver:" + VERSION_CODE);
                Logger.d("Update", "pack name contains dev:" + getPackageName().contains("dev"));
                if (!getPackageName().contains("dev") && (VERSION_CODE < json.getInt("ver"))) {
                    Logger.i("Update", "Start Update window");
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

            Executors.newSingleThreadExecutor().execute(() -> {
                new SponsorRepo(getApplicationContext()).refreshSponsors();
                new AnnouncementRepo(activity).refreshAnnouncements();
                if (app_pref.getBoolean("has_account", false)) {
                    String TAG = "login check";

                    Logger.d(TAG, "Start.");
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

                }
            });

        }
    };


    private void showUpdateDialog(String ver, String url, String logs) {
        final AlertDialog.Builder normalDialog = new AlertDialog.Builder(this);
        normalDialog.setTitle("获取到新版本: " + ver);
        normalDialog.setMessage("更新日志：\n" + logs);
        normalDialog.setPositiveButton("打开更新链接",
                (dialog, which) -> {
                    openUrl("https://www.coolapk.com/apk/com.github.haocen2004.bh3_login_simulation", this);
                    dialog.dismiss();
                });
        normalDialog.setNeutralButton("蓝奏云",
                (dialog, which) -> {
                    openUrl(url, this);
                    dialog.dismiss();
                });
        normalDialog.setNegativeButton(R.string.btn_cancel,
                (dialog, which) -> {
                    showCloseUpdateDialog();
                    dialog.dismiss();
                });
        normalDialog.setCancelable(false);
        normalDialog.show();
    }

    private void showCloseUpdateDialog() {
        final AlertDialog.Builder normalDialog = new AlertDialog.Builder(this);
        normalDialog.setTitle("是否关闭更新检查？");
        normalDialog.setMessage("将无法获取扫码器最新更新\n\n以下功能将会一起关闭：\n赞助者列表更新\n公告更新");
        normalDialog.setPositiveButton(R.string.btn_close_update,
                (dialog, which) -> {
                    app_pref.edit().putBoolean("check_update", false).apply();
                    dialog.dismiss();
                });
        normalDialog.setNegativeButton(R.string.btn_cancel,
                (dialog, which) -> dialog.dismiss());
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

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

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


}
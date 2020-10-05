package com.github.haocen2004.login_simulation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.github.haocen2004.login_simulation.util.Network;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONObject;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.github.haocen2004.login_simulation.BuildConfig.VERSION_CODE;
import static com.github.haocen2004.login_simulation.util.Constant.BH_VER;
import static com.github.haocen2004.login_simulation.util.Tools.openUrl;

public class MainActivity extends AppCompatActivity {
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private SharedPreferences app_pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        app_pref = getDefaultSharedPreferences(this);
        if (app_pref.getBoolean("is_first_run", true) || app_pref.getInt("version", 1) != VERSION_CODE) {
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
                        .putBoolean("showBetaInfo", true)
                        .apply();
            }

        }

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        navController = Navigation.findNavController(this, R.id.hostFragment);
        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        appBarConfiguration = new AppBarConfiguration
                .Builder(R.id.mainFragment, R.id.settingsFragment)
                .setOpenableLayout(drawerLayout)
                .build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationView navigationView = findViewById(R.id.navigationView);

        NavigationUI.setupWithNavController(navigationView, navController);

        if (getDefaultSharedPreferences(this).getBoolean("showBetaInfo", true)) {
            showBetaInfoDialog();
        }
        if (!getPackageName().contains("dev")) {
            new Thread(update_rb).start();
        }
        checkPermissions();

    }

    private void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 申请权限
            if (ActivityCompat.shouldShowRequestPermissionRationale(getParent(), Manifest.permission.CAMERA)) {
                Toast.makeText(getApplicationContext(), R.string.request_permission_failed, Toast.LENGTH_SHORT).show();
            }
            if (ActivityCompat.shouldShowRequestPermissionRationale(getParent(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(getApplicationContext(), R.string.request_permission_failed, Toast.LENGTH_SHORT).show();
            }
            ActivityCompat.requestPermissions(getParent(), new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, com.github.haocen2004.login_simulation.util.Constant.REQ_PERM_CAMERA);
        }
    }

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

    private void showUpdateDialog(String code, String url) {
        final AlertDialog.Builder normalDialog = new AlertDialog.Builder(this);
        normalDialog.setTitle("获取到新版本");
        normalDialog.setMessage("请记下提取码 " + code + "\n并点击按钮前往更新");
        normalDialog.setPositiveButton("打开更新链接",
                (dialog, which) -> {
                    openUrl(url, this);
                    dialog.dismiss();
                });
        normalDialog.setCancelable(false);
        normalDialog.show();
    }

    @SuppressLint("HandlerLeak")
    Handler update_check_hd = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String feedback = data.getString("value");
            feedback = feedback.substring(1, feedback.length() - 1).replaceAll("\\\\", "");
            Log.i("Update", "handleMessage: " + feedback);
            try {
                JSONObject json = new JSONObject(feedback);
                app_pref.edit().putString("bh_ver", json.getString("bh_ver")).apply();
                if (app_pref.getInt("version", VERSION_CODE) < json.getInt("ver")) {
                    showUpdateDialog(json.getString("code"), json.getString("update_url"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("Update", "Check Update Failed");
                app_pref.edit().putString("bh_ver", "4.3.0").apply();
            }
            BH_VER = app_pref.getString("bh_ver", "4.3.0");
        }
    };
    Runnable update_rb = () -> {
        String feedback = Network.sendPost("https://service-beurmroh-1256541670.sh.apigw.tencentcs.com/version", "");
        Message msg = new Message();
        Bundle data = new Bundle();
        data.putString("value", feedback);
        msg.setData(data);
        update_check_hd.sendMessage(msg);
    };

}
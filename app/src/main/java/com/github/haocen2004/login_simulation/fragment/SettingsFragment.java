package com.github.haocen2004.login_simulation.fragment;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.github.haocen2004.login_simulation.BuildConfig.VERSION_NAME;
import static com.github.haocen2004.login_simulation.data.Constant.BH_VER;
import static com.github.haocen2004.login_simulation.data.Constant.MI_ADV_MODE;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;

import com.github.haocen2004.login_simulation.R;
import com.github.haocen2004.login_simulation.activity.ActivityManager;
import com.github.haocen2004.login_simulation.data.dialog.ButtonData;
import com.github.haocen2004.login_simulation.data.dialog.DialogData;
import com.github.haocen2004.login_simulation.data.dialog.DialogLiveData;
import com.github.haocen2004.login_simulation.utils.DialogHelper;
import com.github.haocen2004.login_simulation.utils.Logger;

import java.util.Objects;

public class SettingsFragment extends PreferenceFragmentCompat {
    private final String TAG = "Settings";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SharedPreferences app_pref = getDefaultSharedPreferences(getContext());

        findPreference("fab_save_img").setOnPreferenceChangeListener(((preference, newValue) -> {
            if ((Boolean) newValue) {
                if (app_pref.getBoolean("capture_continue_before_result", false)) {
                    DialogData dialogData = new DialogData("功能同时启用提醒", "您同时启用了自动重试功能\n这可能会产生大量图片占用存储空间\n是否继续开启本功能？");
                    dialogData.setPositiveButtonData("确认");
                    dialogData.setNegativeButtonData(new ButtonData("取消") {
                        @Override
                        public void callback(DialogHelper dialogHelper) {
                            super.callback(dialogHelper);
                            app_pref.edit().putBoolean("fab_save_img", false).apply();
                            preference.performClick();
                        }
                    });
                    DialogLiveData.getINSTANCE(null).addNewDialog(dialogData);
                }
            }
            return true;
        }));
        findPreference("capture_continue_before_result").setOnPreferenceChangeListener(((preference, newValue) -> {
            if ((Boolean) newValue) {
                if (app_pref.getBoolean("fab_save_img", false)) {
                    DialogData dialogData = new DialogData("功能同时启用提醒", "您同时启用了自动重试功能\n这可能会产生大量图片占用存储空间\n是否继续开启本功能？");
                    dialogData.setPositiveButtonData("确认");
                    dialogData.setNegativeButtonData(new ButtonData("取消") {
                        @Override
                        public void callback(DialogHelper dialogHelper) {
                            super.callback(dialogHelper);
                            app_pref.edit().putBoolean("capture_continue_before_result", false).apply();
                            preference.performClick();
                        }
                    });
                    DialogLiveData.getINSTANCE(null).addNewDialog(dialogData);
                }
            }
            return true;
        }));

        findPreference("fab_save_img").setSummary(getContext().getExternalFilesDir(null) + "/screenshot/");

        findPreference("check_update").setOnPreferenceChangeListener((preference, newValue) -> {
            if (!((Boolean) newValue)) {
                DialogData dialogData = new DialogData("是否关闭更新提示？", "将无法获取扫码器最新更新\n\n赞助者相关功能将同时不可用\n\n崩坏3版本号将保持更新");
                dialogData.setPositiveButtonData("关闭更新提示");
                dialogData.setNegativeButtonData(new ButtonData("取消") {
                    @Override
                    public void callback(DialogHelper dialogHelper) {
                        super.callback(dialogHelper);
                        app_pref.edit().putBoolean("check_update", true).apply();
                        preference.performClick();
                    }
                });
                DialogLiveData.getINSTANCE(null).addNewDialog(dialogData);
            }
            return true;
        });

        findPreference("dark_type").setOnPreferenceChangeListener((preference, newValue) -> {
            switch (newValue.toString()) {
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
            return true;
        });
        findPreference("server_type").setOnPreferenceChangeListener((preference, newValue) -> {
            switch (newValue.toString()) {
                case "Official":
                    preference.setSummary(getString(R.string.types_official));
                    break;
                case "Bilibili":
                    preference.setSummary(getString(R.string.types_bilibili));
                    break;
                case "Xiaomi":
                    preference.setSummary(getString(R.string.types_xiaomi));
                    break;
                case "UC":
                    preference.setSummary(getString(R.string.types_uc));
                    break;
                case "Vivo":
                    preference.setSummary(getString(R.string.types_vivo));
                    break;
                case "Oppo":
                    preference.setSummary(getString(R.string.types_oppo));
                    break;
                case "Flyme":
                    preference.setSummary(getString(R.string.types_flyme));
                    break;
                case "YYB":
                    preference.setSummary(getString(R.string.types_yyb));
                    break;
                case "Qihoo":
                    preference.setSummary(getString(R.string.types_qihoo));
                    break;
                case "Huawei":
                    preference.setSummary(R.string.types_huawei);
                    break;
                default:
                    preference.setSummary("DEBUG -- SERVER ERROR");
            }
            return true;
        });

        switch (Objects.requireNonNull(app_pref.getString("server_type", "1"))) {
            case "Official":
                findPreference("server_type").setSummary(getString(R.string.types_official));
                break;
            case "Bilibili":
                findPreference("server_type").setSummary(getString(R.string.types_bilibili));
                break;
            case "Xiaomi":
                findPreference("server_type").setSummary(getString(R.string.types_xiaomi));
                break;
            case "UC":
                findPreference("server_type").setSummary(getString(R.string.types_uc));
                break;
            case "Vivo":
                findPreference("server_type").setSummary(getString(R.string.types_vivo));
                break;
            case "Oppo":
                findPreference("server_type").setSummary(getString(R.string.types_oppo));
                break;
            case "Flyme":
                findPreference("server_type").setSummary(getString(R.string.types_flyme));
                break;
            case "YYB":
                findPreference("server_type").setSummary(getString(R.string.types_yyb));
                break;
            case "Qihoo":
                findPreference("server_type").setSummary(getString(R.string.types_qihoo));
                break;
            case "Huawei":
                findPreference("server_type").setSummary(R.string.types_huawei);
                break;
            default:
                findPreference("server_type").setSummary("DEBUG -- SERVER ERROR");
        }

        if (!app_pref.getBoolean("adv_setting", false)) {
            PreferenceCategory hiddenGroup = findPreference("adv_group");
            if (hiddenGroup != null) {
                getPreferenceScreen().removePreference(hiddenGroup);
            }
        } else {
            findPreference("bh_ver_overwrite").setOnPreferenceChangeListener((preference, newValue) -> {
                if ((Boolean) newValue) {
                    DialogData dialogData = new DialogData("确认启用自定义版本号？", "通常只在官方接口下线时启用\n一般无需开启此选项\n\n开启后将不自动获取崩坏3版本号\n必须每次手动设置");
                    dialogData.setPositiveButtonData("启用自定义版本号");
                    dialogData.setNegativeButtonData(new ButtonData("取消") {
                        @Override
                        public void callback(DialogHelper dialogHelper) {
                            super.callback(dialogHelper);
                            app_pref.edit().putBoolean("bh_ver_overwrite", false).apply();
                            preference.performClick();
                        }
                    });
                    DialogLiveData.getINSTANCE(null).addNewDialog(dialogData);
                }
                return true;
            });

            findPreference("beta_update").setOnPreferenceChangeListener((preference, newValue) -> {
                if (VERSION_NAME.contains("snapshot")) {
                    return false;
                }
                if ((Boolean) newValue) {
                    DialogData dialogData = new DialogData("确认启用测试版更新检测？", "测试版使用须知：\n" +
                            "测试版可能会存在一些影响使用的问题 请及时反馈\n" +
                            "反馈问题请详细描述 或使用扫码器内置的bug反馈 日志查看功能\n" +
                            "测试版更新通常为强制更新 即无法关闭更新提示窗口");
                    dialogData.setPositiveButtonData("启用测试版更新检测");
                    dialogData.setNegativeButtonData(new ButtonData("取消") {
                        @Override
                        public void callback(DialogHelper dialogHelper) {
                            super.callback(dialogHelper);
                            app_pref.edit().putBoolean("beta_update", false).apply();
                            preference.performClick();
                        }
                    });
                    DialogLiveData.getINSTANCE(null).addNewDialog(dialogData);
                }
                return true;
            });


            findPreference("keep_capture_no_cooling_down").setOnPreferenceChangeListener((preference, newValue) -> {
                if ((Boolean) newValue) {
                    DialogData dialogData = new DialogData("确认启用持续扫码无冷却？", "可能会占用大量CPU资源而使得扫码器被系统杀死");
                    dialogData.setPositiveButtonData("启用持续扫码无冷却");
                    dialogData.setNegativeButtonData(new ButtonData("取消") {
                        @Override
                        public void callback(DialogHelper dialogHelper) {
                            super.callback(dialogHelper);
                            app_pref.edit().putBoolean("keep_capture_no_cooling_down", false).apply();
                            preference.performClick();
                        }
                    });
                    DialogLiveData.getINSTANCE(null).addNewDialog(dialogData);
                }
                return true;
            });

            findPreference("debug_mode").setOnPreferenceChangeListener((preference, newValue) -> {
                if ((Boolean) newValue) {
                    DialogData dialogData = new DialogData("确认启用调试模式？", "该选项应该只在扫码器作者要求启用时打开\n\n需要重启扫码器才能生效\n确认启用将同时关闭扫码器");
                    dialogData.setPositiveButtonData(new ButtonData("启用调试模式") {
                        @Override
                        public void callback(DialogHelper dialogHelper) {
                            super.callback(dialogHelper);
                            ActivityManager.getInstance().clearActivity();
                        }
                    });
                    dialogData.setNegativeButtonData(new ButtonData("取消") {
                        @Override
                        public void callback(DialogHelper dialogHelper) {
                            super.callback(dialogHelper);
                            app_pref.edit().putBoolean("debug_mode", false).apply();
                            preference.performClick();
                        }
                    });
                    DialogLiveData.getINSTANCE(null).addNewDialog(dialogData);
                }
                return true;
            });

            findPreference("bh_ver_overwrite").setOnPreferenceChangeListener((preference, newValue) -> {
                if ((Boolean) newValue) {
                    BH_VER = app_pref.getString("custom_bh_ver", BH_VER);
                } else {
                    BH_VER = app_pref.getString("bh_ver", BH_VER);
                }
                Logger.d(TAG, "new bh version:" + BH_VER);
                return true;
            });

            findPreference("custom_bh_ver").setOnPreferenceChangeListener((preference, newValue) -> {
                Logger.d(TAG, newValue.toString());
                if (app_pref.getBoolean("bh_ver_overwrite", false)) {
                    BH_VER = newValue.toString();
                }
                Logger.d(TAG, "new bh version:" + BH_VER);
                return true;
            });

            findPreference("mi_adv_mode").setOnPreferenceChangeListener(((preference, newValue) -> {
                MI_ADV_MODE = (boolean) newValue;
                return true;
            }));
        }
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }


}
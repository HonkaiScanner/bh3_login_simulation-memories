package com.github.haocen2004.login_simulation.fragment;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.github.haocen2004.login_simulation.util.Constant.BH_VER;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceFragmentCompat;

import com.github.haocen2004.login_simulation.R;
import com.github.haocen2004.login_simulation.util.Logger;

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
        findPreference("check_update").setOnPreferenceChangeListener((preference, newValue) -> {
            if (!((Boolean) newValue)) {
                final AlertDialog.Builder normalDialog = new AlertDialog.Builder(requireContext());
                normalDialog.setTitle("是否关闭更新检查？");
                normalDialog.setMessage("将无法获取扫码器最新更新\n\n以下功能将会一起关闭：\n赞助者列表更新\n公告更新");
                normalDialog.setPositiveButton(R.string.btn_close_update,
                        (dialog, which) -> dialog.dismiss());
                normalDialog.setNegativeButton(R.string.btn_cancel,
                        (dialog, which) -> {
                            app_pref.edit().putBoolean("check_update", true).apply();
                            preference.performClick();
                            dialog.dismiss();
                        });
                normalDialog.setCancelable(false);
                normalDialog.show();
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
            app_pref.edit().putString("custom_bh_ver", newValue.toString()).apply();
            if (app_pref.getBoolean("bh_ver_overwrite", false)) {
                BH_VER = newValue.toString();
            }
            Logger.d(TAG, "new bh version:" + BH_VER);
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
            default:
                findPreference("server_type").setSummary("DEBUG -- SERVER ERROR");
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
package xyz.hellocraft.login_simulation.utils;

import static xyz.hellocraft.login_simulation.data.Constant.FOLLOW_SYSTEM_ACCENT;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import androidx.annotation.StyleRes;
import androidx.preference.PreferenceManager;

import com.github.haocen2004.login_simulation.R;
import com.google.android.material.color.DynamicColors;

public class ThemeUtils {


    public static int getNightThemeStyleRes(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean("pure_black_theme", false) && isNightMode(context.getResources().getConfiguration())) {
            return R.style.ThemeOverlay_Black;
        } else {
            return R.style.ThemeOverlay;
        }

    }

    public static boolean isNightMode(Configuration configuration) {
        return (configuration.uiMode & Configuration.UI_MODE_NIGHT_YES) > 0;
    }

    public static boolean isSystemAccent() {
        return DynamicColors.isDynamicColorAvailable() && FOLLOW_SYSTEM_ACCENT;
    }

    public static String getColorTheme() {
        if (isSystemAccent()) {
            return "SYSTEM";
        }
        return "CUSTOM";
    }

    @StyleRes
    public static int getColorThemeStyleRes() {
        if (!getColorTheme().equals("SYSTEM")) {
            return R.style.AppBackupTheme;
        }
        return R.style.ThemeOverlay;
    }

}

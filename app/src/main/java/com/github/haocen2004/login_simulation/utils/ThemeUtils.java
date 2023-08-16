package com.github.haocen2004.login_simulation.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import androidx.preference.PreferenceManager;

import com.github.haocen2004.login_simulation.R;

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

}

package com.github.haocen2004.login_simulation.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.github.haocen2004.login_simulation.R;

public class ThemeUtils {


    public static int getNightThemeStyleRes(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean("pure_black_theme", false)) {
            return R.style.ThemeOverlay_Black;
        } else {
            return R.style.ThemeOverlay;
        }

    }

}

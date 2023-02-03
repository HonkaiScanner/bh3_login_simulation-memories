package com.github.haocen2004.login_simulation.utils;

import android.annotation.SuppressLint;
import android.content.Context;

import java.lang.reflect.Method;

public class SystemProperty {
    private final Context mContext;

    public SystemProperty(Context mContext) {
        this.mContext = mContext;
    }

    @SuppressLint("PrivateApi")
    public String getProperty(String key) {
        try {
            ClassLoader classLoader = mContext.getClassLoader();
            Class<?> SystemProperties = classLoader.loadClass("android.os.SystemProperties");
            Method methodGet = SystemProperties.getMethod("get", String.class);
            return (String) methodGet.invoke(SystemProperties, key);
        } catch (Exception e) {
            return null;
        }
    }

}

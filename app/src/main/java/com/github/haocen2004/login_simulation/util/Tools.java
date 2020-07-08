package com.github.haocen2004.login_simulation.util;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Tools {


    public static String signNew(Map<String, Object> paramMap, String paramString) {
        ArrayList<Comparable> arrayList = new ArrayList(paramMap.keySet());
        Collections.sort(arrayList);
        StringBuilder stringBuilder = new StringBuilder();
        for (Comparable str : arrayList) {
            stringBuilder.append(str);
            stringBuilder.append("=");
            stringBuilder.append(paramMap.get(str));
            stringBuilder.append("&");
        }
        return sha256HMAC(stringBuilder.toString().substring(0, -1 + stringBuilder.length()), paramString);
    }

    public static String getDeviceID(Context paramContext) {
        String str = getString(paramContext, "device_id");
        if (TextUtils.isEmpty(str)) {
            str = Settings.System.getString(paramContext.getContentResolver(), "android_id");
            if (TextUtils.isEmpty(str)) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(getDeviceModel());
                stringBuilder.append(getSystemVersion());
                stringBuilder.append(System.currentTimeMillis());
                str = stringBuilder.toString();
            }
            saveString(paramContext, "device_id", str);
        }
        return str;
    }

    public static String getDeviceModel() {
        return Build.MODEL;
    }

    public static String getSystemVersion() {
        return Build.VERSION.RELEASE;
    }
    private static String byteArrayToHexString(byte[] paramArrayOfbyte) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b = 0; paramArrayOfbyte != null && b < paramArrayOfbyte.length; b++) {
            String str = Integer.toHexString(0xFF & paramArrayOfbyte[b]);
            if (str.length() == 1)
                stringBuilder.append('0');
            stringBuilder.append(str);
        }
        return stringBuilder.toString().toLowerCase();
    }

    public static String sha256HMAC(String paramString1, String paramString2) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("sha256HMAC:");
        stringBuilder.append(paramString1);
        stringBuilder.append(" secret ");
        stringBuilder.append(paramString2);
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(paramString2.getBytes(), "HmacSHA256"));
            return byteArrayToHexString(mac.doFinal(paramString1.getBytes()));
        } catch (Exception exception) {
            return "";
        }
    }
    private static String name = "mihoyo_sdk_preference";

    public static final String getString(Context paramContext, String paramString) {
        return paramContext.getSharedPreferences(name, 0).getString(paramString, "");
    }

    public static boolean saveString(Context paramContext, String paramString1, String paramString2) {
        return paramContext.getSharedPreferences(name, 0).edit().putString(paramString1, paramString2).commit();
    }
}

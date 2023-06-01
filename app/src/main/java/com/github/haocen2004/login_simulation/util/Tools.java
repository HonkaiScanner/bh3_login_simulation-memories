package com.github.haocen2004.login_simulation.util;

import static com.github.haocen2004.login_simulation.util.Constant.ENC_DISPATCH;
import static com.github.haocen2004.login_simulation.util.Constant.OFFICIAL_TYPE;
import static com.github.haocen2004.login_simulation.util.Network.sendGet;
import static com.github.haocen2004.login_simulation.util.Network.sendPost;
import static java.lang.Integer.parseInt;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AndroidRuntimeException;

import com.github.haocen2004.login_simulation.data.RoleData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class Tools {
    private static final String TAG = "Tools";
    private static final String name = "scanner_pref";

public static void changeToWDJ(Activity activity) {
    activity.getSharedPreferences("cn.uc.gamesdk.pref", 0).edit()//.clear()
            .putString("cn.uc.gamesdk.res.flag", "r28Or5WQ9/hgeEMZQTX6Bg==")
            .putString("cn.uc.gamesdk.channelid.encrypt.aes", "stEbrNaiO7JuNgae7SN+lQ==")
            .putBoolean("cn.uc.gamesdk.brand.support_splash", true)
            .putBoolean("cn.uc.gamesdk.lib.sdk.model.first.start.814", true)
            .putString("cn.uc.gamesdk.channelid", "WJ_1")
            .putString("cn.uc.gamesdk.setting.packfile.md5", "9477d6d1f66bc094ab24cdb1ff8e572e")
            .putString("cn.uc.gamesdk.systemConfigFile.md5", "aad006450f07047effe757719e444528")
            .putString("cn.uc.gamesdk.channelid.encrypt", "bTkwBQrKJI6Ee+NjoEI=")
            .putString("cn.uc.gamesdk.html.verify", "a16c3a5c249d6ae6721533b9863b0a13")
            .putInt("cn.uc.gamesdk.appcachepolicy", 0)
            .apply();
}

    public static boolean isMIUI(Context context) {
        String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
        String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
        String KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage";

        Logger.d(TAG, "checkMIUI");
        boolean isMIUI;
        SystemProperty systemProperty = new SystemProperty(context);
        String miuiVersionCode = systemProperty.getProperty(KEY_MIUI_VERSION_CODE);
        String miuiVersionName = systemProperty.getProperty(KEY_MIUI_VERSION_NAME);
        String miuiInternalStorage = systemProperty.getProperty(KEY_MIUI_INTERNAL_STORAGE);
        isMIUI = (miuiVersionCode != null
                && !miuiVersionCode.equals(""))
                || (miuiVersionName != null
                && !miuiVersionName.equals(""))
                || (miuiInternalStorage != null &&
                !miuiInternalStorage.equals(""));
        Logger.d(TAG, "ro.miui.ui.version.code:" + systemProperty.getProperty(KEY_MIUI_VERSION_CODE));
        Logger.d(TAG, "ro.miui.ui.version.name:" + systemProperty.getProperty(KEY_MIUI_VERSION_NAME));
        Logger.d(TAG, "ro.miui.internal.storage:" + systemProperty.getProperty(KEY_MIUI_INTERNAL_STORAGE));

        Logger.d(TAG, "checkMIUI: " + isMIUI);
        return isMIUI;
    }

    public static String getOAServer(RoleData roleData) {
        if (ENC_DISPATCH) {
            String getOAUrl = "https://dispatch.scanner.hellocraft.xyz/v1/query_dispatch/?version=" + roleData.getOa_req_key() + "&t=" + System.currentTimeMillis();
            String feedback = sendGet(getOAUrl, false);
            return feedback;
        }
        try {

            String getOAUrl = "https://global2.bh3.com/query_dispatch?version=" + roleData.getOa_req_key() + "&t=" + System.currentTimeMillis();
            Logger.d(TAG, "getOAServer-Param: " + getOAUrl);
            String feedback = sendPost(getOAUrl, "");
            Logger.d(TAG, "getOAServer: " + feedback);
            JSONObject json1 = new JSONObject(feedback);
            if (json1.getInt("retcode") != 0) {
                Logger.getLogger(null).makeToast(json1.getString("msg"));
                return null;
            }
            JSONArray jsonArray = json1.getJSONArray("region_list");
            JSONObject json2 = jsonArray.getJSONObject(0);
            Logger.d(TAG, "Official Server Type: " + OFFICIAL_TYPE);
            if (roleData.getAccount_type().equals("1")) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.getString("name").equals(OFFICIAL_TYPE)) {
                        json2 = jsonObject;
                    }
                }
            }
            String url = json2.getString("dispatch_url");
            feedback = sendPost(url + "?version=" + roleData.getOa_req_key() + "&t=" + System.currentTimeMillis(), "");

            Logger.d(TAG, "getOAServer: " + feedback);

            return feedback;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void openUrl(String url, Context context) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.setData(uri);
        try {
            context.startActivity(intent);
        } catch (AndroidRuntimeException e) {
            Logger.d(TAG, "context requires the FLAG_ACTIVITY_NEW_TASK flag.");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    public static String getDeviceID(Context paramContext) {
        String str = getString(paramContext, "device_id");
        if (TextUtils.isEmpty(str)) {
            str = Settings.System.getString(paramContext.getContentResolver(), "android_id");
            if (TextUtils.isEmpty(str)) {
                str = getDeviceModel() +
                        getSystemVersion() +
                        System.currentTimeMillis();
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

    public static String getUUID(Context context) {
        String uuid = Tools.getString(context, "uuid");
        if (uuid.equals("")) {
            uuid = UUID.randomUUID().toString();
            Tools.saveString(context, "uuid", uuid);
        }
        return uuid;
    }

    public static String getString(Context paramContext, String paramString) {
        return paramContext.getApplicationContext().getSharedPreferences(name, 0).getString(paramString, "");
    }

    public static boolean saveInt(Context paramContext, String paramString, int paramInt) {
        return paramContext.getApplicationContext().getSharedPreferences(name, 0).edit().putInt(paramString, paramInt).commit();
    }

    public static Integer getInt(Context paramContext, String key) {
        return paramContext.getApplicationContext().getSharedPreferences(name, 0).getInt(key, 0);
    }

    public static boolean saveString(Context paramContext, String paramString1, String paramString2) {
        return paramContext.getApplicationContext().getSharedPreferences(name, 0).edit().putString(paramString1, paramString2).commit();
    }

    public static boolean saveBoolean(Context paramContext, String key, boolean value) {
        return paramContext.getApplicationContext().getSharedPreferences(name, 0).edit().putBoolean(key, value).commit();
    }

    public static boolean getBoolean(Context paramContext, String key) {
        return getBoolean(paramContext, key, false);
    }

    public static boolean getBoolean(Context paramContext, String key, boolean defaultRet) {
        return paramContext.getApplicationContext().getSharedPreferences(name, 0).getBoolean(key, defaultRet);
    }


    public static String verifyAccount(Activity activity, String channel_id, String data_json) {

        Map<String, Object> login_map = new HashMap<>();

        String device_id = Tools.getDeviceID(activity);
        login_map.put("device", device_id);
        login_map.put("app_id", 1);
        login_map.put("channel_id", parseInt(channel_id));
        login_map.put("data", data_json);
        Logger.d(TAG, login_map.toString());
        String sign = Encrypt.bh3Sign(login_map);
        ArrayList<String> arrayList = new ArrayList<>(login_map.keySet());
        Collections.sort(arrayList);

        JSONObject login_json = new JSONObject();

        try {

            for (String str : arrayList) {

                login_json.put(str, login_map.get(str));

            }
            login_json.put("sign", sign);

            Logger.d(TAG, "run: " + login_json);
            JSONObject feedback_json = null;
            String feedback = Network.sendPost("https://api-sdk.mihoyo.com/bh3_cn/combo/granter/login/v2/login", login_json.toString());

            try {
                feedback_json = new JSONObject(feedback);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (feedback_json != null) {
                if (feedback_json.getInt("retcode") == 0) {
                    Logger.addBlacklist(feedback_json.getJSONObject("data").getString("combo_token"));
                } else {
                    Logger.w(TAG, "wrong feedback: " + feedback);
                }
            }

            Logger.d(TAG, "handleMessage: " + feedback);
            return feedback;
        } catch (Exception ignore) {
            return null;
        }
    }

    public static String genRandomString(int length) {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

}

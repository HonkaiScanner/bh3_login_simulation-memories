package com.github.haocen2004.login_simulation.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import com.github.haocen2004.login_simulation.data.RoleData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.github.haocen2004.login_simulation.util.Constant.OFFICIAL_TYPE;
import static com.github.haocen2004.login_simulation.util.Network.sendPost;

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

    public static String getOAServer(RoleData roleData) {
//        http://106.14.51.73/query_gameserver?version=4.2.0_gf_pc&t=1598631898&uid=21097880
//        https://global2.bh3.com/query_dispatch  version=4.2.0_gf_android_xiaomi
//        https://global1.bh3.com/query_dispatch?version=4.2.0_gf_pc&t=1598673811
        try {
            boolean needLoop = true;
            String feedback = null;
            while (needLoop) {
                feedback = sendPost("https://global2.bh3.com/query_dispatch?version=" + roleData.getOa_req_key() + "&t=" + System.currentTimeMillis(), "");
                if (feedback != null) {
                    needLoop = false;
                }
            }
            Logger.i(TAG, "getOAServer: " + feedback);
            JSONObject json1 = new JSONObject(feedback);
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
            boolean loop = true;
            while (loop) {
                try {
                    feedback = sendPost(url + "?version=" + roleData.getOa_req_key() + "&t=" + System.currentTimeMillis(), "");
                    if (feedback != null) {
                        loop = false;
                    }
                } catch (Exception ignore) {
                }
            }
            Logger.i(TAG, "getOAServer: " + feedback);

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
        context.startActivity(intent);
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

    public static String getString(Context paramContext, String paramString) {
        return paramContext.getSharedPreferences(name, 0).getString(paramString, "");
    }

    public static boolean saveInt(Context paramContext, String paramString, int paramInt) {
        return paramContext.getSharedPreferences(name, 0).edit().putInt(paramString, paramInt).commit();
    }

    public static Integer getInt(Context paramContext, String paramString) {
        return paramContext.getSharedPreferences(name, 0).getInt(paramString, 0);
    }

    public static boolean saveString(Context paramContext, String paramString1, String paramString2) {
        return paramContext.getSharedPreferences(name, 0).edit().putString(paramString1, paramString2).commit();
    }

    public static String verifyAccount(Activity activity, String channel_id, String data_json) {

        Map<String, Object> login_map = new HashMap<>();

        String device_id = Tools.getDeviceID(activity);
        login_map.put("device", device_id);
        login_map.put("app_id", "1");
        login_map.put("channel_id", channel_id);
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

//                Logger.info(login_json.toString());
            Logger.i(TAG, "run: " + login_json.toString());
            boolean needLoop = true;
            String feedback = null;
            while (needLoop) {
                feedback = Network.sendPost("https://api-sdk.mihoyo.com/bh3_cn/combo/granter/login/v2/login", login_json.toString());
                if (feedback != null) {
                    needLoop = false;
                }
            }
            Logger.d(TAG, "handleMessage: " + feedback);
            return feedback;
//            JSONObject feedback_json = null;
//            try {
//                feedback_json = new JSONObject(feedback);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            BuglyLog.i(TAG, "handleMessage: " + feedback);
//                if (feedback_json != null) {
//                    if (feedback_json.getInt("retcode") == 0) {
//
//                        JSONObject data_json2 = feedback_json.getJSONObject("data");
//                        String combo_id = data_json2.getString("combo_id");
//                        String open_id = data_json2.getString("open_id");
//                        String combo_token = data_json2.getString("combo_token");
//                        String account_type = data_json2.getString("account_type");
//
//                        return new RoleData(activity, open_id, "", combo_id, combo_token, channel_id, account_type, oa_tag, special_tag);
//
//                    } else {
//                        return null ;
//                    }
//                } else {
//                    return null;
//                }
        } catch (Exception ignore) {
            return null;
        }
    }

}

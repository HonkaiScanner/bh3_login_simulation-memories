package xyz.hellocraft.login_simulation.utils;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.github.haocen2004.login_simulation.BuildConfig.VERSION_CODE;
import static com.github.haocen2004.login_simulation.BuildConfig.VERSION_NAME;
import static java.lang.Integer.parseInt;
import static xyz.hellocraft.login_simulation.data.Constant.API_ENDPOINT;
import static xyz.hellocraft.login_simulation.data.Constant.OPPO_ADV_MODE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AndroidRuntimeException;

import androidx.annotation.Keep;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.QRCodeEncoder;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import xyz.hellocraft.login_simulation.data.ICallback;
import xyz.hellocraft.login_simulation.data.RoleData;
import xyz.hellocraft.login_simulation.data.dialog.ButtonData;
import xyz.hellocraft.login_simulation.data.dialog.DialogData;
import xyz.hellocraft.login_simulation.data.dialog.DialogLiveData;

public class Tools {
    private static final String TAG = "Tools";
    private static final String name = "scanner_pref";
    private static String deviceId = null;

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

    public static Bitmap generateQRCode(String text, Integer size) {
        Mat mat = new Mat();
        QRCodeEncoder.create().encode(text, mat);
        while (mat.cols() < size) {
            Imgproc.resize(mat, mat, new Size(0, 0), 2, 2, Imgproc.INTER_NEAREST);
        }
        Bitmap bitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bitmap);
        Logger.d(TAG, "generating " + mat.width() + "x" + mat.height() + " qrcode");
        return bitmap;
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

    public static void showSecondConfirmDialog(String target, String value, ICallback action) {
        DialogData dialogData = new DialogData("二次确认", "确认将 " + target + " 修改为\n" + value + " 吗？");
        dialogData.setPositiveButtonData(new ButtonData("确认") {
            @Override
            public void callback(DialogHelper dialogHelper) {
                super.callback(dialogHelper);
                action.run(null);
            }
        });
        dialogData.setNegativeButtonData("取消");
        DialogLiveData.getINSTANCE().addNewDialog(dialogData);

    }

    private static final List<String> WHITELIST_FILES = List.of("sponsors_db", "shared_prefs", "VMh6lRyyinstallation");

    public static void traverseFilesAndDelete(File dir) {
        File[] files = dir.listFiles(); // 获取目录中的所有文件和文件夹
        if (files != null) {
            for (File file : files) {
                if (!WHITELIST_FILES.contains(file.getName()) && !file.getName().contains("bugly")) {
                    if (file.isDirectory()) {
                        Logger.d("delCache", "find directory: " + file.getPath());
                        traverseFilesAndDelete(file); // 递归遍历子目录
                    }
                    Logger.d("delCache", "delete file [" + file.getName() + "]: " + file.delete());

                }
            }
        }
    }

    public static String getOAServer(RoleData roleData) {
        //            if (ENC_DISPATCH) {
        Map<String, String> headerMap = new HashMap<>();
        Map<String, Object> oaMap = new HashMap<>();
        long currentMs = System.currentTimeMillis();
        String open_id = roleData.getOpen_id();
        if (TextUtils.isEmpty(open_id)) {
            open_id = roleData.getOa_req_key() + "_" + (deviceId == null ? deviceId : "null");
        }
        oaMap.put("x-req-openid", open_id);
        headerMap.put("x-req-openid", open_id);
        try {
            String deviceFp = DeviceFpManager.getInstance().getDeviceFp(null);
            headerMap.put("x-req-fp", deviceFp);
            oaMap.put("x-req-fp", deviceFp);
        } catch (Exception ignore) {
            headerMap.put("x-req-fp", "deviceFp");
            oaMap.put("x-req-fp", "deviceFp");
        }
        oaMap.put("x-req-name", VERSION_NAME + ":" + VERSION_CODE);
        oaMap.put("x-req-code", VERSION_CODE);
        headerMap.put("x-req-name", VERSION_NAME + ":" + VERSION_CODE);
        headerMap.put("x-req-code", String.valueOf(VERSION_CODE));
        oaMap.put("x-req-version", roleData.getOa_req_key());
        oaMap.put("x-req-ts", (currentMs / 2000) + "t");
        headerMap.put("x-req-version", roleData.getOa_req_key());
        String sign = Encrypt.HMACSign(oaMap, false);
        StringBuilder salt = new StringBuilder();
        StringBuilder processedSign = new StringBuilder();
        sign = new StringBuffer(sign).reverse().toString();
        int count = 0;
        int offset = 0;
        do {
            count++;
            int pos = new Random().nextInt(sign.length() - 1);
            int pos2 = pos + new Random().nextInt(4) + 4;
            pos2 = Math.min(pos2, sign.length() - 1);

            processedSign.append(sign.substring(pos, pos2));
            salt.append(pos).append("|").append(pos2).append("|");
            offset += pos;
            offset += pos2;
        } while (processedSign.length() < sign.length());
        String splitSign = processedSign.toString().substring(0, sign.length());
        oaMap.put("x-req-sign", splitSign);
        String sign2 = Encrypt.HMACSign(oaMap, false);
        headerMap.put("x-req-sign", splitSign.substring(0, 16) + sign2.substring(16));
        headerMap.put("x-req-salt", count + "|" + salt);
        if (offset % 2 == 0) offset = offset * -1;
        String getOAUrl = API_ENDPOINT + "/query_dispatch/?version=" + roleData.getOa_req_key() + "&t=" + (currentMs + offset);
        String feedback = Network.sendGet(getOAUrl, headerMap, false);
        Logger.d("GetOAServer", feedback);
//                Logger.getLogger(null).makeToast(json1.getString("msg"));
        try {
            JSONObject json1 = new JSONObject(feedback);
            if (json1.getInt("retcode") != 0) {
                Logger.getLogger(null).makeToast(json1.getString("message"));
                return null;
            }
        } catch (Exception ignore) {
            return null;
        }
        return feedback;
//            }
//            String getOAUrl = "https://global2.bh3.com/query_dispatch?version=" + roleData.getOa_req_key() + "&t=" + System.currentTimeMillis();
//            Logger.d(TAG, "getOAServer-Param: " + getOAUrl);
//            String feedback = sendPost(getOAUrl, "");
//            Logger.d(TAG, "getOAServer: " + feedback);
//            JSONObject json1 = new JSONObject(feedback);
//            if (json1.getInt("retcode") != 0) {
//                Logger.getLogger(null).makeToast(json1.getString("msg"));
//                return null;
//            }
//            JSONArray jsonArray = json1.getJSONArray("region_list");
//            JSONObject json2 = jsonArray.getJSONObject(0);
//            Logger.d(TAG, "Official Server Type: " + OFFICIAL_TYPE);
//            if (roleData.getAccount_type().equals("1")) {
//                for (int i = 0; i < jsonArray.length(); i++) {
//                    JSONObject jsonObject = jsonArray.getJSONObject(i);
//                    if (jsonObject.getString("name").equals(OFFICIAL_TYPE)) {
//                        json2 = jsonObject;
//                    }
//                }
//            }
//            String url = json2.getString("dispatch_url");
//            feedback = sendPost(url + "?version=" + roleData.getOa_req_key() + "&t=" + System.currentTimeMillis(), "");
//
//            Logger.d(TAG, "getOAServer: " + feedback);
//
//            return feedback;
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
        if (deviceId != null) return deviceId;
        deviceId = getString(paramContext, "device_id");
        if (TextUtils.isEmpty(deviceId)) {
            deviceId = Settings.System.getString(paramContext.getContentResolver(), "android_id");
            if (TextUtils.isEmpty(deviceId)) {
                deviceId = getDeviceModel() +
                        getSystemVersion() +
                        System.currentTimeMillis();
            }
            saveString(paramContext, "device_id", deviceId);
        }
        return deviceId;
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

    public static Long getLong(Context paramContext, String key) {
        return paramContext.getApplicationContext().getSharedPreferences(name, 0).getLong(key, 0L);
    }

    public static boolean saveLong(Context paramContext, String key, Long value) {
        return paramContext.getApplicationContext().getSharedPreferences(name, 0).edit().putLong(key, value).commit();
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

    public static void saveBooleanAtDefault(Context paramContext, String key, boolean value) {
        getDefaultSharedPreferences(paramContext).edit().putBoolean(key, value).apply();
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

    @Keep
    public static boolean verifyOfficialPack(Context context, String packageName) {
        if (packageName.equals("com.miHoYo.bh3.nearme.gamecenter") && !OPPO_ADV_MODE) {
            Logger.d("verifyOfficialPack", "OPPO_ADV_MODE is false, skip oppo channel verification");
            return true;
        }
        try {
            List<ApplicationInfo> allApps = context.getPackageManager().getInstalledApplications(0);
            for (ApplicationInfo ai : allApps) {
                if (ai.packageName.equals(packageName)) {
                    PackageInfo info = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
                    if (info.versionCode > 5) {
                        return true;
                    } else {
                        Logger.d("verifyOfficialPack", info.toString());
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            Logger.d("verifyOfficialPack", "Didn't find " + packageName);
            e.printStackTrace();
        }
        return false;
    }

    public static Bitmap setBitmapAlpha(Bitmap source, int alpha) {
        Bitmap result = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        paint.setAlpha(alpha); // alpha: 0（完全透明）到 255（完全不透明）
        canvas.drawBitmap(source, 0, 0, paint);
        return result;
    }

}

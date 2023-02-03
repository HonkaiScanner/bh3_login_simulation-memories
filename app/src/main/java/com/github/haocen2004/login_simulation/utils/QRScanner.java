package com.github.haocen2004.login_simulation.utils;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.github.haocen2004.login_simulation.utils.Constant.HAS_TIPS;
import static com.github.haocen2004.login_simulation.utils.Constant.TIPS;
import static com.github.haocen2004.login_simulation.utils.Logger.processWithBlackList;
import static java.lang.Integer.parseInt;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.github.haocen2004.login_simulation.R;
import com.github.haocen2004.login_simulation.activity.ActivityManager;
import com.github.haocen2004.login_simulation.data.RoleData;
import com.tencent.bugly.crashreport.CrashReport;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class QRScanner {
    private static final String TAG = "QRScanner";
    private final String device_id;
    private String open_id;
    private String open_token;
    private String combo_token;
    private String combo_id;
    private String app_id;
    private String channel_id;
    private String ticket;
    private final String account_type;
    private String biz_key;
    private final Boolean is_official;
    private Logger Log;
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            String feedback = Network.sendPost("https://api-sdk.mihoyo.com/" + biz_key + "/combo/panda/qrcode/scan", qr_check_json.toString());
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("value", feedback);
            msg.setData(data);
            handler.sendMessage(msg);
        }
    };
    private RoleData roleData;
    private final AppCompatActivity activity;
    private boolean fabMode = false;
    @SuppressLint("HandlerLeak")
    private final Handler defaultHandle = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
        }
    };
    @SuppressLint("HandlerLeak")
    Handler handler2 = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String feedback = data.getString("value");

//            Logger.debug(feedback);

            Logger.d(TAG, "handleMessage: " + feedback);

            try {
                JSONObject feedback_json = new JSONObject(feedback);
                if (feedback_json.getInt("retcode") == 0) {
                    makeToast(activity.getString(R.string.login_succeed));
                    Tools.saveInt(activity, "succ_count", Tools.getInt(activity, "succ_count") + 1);
                    new Thread(() -> Network.sendPost("https://api.scanner.hellocraft.xyz/scan_succ_upload", processWithBlackList(confirm_json.toString()), false)).start();
                    if (getDefaultSharedPreferences(activity).getBoolean("quit_on_success", false)) {

                        defaultHandle.postDelayed(() -> {
                            makeToast("自动退出已启用\n将在5s后自动退出扫码器");
                            defaultHandle.postDelayed(() -> {
                                if (getDefaultSharedPreferences(activity).getBoolean("quit_on_success", false)) {
                                    ActivityManager.getInstance().clearActivity();
                                }
                            }, 5000);

                        }, 3000);
                    }
                } else {

                    Logger.w(TAG, "handleMessage: 扫码登录失败 2");
                    makeToast("登录失败 code: " + feedback_json.getInt("retcode") + "\n" + feedback_json.getString("msg"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    private JSONObject confirm_json, qr_check_json, oaserver;
    private final Map<String, Object> qr_check_map;
    Runnable runnable2 = new Runnable() {
        @Override
        public void run() {
            Looper.prepare();
            genRequest();

            Logger.d("Network", "biz_key: " + biz_key);
            String feedback = Network.sendPost("https://api-sdk.mihoyo.com/" + biz_key + "/combo/panda/qrcode/confirm", confirm_json.toString().replace("\\/", "/"));

            Logger.d("Network", "feedback: " + feedback);

            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("value", feedback);
            msg.setData(data);
            handler2.sendMessage(msg);
        }
    };
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String feedback = data.getString("value");
//            Logger.debug(feedback);

            Logger.d(TAG, "handleMessage: " + feedback);
            try {
                JSONObject feedback_json = new JSONObject(feedback);
                if (feedback_json.getInt("retcode") == 0) {
                    if (getDefaultSharedPreferences(activity).getBoolean("auto_confirm", false)) {
                        new Thread(runnable2).start();
                    } else {
                        showNormalDialog();
                    }
                } else {
                    makeToast(activity.getString(R.string.outdate_qr));
//                    Logger.warning("二维码已过期");

                    Logger.w(TAG, "handleMessage: 二维码已过期");
                }
            } catch (JSONException e) {
                makeToast("网络请求错误！");
                CrashReport.postCatchedException(e);
                e.printStackTrace();
            }
        }
    };

    public QRScanner(AppCompatActivity activity, RoleData roleData) {

        this.activity = activity;
        device_id = Tools.getDeviceID(activity);
        open_id = roleData.getOpen_id();
        open_token = roleData.getOpen_token();
        combo_id = roleData.getCombo_id();
        combo_token = roleData.getCombo_token();
//        app_id = "1";
        channel_id = roleData.getChannel_id();
        oaserver = roleData.getOaserver();
        account_type = roleData.getAccount_type();
        qr_check_map = new HashMap<>();
        this.roleData = roleData;
        is_official = false;
        Log = Logger.getLogger(activity);

    }

    public QRScanner(AppCompatActivity activity, Boolean is_official) {
        this.activity = activity;
        this.is_official = is_official;
        device_id = Tools.getDeviceID(activity);
        account_type = "1";
        qr_check_map = new HashMap<>();
    }

    public boolean parseUrl(String[] urls) {
        for (String paramResult : urls) {
            if (paramResult.contains("qr_code_in_game.html")) {
                String[] split = paramResult.split("\\?");
                String[] param = split[1].split("&");
                for (String key : param) {
                    if (key.startsWith("ticket")) {
                        ticket = key.split("=")[1];

                        Logger.i("Parse QRCode", "ticket: " + ticket);
                    }
                    if (key.startsWith("app_id")) {
                        app_id = key.split("=")[1];

                        Logger.i("Parse QRCode", "app_id: " + app_id);
                    }
                    if (key.startsWith("biz_key")) {
                        biz_key = key.split("=")[1];

                        Logger.i("Parse QRCode", "biz_key: " + biz_key);
                    }
                }

                return true;

            } else {

                Logger.w("Parse QRCode", "Wrong QRCode,result: " + paramResult);

            }
        }
        Logger.d(TAG, Arrays.toString(urls));
        makeToast("请扫描正确的二维码");
        return false;
    }

    public void start() {
        if (app_id.contains("4")) {
            if (!account_type.equals("1")) {

                makeToast("原神登录暂时只支持官服");

                return;
            }
        }


        qr_check_map.put("device", device_id);
        qr_check_map.put("app_id", app_id);
        qr_check_map.put("ts", System.currentTimeMillis());
        qr_check_map.put("ticket", ticket);
        String sign = Encrypt.bh3Sign(qr_check_map);
        qr_check_json = new JSONObject();
        ArrayList<String> arrayList = new ArrayList<>(qr_check_map.keySet());
        Collections.sort(arrayList);
        try {
            for (String str : arrayList) {
                qr_check_json.put(str, qr_check_map.get(str));
            }
            qr_check_json.put("sign", sign);

            Logger.d(TAG, "getScanRequest: " + qr_check_json.toString());
            new Thread(runnable).start();

        } catch (Exception ignore) {
        }
    }

    public void genRequest() {

        SharedPreferences app_pref = getDefaultSharedPreferences(activity);
        JSONObject raw_json = new JSONObject();
        JSONObject payload_json = new JSONObject();
        JSONObject ext_json = new JSONObject();
        JSONObject data_json = new JSONObject();
        JSONObject dispatch_json = new JSONObject();
        confirm_json = new JSONObject();
        try {
            if (app_id.contains("4") || is_official) {
                SharedPreferences preferences = activity.getSharedPreferences("official_user_" + app_pref.getInt("official_slot", 1), Context.MODE_PRIVATE);

                raw_json.put("uid", preferences.getString("uid", ""))
                        .put("token", preferences.getString("token", ""));

                payload_json.put("raw", raw_json.toString())
                        .put("proto", "Account");


                confirm_json.put("device", device_id)
                        .put("app_id", app_id)
                        .put("ticket", ticket)
                        .put("payload", payload_json);
                return;
            }
            StringBuilder custom_name = new StringBuilder();
            custom_name.append(app_pref.getString("custom_username", "崩坏3外置扫码器用户"));

            if (HAS_TIPS) custom_name.append("\n").append(TIPS);
            String server_type;
            switch (Objects.requireNonNull(app_pref.getString("server_type", ""))) {
                case "Official":
                    switch (app_pref.getInt("official_type", 0)) {
                        case 1:
                            server_type = "全平台(桌面)服";
                            break;
                        case 2:
                            server_type = "IOS国服";
                            break;
                        default:
                            server_type = "安卓国服";
                            break;
                    }
                    break;
                case "Bilibili":
                    server_type = activity.getString(R.string.types_bilibili);
                    break;
                case "Xiaomi":
                    server_type = activity.getString(R.string.types_xiaomi);
                    break;
                case "UC":
                    server_type = activity.getString(R.string.types_uc);
                    break;
                case "Vivo":
                    server_type = activity.getString(R.string.types_vivo);
                    break;
                case "Oppo":
                    server_type = activity.getString(R.string.types_oppo);
                    break;
                case "Flyme":
                    server_type = activity.getString(R.string.types_flyme);
                    break;
                case "YYB":
                    server_type = activity.getString(R.string.types_yyb);
                    break;
                case "Huawei":
                    server_type = activity.getString(R.string.types_huawei);
                    break;
                case "Qihoo":
                    server_type = activity.getString(R.string.types_qihoo);
                    break;
                default:
                    server_type = "获取服务器错误";
            }

            custom_name.append("\n").append(server_type).append("\n");

            raw_json.put("heartbeat", false)
                    .put("open_id", open_id)
                    .put("device_id", device_id)
                    .put("app_id", app_id)
                    .put("channel_id", channel_id)
                    .put("combo_token", combo_token)
                    .put("asterisk_name", custom_name)
                    .put("combo_id", combo_id)
                    .put("account_type", account_type);

            if (roleData.isUc_sign()) {
                raw_json.put("is_wdj", app_pref.getBoolean("use_wdj", false));
            }

            if (open_token != null && !open_token.isEmpty()) {
                raw_json.put("open_token", open_token)
                        .put("guest", false);
            }


            dispatch_json.put("account_url", oaserver.getString("account_url"))
                    .put("account_url_backup", oaserver.getString("account_url_backup"))
                    .put("asset_bundle_url_list", oaserver.getJSONArray("asset_bundle_url_list"))
                    .put("ex_resource_url_list", oaserver.getJSONArray("ex_resource_url_list"))
                    .put("ex_audio_and_video_url_list", oaserver.getJSONArray("ex_audio_and_video_url_list"))
                    .put("ext", oaserver.getJSONObject("ext"))
                    .put("gameserver", oaserver.getJSONObject("gameserver"))
                    .put("gateway", oaserver.getJSONObject("gateway"))
                    .put("oaserver_url", oaserver.get("oaserver_url"))
                    .put("server_cur_time", oaserver.get("server_cur_time"))
                    .put("server_cur_timezone", oaserver.get("server_cur_timezone"))
                    .put("region_name", oaserver.getString("region_name"))
                    .put("retcode", "0")
                    .put("is_data_ready", true)
                    .put("server_ext", oaserver.getJSONObject("server_ext"));


            data_json.put("accountType", roleData.getAccountType())
                    .put("accountID", open_id)
                    .put("accountToken", combo_token)
                    .put("dispatch", dispatch_json);

            ext_json.put("data", data_json);

            payload_json.put("raw", raw_json.toString())
                    .put("proto", "Combo")
                    .put("ext", ext_json.toString().replace("\\", ""));
            confirm_json.put("device", device_id)
                    .put("app_id", parseInt(app_id))
                    .put("ts", System.currentTimeMillis())
                    .put("ticket", ticket)
                    .put("payload", payload_json);
            qr_check_map.put("payload", payload_json);
            String sign2 = Encrypt.bh3Sign(qr_check_map);
            confirm_json.put("sign", sign2);
        } catch (Exception e) {
            makeToast("扫码参数构建错误!");
//            CrashReport.postCatchedException(e);
            e.printStackTrace();
            return;
        }

        Logger.d(TAG, "genRequest: " + confirm_json.toString());
    }

    private void showNormalDialog() {

        final AlertDialog.Builder normalDialog = new AlertDialog.Builder(activity);
        normalDialog.setTitle("扫码成功");
        normalDialog.setMessage("等待确认是否登录");
        normalDialog.setPositiveButton("确定",
                (dialog, which) -> new Thread(runnable2).start());
        normalDialog.setNegativeButton("取消",
                (dialog, which) -> makeToast("登录已被用户取消"));

        normalDialog.show();
    }


    private void makeToast(String msg) {
        if (fabMode) {
            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
        } else {
            try {
                Log.makeToast(msg);
            } catch (Exception e) {
                Logger.w(TAG, "Logger Class Missing... try get it.");
                Log = Logger.getLogger(null);
                makeToast(msg);
            }
        }
    }
}

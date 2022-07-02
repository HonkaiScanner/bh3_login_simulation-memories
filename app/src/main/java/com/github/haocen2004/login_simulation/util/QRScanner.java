package com.github.haocen2004.login_simulation.util;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.github.haocen2004.login_simulation.util.Logger.processWithBlackList;
import static java.lang.Integer.parseInt;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.github.haocen2004.login_simulation.R;
import com.github.haocen2004.login_simulation.data.RoleData;
import com.tencent.bugly.crashreport.CrashReport;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


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
    private Boolean is_official = false;
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
                    new Thread(() -> Network.sendPost("https://api.scanner.hellocraft.xyz/scan_succ_upload", processWithBlackList(confirm_json.toString()), false)).start();
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1 && getDefaultSharedPreferences(activity).getBoolean("create_short_cut", false)) {
//                        ShortcutManager shortcutManager = activity.getSystemService(ShortcutManager.class);
//                        shortcutManager.addDynamicShortcuts(new ShortcutInfo.Builder(activity, "test_1").setIcon(R.mipmap.ic_launcher).setShortLabel().setLongLabel().setIntent(new Intent(activity, MainActivity.class)).build());
//                    }
                } else {
//                    Logger.warning("扫码登录失败2");

                    Logger.w(TAG, "handleMessage: 扫描登录失败2");
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

    //    private String scanResult;
//    private String ;
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


//            Map<String, Object> qr_check_map = new HashMap<>();
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

//                Logger.debug(qr_check_json.toString());


            Logger.d(TAG, "getScanRequest: " + qr_check_json.toString());
            new Thread(runnable).start();

//                String feedback = Network.sendPost("https://api-sdk.mihoyo.com/bh3_cn/combo/panda/qrcode/scan",qr_check_json.toString());
//
//                System.out.println(feedback);
//
//                JSONObject feedback_json = new JSONObject(feedback);
//                if (feedback_json.getInt("retcode") == 0){
//
//                } else {
//                    Logger.warning("扫码登录失败1");
//                }

        } catch (Exception ignore) {
        }
    }

    public void genRequest() {


        JSONObject raw_json = new JSONObject();
        JSONObject payload_json = new JSONObject();
        JSONObject ext_json = new JSONObject();
        JSONObject data_json = new JSONObject();
        JSONObject dispatch_json = new JSONObject();
        confirm_json = new JSONObject();
        try {
            if (app_id.contains("4") || is_official) {
//{"app_id":4,"device":"c3a0a429-3d2a-36d1-8a4b-255aeae8a9d5","payload":{"proto":"Account","raw":"{\"uid\":\"214525854\",\"token\":\"cScORPGe3TUxbiiVZ5nuIVp1qOErNnl7\"}"},"ticket":"5f84394af05bdb23e5ce451b"}
                SharedPreferences preferences = activity.getSharedPreferences("official_user_" + getDefaultSharedPreferences(activity).getInt("official_slot", 1), Context.MODE_PRIVATE);

                raw_json.put("uid", preferences.getString("uid", ""))
                        .put("token", preferences.getString("token", ""));

                payload_json.put("raw", raw_json.toString())
                        .put("proto", "Account");

//                    .put("ext", ext_json.toString());

            confirm_json.put("device", device_id)
                    .put("app_id", app_id)
//                    .put("ts", System.currentTimeMillis())
                    .put("ticket", ticket)
                    .put("payload", payload_json);
            return;
        }
        raw_json.put("heartbeat", false)
                .put("open_id", open_id)
                .put("device_id", device_id)
                .put("app_id", app_id)
                .put("channel_id", channel_id)
                .put("combo_token", combo_token)
                .put("asterisk_name", getDefaultSharedPreferences(activity).getString("custom_username", "崩坏3外置扫码器用户"))
                .put("combo_id", combo_id)
                .put("account_type", account_type);

        if (roleData.isUc_sign()) {
            raw_json.put("is_wdj", getDefaultSharedPreferences(activity).getBoolean("use_wdj", false));
        }
        if (!open_token.isEmpty()) {
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
//                .put("oaserver_url","http://139.196.248.220:1080")
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
                .put("ext", ext_json.toString().replace("\\",""));
            confirm_json.put("device", device_id)
                    .put("app_id", parseInt(app_id))
                    .put("ts", System.currentTimeMillis())
                    .put("ticket", ticket)
                    .put("payload", payload_json);
            qr_check_map.put("payload", payload_json);
            String sign2 = Encrypt.bh3Sign(qr_check_map);
            confirm_json.put("sign", sign2);
        } catch (Exception e) {
            makeToast("扫码参数构建错误！\n开始上传错误数据...");
            CrashReport.postCatchedException(e);
            e.printStackTrace();
            return;
        }

//        Logger.debug(confirm_json.toString());

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
        try {
            Log.makeToast(msg);
        } catch (Exception e) {
            Logger.w(TAG, "Logger Class Missing... try get it.");
            Log = Logger.getLogger(null);
            makeToast(msg);
        }
//        Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
    }
}

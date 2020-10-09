package com.github.haocen2004.login_simulation.util;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

public class QRScanner {
    private String device_id;
    private String open_id;
    private String open_token;
    private String combo_token;
    private String combo_id;
    private String app_id;
    private String channel_id;
    private String ticket;
    private String account_type;
    private static String TAG = "QRScanner";

    private RoleData roleData;

    private AppCompatActivity activity;
    private JSONObject confirm_json, qr_check_json, oaserver;
    private Map<String, Object> qr_check_map;

    //    private String scanResult;
//    private String ;
    public QRScanner(AppCompatActivity activity, RoleData roleData) {

        this.activity = activity;
        device_id = Tools.getDeviceID(activity);
        open_id = roleData.getOpen_id();
        open_token = roleData.getOpen_token();
        combo_id = roleData.getCombo_id();
        combo_token = roleData.getCombo_token();
        app_id = "1";
        channel_id = roleData.getChannel_id();
        oaserver = roleData.getOaserver();
        account_type = roleData.getAccount_type();
        qr_check_map = new HashMap<>();
        this.roleData = roleData;


    }

    public void parseUrl(String paramResult) {
        if (paramResult.contains("qr_code_in_game.html")) {
            String[] split = paramResult.split("\\?");
            String[] param = split[1].split("&");
            for (String key : param) {
                if (key.startsWith("ticket")) {
                    ticket = key.split("=")[1];
                }
            }
        } else {
            makeToast("请扫描正确的二维码");
        }
    }
    public void getScanRequest(){
//            Map<String, Object> qr_check_map = new HashMap<>();
            qr_check_map.put("device", device_id);
            qr_check_map.put("app_id","1");
            qr_check_map.put("ts", System.currentTimeMillis());
            qr_check_map.put("ticket", ticket);
            String sign = Tools.bh3Sign(qr_check_map);
            qr_check_json = new JSONObject();
            ArrayList<String> arrayList = new ArrayList<>(qr_check_map.keySet());
            Collections.sort(arrayList);
            try {
                for (String str : arrayList) {
                    qr_check_json.put(str, qr_check_map.get(str));
                }
                qr_check_json.put("sign",sign);

//                Logger.debug(qr_check_json.toString());

                Log.d(TAG, "getScanRequest: " + qr_check_json.toString());
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

            }catch (Exception ignore) {}
    }


    public void genRequest() throws JSONException {

        JSONObject raw_json = new JSONObject();
        JSONObject payload_json = new JSONObject();
        JSONObject ext_json = new JSONObject();
        JSONObject data_json = new JSONObject();
        JSONObject dispatch_json = new JSONObject();

        raw_json.put("heartbeat", false)
                .put("open_id", open_id)
                .put("device_id", device_id)
                .put("app_id", app_id)
                .put("channel_id", channel_id)
                .put("combo_token", combo_token)
                .put("asterisk_name", "崩坏3外置扫码器用户")
                .put("combo_id", combo_id)

                .put("account_type", account_type);
        if (roleData.isUc_sign()) {
            raw_json.put("is_wdj", false);
        }
        if (!open_token.isEmpty()) {
            raw_json.put("open_token", open_token)
                    .put("guest", false);
        }


        dispatch_json.put("account_url", oaserver.get("account_url"))
                .put("account_url_backup", oaserver.get("account_url_backup"))
                .put("asset_boundle_url", oaserver.get("asset_boundle_url"))
                .put("ex_resource_url", oaserver.get("ex_resource_url"))
                .put("ext", oaserver.getJSONObject("ext"))
                .put("gameserver", oaserver.getJSONObject("gameserver"))
                .put("gateway", oaserver.getJSONObject("gateway"))
                .put("oaserver_url", oaserver.get("oaserver_url"))
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
                .put("ext", ext_json.toString());

        confirm_json = new JSONObject();
        confirm_json.put("device", device_id)
                .put("app_id", app_id)
                .put("ts", System.currentTimeMillis())
                .put("ticket", ticket)
                .put("payload", payload_json);

        qr_check_map.put("payload", payload_json);
        String sign2 = Tools.bh3Sign(qr_check_map);
        confirm_json.put("sign", sign2);

//        Logger.debug(confirm_json.toString());
        Log.d(TAG, "genRequest: " + confirm_json.toString());
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String feedback = data.getString("value");
//            Logger.debug(feedback);
            Log.d(TAG, "handleMessage: " + feedback);
            try {
                JSONObject feedback_json = new JSONObject(feedback);
                if (feedback_json.getInt("retcode") == 0) {
                    if (getDefaultSharedPreferences(activity).getBoolean("auto_confirm", false)) {
                        new Thread(runnable2).start();
                    } else {
                        showNormalDialog();
                    }
                } else {
                    makeToast("二维码已过期");
//                    Logger.warning("二维码已过期");
                    Log.w(TAG, "handleMessage: 二维码已过期");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            String feedback = Network.sendPost("https://api-sdk.mihoyo.com/bh3_cn/combo/panda/qrcode/scan",qr_check_json.toString());
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("value", feedback);
            msg.setData(data);
            handler.sendMessage(msg);
        }
    };

    private void showNormalDialog(){

        final AlertDialog.Builder normalDialog = new AlertDialog.Builder(activity);
        normalDialog.setTitle("扫码成功");
        normalDialog.setMessage("等待确认是否登录");
        normalDialog.setPositiveButton("确定",
                (dialog, which) -> new Thread(runnable2).start());
        normalDialog.setNegativeButton("取消",
                (dialog, which) -> Toast.makeText(activity, "登录已被用户取消", Toast.LENGTH_LONG).show());

        normalDialog.show();
    }

    @SuppressLint("HandlerLeak")
    Handler handler2 = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String feedback = data.getString("value");

//            Logger.debug(feedback);
            Log.d(TAG, "handleMessage: " + feedback);

            try {
                JSONObject feedback_json = new JSONObject(feedback);
                if (feedback_json.getInt("retcode") == 0) {
                    makeToast("登录成功");
                } else {
//                    Logger.warning("扫码登录失败2");
                    Log.w(TAG, "handleMessage: 扫描登录失败2");
                    makeToast("登录失败 code:2");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    Runnable runnable2 = new Runnable() {
        @Override
        public void run() {
            String feedback = null;

            try {
                genRequest();
                feedback = Network.sendPost("https://api-sdk.mihoyo.com/bh3_cn/combo/panda/qrcode/confirm", confirm_json.toString());
                Network.sendPost("https://service-beurmroh-1256541670.sh.apigw.tencentcs.com/succeed", "");
                Log.i("Network", "run: succeed upload");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("value", feedback);
            msg.setData(data);
            handler2.sendMessage(msg);
        }
    };

    private void makeToast(String msg){
        Toast.makeText(activity,msg,Toast.LENGTH_LONG).show();
    }
}

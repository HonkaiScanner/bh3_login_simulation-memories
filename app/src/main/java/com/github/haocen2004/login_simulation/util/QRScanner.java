package com.github.haocen2004.login_simulation.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

public class QRScanner {
    private String device_id;
    private String uid;
    private String combo_token;
    private String asterisk_name;
    private String combo_id;
    private String app_id;
    private String channel_id;
    private String ip;
    private String oaserver_url;
    private String ticket;
    private Activity activity;
    private JSONObject confirm_json,qr_check_json;
    private Map<String,Object> qr_check_map;

//    private String scanResult;
//    private String ;
    public QRScanner(Activity activity,String uid,String combo_id,String combo_token,String app_id,String channel_id,String ip,String oaserver_url){

        this.activity = activity;
        device_id = Tools.getDeviceID(activity);
        this.uid = uid;
        this.combo_id = combo_id;
        this.combo_token = combo_token;
        this.app_id = app_id;
        this.channel_id = channel_id;
        this.ip = ip;
        this.oaserver_url = oaserver_url;
        qr_check_map = new HashMap<>();

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

                Logger.debug(qr_check_json.toString());

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
        JSONObject ext2_json = new JSONObject();
        JSONObject gateway_json = new JSONObject(); //Same with game server
        JSONObject server_ext_json = new JSONObject();

        raw_json.put("heartbeat",false)
                .put("open_id", uid)
                .put("device_id", device_id)
                .put("app_id",app_id)
                .put("channel_id", channel_id)
                .put("combo_token",combo_token)
                .put("asterisk_name", "崩坏3外置扫码器用户")
                .put("combo_id",combo_id);

        server_ext_json.put("cdkey_url","https://api-takumi.mihoyo.com/common/")
                .put("is_official","1");

        gateway_json.put("ip",ip)
//        gateway_json.put("ip","106.14.219.183")
                .put("port","15100");

        ext2_json.put("disable_msad","1")
                .put("ex_res_server_url","bundle.bh3.com/tmp/Original")
                .put("ex_res_use_http","0")
                .put("forbid_recharge","0")
                .put("is_checksum_off","0")
                .put("mtp_debug_switch","0")
                .put("mtp_level","1")
                .put("res_use_asset_boundle","1")
                .put("show_version_text","0")
                .put("update_streaming_asb","1");

        dispatch_json.put("account_url","https://gameapi.account.mihoyo.com")
                .put("account_url_backup", "http://webapi.account.mihoyo.com")
                .put("asset_boundle_url","https://bundle.bh3.com/asset_bundle/bb01/1.0")
                .put("ex_resource_url","bundle.bh3.com/tmp/Original")
                .put("ext",ext2_json)
                .put("gameserver",gateway_json)
                .put("gateway",gateway_json)
                .put("oaserver_url",oaserver_url)
//                .put("oaserver_url","http://139.196.248.220:1080")
                .put("region_name","bb01")
                .put("retcode","0")
                .put("server_ext", server_ext_json);

        data_json.put("accountType","2")
                .put("accountID",uid)
                .put("accountToken",combo_token)
                .put("dispatch",dispatch_json);

        ext_json.put("data",data_json);

        payload_json.put("raw",raw_json.toString())
                .put("proto","Combo")
                .put("ext",ext_json.toString());

        confirm_json = new JSONObject();
        confirm_json.put("device",device_id)
                .put("app_id",app_id)
                .put("ts", System.currentTimeMillis())
                .put("ticket",ticket)
                .put("payload",payload_json);

        qr_check_map.put("payload",payload_json);
        String sign2 = Tools.bh3Sign(qr_check_map);
        confirm_json.put("sign",sign2);

        Logger.debug(confirm_json.toString());

    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String feedback = data.getString("value");
            Logger.debug(feedback);

            try {
                JSONObject feedback_json = new JSONObject(feedback);
                if (feedback_json.getInt("retcode") == 0){
                    if (getDefaultSharedPreferences(activity).getBoolean("auto_confirm",false)) {
                        new Thread(runnable2).start();
                    } else {
                        showNormalDialog();
                    }
                } else {
                    Logger.warning("扫码登录失败1");
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
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new Thread(runnable2).start();
                    }
                });
        normalDialog.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(activity,"登录已被用户取消",Toast.LENGTH_LONG).show();
                    }
                });

        normalDialog.show();
    }

    @SuppressLint("HandlerLeak")
    Handler handler2 = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String feedback = data.getString("value");
            Logger.debug(feedback);

            try {
                JSONObject feedback_json = new JSONObject(feedback);
                if (feedback_json.getInt("retcode") == 0){
                    makeToast("登录成功");
                } else {
                    Logger.warning("扫码登录失败2");
                    makeToast("登录失败");
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
                feedback = Network.sendPost("https://api-sdk.mihoyo.com/bh3_cn/combo/panda/qrcode/confirm",confirm_json.toString());
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

package com.github.haocen2004.login_simulation.login;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.github.haocen2004.login_simulation.R;
import com.github.haocen2004.login_simulation.util.Logger;
import com.github.haocen2004.login_simulation.util.Network;
import com.github.haocen2004.login_simulation.util.Tools;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Official implements Login {

    private JSONObject login_json;
    private String token;
    private String uid;
    private String username;
    private String password;
    private String combo_id;
    private String combo_token;
    private Activity activity;
    private boolean isLogin;

    public Official(Activity activity){
        isLogin=false;
        this.activity = activity;
    }
    @Override
    public void login() {
        AlertDialog.Builder customizeDialog =
                new AlertDialog.Builder(activity);
        final View dialogView = LayoutInflater.from(activity)
                .inflate(R.layout.offical_login_layout,null);
        customizeDialog.setTitle("login");
        customizeDialog.setView(dialogView);
        customizeDialog.setPositiveButton("yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 获取EditView中的输入内容
                        EditText edit_text = (EditText) dialogView.findViewById(R.id.username);
                        EditText password_text = (EditText) dialogView.findViewById(R.id.password);
                        username = edit_text.getText().toString();
                        password = password_text.getText().toString();
                        loginByAccount();
                    }
                });
        customizeDialog.show();
    }

    public void loginByAccount() {

        login_json = new JSONObject();
        try {
            login_json.put("account", username);
            login_json.put("password", Tools.encryptByPublicKey(password, "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDDvekdPMHN3AYhm/vktJT+YJr7cI5DcsNKqdsx5DZX0gDuWFuIjzdwButrIYPNmRJ1G8ybDIF7oDW2eEpm5sMbL9zs\n9ExXCdvqrn51qELbqj0XxtMTIpaCHFSI50PfPpTFV9Xt/hmyVwokoOXFlAEgCn+Q\nCgGs52bFoYMtyi+xEQIDAQAB\n"));
            login_json.put("is_crypto", "true");
            new Thread(login_runnable).start();
        } catch (JSONException e) {
            e.printStackTrace();
        }

//        https://api-sdk.mihoyo.com/bh3_cn/mdk/shield/api/login?

    }
    @SuppressLint("HandlerLeak")
    Handler login_handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String feedback = data.getString("value");
            Logger.debug(feedback);

            try {
                JSONObject feedback_json = new JSONObject(feedback);
                if (feedback_json.getInt("retcode") == 0){
                    JSONObject data_json = feedback_json.getJSONObject("data");
                    JSONObject account_json = data_json.getJSONObject("account");
                    token = account_json.getString("token");
                    uid = account_json.getString("uid");
                    new Thread(login_runnable2).start();
                } else {
                    Logger.warning("登录失败");
                    Logger.warning(feedback);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    @SuppressLint("HandlerLeak")
    Handler login_handler2 = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String feedback = data.getString("value");
            Logger.debug(feedback);

            try {
                JSONObject feedback_json = new JSONObject(feedback);
                if (feedback_json.getInt("retcode") == 0){
                    JSONObject account_json = feedback_json.getJSONObject("data");
                    combo_id = account_json.getString("combo_id");
                    combo_token = account_json.getString("combo_token");
                    isLogin=true;
                    Toast.makeText(activity,"login succeed.",Toast.LENGTH_LONG).show();
                } else {
                    Logger.warning("登录失败");
                    Logger.warning(feedback);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    Runnable login_runnable = new Runnable() {
        @Override
        public void run() {
            String feedback = Network.sendPost("https://api-sdk.mihoyo.com/bh3_cn/mdk/shield/api/login",login_json.toString());
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("value", feedback);
            msg.setData(data);
            login_handler.sendMessage(msg);
        }
    };

    Runnable login_runnable2 = new Runnable() {
        @Override
        public void run() {
            Map<String, Object> login_map = new HashMap<>();

            login_map.put("device",  Tools.getDeviceID(activity.getApplicationContext()));
            login_map.put("app_id",1);
            login_map.put("channel_id",1);

            String data_json = "{\"uid\":" +
                    uid +
                    ",\"token\":\"" +
                    token +
                    "\",\"guest\":false}";

            login_map.put("data", data_json);

            String sign = Tools.bh3Sign(login_map);
            ArrayList<String> arrayList = new ArrayList<>(login_map.keySet());
            Collections.sort(arrayList);

            JSONObject login_json = new JSONObject();
            try {
                for (String str : arrayList) {

                    login_json.put(str, login_map.get(str));

                }

                login_json.put("sign", sign);

                Logger.debug(login_json.toString());
            }catch (JSONException e){
                Logger.warning("JSON PUT ERROR");
            }
            //https://api-sdk.mihoyo.com/bh3_cn/combo/granter/login/v2/login
            String feedback = Network.sendPost("https://api-sdk.mihoyo.com/bh3_cn/combo/granter/login/v2/login",login_json.toString());
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("value", feedback);
            msg.setData(data);
            login_handler2.sendMessage(msg);
        }
    };
    @Override
    public String getCombo_id() {
        return combo_id;
    }
    @Override
    public String getUid() {
        return uid;
    }


    @Override
    public void logout() {

    }
    @Override
    public String getCombo_token() {
        return combo_token;
    }
    @Override
    public boolean isLogin() {
        return isLogin;
    }
}

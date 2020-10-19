package com.github.haocen2004.login_simulation.login;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.github.haocen2004.login_simulation.R;
import com.github.haocen2004.login_simulation.util.Network;
import com.github.haocen2004.login_simulation.util.RoleData;
import com.github.haocen2004.login_simulation.util.Tools;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

public class Official implements LoginImpl {

    private JSONObject login_json;
    private String token;
    private String uid;
    private String username;
    private String password;
    private AppCompatActivity activity;
    private boolean isLogin;
    private SharedPreferences preferences;
    private static String TAG = "Official Login.";
    @SuppressLint("HandlerLeak")
    Handler login_handler = new Handler() {
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
                    JSONObject data_json = feedback_json.getJSONObject("data");
                    JSONObject account_json = data_json.getJSONObject("account");
                    token = account_json.getString("token");
                    uid = account_json.getString("uid");
                    preferences.edit()
                            .clear()
                            .putString("token", token)
                            .putString("uid", uid)
                            .putBoolean("has_token", true)
                            .apply();
                    new Thread(login_runnable2).start();
                } else {
//                    Logger.warning("登录失败");
                    Log.w(TAG, "handleMessage: 登录失败" + feedback);
//                    Logger.warning(feedback);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    Runnable login_runnable = new Runnable() {
        @Override
        public void run() {
            String feedback;
            if (!preferences.getBoolean("has_token", false)) {
                feedback = Network.sendPost("https://api-sdk.mihoyo.com/bh3_cn/mdk/shield/api/login", login_json.toString());
            } else {
                feedback = Network.sendPost("https://api-sdk.mihoyo.com/bh3_cn/mdk/shield/api/verify", login_json.toString());
            }
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("value", feedback);
            msg.setData(data);
            login_handler.sendMessage(msg);
        }
    };
    private RoleData roleData;
    @SuppressLint("HandlerLeak")
    Handler login_handler2 = new Handler() {
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
                    JSONObject account_json = feedback_json.getJSONObject("data");
                    String combo_id = account_json.getString("combo_id");
                    String combo_token = account_json.getString("combo_token");

                    roleData = new RoleData(uid, token, combo_id, combo_token, "1", "1", "");
                    isLogin = true;
                    Toast.makeText(activity, R.string.login_succeed, Toast.LENGTH_LONG).show();

                } else {
//                    Logger.warning("登录失败");
//                    Logger.warning(feedback);
                    Log.w(TAG, "handleMessage: 登录失败" + feedback);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    Runnable login_runnable2 = new Runnable() {
        @Override
        public void run() {
            Map<String, Object> login_map = new HashMap<>();

            login_map.put("device", Tools.getDeviceID(activity.getApplicationContext()));
            login_map.put("app_id", "1");
            login_map.put("channel_id", "1");

            String data_json = "{\"uid\":\"" +
                    uid +
                    "\",\"token\":\"" +
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

//                Logger.debug(login_json.toString());
                Log.d(TAG, "run: " + login_json.toString());
            } catch (JSONException e) {
//                Logger.warning("JSON PUT ERROR");
                Log.w(TAG, "run: JSON WRONG\n" + e);
            }

            //https://api-sdk.mihoyo.com/bh3_cn/combo/granter/login/v2/login
            String feedback = Network.sendPost("https://api-sdk.mihoyo.com/bh3_cn/combo/granter/login/v2/login", login_json.toString());
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("value", feedback);
            msg.setData(data);
            login_handler2.sendMessage(msg);
        }
    };

    public Official(AppCompatActivity activity) {
        isLogin = false;
        this.activity = activity;
        preferences = activity.getSharedPreferences("official_user_" + getDefaultSharedPreferences(activity).getInt("official_slot", 1), Context.MODE_PRIVATE);
    }

    @Override
    public void login() {
        if (!preferences.getBoolean("has_token", false)) {
            AlertDialog.Builder customizeDialog =
                    new AlertDialog.Builder(activity);
            final View dialogView = LayoutInflater.from(activity)
                    .inflate(R.layout.offical_login_layout, null);
            customizeDialog.setTitle(R.string.types_official);
            customizeDialog.setView(dialogView);
            customizeDialog.setPositiveButton(R.string.btn_login,
                    (dialog, which) -> {
                        // 获取EditView中的输入内容
                        EditText edit_text = dialogView.findViewById(R.id.username);
                        EditText password_text = dialogView.findViewById(R.id.password);
                        username = edit_text.getText().toString();
                        password = password_text.getText().toString();
                        loginByAccount();
                    });
            customizeDialog.show();
        } else {
            //https://api-sdk.mihoyo.com/bh3_cn/mdk/shield/api/verify?
            login_json = new JSONObject();
            try {

                login_json.put("uid", preferences.getString("uid", ""));
                login_json.put("token", preferences.getString("token", ""));

                new Thread(login_runnable).start();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void loginByAccount() {

        login_json = new JSONObject();
        try {
            login_json.put("account", username);
            login_json.put("password", Tools.encryptByPublicKey(password, "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDDvekdPMHN3AYhm/vktJT+YJr7cI5DcsNKqdsx5DZX0gDuWFuIjzdwButrIYPNmRJ1G8ybDIF7oDW2eEpm5sMbL9zs\n9ExXCdvqrn51qELbqj0XxtMTIpaCHFSI50PfPpTFV9Xt/hmyVwokoOXFlAEgCn+Q\nCgGs52bFoYMtyi+xEQIDAQAB\n"));
            login_json.put("is_crypto", "true");
            preferences.edit().putString("account", username).apply();
            new Thread(login_runnable).start();
        } catch (JSONException e) {
            e.printStackTrace();
        }

//        https://api-sdk.mihoyo.com/bh3_cn/mdk/shield/api/login?

    }

    @Override
    public void logout() {
        preferences.edit().clear().apply();
        isLogin = false;
    }

    @Override
    public RoleData getRole() {
        return roleData;
    }

    @Override
    public boolean isLogin() {
        return isLogin;
    }
}

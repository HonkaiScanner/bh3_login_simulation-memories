package com.github.haocen2004.login_simulation.login;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bsgamesdk.android.BSGameSdk;
import com.bsgamesdk.android.callbacklistener.BSGameSdkError;
import com.bsgamesdk.android.callbacklistener.CallbackListener;
import com.bsgamesdk.android.callbacklistener.InitCallbackListener;
import com.bsgamesdk.android.utils.LogUtils;
import com.github.haocen2004.login_simulation.util.Logger;
import com.github.haocen2004.login_simulation.util.Network;
import com.github.haocen2004.login_simulation.util.RoleData;
import com.github.haocen2004.login_simulation.util.Tools;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.github.haocen2004.login_simulation.util.Constant.BS_APP_KEY;

public class Bilibili implements LoginImpl {

    private String access_token;
    private String username;
    private String uid;
    private BSGameSdk gameSdk;
    private SharedPreferences preferences;
    private String device_id;
    private AppCompatActivity activity;
    private boolean isLogin;
    private RoleData roleData;

    public Bilibili(AppCompatActivity activity) {
        this.activity = activity;
        device_id = Tools.getDeviceID(activity);
        //isLogin = false;
    }

    private void doBiliLogin() {

        gameSdk.login(new CallbackListener() {

            @Override
            public void onSuccess(Bundle arg0) {
                // 此处为操作成功时执行，返回值通过Bundle传回

                uid = arg0.getString("uid");
                username = arg0.getString("username");
                access_token = arg0.getString("access_token");

                preferences.edit().clear().apply();
                preferences.edit().putString("username", username)
                        .apply();
                preferences.edit().putString("uid", uid).apply();
                doBHLogin();
            }

            @Override
            public void onFailed(BSGameSdkError arg0) {
                // 此处为操作失败时执行，返回值为BSGameSdkError类型变量，其中包含ErrorCode和ErrorMessage
                LogUtils.d("onFailed\nErrorCode : "
                        + arg0.getErrorCode() + "\nErrorMessage : "
                        + arg0.getErrorMessage());
                makeToast("onFailed\nErrorCode : "
                        + arg0.getErrorCode() + "\nErrorMessage : "
                        + arg0.getErrorMessage());
            }

            @Override
            public void onError(BSGameSdkError arg0) {
                // 此处为操作异常时执行，返回值为BSGameSdkError类型变量，其中包含ErrorCode和ErrorMessage
                LogUtils.d("onError\nErrorCode : "
                        + arg0.getErrorCode() + "\nErrorMessage : "
                        + arg0.getErrorMessage());
                makeToast("onError\nErrorCode : " + arg0.getErrorCode()
                        + "\nErrorMessage : " + arg0.getErrorMessage());
            }
        });
    }

    @Override
    public void logout() {
        isLogin = false;
        gameSdk.logout(new CallbackListener() {

            @Override
            public void onSuccess(Bundle arg0) {
                // 此处为操作成功时执行，返回值通过Bundle传回
                LogUtils.d("onSuccess");
                try {
                    preferences.edit().clear().apply();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                makeToast("账号已退出");

            }

            @Override
            public void onFailed(BSGameSdkError arg0) {
                // 此处为操作失败时执行，返回值为BSGameSdkError类型变量，其中包含ErrorCode和ErrorMessage
                LogUtils.d("onFailed\nErrorCode : "
                        + arg0.getErrorCode() + "\nErrorMessage : "
                        + arg0.getErrorMessage());
                makeToast("onFailed\nErrorCode : "
                        + arg0.getErrorCode() + "\nErrorMessage : "
                        + arg0.getErrorMessage());
            }

            @Override
            public void onError(BSGameSdkError arg0) {
                // 此处为操作异常时执行，返回值为BSGameSdkError类型变量，其中包含ErrorCode和ErrorMessage
                LogUtils.d("onError\nErrorCode : "
                        + arg0.getErrorCode() + "\nErrorMessage : "
                        + arg0.getErrorMessage());
                makeToast("onError\nErrorCode : " + arg0.getErrorCode()
                        + "\nErrorMessage : " + arg0.getErrorMessage());
            }
        });
    }

    @Override
    public void login() {

        BSGameSdk.initialize(true, activity, "590", "180",
                "378", BS_APP_KEY, new InitCallbackListener() {
                    @Override
                    public void onSuccess() {
                        Logger.info("Bilibili SDK setup succeed");
                    }

                    @Override
                    public void onFailed() {

                        Logger.warning("Bilibili SDK setup Failed");

                    }
                }, () -> System.exit(0));

        gameSdk = BSGameSdk.getInstance();
        preferences = activity.getSharedPreferences("bili_user", Context.MODE_PRIVATE);
        doBiliLogin();

    }

    @Override
    public RoleData getRole() {
        return roleData;
    }

    @SuppressLint("ShowToast")
    private void makeToast(String result) {
        try {
            Toast.makeText(activity, result, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Looper.prepare();
            Toast.makeText(activity, result, Toast.LENGTH_LONG).show();
            Looper.loop();
        }
    }

    @Override
    public boolean isLogin() {
        return isLogin;
    }

    public void doBHLogin() {

        Map<String, Object> login_map = new HashMap<>();

        login_map.put("device", device_id);
        login_map.put("app_id", "1");
        login_map.put("channel_id", "14");

        String data_json = "{\"uid\":" +
                uid +
                ",\"access_key\":\"" +
                access_token +
                "\"}";

        login_map.put("data", data_json);

        String sign = Tools.bh3Sign(login_map);
        ArrayList<String> arrayList = new ArrayList<>(login_map.keySet());
        Collections.sort(arrayList);

        JSONObject login_json = new JSONObject();

        try {

            for (String str : arrayList) {

                login_json.put(str, login_map.get(str));

            }

            login_json.put("sign",sign);

            Logger.info(login_json.toString());
            String feedback = Network.sendPost("https://api-sdk.mihoyo.com/bh3_cn/combo/granter/login/v2/login",login_json.toString());
            JSONObject feedback_json = new JSONObject(feedback);
            Logger.info(feedback);

            if (feedback_json.getInt("retcode") == 0) {

                JSONObject data_json2 = feedback_json.getJSONObject("data");
                String combo_id = data_json2.getString("combo_id");
                String combo_token = data_json2.getString("combo_token");
                String open_id = data_json2.getString("open_id");
                roleData = new RoleData(open_id, "", combo_id, combo_token, "14", "2", "bilibili");

                isLogin = true;
                makeToast("登录成功");


            } else {

                makeToast(feedback_json.getString("message"));
                isLogin = false;

            }

        }catch (Exception ignore) {}

    }

}


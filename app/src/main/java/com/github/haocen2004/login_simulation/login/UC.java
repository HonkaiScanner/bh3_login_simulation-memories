package com.github.haocen2004.login_simulation.login;

import static com.github.haocen2004.login_simulation.util.Tools.verifyAccount;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.haocen2004.login_simulation.data.RoleData;
import com.github.haocen2004.login_simulation.util.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import cn.gundam.sdk.shell.even.SDKEventKey;
import cn.gundam.sdk.shell.even.SDKEventReceiver;
import cn.gundam.sdk.shell.even.Subscribe;
import cn.gundam.sdk.shell.exception.AliLackActivityException;
import cn.gundam.sdk.shell.exception.AliNotInitException;
import cn.gundam.sdk.shell.open.ParamInfo;
import cn.gundam.sdk.shell.open.UCOrientation;
import cn.gundam.sdk.shell.param.SDKParamKey;
import cn.gundam.sdk.shell.param.SDKParams;
import cn.uc.gamesdk.UCGameSdk;

public class UC implements LoginImpl {

    private final AppCompatActivity activity;
    private final LoginCallback callback;
    private UCGameSdk sdk;
    private String sid;
    private boolean isLogin;
    private RoleData roleData;
    private static final String TAG = "UC Login";
    private final Logger Log;
    private Boolean init = false;
    private final SDKEventReceiver eventReceiver = new SDKEventReceiver() {

        @Subscribe(event = SDKEventKey.ON_INIT_SUCC)
        private void onInitSucc() {
            Logger.d(TAG, "Init SUCCEED");
            init = true;
            try {
                sdk.login(activity, null);
            } catch (AliNotInitException | AliLackActivityException e) {
                Logger.d(TAG, "Login Failed.");
                e.printStackTrace();
            }
        }

        @Subscribe(event = SDKEventKey.ON_LOGIN_SUCC)
        private void onLoginSucc(String sid) {
//            System.out.println("开始登陆" + sid);
            Logger.i(TAG, "onLoginSucc: sid:" + sid);
            setSid(sid);
            Logger.addBlacklist(sid);
            doBHLogin();
        }

        @Subscribe(event = SDKEventKey.ON_LOGIN_FAILED)
        private void onLoginFailed() {
            callback.onLoginFailed();
        }

        @Subscribe(event = SDKEventKey.ON_INIT_FAILED)
        private void onInitFailed() {
            callback.onLoginFailed();
        }

    };

    public void setSid(String sid) {
        this.sid = sid;
        Logger.addBlacklist(sid);
    }

    public UC(AppCompatActivity activity, LoginCallback callback) {
        this.callback = callback;
        this.activity = activity;
        isLogin = false;
        Log = Logger.getLogger(activity);

    }

    @Override
    public void login() {
        if (!init) {
            sdk = UCGameSdk.defaultSdk();
            sdk.registerSDKEventReceiver(this.eventReceiver);
            ParamInfo gpi = new ParamInfo();

            gpi.setGameId(654463);

            if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                gpi.setOrientation(UCOrientation.PORTRAIT);
            } else {
                gpi.setOrientation(UCOrientation.LANDSCAPE);
            }
            SDKParams sdkParams = new SDKParams();
            sdkParams.put(SDKParamKey.GAME_PARAMS, gpi);
            try {

                sdk.initSdk(activity, sdkParams);

            } catch (AliLackActivityException e) {
                e.printStackTrace();
                callback.onLoginFailed();
            }
        } else {
            try {
                Logger.d(TAG, "try to login...");
                sdk.login(activity, null);
            } catch (AliNotInitException | AliLackActivityException e) {
                Logger.d(TAG, "Login Failed.");
                callback.onLoginFailed();
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("HandlerLeak")
    Handler login_handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String feedback = data.getString("value");
//            Logger.debug(feedback);
            Logger.d(TAG, "handleMessage: " + feedback);
            JSONObject feedback_json = null;
            try {
                feedback_json = new JSONObject(feedback);
            } catch (JSONException e) {
                e.printStackTrace();
                callback.onLoginFailed();
            }
//            Logger.info(feedback);
            Logger.i(TAG, "handleMessage: " + feedback);
            try {
                if (feedback_json.getInt("retcode") == 0) {

                    JSONObject data_json2 = feedback_json.getJSONObject("data");
                    String combo_id = data_json2.getString("combo_id");
                    String open_id = data_json2.getString("open_id");
                    String combo_token = data_json2.getString("combo_token");
                    String account_type = data_json2.getString("account_type");
                    String data2 = data_json2.getString("data");
                    Logger.addBlacklist(combo_token);
                    int special_tag = 1;
                    if (data2.contains("true")) {
                        special_tag = 3;
                    }

                    roleData = new RoleData(activity, open_id, "", combo_id, combo_token, "20", account_type, "uc", special_tag, callback);

                    isLogin = true;
                } else {

                    makeToast(feedback_json.getString("message"));
                    isLogin = false;
                    callback.onLoginFailed();

                }
            } catch (JSONException e) {
                e.printStackTrace();
                callback.onLoginFailed();
            }
        }
    };

    Runnable login_runnable = new Runnable() {
        @Override
        public void run() {
            String data_json = "{\"sid\":\"" + sid + "\"}";
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("value", verifyAccount(activity, "20", data_json));
            msg.setData(data);
            login_handler.sendMessage(msg);
        }

    };

    public void doBHLogin() {
        new Thread(login_runnable).start();
    }

    private void makeToast(String result) {
        try {
            Log.makeToast(result);
        } catch (Exception e) {
            Looper.prepare();
            Log.makeToast(result);
            Looper.loop();
        }
    }

    @Override
    public void logout() {
        try {
            sdk.logout(activity, null);
            isLogin = false;
        } catch (AliLackActivityException | AliNotInitException e) {
            e.printStackTrace();
            isLogin = false;
        }
    }

    @Override
    public RoleData getRole() {
        return roleData;
    }

    @Override
    public boolean isLogin() {
        return isLogin;
    }

    @Override
    public String getUsername() {
        return sid;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }
}

package com.github.haocen2004.login_simulation.login;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.github.haocen2004.login_simulation.BuildConfig;
import com.github.haocen2004.login_simulation.R;
import com.github.haocen2004.login_simulation.data.RoleData;
import com.github.haocen2004.login_simulation.util.Logger;
import com.github.haocen2004.login_simulation.util.Tools;
import com.vivo.unionsdk.open.VivoAccountCallback;
import com.vivo.unionsdk.open.VivoUnionSDK;

import org.json.JSONException;
import org.json.JSONObject;

import static com.github.haocen2004.login_simulation.util.Constant.VIVO_APP_KEY;
import static com.github.haocen2004.login_simulation.util.Tools.verifyAccount;

public class Vivo implements LoginImpl {
    private final Activity activity;
    private boolean isLogin;
    private String uid;
    private String token;
    private RoleData roleData;
    private final String device_id;
    private static final String TAG = "Vivo Login";
    private final Logger Log;
    private final LoginCallback loginCallback;

    private final VivoAccountCallback callback = new VivoAccountCallback() {
        @Override
        public void onVivoAccountLogin(String s, String s1, String s2) {
            uid = s1;
            token = s2;
            Logger.addBlacklist(token);
            doBHLogin();
//            roleData = new RoleData()
        }

        @Override
        public void onVivoAccountLogout(int i) {
            makeToast(activity.getString(R.string.logout));
            isLogin = false;
        }

        @Override
        public void onVivoAccountLoginCancel() {
            isLogin = false;
        }
    };

    public Vivo(Activity activity, LoginCallback callback) {
        loginCallback = callback;
        this.activity = activity;
        device_id = Tools.getDeviceID(activity);
        VivoUnionSDK.initSdk(activity, VIVO_APP_KEY, BuildConfig.DEBUG);
        VivoUnionSDK.registerAccountCallback(activity, this.callback);
        Log = Logger.getLogger(activity);
    }

    @Override
    public void login() {
        VivoUnionSDK.login(activity);
    }

    @Override
    public void logout() {

    }

    @Override
    public RoleData getRole() {
        return roleData;
    }

    @Override
    public boolean isLogin() {
        return isLogin;
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
                loginCallback.onLoginFailed();
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
                    Logger.addBlacklist(combo_token);

                    roleData = new RoleData(activity, open_id, "", combo_id, combo_token, "19", account_type, "vivo", 2, loginCallback);

                    isLogin = true;
//                    makeToast(activity.getString(R.string.login_succeed));

                } else {

                    makeToast(feedback_json.getString("message"));
                    isLogin = false;
                    loginCallback.onLoginFailed();

                }
            } catch (JSONException e) {
                e.printStackTrace();
                loginCallback.onLoginFailed();
            }
        }
    };

    Runnable login_runnable = new Runnable() {
        @Override
        public void run() {
            String data_json = "{\"authtoken\":\"" + token + "\"}";
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("value", verifyAccount(activity, "19", data_json));
            msg.setData(data);
            login_handler.sendMessage(msg);
        }

    };

    public void doBHLogin() {
        new Thread(login_runnable).start();
    }

    @SuppressLint("ShowToast")
    private void makeToast(String result) {
        try {
            Log.makeToast(result);
//            Toast.makeText(activity, result, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Looper.prepare();
            Log.makeToast(result);
//            Toast.makeText(activity, result, Toast.LENGTH_LONG).show();
            Looper.loop();
        }
    }
}

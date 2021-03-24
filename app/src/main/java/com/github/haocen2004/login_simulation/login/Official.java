package com.github.haocen2004.login_simulation.login;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.github.haocen2004.login_simulation.Data.RoleData;
import com.github.haocen2004.login_simulation.R;
import com.github.haocen2004.login_simulation.util.Encrypt;
import com.github.haocen2004.login_simulation.util.Logger;
import com.github.haocen2004.login_simulation.util.Network;

import org.json.JSONException;
import org.json.JSONObject;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.github.haocen2004.login_simulation.util.Constant.BH_PUBLIC_KEY;
import static com.github.haocen2004.login_simulation.util.Tools.verifyAccount;

public class Official implements LoginImpl {

    private JSONObject login_json;
    private String token;
    private String uid;
    private String username;
    private String password;
    private final AppCompatActivity activity;
    private boolean isLogin;
    private final SharedPreferences preferences;
    private static final String TAG = "Official Login.";
    private final Logger Log;
    @SuppressLint("HandlerLeak")
    Handler login_handler = new Handler(Looper.getMainLooper()) {
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
                    Logger.w(TAG, "handleMessage: 登录失败" + feedback);
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
    Handler login_handler2 = new Handler(Looper.getMainLooper()) {
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
                    JSONObject account_json = feedback_json.getJSONObject("data");
                    String combo_id = account_json.getString("combo_id");
                    String combo_token = account_json.getString("combo_token");

                    roleData = new RoleData(activity, uid, token, combo_id, combo_token, "1", "1", "", 0);
                    isLogin = true;
                    Log.makeToast(R.string.login_succeed);

                } else {
                    Logger.w(TAG, "handleMessage: 登录失败：" + feedback);
                    Log.makeToast("登录失败：" + feedback);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    Runnable login_runnable2 = new Runnable() {
        @Override
        public void run() {
            JSONObject data_json = new JSONObject();
            try {
                data_json.put("uid", uid).put("token", token).put("guest", false);
                Message msg = new Message();
                Bundle data = new Bundle();
                data.putString("value", verifyAccount(activity, "1", data_json.toString()));
                msg.setData(data);
                login_handler2.sendMessage(msg);

            } catch (JSONException e) {
                Logger.w(TAG, "run: JSON WRONG\n" + e);
            }
        }
    };

    public Official(AppCompatActivity activity) {
        isLogin = false;
        this.activity = activity;
        preferences = activity.getSharedPreferences("official_user_" + getDefaultSharedPreferences(activity).getInt("official_slot", 1), Context.MODE_PRIVATE);
        Log = Logger.getLogger(activity);
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
            login_json.put("password", Encrypt.encryptByPublicKey(password, BH_PUBLIC_KEY));
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

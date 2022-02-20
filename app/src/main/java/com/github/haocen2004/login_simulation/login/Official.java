package com.github.haocen2004.login_simulation.login;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.github.haocen2004.login_simulation.util.Constant.BH_PUBLIC_KEY;
import static com.github.haocen2004.login_simulation.util.Constant.MDK_VERSION;
import static com.github.haocen2004.login_simulation.util.Tools.getDeviceID;
import static com.github.haocen2004.login_simulation.util.Tools.verifyAccount;

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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.geetest.sdk.GT3ConfigBean;
import com.geetest.sdk.GT3ErrorBean;
import com.geetest.sdk.GT3GeetestUtils;
import com.geetest.sdk.GT3Listener;
import com.github.haocen2004.login_simulation.R;
import com.github.haocen2004.login_simulation.data.RoleData;
import com.github.haocen2004.login_simulation.util.Encrypt;
import com.github.haocen2004.login_simulation.util.Logger;
import com.github.haocen2004.login_simulation.util.Network;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Official implements LoginImpl {

    private static final String TAG = "Official Login.";
    private final AppCompatActivity activity;
    private final SharedPreferences preferences;
    private final Logger Log;
    private JSONObject login_json;
    private final Map<String, String> login_map = new HashMap<>();
    private final LoginCallback loginCallback;
    Runnable login_runnable = new Runnable() {
        @Override
        public void run() {
            String feedback;
            while (true) {
                if (!preferences.getBoolean("has_token", false)) {
                    feedback = Network.sendPost("https://api-sdk.mihoyo.com/bh3_cn/mdk/shield/api/login", login_json.toString(), login_map);
                } else {
                    feedback = Network.sendPost("https://api-sdk.mihoyo.com/bh3_cn/mdk/shield/api/verify", login_json.toString());
                }
                if (feedback != null) {
                    break;
                }
                Log.makeToast("网络请求错误\n2s后重试");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("value", feedback);
            msg.setData(data);
            login_handler.sendMessage(msg);
        }
    };
    private JSONObject risky_check_json;
    Runnable risky_check_runnable = new Runnable() {
        @Override
        public void run() {
            Map<String, String> map = new HashMap<>();
            map.put("x-rpc-device_id", getDeviceID(activity));
            map.put("x-rpc-client_type", "2");
            map.put("x-rpc-mdk_version", MDK_VERSION);
            map.put("x-rpc-game_biz", "bh3_cn");
            map.put("x-rpc-language", "zh-cn");
            map.put("x-rpc-channel_version", MDK_VERSION);
            map.put("x-rpc-channel_id", "1");
            map.put("User-Agent", "okhttp/3.10.0");
            String feedback;
            while (true) {
                feedback = Network.sendPost("https://gameapi-account.mihoyo.com/account/risky/api/check", risky_check_json.toString(), map);
                if (feedback != null) {
                    break;
                }
                Log.makeToast("网络请求错误\n2s后重试");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("value", feedback);
            msg.setData(data);
            risky_check_handler.sendMessage(msg);
        }
    };
    private String token;
    private String email;
    private String uid;
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
                loginCallback.onLoginFailed();
            }
        }
    };
    @SuppressLint("HandlerLeak")
    Handler login_handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String feedback = data.getString("value");
//            Logger.debug(feedback);
            Logger.d(TAG, "login_handler: " + feedback);

            try {
                JSONObject feedback_json = new JSONObject(feedback);
                if (feedback_json.getInt("retcode") == 0) {
                    JSONObject data_json = feedback_json.getJSONObject("data");
                    JSONObject account_json = data_json.getJSONObject("account");
                    token = account_json.getString("token");
                    uid = account_json.getString("uid");
                    email = account_json.getString("email");
                    preferences.edit()
                            .clear()
                            .putString("token", token)
                            .putString("uid", uid)
                            .putBoolean("has_token", true)
                            .apply();
                    Logger.addBlacklist(token);
                    new Thread(login_runnable2).start();
                } else {
//                    Logger.warning("登录失败");
                    Log.makeToast("登录失败: " + feedback_json.getInt("retcode") + "\n" + feedback_json.getString("message"));
                    Logger.w(TAG, "handleMessage: 登录失败1" + feedback);
                    loginCallback.onLoginFailed();
//                    Logger.warning(feedback);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                loginCallback.onLoginFailed();
            }
        }
    };
    private String username;
    private String password;
    private boolean isLogin;
    private GT3ConfigBean gt3ConfigBean;
    private final GT3GeetestUtils gt3GeetestUtils;
    Handler risky_check_handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String feedback = data.getString("value");
            try {
                JSONObject data_json = new JSONObject(feedback);
                if (!data_json.getString("message").equals("OK")) {
                    Log.makeToast("验证信息请求失败：");
                    Logger.e(TAG, "验证信息请求失败：" + feedback);
                    loginCallback.onLoginFailed();
                    return;
                }
                if (feedback.contains("\"geetest\":null")) {
                    Logger.d(TAG, "无需极验校验，直接开始登陆");

                    login_map.put("x-rpc-device_id", getDeviceID(activity));
                    login_map.put("x-rpc-client_type", "2");
                    login_map.put("x-rpc-mdk_version", MDK_VERSION);
                    login_map.put("x-rpc-game_biz", "bh3_cn");
                    login_map.put("x-rpc-language", "zh-cn");
                    login_map.put("x-rpc-channel_version", MDK_VERSION);
                    login_map.put("x-rpc-channel_id", "1");
                    login_map.put("User-Agent", "okhttp/3.10.0");
                    login_map.put("x-rpc-risky", "id=" + data_json.getJSONObject("data").getString("id"));
                    new Thread(login_runnable).start();
                    return;
                }
                JSONObject feedback_json = data_json.getJSONObject("data").getJSONObject("geetest");
                JSONObject api1_json = new JSONObject();
                api1_json.put("challenge", feedback_json.getString("challenge"));
                api1_json.put("gt", feedback_json.getString("gt"));
                api1_json.put("success", feedback_json.getString("success"));
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("id=");
                stringBuilder.append(data_json.getJSONObject("data").getString("id"));
                gt3ConfigBean = new GT3ConfigBean();
                gt3ConfigBean.setPattern(1);
                gt3ConfigBean.setCanceledOnTouchOutside(false);
                gt3ConfigBean.setLang(null);
                gt3ConfigBean.setTimeout(10000);
                gt3ConfigBean.setWebviewTimeout(10000);
                gt3ConfigBean.setListener(new GT3Listener() {
                    public void onButtonClick() {
                        gt3ConfigBean.setApi1Json(api1_json);
                        gt3GeetestUtils.getGeetest();
                    }

                    public void onClosed(int param1Int) {
                    }

                    public void onSuccess(String s) {
                    }

                    public void onReceiveCaptchaCode(int i) {
                    }

                    public void onDialogResult(String param1String) {
                        login_map.put("x-rpc-device_id", getDeviceID(activity));
                        login_map.put("x-rpc-client_type", "2");
                        login_map.put("x-rpc-mdk_version", MDK_VERSION);
                        login_map.put("x-rpc-game_biz", "bh3_cn");
                        login_map.put("x-rpc-language", "zh-cn");
                        login_map.put("x-rpc-channel_version", MDK_VERSION);
                        login_map.put("x-rpc-channel_id", "1");
                        login_map.put("User-Agent", "okhttp/3.10.0");
                        try {
                            JSONObject jsonObject = new JSONObject(param1String);
                            stringBuilder.append(";c=");
                            stringBuilder.append(jsonObject.getString("geetest_challenge"));
                            stringBuilder.append(";s=");
                            stringBuilder.append(jsonObject.getString("geetest_seccode"));
                            stringBuilder.append(";v=");
                            stringBuilder.append(jsonObject.getString("geetest_validate"));
                            Logger.d(TAG, "极验验证完成,risky信息：" + stringBuilder.toString());
                            login_map.put("x-rpc-risky", stringBuilder.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                            loginCallback.onLoginFailed();
                        }

                        gt3GeetestUtils.showSuccessDialog();
                        new Thread(login_runnable).start();
//                        Logger.d(TAG,"极验完成:"+param1String);
                    }

                    @Override
                    public void onStatistics(String s) {

                    }

                    public void onFailed(GT3ErrorBean param1GT3ErrorBean) {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("onError :");
                        stringBuilder.append(param1GT3ErrorBean.toString());
                        Logger.d(TAG, stringBuilder.toString());
                        loginCallback.onLoginFailed();
                    }
                });
                gt3GeetestUtils.init(gt3ConfigBean);
                gt3GeetestUtils.startCustomFlow();
            } catch (JSONException e) {
                e.printStackTrace();
                Log.makeToast("验证信息请求失败：");
                Logger.e(TAG, "验证信息请求失败：" + feedback);
                loginCallback.onLoginFailed();
            }
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
            gt3GeetestUtils.dismissGeetestDialog();
            gt3GeetestUtils.destory();
            Logger.d(TAG, "handleMessage: " + feedback);

            try {
                JSONObject feedback_json = new JSONObject(feedback);
                if (feedback_json.getInt("retcode") == 0) {
                    JSONObject account_json = feedback_json.getJSONObject("data");
                    String combo_id = account_json.getString("combo_id");
                    String combo_token = account_json.getString("combo_token");
                    Logger.addBlacklist(combo_token);
                    Logger.addBlacklist(token);

                    roleData = new RoleData(activity, uid, token, combo_id, combo_token, "1", "1", "", 0, loginCallback);
                    isLogin = true;
//                    Log.makeToast(R.string.login_succeed);
//                    loginCallback.onLoginSucceed();

                } else {
                    Logger.w(TAG, "handleMessage: 登录失败2：" + feedback);
                    Log.makeToast("登录失败：" + feedback);
                    loginCallback.onLoginFailed();
                }
            } catch (JSONException e) {
                loginCallback.onLoginFailed();
                e.printStackTrace();
            }
        }
    };

    public Official(AppCompatActivity activity, LoginCallback callback) {
        loginCallback = callback;
        isLogin = false;
        this.activity = activity;
        preferences = activity.getSharedPreferences("official_user_" + getDefaultSharedPreferences(activity).getInt("official_slot", 1), Context.MODE_PRIVATE);
        Log = Logger.getLogger(activity);

        gt3GeetestUtils = new GT3GeetestUtils(activity);
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
            customizeDialog.setNegativeButton(R.string.btn_cancel, (dialog, which) -> {
                Log.makeToast(R.string.login_cancel);
                loginCallback.onLoginFailed();
            });
            customizeDialog.setCancelable(false);
            customizeDialog.show();
        } else {
            //https://api-sdk.mihoyo.com/bh3_cn/mdk/shield/api/verify?
            login_json = new JSONObject();
            try {
                String localToken = preferences.getString("token", "");
                login_json.put("uid", preferences.getString("uid", ""));
                login_json.put("token", localToken);
                Logger.addBlacklist(localToken);
                new Thread(login_runnable).start();
            } catch (JSONException e) {
                e.printStackTrace();
                loginCallback.onLoginFailed();
            }
        }
    }

    public void loginByAccount() {


        risky_check_json = new JSONObject();
        try {
            risky_check_json.put("username", username);
            risky_check_json.put("action_type", "login");
            risky_check_json.put("api_name", "/shield/api/login");
//            preferences.edit().putString("account", username).apply();
            new Thread(risky_check_runnable).start();
        } catch (JSONException e) {
            e.printStackTrace();
            loginCallback.onLoginFailed();
        }


        login_json = new JSONObject();
        try {
            login_json.put("account", username);
            login_json.put("password", Encrypt.encryptByPublicKey(password, BH_PUBLIC_KEY));
            login_json.put("is_crypto", "true");
            login_json.put("game_key", "bh3_cn");
            preferences.edit().putString("account", username).apply();
//            new Thread(login_runnable).start();
        } catch (JSONException e) {
            e.printStackTrace();
            loginCallback.onLoginFailed();
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

    @Override
    public String getUsername() {
        return email;
    }
}

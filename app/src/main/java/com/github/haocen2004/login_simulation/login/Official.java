package com.github.haocen2004.login_simulation.login;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.github.haocen2004.login_simulation.data.Constant.BH_PUBLIC_KEY;
import static com.github.haocen2004.login_simulation.data.Constant.MDK_VERSION;
import static com.github.haocen2004.login_simulation.utils.Tools.getDeviceID;
import static com.github.haocen2004.login_simulation.utils.Tools.verifyAccount;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
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
import com.github.haocen2004.login_simulation.utils.Encrypt;
import com.github.haocen2004.login_simulation.utils.Logger;
import com.github.haocen2004.login_simulation.utils.Network;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Official implements LoginImpl {

    private static final String TAG = "Official Login";
    private final AppCompatActivity activity;
    private final SharedPreferences preferences;
    private final Logger Log;
    private JSONObject login_json;
    Runnable getSmsRunnable = new Runnable() {
        @Override
        public void run() {

            String smsParam = "{\"mobile\":\"" + username + "\",\"area\":\"+86\"}";

            String feedback = Network.sendPost("https://api-sdk.mihoyo.com/bh3_cn/mdk/shield/api/loginCaptcha", smsParam, login_map);

            Logger.d(TAG, "getSmsCode: " + feedback);

            if (feedback.contains("retcode\":0")) {
                Log.makeToast("验证码发送成功！");
            } else {
                Log.makeToast("验证码发送失败\n请联系开发者适配或重试");
            }
        }
    };
    private final Map<String, String> login_map = new HashMap<>();
    private final LoginCallback loginCallback;
    Runnable login_runnable = new Runnable() {
        @Override
        public void run() {
            String feedback;
            if (!preferences.getBoolean("has_token", false)) {
                feedback = Network.sendPost("https://api-sdk.mihoyo.com/bh3_cn/mdk/shield/api/login", login_json.toString(), login_map);
            } else {

                Map<String, String> map = new HashMap<>();

                map.put("x-rpc-device_id", getDeviceID(activity));
                map.put("x-rpc-client_type", "2");
                map.put("x-rpc-game_biz", "bh3_cn");
                map.put("x-rpc-language", "zh-cn");
                map.put("x-rpc-channel_id", "1");
                map.put("User-Agent", "okhttp/3.10.0");

                feedback = Network.sendPost("https://api-sdk.mihoyo.com/bh3_cn/mdk/shield/api/verify", login_json.toString(), map);
            }

            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("value", feedback);
            msg.setData(data);
            login_handler.sendMessage(msg);
        }
    };
    private boolean smsMode = false;
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
                    if (smsMode) {
                        new Thread(getSmsRunnable).start();
                    } else {
                        new Thread(login_runnable).start();
                    }
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
                            Logger.d(TAG, "极验验证完成,risky信息：" + stringBuilder);
                            login_map.put("x-rpc-risky", stringBuilder.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                            loginCallback.onLoginFailed();
                        }

                        gt3GeetestUtils.showSuccessDialog();
                        if (smsMode) {
                            new Thread(getSmsRunnable).start();
                        } else {
                            new Thread(login_runnable).start();
                        }
                    }

                    @Override
                    public void onStatistics(String s) {

                    }

                    public void onFailed(GT3ErrorBean param1GT3ErrorBean) {
                        String stringBuilder = "onError :" +
                                param1GT3ErrorBean.toString();
                        Logger.d(TAG, stringBuilder);
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
    private int smsCooling = 60;
    Handler smsTimeoutHandle = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Logger.d(TAG, "sms countdown " + smsCooling);
            if (smsCooling > 0) {
                smsCooling--;
                try {
                    activity.runOnUiThread(() -> getSmsButton.setText("" + smsCooling));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                smsTimeoutHandle.sendEmptyMessageDelayed(0, 1000);
            } else {
                try {
                    activity.runOnUiThread(() -> {
                        getSmsButton.setEnabled(true);
                        getSmsButton.setText(R.string.btn_get_sms);
                    });
                } catch (Exception ignore) {
                }
            }
        }
    };
    private String captcha;
    Runnable smsLoginRunnable = new Runnable() {
        @Override
        public void run() {

            Map<String, String> map = new HashMap<>();

            map.put("x-rpc-device_id", getDeviceID(activity));
            map.put("x-rpc-client_type", "2");
            map.put("x-rpc-game_biz", "bh3_cn");
            map.put("x-rpc-language", "zh-cn");
            map.put("x-rpc-channel_id", "1");
            map.put("User-Agent", "okhttp/3.10.0");

            String smsLoginParam = "{\"mobile\":\"" + username + "\",\"captcha\":\"" + captcha + "\",\"action\":\"Login\",\"area\":\"+86\"}";

            String feedback = Network.sendPost("https://api-sdk.mihoyo.com/bh3_cn/mdk/shield/api/loginMobile", smsLoginParam, map);


            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("value", feedback);
            msg.setData(data);
            login_handler.sendMessage(msg);
        }
    };
    private Button getSmsButton;
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
                    email = account_json.getString("mobile");
                    if (email.equals("")) {
                        email = account_json.getString("email");
                    }
                    preferences.edit()
                            .clear()
                            .putString("username", email)
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

    @Override
    public void login() {


        if (!preferences.getBoolean("has_token", false)) {
            smsMode = false;
            new MaterialAlertDialogBuilder(activity)
                    .setTitle(activity.getString(R.string.sel_server))
                    .setSingleChoiceItems(R.array.official_login_type, 0, (dialogInterface, i) -> {
                        String[] loginType = activity.getResources().getStringArray(R.array.official_login_type_values);
                        Logger.d(TAG, "select " + loginType[i]);
                        switch (loginType[i]) {
                            case "password":
                                MaterialAlertDialogBuilder customizeDialog =
                                        new MaterialAlertDialogBuilder(activity);
                                final View dialogView = LayoutInflater.from(activity)
                                        .inflate(R.layout.offical_login_layout, null);
                                customizeDialog.setTitle(R.string.types_official);
                                customizeDialog.setView(dialogView);
                                customizeDialog.setPositiveButton(R.string.btn_login,
                                        (dialog, which) -> {
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
                                break;
                            case "sms":
                                smsMode = true;
                                String phonePattern = "^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\\d{8}$";
                                MaterialAlertDialogBuilder smsDialog =
                                        new MaterialAlertDialogBuilder(activity);
                                final View smsDialogView = LayoutInflater.from(activity)
                                        .inflate(R.layout.offical_sms_login_layout, null);
                                smsDialog.setTitle(R.string.types_official);
                                smsDialog.setView(smsDialogView);
                                smsDialog.setPositiveButton(R.string.btn_login, null);
                                smsDialog.setNegativeButton(R.string.btn_cancel, (dialog, which) -> {
                                    Log.makeToast(R.string.login_cancel);
                                    loginCallback.onLoginFailed();
                                });
                                smsDialog.setCancelable(false);
                                AlertDialog alertDialog = smsDialog.create();
                                alertDialog.setOnShowListener(dialog -> {


                                    EditText edit_text = smsDialogView.findViewById(R.id.phoneNumber);
                                    getSmsButton = smsDialogView.findViewById(R.id.btn_sms);
                                    getSmsButton.setOnClickListener(v -> {

                                        smsCooling = 60;
                                        username = edit_text.getText().toString();
                                        Logger.d(TAG, "try get sms code for " + username);
                                        if (Pattern.matches(phonePattern, username)) {

                                            v.setEnabled(false);

                                            getSmsCode(username);

                                            smsTimeoutHandle.sendEmptyMessageDelayed(0, 1000);

                                        } else {
                                            Log.makeToast("手机号格式不正确！");
                                        }
                                    });
                                    Button button = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                                    button.setOnClickListener(v -> {
                                        EditText password_text = smsDialogView.findViewById(R.id.sms);
                                        username = edit_text.getText().toString();
                                        captcha = password_text.getText().toString().strip();

                                        if (captcha.length() != 6) {
                                            Log.makeToast("验证码格式不正确！");
                                        } else {
                                            new Thread(smsLoginRunnable).start();
                                            alertDialog.dismiss();

                                        }
                                    });
                                });
                                alertDialog.setOnDismissListener(dialog -> smsCooling = 0);
                                alertDialog.show();

                                break;
                            case "qrcode":
                                Log.makeToast("TODD");
                                loginCallback.onLoginFailed();
                                break;
                            default:
                                Log.makeToast(R.string.login_cancel);
                                loginCallback.onLoginFailed();
                        }
                        dialogInterface.dismiss();
                    })
                    .setNegativeButton(activity.getString(R.string.btn_cancel), (dialog, which) -> {
                        Log.makeToast(R.string.login_cancel);
                        loginCallback.onLoginFailed();
                    })
                    .show();
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

    private RoleData roleData;
    @SuppressLint("HandlerLeak")
    Handler login_handler2 = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String feedback = data.getString("value");
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

                    roleData = new RoleData(uid, token, combo_id, combo_token, "1", "1", "", 0, loginCallback);
                    isLogin = true;
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

    private void getSmsCode(String mobile) {
        risky_check_json = new JSONObject();
        try {
            risky_check_json.put("mobile", mobile);
            risky_check_json.put("action_type", "login");
            risky_check_json.put("api_name", "/shield/api/loginCaptcha");
//            preferences.edit().putString("account", username).apply();
            new Thread(risky_check_runnable).start();
        } catch (JSONException e) {
            e.printStackTrace();
            loginCallback.onLoginFailed();
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
    public boolean logout() {
        isLogin = false;
        return true;
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

    @Override
    public void setRole(RoleData roleData) {
        this.roleData = roleData;
        isLogin = true;
    }

}

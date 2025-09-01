package xyz.hellocraft.login_simulation.login;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static xyz.hellocraft.login_simulation.data.Constant.INTENT_EXTRA_KEY_TENCENT_LOGIN;
import static xyz.hellocraft.login_simulation.data.Constant.YYB_INIT;
import static xyz.hellocraft.login_simulation.utils.Logger.getLogger;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import xyz.hellocraft.login_simulation.activity.WeGameLoginActivity;
import xyz.hellocraft.login_simulation.data.RoleData;
import xyz.hellocraft.login_simulation.utils.Logger;
import xyz.hellocraft.login_simulation.utils.Network;
import xyz.hellocraft.login_simulation.utils.Tools;

//public class Tencent implements LoginImpl, UserListener {
public class Wegame implements LoginImpl {

    private static final String TAG = "Tencent Login";
    private final AppCompatActivity activity;
    private final Logger Log;
    private final LoginCallback callback;
    private String access_token;
    private String username;
    private String open_id;
    private boolean isLogin;
    private RoleData roleData;
    private String verify_data;
    private String wegame_verify_data;
    private SharedPreferences preferences;
    @SuppressLint("HandlerLeak")
    Handler login_handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String feedback = data.getString("value");
            Logger.d(TAG, "handleMessage: " + feedback);

            try {
                JSONObject feedback_json = new JSONObject(feedback);
                if (feedback_json.getInt("retcode") == 0) {
                    JSONObject data_json2 = feedback_json.getJSONObject("data");
                    String combo_id = data_json2.getString("combo_id");
                    String open_id = data_json2.getString("open_id");
                    String combo_token = data_json2.getString("combo_token");

                    roleData = new RoleData(open_id, "", combo_id, combo_token, "27", "27", "wegame", 11, callback);
                    isLogin = true;

                } else {
                    Logger.w(TAG, "handleMessage: 登录失败：" + feedback);
                    Log.makeToast("登录失败：" + feedback);
                    callback.onLoginFailed();
                    preferences.edit().clear().apply();
                }
            } catch (JSONException e) {
                callback.onLoginFailed();
                e.printStackTrace();
            }
        }
    };
    Runnable login_runnable = new Runnable() {
        @Override
        public void run() {
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("value", Tools.verifyAccount(activity, "27", verify_data));
            msg.setData(data);
            login_handler.sendMessage(msg);

        }
    };

    @SuppressLint("HandlerLeak")
    Handler wegame_verify_handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String feedback = data.getString("value");
            Logger.d(TAG, "handleMessage: " + feedback);
            try {
                JSONObject feedback_json = new JSONObject(feedback);
                if (feedback_json.getInt("code") == 0) {

                    verify_data = "{\"access_token\":\"" +
                            feedback_json.getString("access_token")
                            + "\",\"openid\":\"" +
                            feedback_json.getString("openid") +
                            "\",\"uid\":\"" +
                            feedback_json.getString("pc_rail_id") +
                            "\"}";
                    Logger.addBlacklist(feedback_json.getString("access_token"));
                    new Thread(login_runnable).start();

                } else {
                    Logger.w(TAG, "handleMessage: 登录失败：" + feedback);
                    Log.makeToast("登录失败：" + feedback);
                    callback.onLoginFailed();
                    preferences.edit().clear().apply();
                }
            } catch (JSONException e) {
                callback.onLoginFailed();
                e.printStackTrace();
            }

        }
    };
    Runnable wegame_verify_runnable = new Runnable() {
        @Override
        public void run() {
            Message msg = new Message();
            Bundle data = new Bundle();
            Map<String, String> union_login_map = new HashMap<>();
            union_login_map.put("Content-Type", "application/json");
            data.putString("value", Network.sendPost("https://m.wegame.com.cn/web/oauth2.0/union_login", wegame_verify_data, union_login_map));
            msg.setData(data);
            wegame_verify_handler.sendMessage(msg);

        }
    };

    public Wegame(AppCompatActivity activity, LoginCallback loginCallback) {
        callback = loginCallback;
        this.activity = activity;
        Log = getLogger(activity);
        //isLogin = false;
    }

    @Override
    public boolean logout() {
        isLogin = false;
        return true;
        //
//        YSDKApi.logout();
    }

    private void init() {
        if (YYB_INIT) return;
//        YSDKApi.init();
//        YSDKApi.setUserListener(this);
    }

    @Override
    public RoleData getRole() {
        return roleData;
    }

    @Override
    public void setRole(RoleData roleData) {
        this.roleData = roleData;
        isLogin = true;
    }

    @Override
    public boolean isLogin() {
        return isLogin;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public void login() {
        preferences = activity.getSharedPreferences("wegame_user_" + PreferenceManager.getDefaultSharedPreferences(activity).getInt("wegame_slot", 1), MODE_PRIVATE);
        Logger.d(TAG, "loading " + preferences.getString("username", "oldCache"));

        if (!Objects.equals(preferences.getString("openkey", ""), "")) {

            username = preferences.getString("username", "本地缓存登陆");
            open_id = preferences.getString("openid", "");
            access_token = preferences.getString("openkey", "");

            wegame_verify_data = "{\"appid\":\"40000095\",\"app_key\":\"6dcc7197c6cec4cd050ead6262702302\",\"login_type\":\"game_login\",\"third_type\":0,\"login_state_type\":2,\"login_state_openid\":\"" +
                    open_id +
                    "\",\"login_state_token\":\"" +
                    access_token +
                    "\",\"common_parameters\":{\"sdkversion\":\"3.1.1\",\"plat\":0}}";
            Logger.addBlacklist(access_token);
            new Thread(wegame_verify_runnable).start();
        } else {

            Log.makeToast("尝试使用网页登陆中\nTX网页登陆限制较大\n不一定能成功登陆\n\n网页卡白屏请返回重试");
            Intent intent = new Intent(activity, WeGameLoginActivity.class);
            //Constant.REQ_TENCENT_WEB_LOGIN_CALLBACK
            callback.launchActivityForResult(intent, activityResult -> {
                if (activityResult.getResultCode() == RESULT_OK) {
                    Logger.d("tencent login", "on succ callback");

                    String url_text = null;
                    if (activityResult.getData() != null) {
                        url_text = activityResult.getData().getStringExtra(INTENT_EXTRA_KEY_TENCENT_LOGIN);
                        username = activityResult.getData().getStringExtra("tencent.login.uin");
                    } else {
                        callback.onLoginFailed();
                        return;
                    }
                    URL url;
                    try {
                        url = new URL(url_text);
                        String ref = url.getRef();
                        for (String s : ref.split("&")) {
                            if (s.contains("access_token")) {
                                access_token = s.split("=")[1];
                            }
                            if (s.contains("openid")) {
                                open_id = s.split("=")[1];
                            }
                        }
                    } catch (MalformedURLException ignore) {
                        callback.onLoginFailed();
                        return;
                    }
                    preferences.edit().putString("openid", open_id).putString("username", username).putString("openkey", access_token).apply();
//                            url.getQuery();
//                        open_id = userLoginRet.open_id;
//                        username = "网页登陆";
//                        access_token = userLoginRet.getAccessToken();

                    wegame_verify_data = "{\"appid\":\"40000095\",\"app_key\":\"6dcc7197c6cec4cd050ead6262702302\",\"login_type\":\"game_login\",\"third_type\":0,\"login_state_type\":2,\"login_state_openid\":\"" +
                            open_id +
                            "\",\"login_state_token\":\"" +
                            access_token +
                            "\",\"common_parameters\":{\"sdkversion\":\"3.1.1\",\"plat\":0}}";
                    Logger.addBlacklist(access_token);
                    Logger.d(TAG, wegame_verify_data);
                    new Thread(wegame_verify_runnable).start();
                } else if (activityResult.getResultCode() == RESULT_CANCELED) {
                    Logger.d("tencent login", "on cancel callback");

                    preferences.edit().clear().apply();
                    callback.onLoginFailed();
                }
            });
//                callback.getCallbackFragment().startActivityForResult(intent, REQ_TENCENT_WEB_LOGIN_CALLBACK);
//                callback.onLoginFailed();
        }
//            return;

    }
}


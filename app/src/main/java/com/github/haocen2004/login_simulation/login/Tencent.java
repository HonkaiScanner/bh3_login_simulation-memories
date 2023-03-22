package com.github.haocen2004.login_simulation.login;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static com.github.haocen2004.login_simulation.data.Constant.INTENT_EXTRA_KEY_TENCENT_LOGIN;
import static com.github.haocen2004.login_simulation.data.Constant.YYB_INIT;
import static com.github.haocen2004.login_simulation.utils.Logger.getLogger;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.Keep;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.github.haocen2004.login_simulation.activity.TencentLoginActivity;
import com.github.haocen2004.login_simulation.data.RoleData;
import com.github.haocen2004.login_simulation.utils.Logger;
import com.github.haocen2004.login_simulation.utils.Tools;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

//public class Tencent implements LoginImpl, UserListener {
public class Tencent implements LoginImpl {

    private static final String TAG = "Tencent Login";
    private String access_token;
    private String username;
    private String open_id;
    private final AppCompatActivity activity;
    private boolean isLogin;
    private RoleData roleData;
    private final Logger Log;
    private final LoginCallback callback;
    private String verify_data;
    private boolean first_auto_login = false;
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

                    roleData = new RoleData(open_id, "", combo_id, combo_token, "13", "13", "tencent", 7, callback);
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

    public Tencent(AppCompatActivity activity, LoginCallback loginCallback) {
        callback = loginCallback;
        this.activity = activity;
        Log = getLogger(activity);
        preferences = activity.getSharedPreferences("tencent_user_" + PreferenceManager.getDefaultSharedPreferences(activity).getInt("tencent_slot", 1), MODE_PRIVATE);
        //isLogin = false;
    }

    @Keep
    private boolean getHooked() {
        Logger.d(TAG, "Xposed Checking..."); // for TaiChi and VXP short method hook compatibility
        return false;
    }

    @Override
    public boolean logout() {
        preferences.edit().clear().apply();
        new File(activity.getFilesDir().getParent(), "shared_prefs/tencent_user_" + PreferenceManager.getDefaultSharedPreferences(activity).getInt("tencent_slot", 1) + ".xml").delete();
        isLogin = false;
        return true;
        //
//        YSDKApi.logout();
    }

    private void init() {
        if (YYB_INIT) return;
//        YSDKApi.init();
//        YSDKApi.setUserListener(this);
        first_auto_login = true;
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
        return username;
    }

    @Override
    public void setRole(RoleData roleData) {
        this.roleData = roleData;
        isLogin = true;
    }


//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        String url_text = data.getStringExtra(INTENT_EXTRA_KEY_TENCENT_LOGIN);
//        URL url;
//        try {
//            url = new URL(url_text);
//            String ref = url.getRef();
//            for (String s : ref.split("&")) {
//                if (s.contains("access_token")) {
//                    access_token = s.split("=")[1];
//                }
//                if (s.contains("openid")) {
//                    open_id = s.split("=")[1];
//                }
//            }
//        } catch (MalformedURLException ignore) {
//            callback.onLoginFailed();
//            return;
//        }
//        Tools.saveString(activity, "tencent_openid", open_id);
//        Tools.saveString(activity, "tencent_openkey", access_token);
////        url.getQuery()
////        open_id = userLoginRet.open_id;
//        username = "网页登陆";
////        access_token = userLoginRet.getAccessToken();
//
//        int platform = 1; // qq = 1
//        verify_data = "{\"platform\":" +
//                platform
//                + ",\"openid\":\"" +
//                open_id +
//                "\",\"openkey\":\"" +
//                access_token +
//                "\",\"is_teenager\":false}";
//        Logger.addBlacklist(access_token);
//        new Thread(login_runnable).start();
//    }

//    @Override
//    public void OnLoginNotify(UserLoginRet userLoginRet) {
////
//        if (userLoginRet.flag != 0) {
//            if (first_auto_login) {
//                YSDKApi.login(ePlatform.QQ);
//                first_auto_login = false;
//                return;
//            }
//            Logger.d(TAG, userLoginRet.toString());
//            callback.onLoginFailed();
//            Log.makeToast("登陆失败\n" + userLoginRet.msg);
//        } else {
//            open_id = userLoginRet.open_id;
//            username = userLoginRet.nick_name;
//            access_token = userLoginRet.getAccessToken();
//
//            int platform = userLoginRet.platform; // qq == 1
//            verify_data = "{\"platform\":" +
//                    platform
//                    + ",\"openid\":\"" +
//                    open_id +
//                    "\",\"openkey\":\"" +
//                    access_token +
//                    "\",\"is_teenager\":false}";
//            Logger.addBlacklist(access_token);
//            new Thread(login_runnable).start();
//        }
////        userLoginRet.get
//    }

    Runnable login_runnable = new Runnable() {
        @Override
        public void run() {
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("value", Tools.verifyAccount(activity, "13", verify_data));
            msg.setData(data);
            login_handler.sendMessage(msg);

        }
    };

    @Override
    public void login() {
        if (!getHooked()) {
            if (!Objects.equals(preferences.getString("openkey", ""), "")) {

                username = preferences.getString("username", "本地缓存登陆");
                open_id = preferences.getString("openid", "");
                access_token = preferences.getString("openkey", "");

                int platform = 1; // qq = 1
                verify_data = "{\"platform\":" +
                        platform
                        + ",\"openid\":\"" +
                        open_id +
                        "\",\"openkey\":\"" +
                        access_token +
                        "\",\"is_teenager\":false}";
                Logger.addBlacklist(access_token);
                new Thread(login_runnable).start();
            } else {

                Log.makeToast("尝试使用网页登陆中\nTX网页登陆限制较大\n不一定能成功登陆\n\n网页卡白屏请返回重试");
                Intent intent = new Intent(activity, TencentLoginActivity.class);
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

                        int platform = 1; // qq = 1
                        verify_data = "{\"platform\":" +
                                platform
                                + ",\"openid\":\"" +
                                open_id +
                                "\",\"openkey\":\"" +
                                access_token +
                                "\",\"is_teenager\":false}";
                        Logger.addBlacklist(access_token);
                        new Thread(login_runnable).start();
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
        } else {
            init();
            if (!first_auto_login) {
//                YSDKApi.login(ePlatform.QQ);
            }
        }

    }
}


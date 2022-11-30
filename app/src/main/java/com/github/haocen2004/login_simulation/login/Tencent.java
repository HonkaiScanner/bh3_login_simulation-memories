package com.github.haocen2004.login_simulation.login;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.github.haocen2004.login_simulation.util.Constant.INTENT_EXTRA_KEY_TENCENT_LOGIN;
import static com.github.haocen2004.login_simulation.util.Constant.YYB_INIT;
import static com.github.haocen2004.login_simulation.util.Logger.getLogger;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Keep;
import androidx.appcompat.app.AppCompatActivity;

import com.github.haocen2004.login_simulation.activity.TencentLoginActivity;
import com.github.haocen2004.login_simulation.data.RoleData;
import com.github.haocen2004.login_simulation.util.Logger;
import com.github.haocen2004.login_simulation.util.Tools;

import org.json.JSONException;
import org.json.JSONObject;

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
    private ActivityResultLauncher activityResultLauncher;

    public Tencent(AppCompatActivity activity, LoginCallback loginCallback) {
        callback = loginCallback;
        this.activity = activity;
        Log = getLogger(activity);
        //isLogin = false;
    }

    @Override
    public void logout() {
        Tools.saveString(activity, "tencent_openid", "");
        Tools.saveString(activity, "tencent_openkey", "");
        isLogin = false;
        //
//        YSDKApi.logout();
    }

    @Keep
    private boolean getHooked() {
        Logger.d(TAG, "Xposed Checking..."); // for TaiChi and VXP short method hook compatibility
        return false;
    }

    @Override
    public void login() {
        if (!getHooked()) {
            if (!Objects.equals(Tools.getString(activity, "tencent_openkey"), "")) {

                username = "本地缓存登陆";
                open_id = Tools.getString(activity, "tencent_openid");
                access_token = Tools.getString(activity, "tencent_openkey");

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
                        Tools.saveString(activity, "tencent_openid", open_id);
                        Tools.saveString(activity, "tencent_openkey", access_token);
//                            url.getQuery();
//                        open_id = userLoginRet.open_id;
                        username = "网页登陆";
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

                        Tools.saveString(activity, "tencent_openid", "");
                        Tools.saveString(activity, "tencent_openkey", "");
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
                    Tools.saveString(activity, "tencent_openid", "");
                    Tools.saveString(activity, "tencent_openkey", "");
                }
            } catch (JSONException e) {
                callback.onLoginFailed();
                e.printStackTrace();
            }
        }
    };
}


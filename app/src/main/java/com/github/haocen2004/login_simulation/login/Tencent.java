package com.github.haocen2004.login_simulation.login;

import static com.github.haocen2004.login_simulation.util.Constant.YYB_INIT;
import static com.github.haocen2004.login_simulation.util.Logger.getLogger;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.appcompat.app.AppCompatActivity;

import com.github.haocen2004.login_simulation.data.RoleData;
import com.github.haocen2004.login_simulation.util.Logger;
import com.github.haocen2004.login_simulation.util.Tools;
import com.tencent.ysdk.api.YSDKApi;
import com.tencent.ysdk.framework.common.ePlatform;
import com.tencent.ysdk.module.user.UserListener;
import com.tencent.ysdk.module.user.UserLoginRet;
import com.tencent.ysdk.module.user.UserRelationRet;
import com.tencent.ysdk.module.user.WakeupRet;

import org.json.JSONException;
import org.json.JSONObject;

public class Tencent implements LoginImpl, UserListener {

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

    public Tencent(AppCompatActivity activity, LoginCallback loginCallback) {
        callback = loginCallback;
        this.activity = activity;
        Log = getLogger(activity);
        //isLogin = false;
    }

    @Override
    public void logout() {
        isLogin = false;
        //
        YSDKApi.logout();
    }

    private boolean getHooked() {
        return false;
    }

    @Override
    public void login() {
        if (!getHooked()) {
            Log.makeToast("未启用Xposed模块！");
            callback.onLoginFailed();
        }
        init();
        if (!first_auto_login) {
            YSDKApi.login(ePlatform.QQ);
        }

    }

    private void init() {
        if (YYB_INIT) return;
        YSDKApi.init();
        YSDKApi.setUserListener(this);
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
    public void OnLoginNotify(UserLoginRet userLoginRet) {
//
        if (userLoginRet.flag != 0) {
            if (first_auto_login) {
                YSDKApi.login(ePlatform.QQ);
                return;
            }
            Logger.d(TAG, userLoginRet.toString());
            callback.onLoginFailed();
            Log.makeToast("登陆失败\n" + userLoginRet.msg);
        } else {
            open_id = userLoginRet.open_id;
            username = userLoginRet.nick_name;
            access_token = userLoginRet.getAccessToken();

            int platform = userLoginRet.platform;

            verify_data = "{\"platform\":" +
                    platform
                    + ",\"openid\":\"" +
                    open_id +
                    "\",\"openkey\":\"" +
                    access_token +
                    "\",\"is_teenager\":false}";
            Logger.addBlacklist(access_token);
            new Thread(login_runnable).start();
        }
//        userLoginRet.get
    }

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
//                        String account_type = data_json2.getString("account_type");

                    roleData = new RoleData(activity, open_id, "", combo_id, combo_token, "13", "2", "tencent", 0, callback);
                    isLogin = true;

                } else {
                    Logger.w(TAG, "handleMessage: 登录失败：" + feedback);
                    Log.makeToast("登录失败：" + feedback);
                    callback.onLoginFailed();
                }
            } catch (JSONException e) {
                callback.onLoginFailed();
                e.printStackTrace();
            }
        }
    };

    @Override
    public void OnWakeupNotify(WakeupRet wakeupRet) {
        Logger.d(TAG, wakeupRet.toString());
    }

    @Override
    public void OnRelationNotify(UserRelationRet userRelationRet) {
        Logger.d(TAG, userRelationRet.toString());
    }
}


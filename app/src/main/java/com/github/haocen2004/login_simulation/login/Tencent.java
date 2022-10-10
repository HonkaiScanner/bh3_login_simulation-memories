package com.github.haocen2004.login_simulation.login;

import static com.github.haocen2004.login_simulation.util.Constant.INTENT_EXTRA_KEY_TENCENT_LOGIN;
import static com.github.haocen2004.login_simulation.util.Constant.REQ_TENCENT_WEB_LOGIN_CALLBACK;
import static com.github.haocen2004.login_simulation.util.Constant.YYB_INIT;
import static com.github.haocen2004.login_simulation.util.Logger.getLogger;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.Keep;
import androidx.appcompat.app.AppCompatActivity;

import com.github.haocen2004.login_simulation.activity.TencentLoginActivity;
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

import java.net.MalformedURLException;
import java.net.URL;

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

    @Keep
    private boolean getHooked() {
        Logger.d(TAG, "Xposed Checking..."); // for TaiChi and VXP short method hook compatibility
        return false;
    }

    @Override
    public void login() {
        if (!getHooked()) {
            Log.makeToast("未启用Xposed模块！\n尝试使用网页登陆中\n网页加载不出请返回重试登陆");
            Intent intent = new Intent(callback.getCallbackFragment().getActivity(), TencentLoginActivity.class);
            callback.getCallbackFragment().startActivityForResult(intent, REQ_TENCENT_WEB_LOGIN_CALLBACK);
            callback.onLoginFailed();
//            return;
        } else {
            init();
            if (!first_auto_login) {
                YSDKApi.login(ePlatform.QQ);
            }
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        String url_text = data.getStringExtra(INTENT_EXTRA_KEY_TENCENT_LOGIN);
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
        }
//        url.getQuery()
//        open_id = userLoginRet.open_id;
        username = "网页登陆";
//        access_token = userLoginRet.getAccessToken();

        int platform = 1;
//https://imgcache.qq.com/open/connect/widget/mobile/login/proxy.htm?#
// &t=1665408364#
// &openid=2D7B229B8C556673ACDE32C1A7CBB1BE
// &appid=1105553399
// &access_token=FFDBB36754F8B802E35CBADBC2FC5C50
// &pay_token=C48E79CF2F0FD86CDC4F3432480022D2
// &key=601f032ef9ae155434c0e4fb0de223ad
// &serial=
// &token_key=
// &browser=0
// &browser_error=0
// &status_os=13.3.1
// &sdkv=3.3.8_lite
// &status_machine=iPhone9%2C1
// &update_auth=
// &has_auth=
// &auth_time=1665408364
// &page_type=0
// &redirect_uri_key=227E4CD7DF08FCAD0F89222B85B12A2E09657305DC2B707FB9300AD637FE51DEC4E37F72D788E40AFA456BA57830390F27961D909E41FBA0B47666A53C4EFCF2D80D3BFC6554131FAD384F031640D7E3B6A7F056E3AFF9D64E879AAC01881062BA8E7CBA9B4343B6BEE7DCF68F7B53F168AB2F60511E6ED055A6C63E860443A599F69A9D1CFD8503FD5A4B72179EB6BDC08E5C605402647ADA91BA1CAB6D25F7031FA9A9C2AE8B725955D553E2909BDB17AD5AA3720B6CEEE4AB987B46F57DCE3F2B122E21089B7305AD790F4AE8BF4A86C2D94B230E8B5430AF836C6337A12D092651FBE9808374B47F49B0683C9EBE4880DF9A9A577207267C54D0D4B302BAE2DEF999F5AE2F45A4101A41EE400AAE871095340F0A2985FDF3AB8D97E014518ECFE74A373A62BC94CFFFAD6CBC2DB2
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

    @Override
    public void OnLoginNotify(UserLoginRet userLoginRet) {
//
        if (userLoginRet.flag != 0) {
            if (first_auto_login) {
                YSDKApi.login(ePlatform.QQ);
                first_auto_login = false;
                return;
            }
            Logger.d(TAG, userLoginRet.toString());
            callback.onLoginFailed();
            Log.makeToast("登陆失败\n" + userLoginRet.msg);
        } else {
            open_id = userLoginRet.open_id;
            username = userLoginRet.nick_name;
            access_token = userLoginRet.getAccessToken();

            int platform = userLoginRet.platform; // qq == 1
//https://imgcache.qq.com/open/connect/widget/mobile/login/proxy.htm?#
// &t=1665408364#
// &openid=2D7B229B8C556673ACDE32C1A7CBB1BE
// &appid=1105553399
// &access_token=FFDBB36754F8B802E35CBADBC2FC5C50
// &pay_token=C48E79CF2F0FD86CDC4F3432480022D2
// &key=601f032ef9ae155434c0e4fb0de223ad
// &serial=
// &token_key=
// &browser=0
// &browser_error=0
// &status_os=13.3.1
// &sdkv=3.3.8_lite
// &status_machine=iPhone9%2C1
// &update_auth=
// &has_auth=
// &auth_time=1665408364
// &page_type=0
// &redirect_uri_key=227E4CD7DF08FCAD0F89222B85B12A2E09657305DC2B707FB9300AD637FE51DEC4E37F72D788E40AFA456BA57830390F27961D909E41FBA0B47666A53C4EFCF2D80D3BFC6554131FAD384F031640D7E3B6A7F056E3AFF9D64E879AAC01881062BA8E7CBA9B4343B6BEE7DCF68F7B53F168AB2F60511E6ED055A6C63E860443A599F69A9D1CFD8503FD5A4B72179EB6BDC08E5C605402647ADA91BA1CAB6D25F7031FA9A9C2AE8B725955D553E2909BDB17AD5AA3720B6CEEE4AB987B46F57DCE3F2B122E21089B7305AD790F4AE8BF4A86C2D94B230E8B5430AF836C6337A12D092651FBE9808374B47F49B0683C9EBE4880DF9A9A577207267C54D0D4B302BAE2DEF999F5AE2F45A4101A41EE400AAE871095340F0A2985FDF3AB8D97E014518ECFE74A373A62BC94CFFFAD6CBC2DB2
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

                    roleData = new RoleData(activity, open_id, "", combo_id, combo_token, "13", "13", "tencent", 7, callback);
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


package com.github.haocen2004.login_simulation.login;

import static com.github.haocen2004.login_simulation.util.Constant.QIHOO_INIT;
import static com.github.haocen2004.login_simulation.util.Logger.getLogger;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.github.haocen2004.login_simulation.data.RoleData;
import com.github.haocen2004.login_simulation.util.Logger;
import com.github.haocen2004.login_simulation.util.Tools;
import com.qihoo.gamecenter.sdk.activity.ContainerActivity;
import com.qihoo.gamecenter.sdk.common.IDispatcherCallback;
import com.qihoo.gamecenter.sdk.matrix.Matrix;
import com.qihoo.gamecenter.sdk.protocols.ProtocolConfigs;
import com.qihoo.gamecenter.sdk.protocols.ProtocolKeys;
import com.tencent.bugly.crashreport.CrashReport;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class Qihoo implements LoginImpl {

    private static final String TAG = "Qihoo Login";
    private String access_token;
    private String username;
    private String uid;
    private SharedPreferences preferences;
    private final AppCompatActivity activity;
    private boolean isLogin;
    private RoleData roleData;
    private final Logger Log;
    private final LoginCallback callback;

    public Qihoo(AppCompatActivity activity, LoginCallback loginCallback) {
        callback = loginCallback;
        this.activity = activity;
        Log = getLogger(activity);
        //isLogin = false;
    }

    private void doQihooLogin() {


        Intent intent = new Intent(activity, ContainerActivity.class);

        // 界面相关参数，360SDK界面是否以横屏显示。
        intent.putExtra(ProtocolKeys.IS_SCREEN_ORIENTATION_LANDSCAPE, false);

        // 必需参数，使用360SDK的登录模块。
        intent.putExtra(ProtocolKeys.FUNCTION_CODE, ProtocolConfigs.FUNC_CODE_LOGIN);

        // 可选参数，是否在自动登录的过程中显示切换帐号按钮
        intent.putExtra(ProtocolKeys.IS_SHOW_AUTOLOGIN_SWITCH, false);

        //-- 以下参数仅仅针对自动登录过程的控制
        // 可选参数，自动登录过程中是否不展示任何UI，默认展示。
        intent.putExtra(ProtocolKeys.IS_AUTOLOGIN_NOUI, true);
        IDispatcherCallback sdk_callback = data -> {
            // press back
            if (isCancelLogin(data)) {
                callback.onLoginFailed();
                return;
            }
            // 显示一下登录结果
            Logger.d(TAG, data);
//                isLogin = true;
            try {
                JSONObject qihoo_result = new JSONObject(data);
                access_token = qihoo_result.getJSONObject("data").getString("access_token");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String data_json = "{\"access_key\":\"" +
                    access_token +
                    "\"}";
            Logger.addBlacklist(access_token);
            JSONObject feedback_json = null;
            try {
                feedback_json = new JSONObject(Objects.requireNonNull(Tools.verifyAccount(activity, "17", data_json)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {


                if (feedback_json == null) {
                    Log.makeToast("Empty Return");
                    callback.onLoginFailed();
                } else if (feedback_json.getInt("retcode") != 0) {
                    Log.makeToast(feedback_json.getString("message"));
                    callback.onLoginFailed();
                } else {
                    JSONObject data_json2 = feedback_json.getJSONObject("data");
                    String combo_id = data_json2.getString("combo_id");
                    String open_id = data_json2.getString("open_id");
                    String combo_token = data_json2.getString("combo_token");
                    Logger.addBlacklist(combo_token);
//                        String account_type = data_json2.getString("account_type");

                    roleData = new RoleData(activity, open_id, "", combo_id, combo_token, "17", "2", "qihoo", 9, callback);
                    isLogin = true;
//                        makeToast(activity.getString(R.string.login_succeed));
                }
//                doBHLogin();
            } catch (JSONException e) {
                CrashReport.postCatchedException(e);
                Log.makeToast("parse ERROR");
                callback.onLoginFailed();
            }

        };
        Matrix.execute(activity, intent, sdk_callback);

    }

    private boolean isCancelLogin(String data) {
        try {
            JSONObject joData = new JSONObject(data);
            int errno = joData.optInt("errno", -1);
            if (-1 == errno) {
                Log.makeToast(data);
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    @Override
    public void logout() {
        isLogin = false;
        Intent intent = new Intent();
        intent.putExtra(ProtocolKeys.FUNCTION_CODE, ProtocolConfigs.FUNC_CODE_LOGOUT);
        Matrix.execute(activity, intent, Log::makeToast);
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public void login() {

        if (QIHOO_INIT) {
            doQihooLogin();
        } else {
            Matrix.initInApplication(activity.getApplication());

            Matrix.setActivity(activity, (context, functionCode, functionParams) -> {

                Logger.d(TAG + " setActivity", functionParams);

                if (functionCode == ProtocolConfigs.FUNC_CODE_SWITCH_ACCOUNT) {
                    logout();
                    doQihooLogin();
                } else if (functionCode == ProtocolConfigs.FUNC_CODE_INITSUCCESS) {
                    // 初始化成功
                    QIHOO_INIT = true;
                    doQihooLogin();
                } else if (functionCode == ProtocolConfigs.FUNC_CODE_LOGIN) {
                    // sdk 登陆成功 --> verifyAccount
//                    Logger.d(TAG,

                } else if (functionCode == ProtocolConfigs.FUNC_CODE_LOGINAFTER_REALNAME_CALLBACK) {
                    callback.onLoginFailed();
                    // 当收到此回调后才可调用SDK提供的实名状态查询接口、打开实名认证界面接口。具体的返回内容是functionParams，数据格式如下。
                }

            }, true);

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
        return username;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }
}


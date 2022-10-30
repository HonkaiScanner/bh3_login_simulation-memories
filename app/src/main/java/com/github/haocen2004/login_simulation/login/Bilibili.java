package com.github.haocen2004.login_simulation.login;

import static com.github.haocen2004.login_simulation.util.Constant.BILI_APP_KEY;
import static com.github.haocen2004.login_simulation.util.Constant.BILI_INIT;
import static com.github.haocen2004.login_simulation.util.Logger.getLogger;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.bsgamesdk.android.BSGameSdk;
import com.bsgamesdk.android.callbacklistener.BSGameSdkError;
import com.bsgamesdk.android.callbacklistener.CallbackListener;
import com.bsgamesdk.android.callbacklistener.InitCallbackListener;
import com.github.haocen2004.login_simulation.R;
import com.github.haocen2004.login_simulation.data.RoleData;
import com.github.haocen2004.login_simulation.data.dialog.ButtonData;
import com.github.haocen2004.login_simulation.data.dialog.DialogData;
import com.github.haocen2004.login_simulation.data.dialog.DialogLiveData;
import com.github.haocen2004.login_simulation.util.DialogHelper;
import com.github.haocen2004.login_simulation.util.Logger;
import com.github.haocen2004.login_simulation.util.Tools;
import com.tencent.bugly.crashreport.CrashReport;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class Bilibili implements LoginImpl {

    private static final String TAG = "Bilibili Login";
    private String access_token;
    private String username;
    private String uid;
    private BSGameSdk gameSdk;
    private SharedPreferences preferences;
    private final AppCompatActivity activity;
    private boolean isLogin;
    private RoleData roleData;
    private final Logger Log;
    private final LoginCallback callback;

    public Bilibili(AppCompatActivity activity, LoginCallback loginCallback) {
        callback = loginCallback;
        this.activity = activity;
        Log = getLogger(activity);
        //isLogin = false;
    }
    private final CallbackListener biliLogin = new CallbackListener() {

        @Override
        public void onSuccess(Bundle arg0) {
            // 此处为操作成功时执行，返回值通过Bundle传回

            Tools.saveBoolean(activity, "last_bili_login_succeed", true);
            uid = arg0.getString("uid");
            username = arg0.getString("username");
            access_token = arg0.getString("access_token");

            preferences.edit().clear().apply();
            preferences.edit().putString("username", username).putString("uid", uid).apply();

            String data_json = "{\"uid\":" +
                    uid +
                    ",\"access_key\":\"" +
                    access_token +
                    "\"}";
            Logger.addBlacklist(access_token);
            JSONObject feedback_json = null;
            try {
                feedback_json = new JSONObject(Objects.requireNonNull(Tools.verifyAccount(activity, "14", data_json)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {


                if (feedback_json == null) {
                    makeToast("Empty Return");
                    callback.onLoginFailed();
                } else if (feedback_json.getInt("retcode") != 0) {
                    makeToast(feedback_json.getString("message"));
                    callback.onLoginFailed();
                } else {
                    JSONObject data_json2 = feedback_json.getJSONObject("data");
                    String combo_id = data_json2.getString("combo_id");
                    String open_id = data_json2.getString("open_id");
                    String combo_token = data_json2.getString("combo_token");
                    Logger.addBlacklist(combo_token);
//                        String account_type = data_json2.getString("account_type");

                    roleData = new RoleData(activity, open_id, "", combo_id, combo_token, "14", "2", "bilibili", 0, callback);
                    isLogin = true;
//                        makeToast(activity.getString(R.string.login_succeed));
                }
//                doBHLogin();
            } catch (JSONException e) {
                CrashReport.postCatchedException(e);
                makeToast("parse ERROR");
                callback.onLoginFailed();
            }

        }

        @Override
        public void onFailed(BSGameSdkError arg0) {

            Tools.saveBoolean(activity, "last_bili_login_succeed", false);
            // 此处为操作失败时执行，返回值为BSGameSdkError类型变量，其中包含ErrorCode和ErrorMessage
            Logger.e(TAG, "onFailed\nErrorCode : "
                    + arg0.getErrorCode() + "\nErrorMessage : "
                    + arg0.getErrorMessage());
            makeToast("onFailed\nErrorCode : "
                    + arg0.getErrorCode() + "\nErrorMessage : "
                    + arg0.getErrorMessage());
            callback.onLoginFailed();
        }

        @Override
        public void onError(BSGameSdkError arg0) {

            Tools.saveBoolean(activity, "last_bili_login_succeed", false);
            // 此处为操作异常时执行，返回值为BSGameSdkError类型变量，其中包含ErrorCode和ErrorMessage
            Logger.e(TAG, "onError\nErrorCode : "
                    + arg0.getErrorCode() + "\nErrorMessage : "
                    + arg0.getErrorMessage());
            makeToast("onError\nErrorCode : " + arg0.getErrorCode()
                    + "\nErrorMessage : " + arg0.getErrorMessage());
            callback.onLoginFailed();
        }
    };

    private void doBiliLogin() {
        boolean checkLastLogin = Tools.getBoolean(activity, "last_bili_login_succeed");
        Logger.d(TAG, "checkBiliLastLogin: " + checkLastLogin);
        if (checkLastLogin) {

            gameSdk.login(biliLogin);
        } else {

            DialogData dialogData = new DialogData("B服注意事项", "使用授权登陆可能会导致 登陆失败\n\n请在 扫码器内 使用账户密码登录");
            dialogData.setPositiveButtonData(new ButtonData("我已知晓") {
                @Override
                public void callback(DialogHelper dialogHelper) {
                    super.callback(dialogHelper);
                    gameSdk.login(biliLogin);
                }
            });
            DialogLiveData.getINSTANCE(activity).addNewDialog(dialogData);
        }
    }

    @Override
    public void logout() {
        isLogin = false;
        gameSdk.logout(new CallbackListener() {

            @Override
            public void onSuccess(Bundle arg0) {
                // 此处为操作成功时执行，返回值通过Bundle传回
                Logger.d(TAG, "onSuccess");
                try {
                    preferences.edit().clear().apply();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                makeToast(activity.getString(R.string.logout));

            }

            @Override
            public void onFailed(BSGameSdkError arg0) {
                // 此处为操作失败时执行，返回值为BSGameSdkError类型变量，其中包含ErrorCode和ErrorMessage
                Logger.e(TAG, "onFailed\nErrorCode : "
                        + arg0.getErrorCode() + "\nErrorMessage : "
                        + arg0.getErrorMessage());
                makeToast("onFailed\nErrorCode : "
                        + arg0.getErrorCode() + "\nErrorMessage : "
                        + arg0.getErrorMessage());
            }

            @Override
            public void onError(BSGameSdkError arg0) {
                // 此处为操作异常时执行，返回值为BSGameSdkError类型变量，其中包含ErrorCode和ErrorMessage
                Logger.e(TAG, "onError\nErrorCode : "
                        + arg0.getErrorCode() + "\nErrorMessage : "
                        + arg0.getErrorMessage());
                makeToast("onError\nErrorCode : " + arg0.getErrorCode()
                        + "\nErrorMessage : " + arg0.getErrorMessage());
            }
        });
    }

    @Override
    public void login() {
        if (BILI_INIT) {
            gameSdk = BSGameSdk.getInstance();
            preferences = activity.getSharedPreferences("bili_user", Context.MODE_PRIVATE);
            doBiliLogin();
        } else {
            BSGameSdk.initialize(true, activity, "590", "180",
                    "378", BILI_APP_KEY, new InitCallbackListener() {
                        @Override
                        public void onSuccess() {
//                        Logger.info("Bilibili SDK setup succeed");
                            Logger.i(TAG, "onSuccess: Setup Succeed");
                            doBiliLogin();
                            BILI_INIT = true;
                        }

                        @Override
                        public void onFailed() {

                            Logger.w(TAG, "Bilibili SDK setup Failed");

                        }
                    }, () -> System.exit(0));

            gameSdk = BSGameSdk.getInstance();
            preferences = activity.getSharedPreferences("bili_user", Context.MODE_PRIVATE);
        }
    }

    @Override
    public RoleData getRole() {
        return roleData;
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


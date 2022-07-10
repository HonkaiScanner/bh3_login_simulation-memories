package com.github.haocen2004.login_simulation.login;

import static com.github.haocen2004.login_simulation.util.Logger.getLogger;

import android.annotation.SuppressLint;
import android.app.Activity;

import androidx.appcompat.app.AppCompatActivity;

import com.github.haocen2004.login_simulation.R;
import com.github.haocen2004.login_simulation.data.RoleData;
import com.github.haocen2004.login_simulation.util.Logger;
import com.github.haocen2004.login_simulation.util.Tools;
import com.meizu.gamesdk.model.model.LoginResultCode;
import com.meizu.gamesdk.online.core.MzGameCenterPlatform;
import com.tencent.bugly.crashreport.CrashReport;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class Flyme implements LoginImpl {
    private final Activity activity;
    private boolean isLogin;
    private String uid;
    private String session;
    private String username;
    private RoleData roleData;
    private final LoginCallback callback;
    private final Logger Log;
    private static final String TAG = "Flyme Login";
    private final String appkey = "d19e8422d4fc46ff86e5112058f04876";


    public Flyme(AppCompatActivity activity, LoginCallback loginCallback) {
        callback = loginCallback;
        this.activity = activity;
        Log = getLogger(activity);
        //isLogin = false;
        MzGameCenterPlatform.init(activity, "3129215", appkey);
    }

    @Override
    public void login() {
        MzGameCenterPlatform.login(activity, (code, accountInfo, errorMsg) -> {
            if (code == LoginResultCode.LOGIN_SUCCESS) {
                uid = accountInfo.getUid();
                session = accountInfo.getSession();
                username = accountInfo.getName();
                doBHLogin();
            } else if (code == LoginResultCode.LOGIN_ERROR_CANCEL) {
                makeToast("用户取消登陆");
                callback.onLoginFailed();
            } else {
                makeToast("登陆失败\n\n" + code + "\n" + errorMsg);
                callback.onLoginFailed();
            }
        });
    }

    @Override
    public void logout() {
        MzGameCenterPlatform.logout(activity, (code, mzAccountInfo, msg) -> makeToast(activity.getString(R.string.logout)));
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

    Runnable login_runnable = new Runnable() {
        @Override
        public void run() {

            String data_json = "{\"uid\":\"" +
                    uid +
                    "\",\"session_id\":\"" +
                    session +
                    "\"}";

            Logger.addBlacklist(session);
            JSONObject feedback_json = null;
            try {
                feedback_json = new JSONObject(Objects.requireNonNull(Tools.verifyAccount(activity, "16", data_json)));
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
                    String account_type = data_json2.getString("account_type");

                    roleData = new RoleData(activity, open_id, "", combo_id, combo_token, "16", account_type, "meizu", 8, callback);
                    isLogin = true;

                }
            } catch (JSONException e) {
                CrashReport.postCatchedException(e);
                makeToast("parse ERROR");
                callback.onLoginFailed();
            }
        }

    };

    public void doBHLogin() {
        new Thread(login_runnable).start();
    }

    @SuppressLint("ShowToast")
    private void makeToast(String result) {
        Log.makeToast(result);
    }
}

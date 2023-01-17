package com.github.haocen2004.login_simulation.login;

import static com.github.haocen2004.login_simulation.util.Constant.FLYME_INIT;
import static com.github.haocen2004.login_simulation.util.Constant.OFFICIAL_PACK_INSTALLED;
import static com.github.haocen2004.login_simulation.util.Logger.getLogger;

import android.annotation.SuppressLint;
import android.app.Activity;

import androidx.appcompat.app.AppCompatActivity;

import com.github.haocen2004.login_simulation.R;
import com.github.haocen2004.login_simulation.data.RoleData;
import com.github.haocen2004.login_simulation.data.dialog.DialogData;
import com.github.haocen2004.login_simulation.data.dialog.DialogLiveData;
import com.github.haocen2004.login_simulation.util.Logger;
import com.github.haocen2004.login_simulation.util.Tools;
import com.meizu.gamesdk.model.model.LoginResultCode;
import com.meizu.gamesdk.online.core.MzGameCenterPlatform;
import com.tencent.bugly.crashreport.CrashReport;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class Flyme implements LoginImpl {
    private static final String TAG = "Flyme Login";
    private final Activity activity;
    private final LoginCallback callback;
    private final Logger Log;
    private final String appkey = "d19e8422d4fc46ff86e5112058f04876";
    private boolean isLogin;
    private String uid;
    private String session;
    private String username;
    private RoleData roleData;
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

                    roleData = new RoleData(open_id, "", combo_id, combo_token, "16", account_type, "meizu", 8, callback);
                    isLogin = true;

                }
            } catch (JSONException e) {
                CrashReport.postCatchedException(e);
                makeToast("parse ERROR");
                callback.onLoginFailed();
            }
        }

    };

    public Flyme(AppCompatActivity activity, LoginCallback loginCallback) {
        callback = loginCallback;
        this.activity = activity;
        Log = getLogger(activity);
        //isLogin = false;
        OFFICIAL_PACK_INSTALLED = Tools.verifyOfficialPack(activity, "com.miHoYo.bh3.mz");
        if (OFFICIAL_PACK_INSTALLED) {
            SdkInit(false);
        }

    }

    private void SdkInit(boolean autoLogin) {
        MzGameCenterPlatform.init(activity, "3129215", appkey);
        FLYME_INIT = true;
        if (autoLogin) login();
    }

    @Override
    public void login() {
        OFFICIAL_PACK_INSTALLED = Tools.verifyOfficialPack(activity, "com.miHoYo.bh3.mz");
        if (!OFFICIAL_PACK_INSTALLED) {
            DialogData dialogData = new DialogData("魅族特殊操作提示", "魅族服需要同时安装官方客户端\n\n在您的手机上未检测到官方客户端存在或没有获取手机安装应用列表权限\n\n请授予扫码器获取手机应用列表权限\n并正确安装任意版本官方客户端\n\n无需下载任何资源\n无需下载任何资源\n无需下载任何资源", "我已知晓");
            DialogLiveData.getINSTANCE(activity).addNewDialog(dialogData);
            callback.onLoginFailed();
        } else if (!FLYME_INIT) {
            SdkInit(true);
        } else {
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
    }

    @Override
    public boolean logout() {
        MzGameCenterPlatform.logout(activity, (code, mzAccountInfo, msg) -> makeToast(activity.getString(R.string.logout)));
        return true;
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

    public void doBHLogin() {
        new Thread(login_runnable).start();
    }

    @SuppressLint("ShowToast")
    private void makeToast(String result) {
        Log.makeToast(result);
    }
}

package com.github.haocen2004.login_simulation.login;

import static com.github.haocen2004.login_simulation.util.Constant.MI_INIT;

import android.app.Application;

import androidx.appcompat.app.AppCompatActivity;

import com.github.haocen2004.login_simulation.R;
import com.github.haocen2004.login_simulation.data.RoleData;
import com.github.haocen2004.login_simulation.util.Logger;
import com.github.haocen2004.login_simulation.util.Tools;
import com.tencent.bugly.crashreport.CrashReport;
import com.xiaomi.gamecenter.sdk.MiCommplatform;
import com.xiaomi.gamecenter.sdk.MiErrorCode;
import com.xiaomi.gamecenter.sdk.OnInitProcessListener;
import com.xiaomi.gamecenter.sdk.entry.MiAppInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class Xiaomi extends Application implements LoginImpl {


    private final AppCompatActivity activity;
    private final String TAG = "MiLogin";
    private boolean isLogin;
    private String uid;
    private String username;
    private String session;
    private RoleData roleData;
    private final LoginCallback callback;

    public Xiaomi(AppCompatActivity activity, LoginCallback loginCallback) {
        this.activity = activity;
        callback = loginCallback;
    }

    private void xiaomiLogin() {

        MiCommplatform.getInstance().onUserAgreed(activity);
        MiCommplatform.getInstance().miLogin(activity,
                (code, arg1) -> {
                    switch (code) {
                        case MiErrorCode.MI_XIAOMI_PAYMENT_SUCCESS:// 登陆成功
                            Logger.d(TAG, "Mi Login Success:" + arg1.toString());
                            //获取用户的登陆后的UID（即用户唯一标识）
                            uid = arg1.getUid();
                            username = arg1.getNikename();
                            //以下为获取session并校验流程，如果是网络游戏必须校验,(12小时过期)
                            //获取用户的登陆的Session（请参考5.3.3流程校验Session有效性）
                            session = arg1.getSessionId();
                            //请开发者完成将uid和session提交给开发者自己服务器进行session验证
                            doBHLogin();
                            break;
                        // 登陆失败
                        case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_LOGIN_FAIL:
                        case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_CANCEL:
                        case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_ACTION_EXECUTED:
                        default:
                            Logger.d(TAG, "err:" + code);
                            callback.onLoginFailed();
                            // 登录失败
                            break;
                    }
                });

    }

    @Override
    public void login() {
        if (MI_INIT) {
            xiaomiLogin();
            return;
        }
        MiAppInfo appInfo = new MiAppInfo();
        appInfo.setAppId("2882303761517502034");
        appInfo.setAppKey("5841750261034");

        MiCommplatform.Init(activity, appInfo, new OnInitProcessListener() {
            @Override
            public void finishInitProcess(List<String> loginMethod, int gameConfig) {
                Logger.i(TAG, "finishInitProcess: Init success");
//                MiCommplatform.getInstance().onMainActivityCreate(activity);
                MI_INIT = true;
                xiaomiLogin();
            }

            @Override
            public void onMiSplashEnd() {
                //小米闪屏⻚结束回调,小米闪屏可配,无闪屏也会返回此回调,游戏的闪屏应当在收到此回调之后
            }
        });


    }

    public void doBHLogin() {
        String data_json = "{\"session\":\"" +
                session +
                "\",\"uid\":\"" +
                uid +
                "\"}";
        JSONObject feedback_json = null;
        try {
            feedback_json = new JSONObject(Tools.verifyAccount(activity, "11", data_json));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {


            if (feedback_json == null) {
                makeToast("Empty Return");
            } else if (feedback_json.getInt("retcode") != 0) {
                makeToast(feedback_json.getString("message"));
            } else {
                JSONObject data_json2 = feedback_json.getJSONObject("data");
                String combo_id = data_json2.getString("combo_id");
                String open_id = data_json2.getString("open_id");
                String combo_token = data_json2.getString("combo_token");
                String account_type = data_json2.getString("account_type");

                roleData = new RoleData(open_id, "", combo_id, combo_token, "11", account_type, "xiaomi", 5, callback);
                isLogin = true;
                makeToast(activity.getString(R.string.login_succeed));
            }
        } catch (JSONException e) {
            CrashReport.postCatchedException(e);
            makeToast("parse ERROR");
        }

    }

    @Override
    public void logout() {
        MiCommplatform.getInstance().miAppExit(activity, code -> {
            if (code == MiErrorCode.MI_XIAOMI_EXIT) {
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
    }

    @Override
    public RoleData getRole() {
        return roleData;
    }

    private void makeToast(String result) {
        Logger.getLogger(this).makeToast(result);
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


}

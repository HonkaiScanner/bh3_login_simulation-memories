package com.github.haocen2004.login_simulation.login;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.github.haocen2004.login_simulation.Data.RoleData;
import com.github.haocen2004.login_simulation.R;
import com.github.haocen2004.login_simulation.util.Tools;
import com.nearme.game.sdk.GameCenterSDK;
import com.nearme.game.sdk.callback.ApiCallback;
import com.tencent.bugly.crashreport.BuglyLog;
import com.tencent.bugly.crashreport.CrashReport;

import org.json.JSONException;
import org.json.JSONObject;

import static com.github.haocen2004.login_simulation.util.Tools.verifyAccount;

public class Oppo implements LoginImpl {
    private final Activity activity;
    private boolean isLogin;
    private String uid;
    private String token;
    private RoleData roleData;
    private final String device_id;
    private static final String TAG = "Oppo Login";
    private final String appSecret = "f303388D89043bfEB1A667cfE42ea47E";
    private final GameCenterSDK sdk;


    public Oppo(Activity activity) {
        this.activity = activity;
        device_id = Tools.getDeviceID(activity);
        GameCenterSDK.init(appSecret, activity);
        sdk = GameCenterSDK.getInstance();
    }

    @Override
    public void login() {
        sdk.doLogin(activity, new ApiCallback() {
            @Override
            public void onSuccess(String s) {
                sdk.doGetTokenAndSsoid(new ApiCallback() {
                    public void onFailure(String param2String, int param2Int) {
                        makeToast("登录失败");
                        BuglyLog.w(TAG, "Login Failed. " + param2String + "," + param2Int);
                    }

                    public void onSuccess(String param2String) {
                        BuglyLog.d(TAG, param2String);
                        try {
                            JSONObject json = new JSONObject(param2String);
                            token = json.getString("token");
                            uid = json.getString("ssoid");
                            doBHLogin();
                        } catch (JSONException e) {
                            CrashReport.postCatchedException(e);
                            e.printStackTrace();
                        }
                    }
                });

            }

            @Override
            public void onFailure(String s, int i) {
                makeToast("error:" + s + "\ncode:" + i);
                BuglyLog.d(TAG, "onFailure: s:" + s);
                BuglyLog.d(TAG, "onFailure: i:" + i);
            }
        });
    }

    @Override
    public void logout() {
        makeToast("OppoSdk未提供退出登录接口");
    }

    @Override
    public RoleData getRole() {
        return roleData;
    }

    @Override
    public boolean isLogin() {
        return isLogin;
    }

    @SuppressLint("HandlerLeak")
    Handler login_handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String feedback = data.getString("value");
//            Logger.debug(feedback);
            BuglyLog.d(TAG, "handleMessage: " + feedback);
            JSONObject feedback_json = null;
            try {
                feedback_json = new JSONObject(feedback);
            } catch (JSONException e) {
                BuglyLog.w(TAG, "Login Failed.");
                CrashReport.postCatchedException(e);
                e.printStackTrace();
                return;
            }
//            Logger.info(feedback);
            BuglyLog.i(TAG, "handleMessage: " + feedback);
            try {
                if (feedback_json.getInt("retcode") == 0) {

                    JSONObject data_json2 = feedback_json.getJSONObject("data");
                    String combo_id = data_json2.getString("combo_id");
                    String open_id = data_json2.getString("open_id");
                    String combo_token = data_json2.getString("combo_token");
                    String account_type = data_json2.getString("account_type");

                    roleData = new RoleData(activity, open_id, "", combo_id, combo_token, "18", account_type, "oppo", 0);

                    isLogin = true;
                    makeToast(activity.getString(R.string.login_succeed));

                } else {

                    makeToast(feedback_json.getString("message"));
                    isLogin = false;

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    Runnable login_runnable = new Runnable() {
        @Override
        public void run() {
//            JSONObject data_json = new JSONObject();
//            try {
//                data_json.put("ssoid",uid).put("token",token);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
            String data_json = "{\"token\":\"" +
                    token +
                    "\",\"ssoid\":\"" +
                    uid +
                    "\"}";
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("value", verifyAccount(activity, "18", data_json));
            msg.setData(data);
            login_handler.sendMessage(msg);
        }

    };

    public void doBHLogin() {
        new Thread(login_runnable).start();
    }

    @SuppressLint("ShowToast")
    private void makeToast(String result) {
        try {
            Toast.makeText(activity, result, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Looper.prepare();
            Toast.makeText(activity, result, Toast.LENGTH_LONG).show();
            Looper.loop();
        }
    }
}

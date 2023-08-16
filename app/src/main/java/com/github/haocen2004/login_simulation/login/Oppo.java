package com.github.haocen2004.login_simulation.login;

import static com.github.haocen2004.login_simulation.data.Constant.OPPO_APP_KEY;
import static com.github.haocen2004.login_simulation.data.Constant.OPPO_INIT;
import static com.github.haocen2004.login_simulation.data.Constant.OPPO_OFFICIAL_PACK_INSTALLED;
import static com.github.haocen2004.login_simulation.utils.Tools.verifyAccount;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.github.haocen2004.login_simulation.data.RoleData;
import com.github.haocen2004.login_simulation.data.dialog.DialogData;
import com.github.haocen2004.login_simulation.data.dialog.DialogLiveData;
import com.github.haocen2004.login_simulation.utils.Logger;
import com.github.haocen2004.login_simulation.utils.Tools;
import com.nearme.game.sdk.GameCenterSDK;
import com.nearme.game.sdk.callback.ApiCallback;
import com.tencent.bugly.crashreport.CrashReport;

import org.json.JSONException;
import org.json.JSONObject;

public class Oppo implements LoginImpl {
    private static final String TAG = "Oppo Login";
    private final Activity activity;
    private final Logger Log;
    private final LoginCallback callback;
    private boolean isLogin;
    private String uid;
    private String token;
    private RoleData roleData;
    @SuppressLint("HandlerLeak")
    Handler login_handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String feedback = data.getString("value");
            Logger.d(TAG, "handleMessage: " + feedback);
            JSONObject feedback_json = null;
            try {
                feedback_json = new JSONObject(feedback);
            } catch (JSONException e) {
                Logger.w(TAG, "Login Failed.");
                CrashReport.postCatchedException(e);
                e.printStackTrace();
                callback.onLoginFailed();
                return;
            }
//            Logger.info(feedback);
//            Logger.i(TAG, "handleMessage: " + feedback);
            try {
                if (feedback_json.getInt("retcode") == 0) {

                    JSONObject data_json2 = feedback_json.getJSONObject("data");
                    String combo_id = data_json2.getString("combo_id");
                    String open_id = data_json2.getString("open_id");
                    String combo_token = data_json2.getString("combo_token");
                    String account_type = data_json2.getString("account_type");
                    Logger.addBlacklist(combo_token);

                    roleData = new RoleData(open_id, "", combo_id, combo_token, "18", account_type, "oppo", 4, callback);

                    isLogin = true;
//                    makeToast(activity.getString(R.string.login_succeed));

                } else {

                    makeToast(feedback_json.getString("message"));
                    isLogin = false;
                    callback.onLoginFailed();

                }
            } catch (JSONException e) {
                e.printStackTrace();
                callback.onLoginFailed();
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
    private GameCenterSDK sdk;

    public Oppo(Activity activity, LoginCallback callback) {
        this.callback = callback;
        this.activity = activity;
        OPPO_OFFICIAL_PACK_INSTALLED = Tools.verifyOfficialPack(activity, "com.miHoYo.bh3.nearme.gamecenter");
        if (OPPO_OFFICIAL_PACK_INSTALLED) {
            SdkInit(false);
        }
        Log = Logger.getLogger(activity);
    }

    private void SdkInit(boolean autoLogin) {
        GameCenterSDK.init(OPPO_APP_KEY, activity);
        sdk = GameCenterSDK.getInstance();
        OPPO_INIT = true;
        if (autoLogin) login();
    }

    @Override
    public boolean logout() {
        makeToast("OppoSdk未提供退出登录接口");
        return false;
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
    public void login() {


        OPPO_OFFICIAL_PACK_INSTALLED = Tools.verifyOfficialPack(activity, "com.miHoYo.bh3.nearme.gamecenter");
        if (!OPPO_OFFICIAL_PACK_INSTALLED) {
            DialogData dialogData = new DialogData("Oppo特殊操作提示", "Oppo服需要同时安装官方客户端\n\n在您的手机上未检测到官方客户端存在或没有获取手机安装应用列表权限\n\n请授予扫码器获取手机应用列表权限\n并正确安装任意版本官方客户端\n\n无需下载任何资源\n无需下载任何资源\n无需下载任何资源", "我已知晓");
            DialogLiveData.getINSTANCE().addNewDialog(dialogData);
            callback.onLoginFailed();
        } else if (!OPPO_INIT) {
            SdkInit(true);
        } else {
            sdk.doLogin(activity, new ApiCallback() {
                @Override
                public void onSuccess(String s) {
                    sdk.doGetTokenAndSsoid(new ApiCallback() {
                        public void onFailure(String param2String, int param2Int) {
                            makeToast("登录失败");
                            Logger.w(TAG, "Login Failed. " + param2String + "," + param2Int);
                            callback.onLoginFailed();
                        }

                        public void onSuccess(String param2String) {
//                        Logger.d(TAG, param2String);
                            try {
                                JSONObject json = new JSONObject(param2String);
                                token = json.getString("token");
                                uid = json.getString("ssoid");
                                Logger.addBlacklist(token);
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
                    Logger.d(TAG, "onFailure: s:" + s);
                    Logger.d(TAG, "onFailure: i:" + i);
                    callback.onLoginFailed();
                }
            });
        }
    }

    public void doBHLogin() {
        new Thread(login_runnable).start();
    }

    @SuppressLint("ShowToast")
    private void makeToast(String result) {
        try {
            Log.makeToast(result);
//            Toast.makeText(activity, result, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Looper.prepare();
            Log.makeToast(result);
            Toast.makeText(activity, result, Toast.LENGTH_LONG).show();
            Looper.loop();
        }
    }

    @Override
    public String getUsername() {
        return uid;
    }

}

//package com.github.haocen2004.login_simulation.login;
//
//import android.annotation.SuppressLint;
//import android.app.Activity;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Looper;
//import android.os.Message;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//
//import com.github.haocen2004.login_simulation.R;
//import com.github.haocen2004.login_simulation.util.Network;
//import com.github.haocen2004.login_simulation.util.RoleData;
//import com.github.haocen2004.login_simulation.util.Tools;
//import com.meizu.gamesdk.model.model.LoginResultCode;
//import com.meizu.gamesdk.online.core.MzGameCenterPlatform;
//import com.tencent.bugly.crashreport.BuglyLog;
//import com.tencent.bugly.crashreport.CrashReport;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.Map;
//
//public class Flyme implements LoginImpl {
//    private final Activity activity;
//    private boolean isLogin;
//    private String uid;
//    private String session;
//    private RoleData roleData;
//    private final String device_id;
//    private static final String TAG = "Flyme Login";
//    private final String appkey = "d19e8422d4fc46ff86e5112058f04876";
//
//
//    public Flyme(Activity activity) {
//        this.activity = activity;
//        device_id = Tools.getDeviceID(activity);
//        MzGameCenterPlatform.init(activity, "3129215" , appkey);
//    }
//
//    @Override
//    public void login() {
//        MzGameCenterPlatform.login(activity, (code, accountInfo, errorMsg) -> {
//            if (code == LoginResultCode.LOGIN_SUCCESS) {
//                uid = accountInfo.getUid();
//                session = accountInfo.getSession();
//                doBHLogin();
//            } else {
//                makeToast("登陆失败或取消\n\n如果为游戏框架安装包解析失败请至网上查找\n扫码器因控制包体大小未附带");
//            }
//        });
//    }
//
//    @Override
//    public void logout() {
//        MzGameCenterPlatform.logout(activity, (code, mzAccountInfo, msg) -> makeToast(activity.getString(R.string.logout)));
//    }
//
//    @Override
//    public RoleData getRole() {
//        return roleData;
//    }
//
//    @Override
//    public boolean isLogin() {
//        return isLogin;
//    }
//
//    @SuppressLint("HandlerLeak")
//    Handler login_handler = new Handler() {
//        @Override
//        public void handleMessage(@NonNull Message msg) {
//            super.handleMessage(msg);
//            Bundle data = msg.getData();
//            String feedback = data.getString("value");
////            Logger.debug(feedback);
//            BuglyLog.d(TAG, "handleMessage: " + feedback);
//            JSONObject feedback_json = null;
//            try {
//                feedback_json = new JSONObject(feedback);
//            } catch (JSONException e) {
//                BuglyLog.w(TAG, "Login Failed.");
//                CrashReport.postCatchedException(e);
//                e.printStackTrace();
//                return;
//            }
////            Logger.info(feedback);
//            BuglyLog.i(TAG, "handleMessage: " + feedback);
//            try {
//                if (feedback_json.getInt("retcode") == 0) {
//
//                    JSONObject data_json2 = feedback_json.getJSONObject("data");
//                    String combo_id = data_json2.getString("combo_id");
//                    String open_id = data_json2.getString("open_id");
//                    String combo_token = data_json2.getString("combo_token");
//                    String account_type = data_json2.getString("account_type");
//
//                    roleData = new RoleData(activity,open_id, "", combo_id, combo_token, "16", account_type, "flyme");
//
//                    isLogin = true;
//                    makeToast(activity.getString(R.string.login_succeed));
//
//                } else {
//
//                    makeToast(feedback_json.getString("message"));
//                    isLogin = false;
//
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//    };
//
//    Runnable login_runnable = new Runnable() {
//        @Override
//        public void run() {
//
//            Map<String, Object> login_map = new HashMap<>();
//
//            login_map.put("device", device_id);
//            login_map.put("app_id", "1");
//            login_map.put("channel_id", "16");
//
//            String data_json = "{\"uid\":" +
//                    uid +
//                    ",\"session\":\"" +
//                    session +
//                    "\"}";
//
//            login_map.put("data", data_json);
//
//            String sign = Tools.bh3Sign(login_map);
//            ArrayList<String> arrayList = new ArrayList<>(login_map.keySet());
//            Collections.sort(arrayList);
//
//            JSONObject login_json = new JSONObject();
//
//            try {
//
//                for (String str : arrayList) {
//
//                    login_json.put(str, login_map.get(str));
//
//                }
//
//                login_json.put("sign", sign);
//
////                Logger.info(login_json.toString());
//                BuglyLog.i(TAG, "run: " + login_json.toString());
//                String feedback = Network.sendPost("https://api-sdk.mihoyo.com/bh3_cn/combo/granter/login/v2/login", login_json.toString());
//
//                Message msg = new Message();
//                Bundle data = new Bundle();
//                data.putString("value", feedback);
//                msg.setData(data);
//                login_handler.sendMessage(msg);
//            } catch (Exception ignore) {
//            }
//        }
//
//    };
//
//    public void doBHLogin() {
//        new Thread(login_runnable).start();
//    }
//
//    @SuppressLint("ShowToast")
//    private void makeToast(String result) {
//        try {
//            Toast.makeText(activity, result, Toast.LENGTH_LONG).show();
//        } catch (Exception e) {
//            Looper.prepare();
//            Toast.makeText(activity, result, Toast.LENGTH_LONG).show();
//            Looper.loop();
//        }
//    }
//}

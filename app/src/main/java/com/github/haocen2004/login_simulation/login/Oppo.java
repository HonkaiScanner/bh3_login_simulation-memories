package com.github.haocen2004.login_simulation.login;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Looper;
import android.widget.Toast;

import com.github.haocen2004.login_simulation.R;
import com.github.haocen2004.login_simulation.util.Network;
import com.github.haocen2004.login_simulation.util.RoleData;
import com.github.haocen2004.login_simulation.util.Tools;
import com.nearme.game.sdk.GameCenterSDK;
import com.nearme.game.sdk.callback.ApiCallback;
import com.tencent.bugly.crashreport.BuglyLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Oppo implements LoginImpl {
    private final Activity activity;
    private boolean isLogin;
    private String uid;
    private String token;
    private RoleData roleData;
    private final String appId = "";
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
                try {
                    JSONObject json = new JSONObject(s);
                    token = json.getString("token");
                    uid = json.getString("ssoid");
                    doBHLogin();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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

    }

    @Override
    public RoleData getRole() {
        return roleData;
    }

    @Override
    public boolean isLogin() {
        return isLogin;
    }

    public void doBHLogin() {

        Map<String, Object> login_map = new HashMap<>();

        login_map.put("device", device_id);
        login_map.put("app_id", "1");
        login_map.put("channel_id", "14");

        String data_json = "{\"ssoid\":" +
                uid +
                ",\"token\":\"" +
                token +
                "\"}";

        login_map.put("data", data_json);

        String sign = Tools.bh3Sign(login_map);
        ArrayList<String> arrayList = new ArrayList<>(login_map.keySet());
        Collections.sort(arrayList);

        JSONObject login_json = new JSONObject();

        try {

            for (String str : arrayList) {

                login_json.put(str, login_map.get(str));

            }

            login_json.put("sign", sign);

            BuglyLog.i(TAG, "doBHLogin: " + login_json.toString());
            String feedback = Network.sendPost("https://api-sdk.mihoyo.com/bh3_cn/combo/granter/login/v2/login", login_json.toString());
            JSONObject feedback_json = new JSONObject(feedback);
            BuglyLog.i(TAG, "doBHLogin: " + feedback);

            if (feedback_json.getInt("retcode") == 0) {

                JSONObject data_json2 = feedback_json.getJSONObject("data");
                String combo_id = data_json2.getString("combo_id");
                String combo_token = data_json2.getString("combo_token");
                String open_id = data_json2.getString("open_id");
                roleData = new RoleData(open_id, "", combo_id, combo_token, "18", "2", "oppo");

                isLogin = true;
                makeToast(activity.getString(R.string.login_succeed));


            } else {

                makeToast(feedback_json.getString("message"));
                isLogin = false;

            }

        } catch (Exception ignore) {
        }

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

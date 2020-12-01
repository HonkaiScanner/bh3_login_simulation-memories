package com.github.haocen2004.login_simulation.login;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.github.haocen2004.login_simulation.R;
import com.github.haocen2004.login_simulation.util.Network;
import com.github.haocen2004.login_simulation.util.RoleData;
import com.github.haocen2004.login_simulation.util.Tools;
import com.vivo.unionsdk.open.VivoAccountCallback;
import com.vivo.unionsdk.open.VivoUnionSDK;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Vivo implements LoginImpl {
    private Activity activity;
    private boolean isLogin;
    private String uid;
    private String token;
    private RoleData roleData;
    private String appId = "";
    private String device_id;
    private static String TAG = "Vivo Login";

    private VivoAccountCallback callback = new VivoAccountCallback() {
        @Override
        public void onVivoAccountLogin(String s, String s1, String s2) {
            uid = s1;
            token = s2;
//            roleData = new RoleData()
        }

        @Override
        public void onVivoAccountLogout(int i) {
            isLogin = false;
        }

        @Override
        public void onVivoAccountLoginCancel() {
            isLogin = false;
        }
    };

    public Vivo(Activity activity){
        this.activity = activity;
        device_id = Tools.getDeviceID(activity);
        VivoUnionSDK.initSdk(activity,appId,true);
        VivoUnionSDK.registerAccountCallback(activity,callback);
    }
    @Override
    public void login() {
        VivoUnionSDK.login(activity);
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

        String data_json = "{\"uid\":" +
                uid +
                ",\"access_key\":\"" +
//                access_token +   // 等待拆包获取传参
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

            Log.i(TAG, "doBHLogin: " + login_json.toString());
            String feedback = Network.sendPost("https://api-sdk.mihoyo.com/bh3_cn/combo/granter/login/v2/login", login_json.toString());
            JSONObject feedback_json = new JSONObject(feedback);
            Log.i(TAG, "doBHLogin: " + feedback);

            if (feedback_json.getInt("retcode") == 0) {

                JSONObject data_json2 = feedback_json.getJSONObject("data");
                String combo_id = data_json2.getString("combo_id");
                String combo_token = data_json2.getString("combo_token");
                String open_id = data_json2.getString("open_id");
                roleData = new RoleData(open_id, "", combo_id, combo_token, "14", "2", "vivo");

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

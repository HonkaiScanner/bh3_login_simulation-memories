package com.github.haocen2004.login_simulation.login;

import android.os.Looper;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.haocen2004.login_simulation.util.Logger;
import com.github.haocen2004.login_simulation.util.Network;
import com.github.haocen2004.login_simulation.util.RoleData;
import com.github.haocen2004.login_simulation.util.Tools;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import cn.gundam.sdk.shell.even.SDKEventKey;
import cn.gundam.sdk.shell.even.SDKEventReceiver;
import cn.gundam.sdk.shell.even.Subscribe;
import cn.gundam.sdk.shell.exception.AliLackActivityException;
import cn.gundam.sdk.shell.exception.AliNotInitException;
import cn.gundam.sdk.shell.open.ParamInfo;
import cn.gundam.sdk.shell.open.UCOrientation;
import cn.gundam.sdk.shell.param.SDKParamKey;
import cn.gundam.sdk.shell.param.SDKParams;
import cn.uc.gamesdk.UCGameSdk;

public class UC implements LoginImpl {

    private AppCompatActivity activity;
    private UCGameSdk sdk;
    private String sid;
    private boolean isLogin;
    private RoleData roleData;
    private SDKEventReceiver eventReceiver = new SDKEventReceiver() {

        @Subscribe(event = SDKEventKey.ON_INIT_SUCC)
        private void onInitSucc() throws AliNotInitException, AliLackActivityException {
            sdk.login(activity, null);
        }

        @Subscribe(event = SDKEventKey.ON_LOGIN_SUCC)
        private void onLoginSucc(String sid2) {
            sid = sid2;
            isLogin = true;
            doBHLogin();
        }


    };

    public UC(AppCompatActivity activity) {
        this.activity = activity;
        isLogin = false;


    }

    @Override
    public void login() {
        sdk = UCGameSdk.defaultSdk();
        sdk.registerSDKEventReceiver(this.eventReceiver);
        ParamInfo gpi = new ParamInfo();

        gpi.setGameId(654463);

        gpi.setOrientation(UCOrientation.PORTRAIT);

        SDKParams sdkParams = new SDKParams();
        sdkParams.put(SDKParamKey.GAME_PARAMS, gpi);
        try {
            sdk.initSdk(activity, sdkParams);

        } catch (AliLackActivityException e) {
            e.printStackTrace();
        }
    }

    public void doBHLogin() {

        Map<String, Object> login_map = new HashMap<>();

        String device_id = Tools.getDeviceID(activity);
        login_map.put("device", device_id);
        login_map.put("app_id", "1");
        login_map.put("channel_id", "20");

        String data_json = "{\"sid\":" + sid + "}";

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

            Logger.info(login_json.toString());
            String feedback = Network.sendPost("https://api-sdk.mihoyo.com/bh3_cn/combo/granter/login/v2/login", login_json.toString());
            JSONObject feedback_json = new JSONObject(feedback);
            Logger.info(feedback);

            if (feedback_json.getInt("retcode") == 0) {

                JSONObject data_json2 = feedback_json.getJSONObject("data");
                String combo_id = data_json2.getString("combo_id");
                String combo_token = data_json2.getString("combo_token");
                String account_type = data_json2.getString("account_type");

                roleData = new RoleData(sid, "", combo_id, combo_token, "20", account_type, "4.2.0_gf_android_uc");

                isLogin = true;
                makeToast("登录成功");


            } else {

                makeToast(feedback_json.getString("message"));
                isLogin = false;

            }

        } catch (Exception ignore) {
        }

    }

    private void makeToast(String result) {
        try {
            Toast.makeText(activity, result, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Looper.prepare();
            Toast.makeText(activity, result, Toast.LENGTH_LONG).show();
            Looper.loop();
        }
    }

    @Override
    public void logout() {
        try {
            sdk.logout(activity, null);
            sdk.exit(activity, null);
        } catch (AliLackActivityException | AliNotInitException e) {
            e.printStackTrace();
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
}

package com.github.haocen2004.login_simulation.login;

import android.os.Looper;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.haocen2004.login_simulation.util.Logger;
import com.github.haocen2004.login_simulation.util.Network;
import com.github.haocen2004.login_simulation.util.RoleData;
import com.github.haocen2004.login_simulation.util.Tools;
import com.xiaomi.gamecenter.sdk.MiCommplatform;
import com.xiaomi.gamecenter.sdk.MiErrorCode;
import com.xiaomi.gamecenter.sdk.OnInitProcessListener;
import com.xiaomi.gamecenter.sdk.entry.MiAppInfo;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Xiaomi implements LoginImpl {


    private AppCompatActivity activity;
    private boolean isLogin;
    private String uid;
    private String session;
    private RoleData roleData;

    public Xiaomi(AppCompatActivity activity) {
        this.activity = activity;
    }

    @Override
    public void login() {
        MiAppInfo appInfo = new MiAppInfo();
        appInfo.setAppId("2882303761517502034");
        appInfo.setAppKey("5841750261034");

        MiCommplatform.Init(activity, appInfo, new OnInitProcessListener() {
            @Override
            public void finishInitProcess(List<String> loginMethod, int gameConfig) {
                Logger.info("Init success");
            }

            @Override
            public void onMiSplashEnd() {
                //小米闪屏⻚结束回调,小米闪屏可配,无闪屏也会返回此回调,游戏的闪屏应当在收到此回调之后
            }
        });

        MiCommplatform.getInstance().miLogin(activity,
                (code, arg1) -> {
                    switch (code) {
                        case MiErrorCode.MI_XIAOMI_PAYMENT_SUCCESS:// 登陆成功
                            //获取用户的登陆后的UID（即用户唯一标识）
                            uid = arg1.getUid();

                            //以下为获取session并校验流程，如果是网络游戏必须校验,(12小时过期)
                            //获取用户的登陆的Session（请参考5.3.3流程校验Session有效性）
                            session = arg1.getSessionId();
                            //请开发者完成将uid和session提交给开发者自己服务器进行session验证

                            doBHLogin();
                            break;
                        case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_LOGIN_FAIL:
                            // 登陆失败
                            break;
                        case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_CANCEL:
                            // 取消登录
                            break;
                        case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_ACTION_EXECUTED:
                            //登录操作正在进行中
                            break;
                        default:
                            // 登录失败
                            break;
                    }
                });
    }

    public void doBHLogin() {

        Map<String, Object> login_map = new HashMap<>();

        String device_id = Tools.getDeviceID(activity);
        login_map.put("device", device_id);
        login_map.put("app_id", "1");
        login_map.put("channel_id", "11");

        String data_json = "{\"uid\":" +
                uid +
                ",\"session\":\"" +
                session +
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

            Logger.info(login_json.toString());
            String feedback = Network.sendPost("https://api-sdk.mihoyo.com/bh3_cn/combo/granter/login/v2/login", login_json.toString());
            JSONObject feedback_json = new JSONObject(feedback);
            Logger.info(feedback);

            if (feedback_json.getInt("retcode") == 0) {

                JSONObject data_json2 = feedback_json.getJSONObject("data");
                String combo_id = data_json2.getString("combo_id");
                String combo_token = data_json2.getString("combo_token");
                String open_id = data_json2.getString("open_id");
                String account_type = data_json2.getString("account_type");
                roleData = new RoleData(open_id, "", combo_id, combo_token, "14", account_type, "xiaomi");


                isLogin = true;
                makeToast("登录成功");


            } else {

                makeToast(feedback_json.getString("message"));
                isLogin = false;

            }

        } catch (Exception ignore) {
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
        try {
            Toast.makeText(activity, result, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Looper.prepare();
            Toast.makeText(activity, result, Toast.LENGTH_LONG).show();
            Looper.loop();
        }
    }

    @Override
    public boolean isLogin() {
        return isLogin;
    }
}

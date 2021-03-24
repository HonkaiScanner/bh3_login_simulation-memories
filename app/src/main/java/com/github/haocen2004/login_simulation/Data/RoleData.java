package com.github.haocen2004.login_simulation.Data;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.github.haocen2004.login_simulation.util.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import static com.github.haocen2004.login_simulation.util.Constant.BH_VER;
import static com.github.haocen2004.login_simulation.util.Tools.getOAServer;

public class RoleData {
    private final String open_id;
    private final String open_token;
    private final String combo_id;
    private final String combo_token;
    private final String channel_id;
    private JSONObject oaserver;
    private final String account_type;
    private String accountType;
    private final String oa_req_key;
    private boolean is_setup;
    private boolean uc_sign;
    private final Activity activity;

    Handler getOA_handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String feedback = data.getString("value");
            try {
                oaserver = new JSONObject(feedback);
                Logger.i("GetOAServer", "handleMessage: " + oaserver.toString());
                if (!oaserver.getBoolean("is_data_ready")) {
                    String msg1 = oaserver.getString("msg");
                    if (msg1.contains("更新"))
                        msg1 = "崩坏3维护中或热更新服务器离线\n请等待维护完成\n或尝试在设置里手动更改崩坏3版本并重新启动";

                    Toast.makeText(activity, "OA服务器获取错误\n" + msg1, Toast.LENGTH_LONG).show();
                    return;
                }
                is_setup = true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    Runnable getOA_runnable = () -> {

        Message msg = new Message();
        Bundle data = new Bundle();
        data.putString("value", getOAServer(RoleData.this));
        msg.setData(data);
        getOA_handler.sendMessage(msg);
    };

    public RoleData(Activity activity, String open_id, String open_token, String combo_id, String combo_token, String channel_id, String account_type, String oa_req_key, int special_tag) {
        this.activity = activity;
        this.open_id = open_id;
        this.open_token = open_token;
        this.combo_id = combo_id;
        this.combo_token = combo_token;
        this.channel_id = channel_id;
        this.account_type = account_type;
        this.accountType = account_type;
        this.oa_req_key = BH_VER + "_gf_android" + (oa_req_key.isEmpty() ? "" : "_" + oa_req_key);
        if (special_tag == 1) {
            this.uc_sign = true;
            accountType = "3";
        } else if (special_tag == 2) {
            accountType = "9";
        } else if (special_tag == 3) {
            uc_sign = true;
            accountType = "15";
        } else if (special_tag == 4) {
            accountType = "17";
        }
        new Thread(getOA_runnable).start();
    }


    public String getOpen_id() {
        return open_id;
    }

    public String getCombo_id() {
        return combo_id;
    }

    public String getCombo_token() {
        return combo_token;
    }

    //区分官服和渠道服
    public String getAccount_type() {
        return account_type;
    }

    //渠道id
    public String getAccountType() {
        return accountType;
    }

    public String getChannel_id() {
        return channel_id;
    }

    public JSONObject getOaserver() {
        return oaserver;
    }

    public String getOa_req_key() {
        return oa_req_key;
    }

    public Boolean is_setup() {
        return is_setup;
    }

    public String getOpen_token() {
        return open_token;
    }

    public boolean isUc_sign() {
        return uc_sign;
    }
}
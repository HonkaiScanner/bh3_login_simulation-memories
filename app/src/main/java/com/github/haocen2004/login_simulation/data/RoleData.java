package com.github.haocen2004.login_simulation.data;

import static com.github.haocen2004.login_simulation.data.Constant.BH_VER;
import static com.github.haocen2004.login_simulation.data.Constant.ENC_DISPATCH;
import static com.github.haocen2004.login_simulation.utils.Tools.getOAServer;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.github.haocen2004.login_simulation.R;
import com.github.haocen2004.login_simulation.login.LoginCallback;
import com.github.haocen2004.login_simulation.utils.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RoleData {
    private final String open_id;
    private final String open_token;
    private final String combo_id;
    private final String combo_token;
    private final String channel_id;
    private String str_oaserver;
    private String enc_oaserver;
    private final String account_type;
    private String accountType;
    private final String oa_req_key;
    private boolean is_setup;
    private boolean uc_sign;
    //    private Activity activity;
    private final LoginCallback callback;

    Handler getOA_handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String feedback = data.getString("value");
            try {
                if (feedback != null) {
                    if (ENC_DISPATCH) {
                        enc_oaserver = feedback;
                        is_setup = true;
                        callback.onLoginSucceed(RoleData.this);
                        return;
                    }
                    JSONObject oaserver = new JSONObject(feedback);
                    Logger.i("GetOAServer", "handleMessage: " + oaserver);
                    if (!oaserver.getBoolean("is_data_ready")) {
                        String msg1 = oaserver.getString("msg");
                        if (msg1.contains("更新"))
                            msg1 = "崩坏3维护中或热更新服务器离线\n请等待维护完成\n或尝试在设置里手动更改崩坏3版本并重新启动";
                        callback.onLoginFailed();
                        Logger.getLogger(null).makeToast("OA服务器获取错误\n" + msg1);
                        return;
                    } else if (oaserver.has("stop_end_time")) {
                        if (oaserver.getLong("server_cur_time") < oaserver.getLong("stop_end_time")) {
                            String msg1 = "崩坏3停服维护中\n请等待维护完成后再尝试登陆\n";
                            callback.onLoginFailed();
                            Logger.getLogger(null).makeToast("OA服务器获取错误\n" + msg1);
                            return;
                        }
                    }
                    str_oaserver = feedback;
                    is_setup = true;
                    callback.onLoginSucceed(RoleData.this);
                } else {
                    callback.onLoginFailed();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    Runnable getOA_runnable = () -> {
        Logger.getLogger(null).makeToast(R.string.msg_getOa);
        Message msg = new Message();
        Bundle data = new Bundle();
        data.putString("value", getOAServer(RoleData.this));
        msg.setData(data);
        getOA_handler.sendMessage(msg);
    };

    public RoleData(String open_id, String open_token, String combo_id, String combo_token, String channel_id, String account_type, String oa_req_key, int special_tag, LoginCallback callback) {
        this.callback = callback;
//        this.activity = activity;
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
            accountType = "8";
        } else if (special_tag == 5) {
            accountType = "6";
        } else if (special_tag == 7) {
            accountType = "5";
        } else if (special_tag == 8) {
            accountType = "10";
        } else if (special_tag == 9) {
            accountType = "4";
        } else if (special_tag == 10) {
            accountType = "7";
        }
        Logger.addBlacklist(combo_token);
        Logger.addBlacklist(open_token);


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

    public String getAccount_type() {
        return account_type;
    }

    public String getAccountType() {
        return accountType;
    }

    public String getChannel_id() {
        return channel_id;
    }

    public JSONObject getOaserver() {
        try {
            return new JSONObject(str_oaserver);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
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

    public String getEnc_oaserver() {
        return enc_oaserver;
    }

    public RoleData(Map<String, String> map, LoginCallback callback) {
        open_id = map.get("open_id");
        open_token = map.get("open_token");
        combo_id = map.get("combo_id");
        combo_token = map.get("combo_token");
        channel_id = map.get("channel_id");
        str_oaserver = map.get("str_oaserver");
        account_type = map.get("account_type");
        accountType = map.get("accountType");
        oa_req_key = map.get("oa_req_key");
        is_setup = Boolean.parseBoolean(map.get("is_setup"));
        uc_sign = Boolean.parseBoolean(map.get("uc_sign"));
        this.callback = callback;
    }

    public Map<String, String> getMap() {
        Map<String, String> map = new HashMap<>();

        map.put("open_id", open_id);
        map.put("open_token", open_token);
        map.put("combo_id", combo_id);
        map.put("combo_token", combo_token);
        map.put("channel_id", channel_id);
        map.put("str_oaserver", str_oaserver);
        map.put("account_type", account_type);
        map.put("accountType", accountType);
        map.put("oa_req_key", oa_req_key);
        map.put("is_setup", Boolean.toString(is_setup));
        map.put("uc_sign", Boolean.toString(uc_sign));
        return map;
    }
}
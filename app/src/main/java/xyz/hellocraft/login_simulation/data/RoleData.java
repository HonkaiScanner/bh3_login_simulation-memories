package xyz.hellocraft.login_simulation.data;

import static xyz.hellocraft.login_simulation.data.Constant.BH_VER;
import static xyz.hellocraft.login_simulation.utils.Tools.getOAServer;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.github.haocen2004.login_simulation.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import xyz.hellocraft.login_simulation.login.LoginCallback;
import xyz.hellocraft.login_simulation.utils.Logger;

public class RoleData {
    private String open_id = "";
    private String open_token = "";
    ;
    private String combo_id = "";
    ;
    private String combo_token = "";
    ;
    private String channel_id = "";
    ;
    private String account_type = "";
    ;
    private String oa_req_key = "";
    ;
    //    private Activity activity;
    private final LoginCallback callback;
    private String str_oaserver = "";
    ;
    private String enc_oaserver = "dec_oaserver";
    private String accountType = "";
    ;
    private boolean is_setup = false;
    Handler getOA_handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String feedback = data.getString("value");
            try {
                if (feedback != null) {
                    JSONObject oaserver = new JSONObject(feedback);
                    Logger.i("GetOAServer", "handleMessage: " + oaserver);
                    if (oaserver.getInt("retcode") == 0) {
                        enc_oaserver = oaserver.getString("data");
                        is_setup = true;
                        callback.onLoginSucceed(RoleData.this);
                    } else {
                        Logger.getLogger(null).makeToast(oaserver.getString("message"));
                        callback.onLoginFailed();
                    }
                } else {
                    callback.onLoginFailed();
                }
            } catch (Exception e) {
                e.printStackTrace();
                callback.onLoginFailed();
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
    private boolean uc_sign;

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
        this.oa_req_key = BH_VER + "_gf_android" + (TextUtils.isEmpty(oa_req_key) ? "" : "_" + oa_req_key);
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
        } else if (special_tag == 11) {
            accountType = "21";
        }
        Logger.addBlacklist(combo_token);
        Logger.addBlacklist(open_token);


        new Thread(getOA_runnable).start();
    }


    public RoleData(Map<String, String> map, LoginCallback callback) {
        open_id = map.get("open_id");
        open_token = map.get("open_token");
        combo_id = map.get("combo_id");
        combo_token = map.get("combo_token");
        channel_id = map.get("channel_id");
        str_oaserver = map.get("str_oaserver");
        enc_oaserver = map.get("enc_oaserver");
        account_type = map.get("account_type");
        accountType = map.get("accountType");
        oa_req_key = map.get("oa_req_key");
        uc_sign = Boolean.parseBoolean(map.get("uc_sign"));
        this.callback = callback;
        if (enc_oaserver == null) {
            new Thread(getOA_runnable).start();
        } else {
            is_setup = true;
        }
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

    public Map<String, String> getMap() {
        Map<String, String> map = new HashMap<>();

        map.put("open_id", open_id);
        map.put("open_token", open_token);
        map.put("combo_id", combo_id);
        map.put("combo_token", combo_token);
        map.put("channel_id", channel_id);
        map.put("str_oaserver", str_oaserver);
        map.put("enc_oaserver", enc_oaserver);
        map.put("account_type", account_type);
        map.put("accountType", accountType);
        map.put("oa_req_key", oa_req_key);
        map.put("uc_sign", Boolean.toString(uc_sign));
        return map;
    }
}
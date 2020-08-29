package com.github.haocen2004.login_simulation.util;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import org.json.JSONException;
import org.json.JSONObject;

import static com.github.haocen2004.login_simulation.util.Tools.getOAServer;

public class RoleData {
    private String open_id;
    private String open_token;
    private String combo_id;
    private String combo_token;
    private String channel_id;
    private JSONObject oaserver;
    private String account_type;
    private String oa_req_key;
    private boolean is_setup;
    @SuppressLint("HandlerLeak")
    Handler getOA_handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String feedback = data.getString("value");
            try {
                oaserver = new JSONObject(feedback);
                System.out.println(oaserver.toString());
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

    public RoleData(String open_id, String open_token, String combo_id, String combo_token, String channel_id, String account_type, String oa_req_key) {
        this.open_id = open_id;
        this.open_token = open_token;
        this.combo_id = combo_id;
        this.combo_token = combo_token;
        this.channel_id = channel_id;
        this.account_type = account_type;
        this.oa_req_key = oa_req_key;
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
}


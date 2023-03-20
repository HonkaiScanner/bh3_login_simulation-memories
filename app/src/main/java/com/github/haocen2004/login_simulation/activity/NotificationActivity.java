package com.github.haocen2004.login_simulation.activity;

import android.content.Intent;
import android.os.Bundle;

import com.github.haocen2004.login_simulation.data.dialog.DialogData;
import com.github.haocen2004.login_simulation.data.dialog.DialogLiveData;
import com.github.haocen2004.login_simulation.utils.Logger;

import org.json.JSONException;
import org.json.JSONObject;

public class NotificationActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d("PushService", "onClickNotification");
        Intent nfIntent = new Intent(this, MainActivity.class);
        nfIntent.setAction(Intent.ACTION_MAIN);
        nfIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        nfIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        startActivity(nfIntent);
        Intent dataIntent = getIntent();
        try {
            for (String s : dataIntent.getExtras().keySet()) {
                Logger.d("PushService", s + ":" + dataIntent.getExtras().get(s));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.d("PushService", "NO DATA");
        }
        if (dataIntent != null && dataIntent.hasExtra("com.avoscloud.Data")) {
            String message = dataIntent.getStringExtra("com.avoscloud.Data");
            String channel = dataIntent.getStringExtra("com.avoscloud.Channel");
            Logger.d("PushService", "message=" + message + ", channel=" + channel);
            if (channel.equals("self_login_succ")) {
                DialogData dialogData = new DialogData("扫码成功通知", message, "确定");
                DialogLiveData.getINSTANCE(null).addDelayDialog(dialogData);
            } else {
                try {
                    JSONObject messageJson = new JSONObject(message);
                    DialogData dialogData = new DialogData("新通知：", messageJson.getString("alert"), "确定");
                    DialogLiveData.getINSTANCE(null).addDelayDialog(dialogData);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        finish();
    }
}

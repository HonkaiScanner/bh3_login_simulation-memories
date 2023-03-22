package com.github.haocen2004.login_simulation.login;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.github.haocen2004.login_simulation.data.Constant.BILI_APP_KEY;
import static com.github.haocen2004.login_simulation.data.Constant.BILI_INIT;
import static com.github.haocen2004.login_simulation.data.Constant.DEBUG_MODE;
import static com.github.haocen2004.login_simulation.data.Constant.HAS_ACCOUNT;
import static com.github.haocen2004.login_simulation.utils.Encrypt.paySign;
import static com.github.haocen2004.login_simulation.utils.Logger.getLogger;
import static java.lang.Long.parseLong;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bsgamesdk.android.BSGameSdk;
import com.bsgamesdk.android.callbacklistener.BSGameSdkError;
import com.bsgamesdk.android.callbacklistener.CallbackListener;
import com.bsgamesdk.android.callbacklistener.InitCallbackListener;
import com.bsgamesdk.android.callbacklistener.OrderCallbackListener;
import com.github.haocen2004.login_simulation.R;
import com.github.haocen2004.login_simulation.data.RoleData;
import com.github.haocen2004.login_simulation.data.dialog.ButtonData;
import com.github.haocen2004.login_simulation.data.dialog.DialogData;
import com.github.haocen2004.login_simulation.data.dialog.DialogLiveData;
import com.github.haocen2004.login_simulation.utils.DialogHelper;
import com.github.haocen2004.login_simulation.utils.Logger;
import com.github.haocen2004.login_simulation.utils.Network;
import com.github.haocen2004.login_simulation.utils.Tools;
import com.tencent.bugly.crashreport.CrashReport;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public class Bilibili implements LoginImpl {

    private static final String TAG = "Bilibili Login";
    private String access_token;
    private SharedPreferences app_pref;
    private String username;
    private String uid;
    private String open_id;
    private String combo_token;
    private final CallbackListener biliLogin = new CallbackListener() {

        @Override
        public void onSuccess(Bundle arg0) {
            // 此处为操作成功时执行，返回值通过Bundle传回

            uid = arg0.getString("uid");
            username = arg0.getString("username");
            access_token = arg0.getString("access_token");

            preferences.edit().clear().apply();
            preferences.edit().putString("username", username).putString("uid", uid).putBoolean("last_login_succeed", true).apply();

            String data_json = "{\"uid\":" +
                    uid +
                    ",\"access_key\":\"" +
                    access_token +
                    "\"}";
            Logger.addBlacklist(access_token);
            JSONObject feedback_json = null;
            try {
                feedback_json = new JSONObject(Objects.requireNonNull(Tools.verifyAccount(activity, "14", data_json)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (feedback_json == null) {
                    makeToast("Empty Return");
                    callback.onLoginFailed();
                } else if (feedback_json.getInt("retcode") != 0) {
                    makeToast(feedback_json.getString("message"));
                    callback.onLoginFailed();
                } else {
                    JSONObject data_json2 = feedback_json.getJSONObject("data");
                    String combo_id = data_json2.getString("combo_id");
                    open_id = data_json2.getString("open_id");
                    combo_token = data_json2.getString("combo_token");
                    Logger.addBlacklist(combo_token);
//                        String account_type = data_json2.getString("account_type");
                    saveSlotInfo();
                    roleData = new RoleData(open_id, "", combo_id, combo_token, "14", "2", "bilibili", 0, callback);
                    isLogin = true;
//                        makeToast(activity.getString(R.string.login_succeed));
                }
//                doBHLogin();
            } catch (JSONException e) {
                CrashReport.postCatchedException(e);
                makeToast("parse ERROR");
                callback.onLoginFailed();
            }

        }

        @Override
        public void onFailed(BSGameSdkError arg0) {

            preferences.edit().putBoolean("last_login_succeed", false).apply();
            // 此处为操作失败时执行，返回值为BSGameSdkError类型变量，其中包含ErrorCode和ErrorMessage
            Logger.e(TAG, "onFailed\nErrorCode : "
                    + arg0.getErrorCode() + "\nErrorMessage : "
                    + arg0.getErrorMessage());
            makeToast("onFailed\nErrorCode : "
                    + arg0.getErrorCode() + "\nErrorMessage : "
                    + arg0.getErrorMessage());
            callback.onLoginFailed();
        }

        @Override
        public void onError(BSGameSdkError arg0) {

            preferences.edit().putBoolean("last_login_succeed", false).apply();
            // 此处为操作异常时执行，返回值为BSGameSdkError类型变量，其中包含ErrorCode和ErrorMessage
            Logger.e(TAG, "onError\nErrorCode : "
                    + arg0.getErrorCode() + "\nErrorMessage : "
                    + arg0.getErrorMessage());
            makeToast("onError\nErrorCode : " + arg0.getErrorCode()
                    + "\nErrorMessage : " + arg0.getErrorMessage());
            callback.onLoginFailed();
        }
    };
    private BSGameSdk gameSdk;
    private SharedPreferences preferences;
    private final AppCompatActivity activity;
    private boolean isLogin;
    private RoleData roleData;
    private final Logger Log;
    private final LoginCallback callback;


    public Bilibili(AppCompatActivity activity, LoginCallback loginCallback) {
        callback = loginCallback;
        this.activity = activity;
        Log = getLogger(activity);
        app_pref = getDefaultSharedPreferences(activity);
        //isLogin = false;
    }

    private String honkai_uid;

    private void doBiliLogin() {
        boolean checkLastLogin = preferences.getBoolean("last_login_succeed", false);
        Logger.d(TAG, "checkBiliLastLogin: " + checkLastLogin);
        if (checkLastLogin) {

            gameSdk.login(biliLogin);
        } else {

            DialogData dialogData = new DialogData("B服注意事项", "使用授权登陆可能会导致 登陆失败\n\n请在 扫码器内 使用账户密码登录");
            dialogData.setPositiveButtonData(new ButtonData("我已知晓") {
                @Override
                public void callback(DialogHelper dialogHelper) {
                    super.callback(dialogHelper);
                    gameSdk.login(biliLogin);
                }
            });
            DialogLiveData.getINSTANCE(activity).addNewDialog(dialogData);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public boolean logout() {
        if (DEBUG_MODE && HAS_ACCOUNT) {

            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("为当前账号购买小月卡");    //设置对话框标题
            LinearLayout linearLayout = new LinearLayout(activity);
            final EditText edit = new EditText(activity);
            final TextView textView = new TextView(activity);
            textView.setText("请输入当前登录账户的UID");
            linearLayout.addView(textView);
            linearLayout.addView(edit);
            builder.setView(linearLayout);
            builder.setPositiveButton("确认", (dialog, which) -> {
                dialog.dismiss();
                honkai_uid = edit.getText().toString().strip();
                DialogData confirm_dialog = new DialogData("UID二次确认", "确认为UID: " + honkai_uid + "\n购买 30天水晶大礼包 吗？");
                confirm_dialog.setCancelable(false);
                confirm_dialog.setPositiveButtonData(new ButtonData("确认") {
                    @Override
                    public void callback(DialogHelper dialogHelper) {
                        super.callback(dialogHelper);
                        JSONObject order = new JSONObject();
                        JSONObject who = new JSONObject();
                        JSONObject pay_json = new JSONObject();
                        Map<String, Object> map = new HashMap<>();
                        try {
                            order.put("country", "CHN");
                            order.put("note", "EXPEND_msg");
                            order.put("amount", 3000);
                            order.put("goods_id", "Bh3GiftHardCoinTier5");
                            order.put("goods_extra", "30天水晶大礼包");
                            order.put("client_type", 2);
                            order.put("delivery_url", "");
                            order.put("uid", honkai_uid);
                            order.put("price_tier", "");
                            order.put("goods_title", "30天水晶大礼包");
                            order.put("goods_num", "1");
                            order.put("currency", "CNY");
                            order.put("region", "bb01");
                            order.put("channel_id", 14);
                            order.put("app_id", 1);
                            order.put("device", Tools.getDeviceID(activity));
                            order.put("account", open_id);

                            who.put("channel_id", "14");
                            who.put("account", open_id); //open_id
                            who.put("token", combo_token); //combo_token


                            for (Iterator<String> it = order.keys(); it.hasNext(); ) {
                                String key = it.next();
                                map.put(key, order.get(key));
                            }
                            String sign = paySign(map, "0ebc517adb1b62c6b408df153331f9aa");

                            pay_json.put("who", who);
                            pay_json.put("order", order);
                            pay_json.put("sign", sign);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        new Thread() {
                            @Override
                            public void run() {
                                super.run();
                                String feedback = Network.sendPost("https://api-sdk.mihoyo.com/bh3_cn/combo/cashier/cashier/createOrder", pay_json.toString());
                                Logger.d(TAG, feedback);

                                JSONObject feedback_json = null;
                                try {
                                    feedback_json = new JSONObject(feedback);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                try {

                                    if (feedback_json == null) {
                                        Log.makeToast("Empty Return");
//                                callback.onLoginFailed();
                                    } else if (feedback_json.getInt("retcode") != 0) {
                                        Log.makeToast(feedback_json.getString("message"));
//                                callback.onLoginFailed();
                                    } else {
                                        JSONObject data_json2 = feedback_json.getJSONObject("data");
                                        JSONObject cp_order = new JSONObject(data_json2.getString("encode_order"));

                                        gameSdk.notifyZone("378", "安卓服", honkai_uid, honkai_uid);

                                        gameSdk.pay(parseLong(uid), username, honkai_uid, "378", cp_order.getInt("total_fee"),
                                                cp_order.getInt("game_money"), cp_order.getString("out_trade_no"), cp_order.getString("subject"), cp_order.getString("body"),
                                                cp_order.getString("extension_info"), cp_order.getString("notify_url"), cp_order.getString("order_sign"), new OrderCallbackListener() {
                                                    @Override
                                                    public void onSuccess(String s, String s1) {
                                                        Logger.d(TAG, "bili pay successfully  " + s + " - " + s1);
                                                    }

                                                    @Override
                                                    public void onFailed(String s, BSGameSdkError bsGameSdkError) {
                                                        Logger.d(TAG, "pay onFailed " + s + " " + bsGameSdkError.getErrorMessage());
                                                        Log.makeToast(bsGameSdkError.getErrorMessage());
                                                    }

                                                    @Override
                                                    public void onError(String s, BSGameSdkError bsGameSdkError) {
                                                        Logger.d(TAG, "pay onError " + s + " " + bsGameSdkError.getErrorMessage());
                                                    }
                                                });

                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Log.makeToast("支付失败");
                                }
                            }
                        }.start();

                    }
                });
                confirm_dialog.setNegativeButtonData("取消");
                DialogLiveData.getINSTANCE(activity).addNewDialog(confirm_dialog);
            });
            builder.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());
            builder.setCancelable(false);
            AlertDialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();

            return false;

        } else {
            isLogin = false;
            gameSdk.logout(new CallbackListener() {

                @Override
                public void onSuccess(Bundle arg0) {
                    // 此处为操作成功时执行，返回值通过Bundle传回
                    Logger.d(TAG, "onSuccess");
                    try {
                        preferences.edit().clear().apply();
                        new File(activity.getFilesDir().getParent(), "shared_prefs/bili_user_" + app_pref.getInt("bili_slot", 1) + ".xml").delete();
                        new File(activity.getFilesDir().getParent(), "shared_prefs/usernamelist_" + app_pref.getInt("bili_slot", 1) + ".xml").delete();
                        new File(activity.getFilesDir().getParent(), "shared_prefs/TouristLogin_" + app_pref.getInt("bili_slot", 1) + ".xml").delete();
                        new File(activity.getFilesDir().getParent(), "shared_prefs/login_" + app_pref.getInt("bili_slot", 1) + ".xml").delete();
                        new File(activity.getFilesDir().getParent(), "shared_prefs/userinfoCache_" + app_pref.getInt("bili_slot", 1) + ".xml").delete();
                        Log.makeToast(R.string.cache_delete);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                    makeToast(activity.getString(R.string.logout));

                }

                @Override
                public void onFailed(BSGameSdkError arg0) {
                    // 此处为操作失败时执行，返回值为BSGameSdkError类型变量，其中包含ErrorCode和ErrorMessage
                    Logger.e(TAG, "onFailed\nErrorCode : "
                            + arg0.getErrorCode() + "\nErrorMessage : "
                            + arg0.getErrorMessage());
                    makeToast("onFailed\nErrorCode : "
                            + arg0.getErrorCode() + "\nErrorMessage : "
                            + arg0.getErrorMessage());
                }

                @Override
                public void onError(BSGameSdkError arg0) {
                    // 此处为操作异常时执行，返回值为BSGameSdkError类型变量，其中包含ErrorCode和ErrorMessage
                    Logger.e(TAG, "onError\nErrorCode : "
                            + arg0.getErrorCode() + "\nErrorMessage : "
                            + arg0.getErrorMessage());
                    makeToast("onError\nErrorCode : " + arg0.getErrorCode()
                            + "\nErrorMessage : " + arg0.getErrorMessage());
                }
            });
            return true;
        }
    }

    @Override
    public void login() {
        changeSlotInfo();
        if (BILI_INIT) {
            gameSdk = BSGameSdk.getInstance();
            doBiliLogin();
        } else {
            BSGameSdk.initialize(true, activity, "590", "180",
                    "378", BILI_APP_KEY, new InitCallbackListener() {
                        @Override
                        public void onSuccess() {
//                        Logger.info("Bilibili SDK setup succeed");
                            Logger.i(TAG, "onSuccess: Setup Succeed");
                            doBiliLogin();
                            BILI_INIT = true;
                        }

                        @Override
                        public void onFailed() {

                            Logger.w(TAG, "Bilibili SDK setup Failed");

                        }
                    }, () -> System.exit(0));

            gameSdk = BSGameSdk.getInstance();
        }
    }

    @Override
    public RoleData getRole() {
        return roleData;
    }

    @SuppressLint("ShowToast")
    private void makeToast(String result) {
        try {
            Log.makeToast(result);
//            Toast.makeText(activity, result, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Looper.prepare();
            Log.makeToast(result);
//            Toast.makeText(activity, result, Toast.LENGTH_LONG).show();
            Looper.loop();
        }
    }

    @Override
    public boolean isLogin() {
        return isLogin;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setRole(RoleData roleData) {
        this.roleData = roleData;
        isLogin = true;
    }

    private void saveSlotInfo() {
        Logger.d(TAG, "saving slot " + app_pref.getInt("bili_slot", 1));
        SharedPreferences slotPref = activity.getSharedPreferences("bili_access_token_" + app_pref.getInt("bili_slot", 1), Context.MODE_PRIVATE);
        SharedPreferences mainPref = activity.getSharedPreferences("bili_access_token", Context.MODE_PRIVATE);
        slotPref.edit().clear().apply();
        for (String s : mainPref.getAll().keySet()) {
            if (mainPref.getAll().get(s) instanceof String) {
                slotPref.edit().putString(s, mainPref.getString(s, "")).apply();
            }
        }
        slotPref = activity.getSharedPreferences("login_" + app_pref.getInt("bili_slot", 1), Context.MODE_PRIVATE);
        mainPref = activity.getSharedPreferences("login", Context.MODE_PRIVATE);
        slotPref.edit().clear().apply();
        for (String s : mainPref.getAll().keySet()) {
            if (mainPref.getAll().get(s) instanceof String) {
                slotPref.edit().putString(s, mainPref.getString(s, "")).apply();
            }
        }
        slotPref = activity.getSharedPreferences("TouristLogin_" + app_pref.getInt("bili_slot", 1), Context.MODE_PRIVATE);
        mainPref = activity.getSharedPreferences("TouristLogin", Context.MODE_PRIVATE);
        slotPref.edit().clear().apply();
        for (String s : mainPref.getAll().keySet()) {
            if (mainPref.getAll().get(s) instanceof String) {
                slotPref.edit().putString(s, mainPref.getString(s, "")).apply();
            }
        }
        slotPref = activity.getSharedPreferences("userinfoCache_" + app_pref.getInt("bili_slot", 1), Context.MODE_PRIVATE);
        mainPref = activity.getSharedPreferences("userinfoCache", Context.MODE_PRIVATE);
        slotPref.edit().clear().apply();
        for (String s : mainPref.getAll().keySet()) {
            if (mainPref.getAll().get(s) instanceof String) {
                slotPref.edit().putString(s, mainPref.getString(s, "")).apply();
            }
        }
        slotPref = activity.getSharedPreferences("usernamelist_" + app_pref.getInt("bili_slot", 1), Context.MODE_PRIVATE);
        mainPref = activity.getSharedPreferences("usernamelist", Context.MODE_PRIVATE);
        slotPref.edit().clear().apply();
        for (String s : mainPref.getAll().keySet()) {
            if (mainPref.getAll().get(s) instanceof String) {
                slotPref.edit().putString(s, mainPref.getString(s, "")).apply();
            }
        }
    }

    private void changeSlotInfo() {
        Logger.d(TAG, "loading slot " + app_pref.getInt("bili_slot", 1));
        preferences = activity.getSharedPreferences("bili_user_" + app_pref.getInt("bili_slot", 1), Context.MODE_PRIVATE);
        SharedPreferences slotPref = activity.getSharedPreferences("bili_access_token_" + app_pref.getInt("bili_slot", 1), Context.MODE_PRIVATE);
        SharedPreferences mainPref = activity.getSharedPreferences("bili_access_token", Context.MODE_PRIVATE);
        mainPref.edit().clear().apply();
        for (String s : slotPref.getAll().keySet()) {
            if (slotPref.getAll().get(s) instanceof String) {
                mainPref.edit().putString(s, slotPref.getString(s, "")).apply();
            }
        }
        slotPref = activity.getSharedPreferences("login_" + app_pref.getInt("bili_slot", 1), Context.MODE_PRIVATE);
        mainPref = activity.getSharedPreferences("login", Context.MODE_PRIVATE);
        mainPref.edit().clear().apply();
        for (String s : slotPref.getAll().keySet()) {
            if (slotPref.getAll().get(s) instanceof String) {
                mainPref.edit().putString(s, slotPref.getString(s, "")).apply();
            }
        }
        slotPref = activity.getSharedPreferences("TouristLogin_" + app_pref.getInt("bili_slot", 1), Context.MODE_PRIVATE);
        mainPref = activity.getSharedPreferences("TouristLogin", Context.MODE_PRIVATE);
        mainPref.edit().clear().apply();
        for (String s : slotPref.getAll().keySet()) {
            if (slotPref.getAll().get(s) instanceof String) {
                mainPref.edit().putString(s, slotPref.getString(s, "")).apply();
            }
        }
        slotPref = activity.getSharedPreferences("userinfoCache_" + app_pref.getInt("bili_slot", 1), Context.MODE_PRIVATE);
        mainPref = activity.getSharedPreferences("userinfoCache", Context.MODE_PRIVATE);
        mainPref.edit().clear().apply();
        for (String s : slotPref.getAll().keySet()) {
            if (slotPref.getAll().get(s) instanceof String) {
                mainPref.edit().putString(s, slotPref.getString(s, "")).apply();
            }
        }
        slotPref = activity.getSharedPreferences("usernamelist_" + app_pref.getInt("bili_slot", 1), Context.MODE_PRIVATE);
        mainPref = activity.getSharedPreferences("usernamelist", Context.MODE_PRIVATE);
        mainPref.edit().clear().apply();
        for (String s : slotPref.getAll().keySet()) {
            if (slotPref.getAll().get(s) instanceof String) {
                mainPref.edit().putString(s, slotPref.getString(s, "")).apply();
            }
        }
        activity.getSharedPreferences("com.bilibili.track", Context.MODE_PRIVATE).edit().putString("LOGINID", preferences.getString("uid", "0")).apply();
    }


}


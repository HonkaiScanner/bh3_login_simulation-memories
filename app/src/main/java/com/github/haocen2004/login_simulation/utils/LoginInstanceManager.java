package com.github.haocen2004.login_simulation.utils;

import static com.github.haocen2004.login_simulation.utils.Tools.changeToWDJ;

import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.github.haocen2004.login_simulation.R;
import com.github.haocen2004.login_simulation.login.Bilibili;
import com.github.haocen2004.login_simulation.login.Flyme;
import com.github.haocen2004.login_simulation.login.Huawei;
import com.github.haocen2004.login_simulation.login.LoginCallback;
import com.github.haocen2004.login_simulation.login.LoginImpl;
import com.github.haocen2004.login_simulation.login.Official;
import com.github.haocen2004.login_simulation.login.Oppo;
import com.github.haocen2004.login_simulation.login.Qihoo;
import com.github.haocen2004.login_simulation.login.Tencent;
import com.github.haocen2004.login_simulation.login.UC;
import com.github.haocen2004.login_simulation.login.Vivo;
import com.github.haocen2004.login_simulation.login.Xiaomi;

public class LoginInstanceManager {

    private static LoginInstanceManager INSTANCE;
    private final AppCompatActivity mContext;
    private final Logger Log;
    private final String TAG = "LoginInstanceManager";
    private LoginCallback mCallback;
    private LoginImpl mLoginImpl;
    private String tempServerType = "none";

    public LoginInstanceManager(AppCompatActivity context) {
        this.mContext = context;
        this.Log = Logger.getLogger(context);
        String server_type = PreferenceManager.getDefaultSharedPreferences(mContext).getString("server_type", "");
        Logger.d(TAG, "init, default Server:" + server_type);
    }

    public static LoginInstanceManager getINSTANCE(AppCompatActivity context) {
        if (INSTANCE == null) {
            synchronized (LoginInstanceManager.class) {
                INSTANCE = new LoginInstanceManager(context);
            }
        }
        return INSTANCE;
    }

    public LoginImpl getLoginImpl() {
        if (mLoginImpl == null || tempServerType.equals("none")) {
            genLoginImpl();
        }
        if (!tempServerType.equals("none") && tempServerType.equals(PreferenceManager.getDefaultSharedPreferences(mContext).getString("server_type", ""))) {
            genLoginImpl();
        }
        return mLoginImpl;
    }

    public void setCallback(LoginCallback mCallback) {
        if (mCallback.equals(this.mCallback)) return;
        this.mCallback = mCallback;
    }

    private void genLoginImpl() {
        if (mCallback == null) {
            Log.makeToast("LOGIN CALLBACK IS NULL");
            return;
        }
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        try {
            switch (pref.getString("server_type", "")) {
                case "Official":
                    mLoginImpl = new Official(mContext, mCallback);
                    break;
                case "Xiaomi":
                    mLoginImpl = new Xiaomi(mContext, mCallback);
                    //11
                    break;
                case "Bilibili":
                    mLoginImpl = new Bilibili(mContext, mCallback);
                    //14
                    break;
                case "UC":
                    if (pref.getBoolean("use_wdj", false)) {
                        changeToWDJ(mContext);
                    }
                    mLoginImpl = new UC(mContext, mCallback);
                    //20
                    break;
                case "Vivo":
                    mLoginImpl = new Vivo(mContext, mCallback);
                    break;
                case "Oppo":
                    mLoginImpl = new Oppo(mContext, mCallback);
                    break;
                case "Flyme":
                    mLoginImpl = new Flyme(mContext, mCallback);
                    break;
                case "YYB":
                    mLoginImpl = new Tencent(mContext, mCallback);
                    break;
                case "Huawei":
                    mLoginImpl = new Huawei(mContext, mCallback);
                    break;
                case "Qihoo":
                    mLoginImpl = new Qihoo(mContext, mCallback);
                    break;
                default:
                    Logger.d(TAG, "wrong server: " + pref.getString("server_type", ""));
                    Logger.getLogger(mContext).makeToast(R.string.error_wrong_server);
                    break;
            }
            tempServerType = pref.getString("server_type", "");

        } catch (NullPointerException e) {
            e.printStackTrace();
            Logger.d(TAG, "init loginImpl on wrong time");
        }
    }
}

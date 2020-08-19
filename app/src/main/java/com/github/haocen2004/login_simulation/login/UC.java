package com.github.haocen2004.login_simulation.login;

import android.app.Activity;

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

public class UC implements Login {

    private Activity activity;
    private UCGameSdk sdk;
    private String sid;
    private boolean isLogin;

    public UC(Activity activity) {
        this.activity = activity;
        isLogin = false;
    }

    @Override
    public void login() {
        sdk = UCGameSdk.defaultSdk();
        ParamInfo gpi = new ParamInfo();

        gpi.setGameId(119474);

        gpi.setOrientation(UCOrientation.PORTRAIT);

        SDKParams sdkParams = new SDKParams();
        sdkParams.put(SDKParamKey.GAME_PARAMS, gpi);
        try {
            sdk.initSdk(activity, sdkParams);
            sdk.login(activity, null);
        } catch (AliLackActivityException e) {
            e.printStackTrace();
        } catch (AliNotInitException e) {
            e.printStackTrace();
        }

    }


    private SDKEventReceiver eventReceiver = new SDKEventReceiver() {

        @Subscribe(event = SDKEventKey.ON_LOGIN_SUCC)
        private void onLoginSucc(String sid2) {
            sid = sid2;
            isLogin = true;
        }


    };


    @Override
    public void logout() {
        try {
            sdk.logout(activity, null);
            sdk.exit(activity, null);
        } catch (AliLackActivityException e) {
            e.printStackTrace();
        } catch (AliNotInitException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getCombo_token() {
        return null;
    }

    @Override
    public String getCombo_id() {
        return null;
    }

    @Override
    public String getUid() {
        return null;
    }

    @Override
    public boolean isLogin() {
        return isLogin;
    }
}

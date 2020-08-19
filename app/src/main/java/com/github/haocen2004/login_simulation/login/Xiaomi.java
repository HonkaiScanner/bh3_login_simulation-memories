package com.github.haocen2004.login_simulation.login;

import android.app.Activity;

import com.github.haocen2004.login_simulation.util.Logger;
import com.xiaomi.gamecenter.sdk.MiCommplatform;
import com.xiaomi.gamecenter.sdk.MiErrorCode;
import com.xiaomi.gamecenter.sdk.OnExitListner;
import com.xiaomi.gamecenter.sdk.OnInitProcessListener;
import com.xiaomi.gamecenter.sdk.OnLoginProcessListener;
import com.xiaomi.gamecenter.sdk.entry.MiAccountInfo;
import com.xiaomi.gamecenter.sdk.entry.MiAppInfo;

import java.util.List;

public class Xiaomi implements Login {



    private Activity activity;
    private boolean isLogin;
    private String uid;
    private String session;

    public Xiaomi(Activity activity) {
        this.activity = activity;
    }
    @Override
    public void login() {
        MiAppInfo appInfo = new MiAppInfo();
        appInfo.setAppId("请申请获得");
        appInfo.setAppKey("请申请获得");
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
                new OnLoginProcessListener() {
                    @Override
                    public void finishLoginProcess(int code, MiAccountInfo arg1) {
                        switch (code) {
                            case MiErrorCode.MI_XIAOMI_PAYMENT_SUCCESS:// 登陆成功
                                //获取用户的登陆后的UID（即用户唯一标识）
                                uid = arg1.getUid();

                                //以下为获取session并校验流程，如果是网络游戏必须校验,(12小时过期)
                                //获取用户的登陆的Session（请参考5.3.3流程校验Session有效性）
                                session = arg1.getSessionId();
                                //请开发者完成将uid和session提交给开发者自己服务器进行session验证
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
                    }
                });
    }

    @Override
    public void logout() {
        MiCommplatform.getInstance().miAppExit( activity, new OnExitListner()
        {
            @Override
            public void onExit( int code )
            {
                if ( code == MiErrorCode.MI_XIAOMI_EXIT )
                {
                    android.os.Process.killProcess( android.os.Process.myPid() );
                }
            }
        } );
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
        return uid;
    }

    @Override
    public boolean isLogin() {
        return isLogin;
    }
}

package xyz.hellocraft.login_simulation.login;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;
import static xyz.hellocraft.login_simulation.data.Constant.MI_ADV_MODE;
import static xyz.hellocraft.login_simulation.data.Constant.MI_INIT;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Build;

import androidx.appcompat.app.AppCompatActivity;

import com.github.haocen2004.login_simulation.R;
import com.tencent.bugly.crashreport.CrashReport;
import com.xiaomi.gamecenter.sdk.MiCommplatform;
import com.xiaomi.gamecenter.sdk.MiErrorCode;
import com.xiaomi.gamecenter.sdk.OnInitProcessListener;
import com.xiaomi.gamecenter.sdk.entry.MiAppInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import xyz.hellocraft.login_simulation.data.RoleData;
import xyz.hellocraft.login_simulation.data.dialog.ButtonData;
import xyz.hellocraft.login_simulation.data.dialog.DialogData;
import xyz.hellocraft.login_simulation.data.dialog.DialogLiveData;
import xyz.hellocraft.login_simulation.utils.DialogHelper;
import xyz.hellocraft.login_simulation.utils.Logger;
import xyz.hellocraft.login_simulation.utils.Tools;

public class Xiaomi extends Application implements LoginImpl {


    private final AppCompatActivity activity;
    private final String TAG = "Mi Login";
    private final LoginCallback callback;
    private boolean isLogin;
    private String uid;
    private String username;
    private String session;
    private RoleData roleData;

    public Xiaomi(AppCompatActivity activity, LoginCallback loginCallback) {
        this.activity = activity;
        callback = loginCallback;
    }

    private void xiaomiLogin() {
        xiaomiLogin(true);
    }

    private void xiaomiLogin(boolean tempShowDialog) {

        if ((tempShowDialog && !Tools.getBoolean(activity, "last_mi_login_succ", false)) && !MI_ADV_MODE) {
            DialogData dialogData = new DialogData("小米服使用提示", "目前默认只支持小米账号登陆\n如有其他方式登录需求请绑定一个小米账号\n或在设置内启用 小米渠道测试方案\n每次登陆新账号时该提示都会出现");
            dialogData.setPositiveButtonData(new ButtonData("确认") {
                @Override
                public void callback(DialogHelper dialogHelper) {
                    super.callback(dialogHelper);
                    xiaomiLogin(false);
                }
            });
            DialogLiveData.getINSTANCE().addNewDialog(dialogData);
            return;
        }

        MiCommplatform.getInstance().onUserAgreed(activity);
        MiCommplatform.getInstance().miLogin(activity,
                (code, arg1) -> {
                    switch (code) {
                        case MiErrorCode.MI_XIAOMI_PAYMENT_SUCCESS:// 登陆成功
                            Logger.d(TAG, "Mi Login Success");
                            //获取用户的登陆后的UID（即用户唯一标识）
                            uid = arg1.getUid();
                            username = arg1.getNikename();
                            //以下为获取session并校验流程，如果是网络游戏必须校验,(12小时过期)
                            //获取用户的登陆的Session（请参考5.3.3流程校验Session有效性）
                            session = arg1.getSessionId();
                            Logger.addBlacklist(session);
                            //请开发者完成将uid和session提交给开发者自己服务器进行session验证
                            doBHLogin();
                            break;
                        // 登陆失败
                        case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_LOGIN_FAIL:
                        case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_CANCEL:
                        case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_ACTION_EXECUTED:
                        default:
                            Logger.d(TAG, "err:" + code);
                            callback.onLoginFailed();
                            Tools.saveBoolean(activity, "last_mi_login_succ", false);
                            // 登录失败
                            break;
                    }
                });

    }

    @Override
    public void login() {

        boolean needFallback = false;
        String fallbackReason = "";
        String serviceName = "com.xiaomi.gamecenter.sdk.service";
        if (MI_ADV_MODE) {
            try {
                PackageInfo packageInfo = activity.getPackageManager().getPackageInfo(serviceName, 0);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    if (packageInfo.getLongVersionCode() > 8700100) {
                        needFallback = true;
                        fallbackReason = "小米游戏服务版本过高";
                    }
                } else {
                    if (packageInfo.versionCode > 8700100) {
                        needFallback = true;
                        fallbackReason = "小米游戏服务版本过高";
                    }
                }
                ApplicationInfo applicationInfo = activity.getPackageManager().getApplicationInfo(serviceName, 0);
                if (!applicationInfo.enabled) {
                    needFallback = true;
                    fallbackReason = "小米游戏服务已被冻结";
                }
            } catch (Exception ignore) {
                needFallback = true;
                fallbackReason = "未安装小米游戏服务";
            }
            if (needFallback) {
                MI_ADV_MODE = false;
                fallbackReason = fallbackReason + "\n已自动关闭小米服测试方案\n如有需要请正确配置游戏服务后\n前往设置页面再次开启\n\n如果为提示版本号过高\n可点击下方按钮下载较低版本\n目前测试最后兼容版本为8.7.0";
                getDefaultSharedPreferences(activity).edit().putBoolean("mi_adv_mode", false).apply();
                DialogData dialogData = getDialogData(fallbackReason);
                DialogLiveData.getINSTANCE().addNewDialog(dialogData);
                return;
            }
        }
        if (MI_INIT) {
            xiaomiLogin();
            return;
        }
        MiAppInfo appInfo = new MiAppInfo();
        appInfo.setAppId("2882303761517502034");
        appInfo.setAppKey("5841750261034");

        MiCommplatform.Init(activity, appInfo, new OnInitProcessListener() {
            @Override
            public void finishInitProcess(List<String> loginMethod, int gameConfig) {
                Logger.i(TAG, "finishInitProcess: Init success");
//                MiCommplatform.getInstance().onMainActivityCreate(activity);
                MI_INIT = true;
                xiaomiLogin();
            }

            @Override
            public void onMiSplashEnd() {
                //小米闪屏⻚结束回调,小米闪屏可配,无闪屏也会返回此回调,游戏的闪屏应当在收到此回调之后
            }
        });


    }

    private DialogData getDialogData(String fallbackReason) {
        DialogData dialogData = new DialogData("小米测试方案关闭提示", fallbackReason);
        dialogData.setPositiveButtonData(new ButtonData("确认") {
            @Override
            public void callback(DialogHelper dialogHelper) {
                super.callback(dialogHelper);
                login();
            }
        });
        dialogData.setNeutralButtonData(new ButtonData("下载游戏服务") {
            @Override
            public void callback(DialogHelper dialogHelper) {
                super.callback(dialogHelper);
                Tools.openUrl("https://fga1.market.xiaomi.com/download/AppStore/0bb83c3e48a49488592cd525a5109fb09d7dd8f00/com.xiaomi.gamecenter.sdk.service.apk", activity);
            }
        });
        return dialogData;
    }

    public void doBHLogin() {
        String data_json = "{\"session\":\"" +
                session +
                "\",\"uid\":\"" +
                uid +
                "\"}";
        JSONObject feedback_json = null;
        try {
            feedback_json = new JSONObject(Tools.verifyAccount(activity, "11", data_json));
        } catch (JSONException ignore) {
        }
        try {


            if (feedback_json == null) {
                makeToast("Empty Return");
            } else if (feedback_json.getInt("retcode") != 0) {
                makeToast(feedback_json.getString("message"));
            } else {
                JSONObject data_json2 = feedback_json.getJSONObject("data");
                String combo_id = data_json2.getString("combo_id");
                String open_id = data_json2.getString("open_id");
                String combo_token = data_json2.getString("combo_token");
                String account_type = data_json2.getString("account_type");

                roleData = new RoleData(open_id, "", combo_id, combo_token, "11", account_type, "xiaomi", 5, callback);
                isLogin = true;
                makeToast(activity.getString(R.string.login_succeed));
                Tools.saveBoolean(activity, "last_mi_login_succ", true);
            }
        } catch (JSONException e) {
            CrashReport.postCatchedException(e);
            makeToast("parse ERROR");
        }

    }

    @Override
    public boolean logout() {
        MiCommplatform.getInstance().miAppExit(activity, code -> {
            if (code == MiErrorCode.MI_XIAOMI_EXIT) {
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
        return true;
    }

    @Override
    public RoleData getRole() {
        return roleData;
    }

    @Override
    public void setRole(RoleData roleData) {
        this.roleData = roleData;
        isLogin = true;
    }

    private void makeToast(String result) {
        Logger.getLogger(this).makeToast(result);
    }

    @Override
    public boolean isLogin() {
        return isLogin;
    }

    @Override
    public String getUsername() {
        return username;
    }


}

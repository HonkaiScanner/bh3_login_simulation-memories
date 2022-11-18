package com.github.haocen2004.login_simulation.login;

import static com.github.haocen2004.login_simulation.util.Constant.HUAWEI_INIT;
import static com.github.haocen2004.login_simulation.util.Logger.getLogger;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Looper;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.github.haocen2004.login_simulation.data.RoleData;
import com.github.haocen2004.login_simulation.util.Logger;
import com.github.haocen2004.login_simulation.util.Network;
import com.github.haocen2004.login_simulation.util.Tools;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.api.HuaweiMobileServicesUtil;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.jos.AppParams;
import com.huawei.hms.jos.JosApps;
import com.huawei.hms.jos.JosAppsClient;
import com.huawei.hms.jos.JosStatusCodes;
import com.huawei.hms.jos.games.Games;
import com.huawei.hms.jos.games.GamesStatusCodes;
import com.huawei.hms.jos.games.PlayersClient;
import com.huawei.hms.jos.games.player.Player;
import com.huawei.hms.support.account.AccountAuthManager;
import com.huawei.hms.support.account.request.AccountAuthParams;
import com.huawei.hms.support.account.request.AccountAuthParamsHelper;
import com.huawei.hms.support.account.result.AccountAuthResult;
import com.huawei.hms.support.account.result.AuthAccount;
import com.huawei.hms.utils.ResourceLoaderUtil;
import com.tencent.bugly.crashreport.CrashReport;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Objects;

public class Huawei implements LoginImpl {

    private static final String TAG = "Huawei Login";
    private String username;
    private final AppCompatActivity activity;
    private boolean isLogin;
    private RoleData roleData;
    private final Logger Log;
    private final LoginCallback callback;
    private JSONObject verifyJson;

    public Huawei(AppCompatActivity activity, LoginCallback loginCallback) {
        callback = loginCallback;
        this.activity = activity;
        Log = getLogger(activity);

        //isLogin = false;
    }

    public AccountAuthParams getHuaweiIdParams() {
        return new AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM_GAME).createParams();
    }

    private void doHuaweiLogin() {
        Task<AuthAccount> authAccountTask = AccountAuthManager.getService(activity, getHuaweiIdParams()).silentSignIn();
        authAccountTask.addOnSuccessListener(
                        authAccount -> {
                            Logger.d(TAG, "Authentication succeeded.");
                            Logger.d(TAG, "display:" + authAccount.getDisplayName());
//                            Logger.d(TAG, authAccount.getUid());
                            getGamePlayer();   // 调用游戏登录接口
                        })
                .addOnFailureListener(
                        e -> {
                            if (e instanceof ApiException) {
                                ApiException apiException = (ApiException) e;
                                Logger.d(TAG, "signIn failed:" + apiException.getStatusCode());
                                Logger.d(TAG, "start getSignInIntent");
                                // 在此处实现华为帐号显式授权
                                Intent intent = AccountAuthManager.getService(activity, getHuaweiIdParams()).getSignInIntent();
                                callback.launchActivityForResult(intent, activityResult -> {
                                    Intent data = activityResult.getData();
                                    if (null == activityResult.getData()) {
                                        Logger.d(TAG, "signIn intent is null");
                                        callback.onLoginFailed();
                                        return;
                                    }
                                    String jsonSignInResult = data.getStringExtra("HUAWEIID_SIGNIN_RESULT");
                                    if (TextUtils.isEmpty(jsonSignInResult)) {
                                        Logger.d(TAG, "SignIn result is empty");
                                        callback.onLoginFailed();
                                        return;
                                    }
                                    try {
                                        AccountAuthResult signInResult = new AccountAuthResult().fromJson(jsonSignInResult);
                                        if (0 == signInResult.getStatus().getStatusCode()) {
                                            Logger.d(TAG, "Sign in success.");
                                            Logger.d(TAG, "Sign in result: " + signInResult.toJson());
//                                                SignInCenter.get().updateAuthAccount(signInResult.getAccount());
                                            getGamePlayer();
                                        } else {
                                            Logger.d(TAG, "Sign in failed: " + signInResult.getStatus().getStatusCode());
                                            callback.onLoginFailed();
                                        }
                                    } catch (JSONException var7) {
                                        Logger.d(TAG, "Failed to convert json from signInResult.");
                                        callback.onLoginFailed();
                                    }
                                });
                            }
                        });
    }

    public void getGamePlayer() {
        // 调用getPlayersClient方法初始化
        PlayersClient client = Games.getPlayersClient(activity);
        // 执行游戏登录

        Task<Player> task = client.getGamePlayer(true);
        task.addOnSuccessListener(player -> {


            String accessToken = player.getAccessToken();
            Logger.addBlacklist(accessToken);
            username = player.getDisplayName();
            String body = null;
            try {
                Logger.addBlacklist(URLEncoder.encode(accessToken, "utf-8"));
                body = "extraBody=json%3D%7B%22appId%22%3A%2210624714%22%7D&method=client.hms.gs.getGameAuthSign&hmsApkVersionCode=60700322&accessToken=" + URLEncoder.encode(accessToken, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            String finalBody = body;
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    JSONObject huaweiJson = null;
//                    while (true) {
                    Logger.d(TAG, finalBody);
                    String huaweiFeedback = Network.sendPost("https://jgw-drcn.jos.dbankcloud.cn/gameservice/api/gbClientApi", finalBody);
//                    Logger.d(TAG, huaweiFeedback);
                    try {
                        huaweiJson = new JSONObject(huaweiFeedback);
                        if (!huaweiJson.has("ts")) {
                            Logger.d(TAG, "hw login failed, retry...");

                            return;
                        }
                        Logger.addBlacklist(huaweiJson.getString("gameAuthSign"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
//                    }
                    try {
                        verifyJson = new JSONObject();

                        verifyJson.put("ts", huaweiJson.get("ts"));
                        verifyJson.put("cpid", "890086000102026777");
                        verifyJson.put("playerId", huaweiJson.get("playerId"));
                        verifyJson.put("playerLevel", "1");
                        verifyJson.put("playerSSign", huaweiJson.get("gameAuthSign"));
                        verifyJson.put("time", 0);
                        verifyJson.put("is_teenager", false);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    JSONObject feedback_json = new JSONObject();
                    try {
                        feedback_json = new JSONObject(Objects.requireNonNull(Tools.verifyAccount(activity, "15", verifyJson.toString())));
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
                            String open_id = data_json2.getString("open_id");
                            String combo_token = data_json2.getString("combo_token");
                            Logger.addBlacklist(combo_token);
//                        String account_type = data_json2.getString("account_type");

                            roleData = new RoleData(open_id, "", combo_id, combo_token, "15", "2", "huawei", 10, callback);
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
            }.start();
        });
    }
//            Task<PlayerExtraInfo> task2 = client.getPlayerExtraInfo(player.getOpenId());
//            Logger.addBlacklist(player.getPlayerSign());
//            task2.addOnSuccessListener(playerExtraInfo -> {
//                try {
//
//                    Logger.d(TAG,playerExtraInfo.getPlayerId());
//                    Logger.d(TAG,playerExtraInfo.getOpenId());
//                    Logger.d(TAG,playerExtraInfo.getPlayerDuration()+"");
//                    Logger.d(TAG,playerExtraInfo.getIsAdult()+"");
//                    Logger.d(TAG,playerExtraInfo.getIsRealName()+"");
//
//
//                    verifyJson.put("time",playerExtraInfo.getPlayerDuration());
//                    verifyJson.put("is_teenager",!playerExtraInfo.getIsAdult());
//
//                    new Thread(){
//                        @Override
//                        public void run() {
//                            super.run();
//                            JSONObject feedback_json = new JSONObject();
//                            try {
//                                feedback_json = new JSONObject(Objects.requireNonNull(Tools.verifyAccount(activity, "15", verifyJson.toString())));
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                            try {
//                                if (feedback_json == null) {
//                                    makeToast("Empty Return");
//                                    callback.onLoginFailed();
//                                } else if (feedback_json.getInt("retcode") != 0) {
//                                    makeToast(feedback_json.getString("message"));
//                                    callback.onLoginFailed();
//                                } else {
//                                    JSONObject data_json2 = feedback_json.getJSONObject("data");
//                                    String combo_id = data_json2.getString("combo_id");
//                                    String open_id = data_json2.getString("open_id");
//                                    String combo_token = data_json2.getString("combo_token");
//                                    Logger.addBlacklist(combo_token);
////                        String account_type = data_json2.getString("account_type");
//
//                                    roleData = new RoleData(activity, open_id, "", combo_id, combo_token, "15", "2", "huawei", 0, callback);
//                                    isLogin = true;
////                        makeToast(activity.getString(R.string.login_succeed));
//                                }
////                doBHLogin();
//                            } catch (JSONException e) {
//                                CrashReport.postCatchedException(e);
//                                makeToast("parse ERROR");
//                                callback.onLoginFailed();
//                            }
//
//
//                        }
//                    }.start();
//
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }).addOnFailureListener(e -> {
//                if (e instanceof ApiException) {
//                    String result = "rtnCode:" + ((ApiException) e).getStatusCode();
//                    Logger.d(TAG,result);
//                    callback.onLoginFailed();
//                } else {
//                    Logger.d(TAG, e.toString());
//                    e.printStackTrace();
//                    callback.onLoginFailed();
//                }
//            });
//
//        }).addOnFailureListener(e -> {
//            if (e instanceof ApiException) {
//                String result = "rtnCode:" + ((ApiException) e).getStatusCode();
//                // 获取玩家信息失败，不允许进入游戏，并根据错误码处理
//                if (7400 == ((ApiException) e).getStatusCode()||7018 == ((ApiException) e).getStatusCode()) {
//                    // 7400表示用户未签署联运协议，需要继续调用init接口
//                    // 7018表示初始化失败，需要继续调用init接口
//                    login();
//                } else {
//                    Logger.d(TAG,result);
//                    callback.onLoginFailed();
//                }
//            } else {
//                Logger.d(TAG,e.toString());
//                e.printStackTrace();
//                callback.onLoginFailed();
//            }
//         });
//    }

    @Override
    public void logout() {
        isLogin = false;


    }

    @Override
    public void login() {
        if (HUAWEI_INIT) {
//            gameSdk = BSGameSdk.getInstance();
//            preferences = activity.getSharedPreferences("bili_user", Context.MODE_PRIVATE);
            doHuaweiLogin();
        } else {
            HuaweiMobileServicesUtil.setApplication(activity.getApplication());

            AccountAuthParams params = AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM_GAME;
            JosAppsClient appsClient = JosApps.getJosAppsClient(activity);
            Task<Void> initTask;
            ResourceLoaderUtil.setmContext(activity);  // 设置防沉迷提示语的Context，此行必须添加
            AppParams appParams = new AppParams(params, () -> {
                Logger.d(TAG, "防沉迷退出");
                System.exit(0);
                // 该回调会在如下两种情况下返回:
                // 1.未成年人实名帐号在白天登录游戏，华为会弹框提示玩家不允许游戏，玩家点击“确定”，华为返回回调
                // 2.未成年实名帐号在国家允许的时间登录游戏，到晚上9点，华为会弹框提示玩家已到时间，玩家点击“知道了”，华为返回回调
                // 您可在此处实现游戏防沉迷功能，如保存游戏、调用帐号退出接口或直接游戏进程退出(如System.exit(0))
            });
            // 当您的游戏需要实现智慧屏会员功能时需要实现该回调，游戏过程中会员到期时触发。
            appParams.setExitCallback(retCode -> {
                Logger.d(TAG, "防沉迷退出,code: " + retCode);
                System.exit(0);
                // 您需要在此方法中实现退出游戏的功能，例如保存玩家进度、调用华为帐号退出接口等。
            });
            initTask = appsClient.init(appParams);
            initTask.addOnSuccessListener(aVoid -> {
                Logger.d(TAG, "init success");
                HUAWEI_INIT = true;
                // 游戏初始化成功后需要调用一次浮标显示接口
                Games.getBuoyClient(activity).showFloatWindow();
                // 必须在init成功后，才可以实现登录功能
                // signIn();
                doHuaweiLogin();
            }).addOnFailureListener(
                    e -> {
                        if (e instanceof ApiException) {
                            ApiException apiException = (ApiException) e;
                            int statusCode = apiException.getStatusCode();
                            if (statusCode == JosStatusCodes.JOS_PRIVACY_PROTOCOL_REJECTED) { // 错误码为7401时表示用户未同意华为联运隐私协议
                                Logger.i(TAG, "has reject the protocol");
                                callback.onLoginFailed();
                                // 此处您需禁止玩家进入游戏
                            } else if (statusCode == GamesStatusCodes.GAME_STATE_NETWORK_ERROR) { // 错误码7002表示网络异常
                                Logger.i(TAG, "Network error");
                                Log.makeToast("网络连接失败 请检查网络后重试");
                                callback.onLoginFailed();
                                // 此处您可提示玩家检查网络，请不要重复调用init接口，否则断网情况下可能会造成手机高耗电。
                            } else if (statusCode == 907135003) {
                                // 907135003表示玩家取消HMS Core升级或组件升级
                                Logger.d(TAG, "init statusCode=" + statusCode);
                                login();
                            } else {
                                Logger.d(TAG, "API error: " + statusCode);
                                callback.onLoginFailed();
                                // 在此处实现其他错误码的处理
                            }
                        }
                    });
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


}


package com.github.haocen2004.login_simulation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.bsgamesdk.android.BSGameSdk;
import com.bsgamesdk.android.callbacklistener.BSGameSdkError;
import com.bsgamesdk.android.callbacklistener.CallbackListener;
import com.bsgamesdk.android.callbacklistener.ExitCallbackListener;
import com.bsgamesdk.android.callbacklistener.InitCallbackListener;
import com.bsgamesdk.android.utils.LogUtils;
import com.github.haocen2004.login_simulation.util.Constant;
import com.github.haocen2004.login_simulation.util.Network;
import com.github.haocen2004.login_simulation.util.Tools;
import com.google.zxing.activity.CaptureActivity;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Activity mContext;
    private SharedPreferences preferences;
    public BSGameSdk gameSdk;
    public String app_key = "0ebc517adb1b62c6b408df153331f9aa";
    public static final int OK = 1;
    public String combo_id,combo_token,ticket,uid,access_token,username,result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = MainActivity.this;
        String merchant_id = "590";
        String app_id = "180";
        String server_id = "378";
        String app_key = "dbf8f1b4496f430b8a3c0f436a35b931";
        // 获得BSGameSdk实例

        BSGameSdk.initialize(true, mContext, merchant_id, app_id,
                server_id, app_key, new InitCallbackListener() {
                    @Override
                    public void onSuccess() {
                        makeToast("initialize onSuccess");
                    }

                    @Override
                    public void onFailed() {
                        makeToast("initialize onFailed");
                    }
                }, new ExitCallbackListener() {
                    @Override
                    public void onExit() {
                        finish();
                        System.exit(0);
                    }
                });


        setContentView(R.layout.activity_main);
        initView();

        gameSdk = BSGameSdk.getInstance();
        preferences = mContext.getSharedPreferences("user",
                Context.MODE_PRIVATE);
    }

    private void initView() {
        Button btnQrCode = findViewById(R.id.btn_scan);
        btnQrCode.setOnClickListener(this);
        findViewById(R.id.btnBHlogin).setOnClickListener(this);
        findViewById(R.id.btn_logout).setOnClickListener(this);
        showNormalDialog();
    }

    private void showNormalDialog(){

        final AlertDialog.Builder normalDialog = new AlertDialog.Builder(MainActivity.this);
        normalDialog.setTitle("注意");
        normalDialog.setMessage("本软件仅供学习使用\n请在下载后24小时内删除\n依赖库：\nzxing\nBSGameSDK\nmihoyoSDK\n部分代码来源于反编译崩坏3app\n目前适配: B服 v4.0.0\nBy Hao_cen\nBiliBili UID:269140934");
        normalDialog.setPositiveButton("我已知晓",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        makeToast("感谢使用\nb站关注一下可否？");

                    }
                });
        normalDialog.setNegativeButton("前往B站关注",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Uri uri = Uri.parse("https://space.bilibili.com/269140934");
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    }
                });

        normalDialog.show();
    }

    private void bhLogin() {

        gameSdk.login(new CallbackListener() {

            @Override
            public void onSuccess(Bundle arg0) {
                // 此处为操作成功时执行，返回值通过Bundle传回
                uid = arg0.getString("uid");
                username = arg0.getString("username");
                access_token = arg0.getString("access_token");
                StringBuilder data_json = new StringBuilder();
                data_json.append("{\"uid\":")
                        .append(uid)
                        .append(",\"access_key\":\"")
                        .append(access_token)
                        .append("\"}");

                Map login_map = new HashMap<String,Object>();
                login_map.put("device", Tools.getDeviceID(mContext));
                login_map.put("app_id","1");
                login_map.put("channel_id","14");
                login_map.put("data",data_json.toString());
                String sign = Tools.signNew(login_map, app_key);
                JSONObject login_json = new JSONObject();
                ArrayList<String> arrayList = new ArrayList(login_map.keySet());
                Collections.sort(arrayList);
                try {
                    for (String str : arrayList) {
                        login_json.put(str, login_map.get(str));
                    }
                    login_json.put("sign",sign);

                    String feedback = Network.sendPost("https://api-sdk.mihoyo.com/bh3_cn/combo/granter/login/v2/login",login_json.toString());

                    JSONObject feedback_json = new JSONObject(feedback);
                    if (feedback_json.getInt("retcode") == 0){

                        JSONObject data_json2 = feedback_json.getJSONObject("data");
                        combo_id = data_json2.getString("combo_id");
                        combo_token = data_json2.getString("combo_token");

                        makeToast("登录成功");
                        ((CheckBox) findViewById(R.id.checkBox_BH_login)).setChecked(true);
                    } else {
                        makeToast("登录失败");
                    }

                }catch (Exception ignore) {}
            }

            @Override
            public void onFailed(BSGameSdkError arg0) {
                // 此处为操作失败时执行，返回值为BSGameSdkError类型变量，其中包含ErrorCode和ErrorMessage
                LogUtils.d("onFailed\nErrorCode : "
                        + arg0.getErrorCode() + "\nErrorMessage : "
                        + arg0.getErrorMessage());
                makeToast("onFailed\nErrorCode : "
                        + arg0.getErrorCode() + "\nErrorMessage : "
                        + arg0.getErrorMessage());
            }

            @Override
            public void onError(BSGameSdkError arg0) {
                // 此处为操作异常时执行，返回值为BSGameSdkError类型变量，其中包含ErrorCode和ErrorMessage
                LogUtils.d("onError\nErrorCode : "
                        + arg0.getErrorCode() + "\nErrorMessage : "
                        + arg0.getErrorMessage());
                makeToast("onError\nErrorCode : " + arg0.getErrorCode()
                        + "\nErrorMessage : " + arg0.getErrorMessage());
            }
        });



    }
    private void logout() {
        gameSdk.logout(new CallbackListener() {

            @Override
            public void onSuccess(Bundle arg0) {
                // 此处为操作成功时执行，返回值通过Bundle传回
                LogUtils.d("onSuccess");
                try {
                    preferences.edit().clear().apply();
                } catch ( NullPointerException e){
                    e.printStackTrace();
                }
                makeToast("账号已退出");

            }

            @Override
            public void onFailed(BSGameSdkError arg0) {
                // 此处为操作失败时执行，返回值为BSGameSdkError类型变量，其中包含ErrorCode和ErrorMessage
                LogUtils.d("onFailed\nErrorCode : "
                        + arg0.getErrorCode() + "\nErrorMessage : "
                        + arg0.getErrorMessage());
                makeToast("onFailed\nErrorCode : "
                        + arg0.getErrorCode() + "\nErrorMessage : "
                        + arg0.getErrorMessage());
            }

            @Override
            public void onError(BSGameSdkError arg0) {
                // 此处为操作异常时执行，返回值为BSGameSdkError类型变量，其中包含ErrorCode和ErrorMessage
                LogUtils.d("onError\nErrorCode : "
                        + arg0.getErrorCode() + "\nErrorMessage : "
                        + arg0.getErrorMessage());
                makeToast("onError\nErrorCode : " + arg0.getErrorCode()
                        + "\nErrorMessage : " + arg0.getErrorMessage());
            }
        });
    }
    // 开始扫码
    private void startQrCode() {
        // 申请相机权限
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // 申请权限
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission
                    .CAMERA)) {
                Toast.makeText(this, "请至权限中心打开本应用的相机访问权限", Toast.LENGTH_SHORT).show();
            }
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, Constant.REQ_PERM_CAMERA);
            return;
        }
        // 申请文件读写权限（部分朋友遇到相册选图需要读写权限的情况，这里一并写一下）
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 申请权限
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission
                    .WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "请至权限中心打开本应用的文件读写权限", Toast.LENGTH_SHORT).show();
            }
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Constant.REQ_PERM_EXTERNAL_STORAGE);
            return;
        }
        // 二维码扫码
        Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
        startActivityForResult(intent, Constant.REQ_QR_CODE);
    }

    /**
     * 生成二维码
     */

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_scan:
                startQrCode();
                break;
            case R.id.btnBHlogin:
                bhLogin();
                break;
            case R.id.btn_logout:
                logout();
                break;
            default:
                break;
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //扫描结果回调
        if (requestCode == Constant.REQ_QR_CODE && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            result = bundle.getString(Constant.INTENT_EXTRA_KEY_QR_SCAN);
            //将扫描出的信息显示出来
            // https://user.mihoyo.com/qr_code_in_game.html?app_id=1&app_name=崩坏3&bbs=false&biz_key=bh3_cn&expire=1594571964&ticket=5f04a53c5957682502e05b89
            new Thread(networkTask).start();
        }
    }


    Runnable networkTask = new Runnable() {

        @Override
        public void run() {
            scanQRCode(result);
        }

    };


    private void scanQRCode(String scanResult){
        try {
            if (scanResult.contains("qr_code_in_game.html")) {
                String[] split = scanResult.split("\\?");
                String[] parma = split[1].split("&");
                for (String key: parma){
                    if (key.startsWith("ticket")) {
                        ticket = key.split("=")[1];
                    }
                }

                Map qr_check_map = new HashMap<String,Object>();
                qr_check_map.put("device", Tools.getDeviceID(mContext));
                qr_check_map.put("app_id","1");
                qr_check_map.put("ts", System.currentTimeMillis());
                qr_check_map.put("ticket", ticket);
                String sign = Tools.signNew(qr_check_map, app_key);
                JSONObject qr_check_json = new JSONObject();
                ArrayList<String> arrayList = new ArrayList(qr_check_map.keySet());
                Collections.sort(arrayList);
                try {
                    for (String str : arrayList) {
                        qr_check_json.put(str, qr_check_map.get(str));
                    }
                    qr_check_json.put("sign",sign);

                    System.out.println(qr_check_json.toString());

                    String feedback = Network.sendPost("https://api-sdk.mihoyo.com/bh3_cn/combo/panda/qrcode/scan",qr_check_json.toString());

                    System.out.println(feedback);

                    JSONObject feedback_json = new JSONObject(feedback);
                    if (feedback_json.getInt("retcode") == 0){

                        JSONObject raw_json = new JSONObject();
                        raw_json.put("heartbeat",false)
                                .put("open_id", uid)
                                .put("device_id", Tools.getDeviceID(mContext))
                                .put("app_id",1)
                                .put("channel_id",14)
                                .put("combo_token",combo_token)
                                .put("asterisk_name", username)
                                .put("combo_id",combo_id);
                        JSONObject payload_json = new JSONObject();
                        JSONObject ext_json = new JSONObject();
                        JSONObject data_json = new JSONObject();
                        JSONObject dispatch_json = new JSONObject();
                        JSONObject ext2_json = new JSONObject();
                        JSONObject gateway_json = new JSONObject(); //Same with game server
                        JSONObject server_ext_json = new JSONObject();

                        server_ext_json.put("cdkey_url","https://api-takumi.mihoyo.com/common/")
                                .put("is_official","1");

                        gateway_json.put("ip","106.14.219.183")
                                .put("port","15100");

                        ext2_json.put("disable_msad","1")
                                .put("ex_res_server_url","bundle.bh3.com/tmp/Original")
                                .put("ex_res_use_http","0")
                                .put("forbid_recharge","0")
                                .put("is_checksum_off","0")
                                .put("mtp_debug_switch","0")
                                .put("mtp_level","1")
                                .put("res_use_asset_boundle","1")
                                .put("show_version_text","0")
                                .put("update_streaming_asb","1");

                        dispatch_json.put("account_url","https://gameapi.account.mihoyo.com")
                                .put("account_url_backup", "http://webapi.account.mihoyo.com")
                                .put("asset_boundle_url","https://bundle.bh3.com/asset_bundle/bb01/1.0")
                                .put("ex_resource_url","bundle.bh3.com/tmp/Original")
                                .put("ext",ext2_json)
                                .put("gameserver",gateway_json)
                                .put("gateway",gateway_json)
                                .put("oaserver_url","http://139.196.248.220:1080")
                                .put("region_name","bb01")
                                .put("retcode","0")
                                .put("server_ext", server_ext_json);

                        data_json.put("accountType","2")
                                .put("accountID",uid)
                                .put("accountToken",combo_token)
                                .put("dispatch",dispatch_json);

                        ext_json.put("data",data_json);

                        payload_json.put("raw",raw_json.toString())
                                .put("proto","Combo")
                                .put("ext",ext_json.toString());

                        JSONObject confirm_json = new JSONObject();
                        confirm_json.put("device",Tools.getDeviceID(mContext))
                                .put("app_id","1")
                                .put("ts", System.currentTimeMillis())
                                .put("ticket",ticket)
                                .put("payload",payload_json);

                        qr_check_map.put("payload",payload_json);
                        String sign2 = Tools.signNew(qr_check_map, app_key);
                        confirm_json.put("sign",sign2);

                        System.out.println(confirm_json.toString());
                        String feedback2 = Network.sendPost("https://api-sdk.mihoyo.com/bh3_cn/combo/panda/qrcode/confirm ",confirm_json.toString());
                        System.out.println(feedback2);
                        JSONObject feedback_json2 = new JSONObject(feedback2);
                        if (feedback_json2.getInt("retcode") == 0){
                            makeToast("登录成功！");
                        }else {
                            makeToast("扫码登录失败2");
                        }


                    } else {
                        makeToast("扫码登录失败1");
                    }

                }catch (Exception ignore) {}

            } else {
                makeToast("请扫描正确的二维码");
            }
        }catch (Exception e){
            e.printStackTrace();
            makeToast("请扫描正确的二维码");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constant.REQ_PERM_CAMERA:
                // 摄像头权限申请
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 获得授权
                    startQrCode();
                } else {
                    // 被禁止授权
                    Toast.makeText(MainActivity.this, "请至权限中心打开本应用的相机访问权限", Toast.LENGTH_LONG).show();
                }
                break;
            case Constant.REQ_PERM_EXTERNAL_STORAGE:
                // 文件读写权限申请
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 获得授权
                    startQrCode();
                } else {
                    // 被禁止授权
                    Toast.makeText(MainActivity.this, "请至权限中心打开本应用的文件读写权限", Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }
    }

    private void makeToast(String result) {
        Message msg = new Message();
        msg.what = MainActivity.OK;
        msg.obj = result;
        mHandler.sendMessage(msg);
    }
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (msg.what == MainActivity.OK) {
                Toast.makeText(mContext, (String) msg.obj, Toast.LENGTH_SHORT)
                        .show();
            }
        }
    };



}

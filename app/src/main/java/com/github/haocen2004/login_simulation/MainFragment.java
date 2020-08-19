package com.github.haocen2004.login_simulation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.github.haocen2004.login_simulation.login.Bilibili;
import com.github.haocen2004.login_simulation.login.Login;
import com.github.haocen2004.login_simulation.util.QRScanner;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.zxing.activity.CaptureActivity;
import com.google.zxing.util.Constant;

import static android.app.Activity.RESULT_OK;
import static android.preference.PreferenceManager.getDefaultSharedPreferences;


public class MainFragment extends Fragment implements View.OnClickListener {

    private String combo_id;
    private String combo_token;
    private String uid;
    private String channel_id;
    private Login login;
    private Activity activity;
    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((CollapsingToolbarLayout) requireActivity().findViewById(R.id.collapsingToolbarLayout))
                .setTitle(getString(R.string.page_main));
        activity = getActivity();
        context = getContext();

        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().findViewById(R.id.btn_login).setOnClickListener(this);
        requireActivity().findViewById(R.id.btn_scan).setOnClickListener(this);
        requireActivity().findViewById(R.id.btn_logout).setOnClickListener(this);
        String server_type = null;
        switch(getDefaultSharedPreferences(context).getString("server_type","")) {
            case "Official":
                server_type = getString(R.string.types_official);
                break;
            case "Bilibili":
                server_type = getString(R.string.types_bilibili);
            default:
                break;
        }
        ((TextView) requireActivity().findViewById(R.id.text_select_server)).setText(getString(R.string.types_prefix)+server_type);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.REQ_QR_CODE && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                String result = bundle.getString(com.github.haocen2004.login_simulation.util.Constant.INTENT_EXTRA_KEY_QR_SCAN);
                if (!TextUtils.isEmpty(result)) {
                    QRScanner qrScanner = new QRScanner(activity, uid, combo_id, combo_token, "1", channel_id, "106.14.219.183", "http://139.196.248.220:1080");
                    qrScanner.parseUrl(result);
                    qrScanner.getScanRequest();
                } else {
                    makeToast("扫码结果异常");
                }
            }
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case com.github.haocen2004.login_simulation.util.Constant.REQ_PERM_CAMERA:
            case com.github.haocen2004.login_simulation.util.Constant.REQ_PERM_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startQrCode();
                } else {
                    Toast.makeText(context, R.string.request_permission_failed, Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    private void startQrCode() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // 申请权限
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity   , Manifest.permission
                    .CAMERA)) {
                Toast.makeText(context, R.string.request_permission_failed, Toast.LENGTH_SHORT).show();
            }
            ActivityCompat.requestPermissions(activity , new String[]{Manifest.permission.CAMERA}, com.github.haocen2004.login_simulation.util.Constant.REQ_PERM_CAMERA);
            return;
        }
        Intent intent = new Intent(context, CaptureActivity.class);
        startActivityForResult(intent, com.github.haocen2004.login_simulation.util.Constant.REQ_QR_CODE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_scan:
                try {
                    if (login.isLogin()) {

                        combo_token = login.getCombo_token();
                        combo_id = login.getCombo_id();
                        uid = login.getUid();

                        startQrCode();
                    } else {
                        makeToast("请先登录");
                    }
                }catch (NullPointerException e){
                    e.printStackTrace();
                    makeToast("请先登录");
                }
                break;
            case R.id.btn_login:
                switch(getDefaultSharedPreferences(context).getString("server_type","")) {
                    case "Official":
                        showInfoDialog("Warning","官服暂不适配\n请使用密码登录","我已知晓");
//                        login = new Official(activity);
//                        channel_id = "1";
                        break;
                    case "Bilibili":
                        login = new Bilibili(activity);
                        channel_id = "14";
                        break;
                    default:
                        makeToast("服务器错误");
                        break;
                }
                login.login();
                break;
            case R.id.btn_logout:
                login.logout();
            default:
                break;
        }

    }

    @SuppressLint("ShowToast")
    private void makeToast(String result) {
        try {
            Toast.makeText(context, result, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Looper.prepare();
            Toast.makeText(context, result, Toast.LENGTH_LONG).show();
            Looper.loop();
        }
    }

    private void showInfoDialog(String title,String msg,String btn){

        final AlertDialog.Builder normalDialog = new AlertDialog.Builder(context);
        normalDialog.setTitle(title);
        normalDialog.setMessage(msg);
        normalDialog.setPositiveButton(btn,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        normalDialog.show();
    }


}
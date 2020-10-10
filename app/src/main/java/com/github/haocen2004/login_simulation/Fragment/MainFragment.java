package com.github.haocen2004.login_simulation.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.github.haocen2004.login_simulation.Proxy.Proxy;
import com.github.haocen2004.login_simulation.R;
import com.github.haocen2004.login_simulation.login.Bilibili;
import com.github.haocen2004.login_simulation.login.LoginImpl;
import com.github.haocen2004.login_simulation.login.Official;
import com.github.haocen2004.login_simulation.login.UC;
import com.github.haocen2004.login_simulation.util.QRScanner;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.zxing.activity.CaptureActivity;
import com.google.zxing.util.Constant;

import java.util.Objects;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static androidx.appcompat.app.AppCompatActivity.RESULT_OK;


public class MainFragment extends Fragment implements View.OnClickListener {

    private LoginImpl loginImpl;
    private AppCompatActivity activity;
    private Context context;
    private Proxy proxy;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((CollapsingToolbarLayout) requireActivity().findViewById(R.id.collapsingToolbarLayout))
                .setTitle(getString(R.string.page_main));
        activity = (AppCompatActivity) getActivity();
        context = getContext();
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().findViewById(R.id.btn_login).setOnClickListener(this);
        requireActivity().findViewById(R.id.btn_scan).setOnClickListener(this);
        requireActivity().findViewById(R.id.btn_logout).setOnClickListener(this);
        if (requireActivity().getPackageName().contains("dev")) {
            requireActivity().findViewById(R.id.button_debug).setOnClickListener(this);
            requireActivity().findViewById(R.id.button_debug).setVisibility(View.VISIBLE);
        }
        String server_type;
        switch (Objects.requireNonNull(getDefaultSharedPreferences(context).getString("server_type", ""))) {
            case "Official":
                server_type = getString(R.string.types_official);
                break;
            case "Bilibili":
                server_type = getString(R.string.types_bilibili);
                break;
//            case "Xiaomi":
//                server_type = getString(R.string.types_xiaomi);
//                break;
            case "UC":
                server_type = getString(R.string.types_uc);
                break;
            default:
                server_type = "DEBUG -- SERVER ERROR";
        }
        ((TextView) requireActivity().findViewById(R.id.text_select_server)).setText(getString(R.string.types_prefix) + server_type);
        ((TextView) requireActivity().findViewById(R.id.text_auto_confirm)).setText(getString(R.string.confirm_prefix) + (getDefaultSharedPreferences(context).getBoolean("auto_confirm", false) ? getString(R.string.boolean_true) : getString(R.string.boolean_false)));

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.REQ_QR_CODE && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                String result = bundle.getString(com.github.haocen2004.login_simulation.util.Constant.INTENT_EXTRA_KEY_QR_SCAN);
                if (result != null) {
                    QRScanner qrScanner = new QRScanner((AppCompatActivity) getActivity(), loginImpl.getRole());
                    qrScanner.parseUrl(result);
                    qrScanner.getScanRequest();
                } else {
                    makeToast(getString(R.string.error_scan));
                }
            }
        }
        if (resultCode == RESULT_OK && requestCode == 12580) {
            proxy.prepareNetBare();
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
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission
                    .CAMERA)) {
                Toast.makeText(context, R.string.request_permission_failed, Toast.LENGTH_SHORT).show();
            }
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, com.github.haocen2004.login_simulation.util.Constant.REQ_PERM_CAMERA);
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
                    if (loginImpl.isLogin()) {
                        if (loginImpl.getRole().is_setup()) {
                            startQrCode();
                        } else {
                            makeToast(getString(R.string.error_oa_process));
                        }
                    } else {
                        makeToast(getString(R.string.error_not_login));
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    makeToast(getString(R.string.error_not_login));
                }
                break;
            case R.id.btn_login:
                checkPermissions();
                try {
                    if (loginImpl.isLogin()) {
                        makeToast(getString(R.string.has_login));
                        return;
                    }
                } catch (Exception ignore) {
                }
                switch (Objects.requireNonNull(getDefaultSharedPreferences(context).getString("server_type", ""))) {
                    case "Official":
                        loginImpl = new Official(activity);
                        break;
//                    case "Xiaomi":
//                        loginImpl = new Xiaomi(activity);
//                        //11
//                        break;
                    case "Bilibili":
                        loginImpl = new Bilibili(activity);
                        //14
                        break;
                    case "UC":
                        loginImpl = new UC(activity);
                        //20
                        break;

                    default:
                        makeToast(getString(R.string.error_wrong_server));
                        break;
                }
                loginImpl.login();
                break;
            case R.id.btn_logout:
                try {
                    if ("Official".equals(getDefaultSharedPreferences(context).getString("server_type", ""))) {
                        activity.getSharedPreferences("official_user", Context.MODE_PRIVATE).edit().clear().apply();
                        makeToast(getString(R.string.cache_delete));
                    }
                    if (loginImpl.isLogin()) {
                        loginImpl.logout();
                    }
                } catch (Exception e) {
                    makeToast(getString(R.string.error_not_login));
                }
                break;
            case R.id.button_debug:
                proxy = new Proxy(activity);
                break;
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

    private void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 申请权限
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CAMERA)) {
                Toast.makeText(getContext(), R.string.request_permission_failed, Toast.LENGTH_SHORT).show();
            }
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(getContext(), R.string.request_permission_failed, Toast.LENGTH_SHORT).show();
            }
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, com.github.haocen2004.login_simulation.util.Constant.REQ_PERM_CAMERA);
        }
    }
}
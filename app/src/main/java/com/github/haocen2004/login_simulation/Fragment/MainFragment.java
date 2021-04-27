package com.github.haocen2004.login_simulation.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.github.haocen2004.login_simulation.R;
import com.github.haocen2004.login_simulation.databinding.FragmentMainBinding;
import com.github.haocen2004.login_simulation.login.Bilibili;
import com.github.haocen2004.login_simulation.login.LoginCallback;
import com.github.haocen2004.login_simulation.login.LoginImpl;
import com.github.haocen2004.login_simulation.login.Official;
import com.github.haocen2004.login_simulation.login.Oppo;
import com.github.haocen2004.login_simulation.login.UC;
import com.github.haocen2004.login_simulation.login.Vivo;
import com.github.haocen2004.login_simulation.util.FabScanner;
import com.github.haocen2004.login_simulation.util.Logger;
import com.github.haocen2004.login_simulation.util.QRScanner;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.activity.CaptureActivity;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.decoding.RGBLuminanceSource;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.util.BitmapUtil;
import com.google.zxing.util.Constant;

import java.util.Hashtable;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;
import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.github.haocen2004.login_simulation.util.Constant.OFFICIAL_TYPE;
import static com.github.haocen2004.login_simulation.util.Constant.REQ_CODE_SCAN_GALLERY;
import static com.github.haocen2004.login_simulation.util.Constant.REQ_PERM_CAMERA;
import static com.github.haocen2004.login_simulation.util.Constant.REQ_PERM_EXTERNAL_STORAGE;
import static com.github.haocen2004.login_simulation.util.Constant.REQ_PERM_RECORD;
import static com.github.haocen2004.login_simulation.util.Constant.REQ_PERM_WINDOW;
import static com.github.haocen2004.login_simulation.util.Constant.REQ_QR_CODE;
import static com.github.haocen2004.login_simulation.util.Tools.changeToWDJ;

public class MainFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener, MaterialButtonToggleGroup.OnButtonCheckedListener, LoginCallback {

    private final String TAG = "MainFragment";
    private LoginImpl loginImpl;
    private AppCompatActivity activity;
    private Context context;
    private Boolean isOfficial = false;
    private SharedPreferences pref;
    private FragmentMainBinding binding;
    private Logger Log;
    private FabScanner fabScanner;
    private Boolean loginProgress = false;
    private int currSlot = 999;
    private int currType = 999;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity = (AppCompatActivity) getActivity();
        context = getContext();
        fabScanner = new FabScanner(this);
        pref = getDefaultSharedPreferences(context);
        Log = Logger.getLogger(getContext());
        binding = FragmentMainBinding.inflate(inflater, container, false);
        setRetainInstance(true);
        Logger.setView(binding.getRoot());
        return binding.getRoot();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        binding.btnLogin.setOnClickListener(this);
        binding.btnScan.setOnClickListener(this);
        binding.btnScan.setOnLongClickListener(this);
//        binding.btnLogout.setOnClickListener(this);
        binding.cardViewMain.cardView2.setOnClickListener(this);
        binding.cardViewMain.cardView2.setOnLongClickListener(this);
        binding.officialSlotSelect.addOnButtonCheckedListener(this);
        binding.officialTypeSel.addOnButtonCheckedListener(this);
        binding.tokenCheckBox.setOnCheckedChangeListener((compoundButton, b) -> pref.edit().putBoolean("use_token", b).apply());
        binding.checkBoxWDJ.setOnCheckedChangeListener((compoundButton, b) -> {
            SharedPreferences ucSharedPref = activity.getSharedPreferences("cn.uc.gamesdk.pref", 0);
//            activity.getSharedPreferences("cn.uc.gamesdk.pref.usr_simple_cache",0).edit().clear().apply();
            pref.edit().putBoolean("use_wdj", b).apply();
            if (b) {
                changeToWDJ(activity);
            } else {
                ucSharedPref.edit().clear().apply();
            }
        });
        binding.btnSelpic.setOnClickListener(view1 -> {
            try {
                if (loginImpl.isLogin()) {
                    Intent innerIntent = new Intent(Intent.ACTION_GET_CONTENT); //"android.intent.action.GET_CONTENT"
                    innerIntent.setType("image/*");
                    startActivityForResult(innerIntent, REQ_CODE_SCAN_GALLERY);
                } else {
                    makeToast(getString(R.string.error_not_login));
                }
            } catch (Exception e) {
                makeToast(getString(R.string.error_not_login));
            }
        });
        String server_type;
        binding.officialSlotSelect.setVisibility(View.GONE);
        binding.tokenCheckBox.setVisibility(View.GONE);
        binding.officialTypeSel.setVisibility(View.GONE);
        binding.checkBoxWDJ.setVisibility(View.GONE);
        switch (Objects.requireNonNull(pref.getString("server_type", ""))) {
            case "Official":
                server_type = getString(R.string.types_official);
                binding.officialSlotSelect.setVisibility(View.VISIBLE);
                binding.tokenCheckBox.setVisibility(View.VISIBLE);
                binding.officialTypeSel.setVisibility(View.VISIBLE);
                switch (pref.getInt("official_slot", 1)) {
                    case 1:
                        binding.officialSlotSelect.check(binding.slot1.getId());
                        break;
                    case 2:
                        binding.officialSlotSelect.check(binding.slot2.getId());
                        break;
                    case 3:
                        binding.officialSlotSelect.check(binding.slot3.getId());
                        break;
                }
                switch (pref.getInt("official_type", 0)) {
                    case 1:
                        binding.officialTypeSel.check(binding.radioPc.getId());
                        break;
                    case 2:
                        binding.officialTypeSel.check(binding.radioIOS.getId());
                        break;
                    default:
                        binding.officialTypeSel.check(binding.radioAndroid.getId());
                        break;
                }
                binding.tokenCheckBox.setChecked(pref.getBoolean("use_token", false));
                break;
            case "Bilibili":
                server_type = getString(R.string.types_bilibili);
                break;
            case "Xiaomi":
                server_type = getString(R.string.types_xiaomi);
                break;
            case "UC":
                server_type = getString(R.string.types_uc);
                binding.checkBoxWDJ.setVisibility(View.VISIBLE);
                binding.checkBoxWDJ.setChecked(pref.getBoolean("use_wdj", false));
                break;
            case "Vivo":
                server_type = getString(R.string.types_vivo);
                break;
            case "Oppo":
                server_type = getString(R.string.types_oppo);
                break;
            case "Flyme":
                server_type = getString(R.string.types_flyme);
                break;
            default:
                server_type = "DEBUG -- SERVER ERROR";
        }
        binding.cardViewMain.serverText.setText(getString(R.string.types_prefix) + server_type);
        binding.cardViewMain.loginText.setText(getString(R.string.confirm_prefix) + (pref.getBoolean("auto_confirm", false) ? getString(R.string.boolean_true) : getString(R.string.boolean_false)));
        binding.cardViewMain.loginText2.setText("自动登录：" + (pref.getBoolean("auto_login", false) ? getString(R.string.boolean_true) : getString(R.string.boolean_false)));
        binding.cardViewMain.progressBar.setVisibility(View.INVISIBLE);
        binding.cardViewMain.btnCard1Action1.setOnClickListener(view1 -> {

        });
        binding.cardViewMain.btnCard1Action2.setOnClickListener(view1 -> {
            pref.edit().putBoolean("auto_login", !pref.getBoolean("auto_login", false)).apply();
            makeToast("自动登录：" + (pref.getBoolean("auto_login", false) ? getString(R.string.boolean_true) : getString(R.string.boolean_false)));
            binding.cardViewMain.loginText2.setText("自动登录：" + (pref.getBoolean("auto_login", false) ? getString(R.string.boolean_true) : getString(R.string.boolean_false)));
        });
        binding.cardViewMain.btnCard1Action3.setOnClickListener(view1 -> {
            pref.edit().putBoolean("auto_confirm", !pref.getBoolean("auto_confirm", false)).apply();
            makeToast(getString(R.string.confirm_prefix) + (pref.getBoolean("auto_confirm", false) ? getString(R.string.boolean_true) : getString(R.string.boolean_false)));
            binding.cardViewMain.loginText.setText(getString(R.string.confirm_prefix) + (pref.getBoolean("auto_confirm", false) ? getString(R.string.boolean_true) : getString(R.string.boolean_false)));
        });

        if (!(loginImpl != null && loginImpl.isLogin())) {
            binding.cardViewMain.imageViewChecked.setVisibility(View.VISIBLE);
            binding.cardViewMain.imageViewChecked.setImageResource(R.drawable.ic_baseline_close_24);
        }

        if (pref.getBoolean("last_login_succeed", false) && pref.getBoolean("auto_login", false)) {
            doLogin();
        }
        checkPermissions();
    }

    private void doLogin() {
        if (loginProgress) {
            makeToast("正在登录中...");
            return;
        }
        try {
            if (loginImpl.isLogin()) {
                makeToast(getString(R.string.has_login));
                return;
            }
        } catch (Exception ignore) {
        }
        binding.cardViewMain.imageViewChecked.setVisibility(View.INVISIBLE);
        binding.cardViewMain.progressBar.setVisibility(View.VISIBLE);
        if (loginImpl == null) {
            switch (Objects.requireNonNull(pref.getString("server_type", ""))) {
                case "Official":
                    loginImpl = new Official(activity, this);
                    break;
//                    case "Xiaomi":
//                        loginImpl = new Xiaomi(activity);
//                        //11
//                        break;
                case "Bilibili":
                    loginImpl = new Bilibili(activity, this);
                    //14
                    break;
                case "UC":
                    if (pref.getBoolean("use_wdj", false)) {
                        changeToWDJ(activity);
                    }
                    loginImpl = new UC(activity, this);
                    //20
                    break;
                case "Vivo":
                    loginImpl = new Vivo(activity, this);
                    break;
                case "Oppo":
                    loginImpl = new Oppo(activity, this);
                    break;
//                    case "Flyme":
//                        loginImpl = new Flyme(activity);
//                        break;
                default:
                    makeToast(getString(R.string.error_wrong_server));
                    break;
            }
        }
        loginImpl.login();
        loginProgress = true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == Constant.REQ_QR_CODE) {
                Bundle bundle = data.getExtras();
                if (bundle != null) {
                    String result = bundle.getString(Constant.INTENT_EXTRA_KEY_QR_SCAN);
                    if (result != null) {
                        QRScanner qrScanner;
                        if (isOfficial) {
                            qrScanner = new QRScanner(activity, true);
                        } else {
                            qrScanner = new QRScanner(activity, loginImpl.getRole());
                        }
                        if (!qrScanner.parseUrl(result)) return;
                        qrScanner.start();
                    } else {
                        makeToast(getString(R.string.error_scan));
                    }
                }
            }
            if (requestCode == REQ_CODE_SCAN_GALLERY) {
                final Uri uri = data.getData();

                ProgressDialog mProgress = new ProgressDialog(getContext());
                mProgress.setMessage("正在扫描...");
                mProgress.setCancelable(false);
                mProgress.show();
                activity.runOnUiThread(() -> {
                    Result result = scanningImage(uri);
                    mProgress.dismiss();
                    if (result != null) {
                        Intent resultIntent = new Intent();
                        Bundle bundle = resultIntent.getExtras();
                        if (bundle == null) {
                            bundle = new Bundle();
                        }
                        bundle.putString(Constant.INTENT_EXTRA_KEY_QR_SCAN, result.getText());

                        resultIntent.putExtras(bundle);
                        onActivityResult(REQ_QR_CODE, RESULT_OK, resultIntent);
                    } else {
                        Log.makeToast(com.google.zxing.R.string.note_identify_failed);
                    }
                });
            }
            if (requestCode == REQ_PERM_WINDOW) {
                fabScanner.showAlertScanner();
            }
            if (requestCode == REQ_PERM_RECORD) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Intent service = new Intent(activity, FabScanner.class);
                    service.putExtra("code", resultCode);
                    service.putExtra("data", data);
//                    service.putExtra("fragment");
                    activity.startForegroundService(service);
                } else {
                    fabScanner.setData(resultCode, data);
                }

            }
        }

    }

    public Result scanningImage(Uri uri) {
        if (uri == null) {
            return null;
        }
        Hashtable<DecodeHintType, String> hints = new Hashtable<>();
        hints.put(DecodeHintType.CHARACTER_SET, "UTF8"); //设置二维码内容的编码

        Bitmap scanBitmap = BitmapUtil.decodeUri(getContext(), uri, 500, 500);
        RGBLuminanceSource source = new RGBLuminanceSource(scanBitmap);
        BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
        QRCodeReader reader = new QRCodeReader();
        try {
            return reader.decode(bitmap1, hints);
        } catch (NotFoundException | ChecksumException | FormatException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_PERM_CAMERA:
            case REQ_PERM_EXTERNAL_STORAGE:
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
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, REQ_PERM_CAMERA);
            return;
        }
        Intent intent = new Intent(context, CaptureActivity.class);
        startActivityForResult(intent, com.github.haocen2004.login_simulation.util.Constant.REQ_QR_CODE);
    }

    @Override
    public void onClick(View view) {
        if (binding.btnScan.equals(view)) {
            if (Objects.equals(pref.getString("server_type", ""), "Official") && pref.getBoolean("use_token", false) && activity.getSharedPreferences("official_user_" + pref.getInt("official_slot", 1), Context.MODE_PRIVATE).getBoolean("has_token", false)) {
                makeToast("Token 登录模式");
                isOfficial = true;
                startQrCode();
                return;
            }
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
//                    e.printStackTrace();
                makeToast(getString(R.string.error_not_login));
            }
        } else if (binding.cardViewMain.cardView2.equals(view)) {
            doLogin();
        }

    }


    @SuppressLint("ShowToast")
    private void makeToast(String result) {
        try {

            Log.makeToast(result);
//            Toast.makeText(context, result, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Looper.prepare();
            Log.makeToast(result);
            Looper.loop();
        }
    }

    private void checkPermissions() {

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder normalDialog = new AlertDialog.Builder(getContext());
            normalDialog.setTitle("权限说明");
            normalDialog.setMessage("使用扫码器需要以下权限:\n1.使用摄像头\n用于扫描登录二维码\n\n2.读取设备文件\n用于提供相册扫码\n\n其他权限为各家SDK适配所需\n可不授予权限");
            normalDialog.setPositiveButton("我已知晓",
                    (dialog, which) -> {
                        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, REQ_PERM_CAMERA);
                        dialog.dismiss();
                    });
            normalDialog.setCancelable(false);
            normalDialog.show();
        }
    }

    private void resetOfficialServerType() {
        int i = getDefaultSharedPreferences(activity).getInt("official_type", 0);
        Logger.d(TAG, "resetOfficialServerType: " + i);
        switch (i) {
            case 1:
                OFFICIAL_TYPE = "pc01";
                break;
            case 2:
                OFFICIAL_TYPE = "ios01";
                break;
            default:
                OFFICIAL_TYPE = "android01";
                break;
        }
        Logger.d(TAG, "resetOfficialServerType: " + OFFICIAL_TYPE);
    }

    @Override
    public boolean onLongClick(View view) {
        if (binding.cardViewMain.cardView2.equals(view)) {
            try {
                if ("Official".equals(pref.getString("server_type", ""))) {
                    activity.getSharedPreferences("official_user", Context.MODE_PRIVATE).edit().clear().apply();
                    makeToast(getString(R.string.cache_delete));
                    onLoginFailed();
                }
                if (loginImpl.isLogin()) {
                    loginImpl.logout();
                    onLoginFailed();
                } else {
                    makeToast(getString(R.string.error_not_login));
                }
            } catch (Exception e) {
                makeToast(getString(R.string.error_not_login));
            }
        } else if (binding.btnScan.equals(view)) {
            if (pref.getBoolean("auto_confirm", false)) {
                try {
                    if (loginImpl.isLogin()) {
                        QRScanner qrScanner;
                        if (isOfficial) {
                            qrScanner = new QRScanner(activity, true);
                        } else {
                            qrScanner = new QRScanner(activity, loginImpl.getRole());
                        }
                        fabScanner.setQrScanner(qrScanner);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (!Settings.canDrawOverlays(activity)) {
                                final AlertDialog.Builder normalDialog = new AlertDialog.Builder(activity);
                                normalDialog.setTitle("悬浮窗扫码");
                                normalDialog.setMessage("使用悬浮窗扫码器需要悬浮窗权限和屏幕捕获权限\n请在接下来打开的窗口中授予权限");
                                normalDialog.setPositiveButton("我已知晓",
                                        (dialog, which) -> {
//                        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.SYSTEM_ALERT_WINDOW}, REQ_PERM_WINDOW);
                                            dialog.dismiss();
                                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                                            activity.startActivityForResult(intent, REQ_PERM_WINDOW);

                                        });
                                normalDialog.setCancelable(false);
                                normalDialog.show();
                            } else {
                                fabScanner.showAlertScanner();
                            }
                        } else {
                            fabScanner.showAlertScanner();
                        }

                    } else {
                        Log.makeToast(R.string.error_not_login);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.makeToast(R.string.error_not_login);
                }
            } else {
                final AlertDialog.Builder normalDialog = new AlertDialog.Builder(activity);
                normalDialog.setTitle("悬浮窗扫码");
                normalDialog.setMessage("使用悬浮窗扫码器需要开启自动确认\n是否快捷启用？");
                normalDialog.setPositiveButton("启用",
                        (dialog, which) -> {
//                        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.SYSTEM_ALERT_WINDOW}, REQ_PERM_WINDOW);
                            dialog.dismiss();
                            pref.edit().putBoolean("auto_confirm", true).apply();
                            onLongClick(view);

                        });
                normalDialog.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());
                normalDialog.setCancelable(false);
                normalDialog.show();
            }
        } else {
            makeToast("你点了什么玩意？");
        }

        return true;
    }

    @Override
    public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
        Logger.d(TAG, "onButtonChecked: group" + group);
        Logger.d(TAG, "onButtonChecked: isChecked" + isChecked);
        if (group.equals(binding.officialSlotSelect)) {
            if (checkedId == currSlot) {
                return;
            } else if (currSlot == 999) {
                currSlot = checkedId;
                return;
            }
        }
        if (group.equals(binding.officialTypeSel)) {
            if (checkedId == currType) {
                return;
            } else if (currType == 999) {
                currType = checkedId;
                return;
            }
        }

        SharedPreferences app_pref = getDefaultSharedPreferences(activity);
        switch (checkedId) {
            case R.id.slot1:
                app_pref.edit().putInt("official_slot", 1).apply();
                Logger.d(TAG, "onCheckedChanged: Switch To Slot 1");
                currSlot = checkedId;
                break;
            case R.id.slot2:
                app_pref.edit().putInt("official_slot", 2).apply();
                Logger.d(TAG, "onCheckedChanged: Switch To Slot 2");
                currSlot = checkedId;
                break;
            case R.id.slot3:
                app_pref.edit().putInt("official_slot", 3).apply();
                Logger.d(TAG, "onCheckedChanged: Switch To Slot 3");
                currSlot = checkedId;
                break;
            case R.id.radioPc:
                app_pref.edit().putInt("official_type", 1).apply();
                Logger.d(TAG, "onCheckedChanged: Switch To PC");
                resetOfficialServerType();
                currType = checkedId;
                break;
            case R.id.radioAndroid:
                app_pref.edit().putInt("official_type", 0).apply();
                Logger.d(TAG, "onCheckedChanged: Switch To Android");
                resetOfficialServerType();
                currType = checkedId;
                break;
            case R.id.radioIOS:
                app_pref.edit().putInt("official_type", 2).apply();
                Logger.d(TAG, "onCheckedChanged: Switch To IOS");
                resetOfficialServerType();
                currType = checkedId;
                break;
        }
        try {
            if (loginImpl.isLogin()) {
                loginImpl = new Official(activity, this);
                makeToast("切换后需重新登录");
            }
        } catch (Exception ignore) {
        }
//        Toast.makeText(getContext(), i+"", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoginSucceed() {
        binding.cardViewMain.progressBar.setVisibility(View.INVISIBLE);
        binding.cardViewMain.imageViewChecked.setVisibility(View.VISIBLE);
        binding.cardViewMain.imageViewChecked.setImageResource(R.drawable.ic_baseline_check_circle_outline_24);
        pref.edit().putBoolean("last_login_succeed", true).apply();
        loginProgress = false;
        makeToast("登录成功");
    }

    @Override
    public void onLoginFailed() {
        binding.cardViewMain.progressBar.setVisibility(View.INVISIBLE);
        binding.cardViewMain.imageViewChecked.setVisibility(View.VISIBLE);
        binding.cardViewMain.imageViewChecked.setImageResource(R.drawable.ic_baseline_close_24);
        pref.edit().putBoolean("last_login_succeed", false).apply();
        loginProgress = false;
    }
}
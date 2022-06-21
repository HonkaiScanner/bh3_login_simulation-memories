package com.github.haocen2004.login_simulation.fragment;

import static android.app.Activity.RESULT_OK;
import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.github.haocen2004.login_simulation.util.Constant.CHECK_VER;
import static com.github.haocen2004.login_simulation.util.Constant.OFFICIAL_TYPE;
import static com.github.haocen2004.login_simulation.util.Constant.REQ_CODE_SCAN_GALLERY;
import static com.github.haocen2004.login_simulation.util.Constant.REQ_PERM_CAMERA;
import static com.github.haocen2004.login_simulation.util.Constant.REQ_PERM_EXTERNAL_STORAGE;
import static com.github.haocen2004.login_simulation.util.Constant.REQ_PERM_RECORD;
import static com.github.haocen2004.login_simulation.util.Constant.REQ_PERM_WINDOW;
import static com.github.haocen2004.login_simulation.util.Constant.REQ_QR_CODE;
import static com.github.haocen2004.login_simulation.util.Constant.SP_CHECKED;
import static com.github.haocen2004.login_simulation.util.Tools.changeToWDJ;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
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
import com.github.haocen2004.login_simulation.activity.AboutActivity;
import com.github.haocen2004.login_simulation.activity.ScannerActivity;
import com.github.haocen2004.login_simulation.databinding.FragmentMainBinding;
import com.github.haocen2004.login_simulation.login.Bilibili;
import com.github.haocen2004.login_simulation.login.LoginCallback;
import com.github.haocen2004.login_simulation.login.LoginImpl;
import com.github.haocen2004.login_simulation.login.Official;
import com.github.haocen2004.login_simulation.login.Oppo;
import com.github.haocen2004.login_simulation.login.UC;
import com.github.haocen2004.login_simulation.login.Vivo;
import com.github.haocen2004.login_simulation.login.Xiaomi;
import com.github.haocen2004.login_simulation.util.Constant;
import com.github.haocen2004.login_simulation.util.FabScanner;
import com.github.haocen2004.login_simulation.util.Logger;
import com.github.haocen2004.login_simulation.util.QRScanner;
import com.github.haocen2004.login_simulation.util.SocketHelper;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.king.wechat.qrcode.WeChatQRCodeDetector;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

//import com.google.zxing.BinaryBitmap;
//import com.google.zxing.ChecksumException;
//import com.google.zxing.DecodeHintType;
//import com.google.zxing.FormatException;
//import com.google.zxing.NotFoundException;
//import com.google.zxing.Result;
//import com.google.zxing.activity.CaptureActivity;
//import com.google.zxing.common.HybridBinarizer;
//import com.google.zxing.decoding.RGBLuminanceSource;
//import com.google.zxing.qrcode.QRCodeReader;
//import com.google.zxing.util.BitmapUtil;
//import com.google.zxing.util.Constant;

public class MainFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener, MaterialButtonToggleGroup.OnButtonCheckedListener, LoginCallback {

    private final String TAG = "MainFragment";
    private LoginImpl loginImpl;
    private AppCompatActivity activity;
    private Context context;
    private boolean isOfficial = false;
    private SharedPreferences pref;
    private FragmentMainBinding binding;
    private Logger Log;
    private FabScanner fabScanner;
    private SocketHelper socketHelper;
    private boolean loginProgress = false;
    private int currSlot = 999;
    private int currType = 999;
    private boolean currLoginTry = false;
    private final Handler spCheckHandle = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
        }
    };
    private boolean needRestart = false;
    private boolean accSwitch = false;

    @SuppressLint("SetTextI18n") // 离谱检测 明明已经i18n了
    private void delaySPCheck() {
        Logger.d("SPCheck", "检查赞助者账号及自动登陆信息中...");
        if (pref.getBoolean("last_login_succeed", false) && pref.getBoolean("auto_login", false) && !loginProgress) {
            try {
                if (loginImpl.isLogin()) {
                    Logger.d("AutoLogin", "当前用户已登录");
                } else {
                    if (currLoginTry) {
                        if (!needRestart) {
                            Logger.d("AutoLogin", "开始自动登陆...");
                            doLogin();
                        }
                        currLoginTry = false;
                    } else {
//                makeToast("自动登录将在3s后开始");
                        currLoginTry = true;
                    }
                }
            } catch (Exception e) {
                if (currLoginTry) {
                    if (!needRestart) {
                        Logger.d("AutoLogin", "开始自动登陆...");
                        doLogin();

                    }
                    currLoginTry = false;
                } else {
//                makeToast("自动登录将在3s后开始");
                    currLoginTry = true;
                }
            }
        } else {
            Logger.d("AutoLogin", "无自动登陆任务 上次登陆情况：" + pref.getBoolean("last_login_succeed", false) + " 当前是否已有登陆进程：" + loginProgress);
            currLoginTry = false;
        }
        try {
            if (!CHECK_VER) {
                binding.cardViewMain.loginText2.setVisibility(View.INVISIBLE);
//            binding.cardViewMain.loginText2.setText(getString(R.string.sp_login_pref) + getString(R.string.update_check_off));
                if (currLoginTry) {
                    Logger.d("AutoLogin", "无自动更新 - 等待自动登陆尝试中...");
                    spCheckHandle.postDelayed(this::delaySPCheck, 1500);
                }
                Logger.d("SPCheck", "无自动更新 - 无自动登陆 - 当前线程结束");
                return;
            }
            if (!SP_CHECKED) {
                if (pref.getBoolean("has_account", false)) {
                    binding.cardViewMain.loginText2.setVisibility(View.VISIBLE);
                    Logger.d("SPCheck", "等待赞助者账号登陆中...");
                    spCheckHandle.postDelayed(this::delaySPCheck, 1500);
                    return;
                } else {
                    Logger.d("SPCheck", "未登录赞助者账号");
                    binding.cardViewMain.loginText2.setText(activity.getString(R.string.sp_login_pref) + (pref.getBoolean("has_account", false) ? activity.getString(R.string.login_true) : activity.getString(R.string.login_false)));
                    SP_CHECKED = true;
                }
            } else {
                Logger.d("SPCheck", "结束赞助者信息检查");
                binding.cardViewMain.loginText2.setText(activity.getString(R.string.sp_login_pref) + (pref.getBoolean("has_account", false) ? activity.getString(R.string.login_true) : activity.getString(R.string.login_false)));
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
            Logger.d(TAG, "未找到界面...");
        }
        if (currLoginTry) {
            Logger.d("AutoLogin", "有自动更新 - 等待自动登陆尝试中...");
            spCheckHandle.postDelayed(this::delaySPCheck, 1500);
        }
        Logger.d("SPCheck", "当前线程结束");
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity = (AppCompatActivity) getActivity();
        context = getContext();
        fabScanner = new FabScanner(this);
        socketHelper = new SocketHelper();
        pref = getDefaultSharedPreferences(context);
        Log = Logger.getLogger(getContext());
        binding = FragmentMainBinding.inflate(inflater, container, false);
//        setRetainInstance(true);
//        Logger.setView(binding.getRoot());
        return binding.getRoot();
    }

    @SuppressLint("SetTextI18n")
    private void refreshView() {
        String server_type;
        binding.officialSlotSelect.setVisibility(View.GONE);
        binding.tokenCheckBox.setVisibility(View.GONE);
        binding.officialTypeSel.setVisibility(View.GONE);
        binding.checkBoxWDJ.setVisibility(View.GONE);
//        binding.cardViewMain.loginText2.setVisibility(View.INVISIBLE);
        switch (Objects.requireNonNull(pref.getString("server_type", ""))) {
            case "Official":
                server_type = activity.getString(R.string.types_official);
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
                server_type = activity.getString(R.string.types_bilibili);
                break;
            case "Xiaomi":
                server_type = activity.getString(R.string.types_xiaomi);
                break;
            case "UC":
                server_type = activity.getString(R.string.types_uc);
                binding.checkBoxWDJ.setVisibility(View.VISIBLE);
                binding.checkBoxWDJ.setChecked(pref.getBoolean("use_wdj", false));
                break;
            case "Vivo":
                server_type = activity.getString(R.string.types_vivo);
                break;
            case "Oppo":
                server_type = activity.getString(R.string.types_oppo);
                break;
            case "Flyme":
                server_type = activity.getString(R.string.types_flyme);
                break;
            default:
                server_type = "DEBUG -- SERVER ERROR";
        }
        binding.cardViewMain.serverText.setText(activity.getString(R.string.types_prefix) + server_type);
        boolean isLogin = loginImpl != null && loginImpl.isLogin();
        if (isLogin) {
            binding.cardViewMain.progressBar.setVisibility(View.INVISIBLE);
            binding.cardViewMain.imageViewChecked.setVisibility(View.VISIBLE);
            binding.cardViewMain.imageViewChecked.setImageResource(R.drawable.ic_baseline_check_circle_outline_24);
        } else {
            binding.cardViewMain.imageViewChecked.setVisibility(View.VISIBLE);
            binding.cardViewMain.imageViewChecked.setImageResource(R.drawable.ic_baseline_close_24);

        }
        binding.cardViewMain.loginText.setText(activity.getString(R.string.bh_login_pref) + (isLogin ? loginImpl.getUsername() : activity.getString(R.string.login_false)));
        binding.cardViewMain.btnCard1Action2.setIconResource(pref.getBoolean("auto_login", false) ? R.drawable.ic_baseline_done_24 : R.drawable.ic_baseline_close_24);
        binding.cardViewMain.btnCard1Action3.setIconResource(pref.getBoolean("auto_confirm", false) ? R.drawable.ic_baseline_done_24 : R.drawable.ic_baseline_close_24);
//        binding.cardViewMain.loginText2.setText("赞助者状态：" + (HAS_ACCOUNT ? "已登录" : "未登录"));
        binding.cardViewMain.progressBar.setVisibility(View.INVISIBLE);


        if (needRestart) {
            binding.cardViewMain.serverText.setText(R.string.logged_and_restart);
            binding.cardViewMain.imageViewChecked.setImageResource(R.drawable.ic_baseline_close_24);
        }
    }

    private void setupListener() {
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

                    Intent pickIntent = new Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                    startActivityForResult(pickIntent, REQ_CODE_SCAN_GALLERY);


//                    Intent innerIntent = new Intent(Intent.ACTION_GET_CONTENT); //"android.intent.action.GET_CONTENT"
//                    innerIntent.setType("image/*");
//                    startActivityForResult(innerIntent, REQ_CODE_SCAN_GALLERY);
                } else {
                    makeToast(R.string.error_not_login);
                }
            } catch (Exception e) {
                makeToast(R.string.error_not_login);
            }
        });
        binding.cardViewMain.btnCard1Action1.setOnClickListener(view1 -> {
            String[] singleChoiceItems = getResources().getStringArray(R.array.server_types);
            String[] serverList = getResources().getStringArray(R.array.server_types_value);
            String currServer = pref.getString("server_type", "");

            int itemSelected = 0;
            for (String s : serverList) {
                if (currServer.equals(s)) {
                    break;
                }
                itemSelected++;
            }
            new AlertDialog.Builder(context)
                    .setTitle(activity.getString(R.string.sel_server))
                    .setSingleChoiceItems(singleChoiceItems, itemSelected, (dialogInterface, i) -> {

                        pref.edit().putString("server_type", serverList[i]).apply();
                        if (loginImpl != null && loginImpl.isLogin()) {
                            Log.makeToast(R.string.logged_and_restart);
                            needRestart = true;
                        }
                        refreshView();
                        dialogInterface.dismiss();
                    })
                    .setNegativeButton(activity.getString(R.string.btn_cancel), null)
                    .show();
        });
        binding.cardViewMain.btnCard1Action2.setOnClickListener(view1 -> {
            boolean newStatus = !pref.getBoolean("auto_login", false);
            pref.edit().putBoolean("auto_login", newStatus).apply();
            makeToast(activity.getString(R.string.auto_login_pref) + (newStatus ? activity.getString(R.string.boolean_true) : activity.getString(R.string.boolean_false)));
//            binding.cardViewMain.loginText2.setText("自动登录：" + (pref.getBoolean("auto_login", false) ? getString(R.string.boolean_true) : getString(R.string.boolean_false)));
            binding.cardViewMain.btnCard1Action2.setIconResource(newStatus ? R.drawable.ic_baseline_done_24 : R.drawable.ic_baseline_close_24);
        });
        binding.cardViewMain.btnCard1Action3.setOnClickListener(view1 -> {
            boolean newStatus = !pref.getBoolean("auto_confirm", false);
            pref.edit().putBoolean("auto_confirm", newStatus).apply();
            makeToast(activity.getString(R.string.confirm_prefix) + (newStatus ? activity.getString(R.string.boolean_true) : activity.getString(R.string.boolean_false)));
//            binding.cardViewMain.loginText2.setText("自动登录：" + (pref.getBoolean("auto_login", false) ? getString(R.string.boolean_true) : getString(R.string.boolean_false)));
            binding.cardViewMain.btnCard1Action3.setIconResource(newStatus ? R.drawable.ic_baseline_done_24 : R.drawable.ic_baseline_close_24);
        });
        binding.aboutTextView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AboutActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupListener();
        refreshView();
        checkPermissions();
        Logger.d("SPCheck", "初始化检查中...");
        delaySPCheck();
    }


    private void doLogin() {
        if (needRestart) {
            makeToast(R.string.logged_and_restart);
            return;
        }
        if (loginProgress) {
            makeToast(R.string.login_process);
            return;
        }
        try {
            if (loginImpl.isLogin()) {
                makeToast(R.string.has_login);
                return;
            }
        } catch (Exception ignore) {
        }
        binding.cardViewMain.imageViewChecked.setVisibility(View.INVISIBLE);
        binding.cardViewMain.progressBar.setVisibility(View.VISIBLE);
        pref.edit().putBoolean("last_login_succeed", false).apply();
        if (loginImpl == null) {
            switch (Objects.requireNonNull(pref.getString("server_type", ""))) {
                case "Official":
                    loginImpl = new Official(activity, this);
                    break;
                case "Xiaomi":
                    loginImpl = new Xiaomi(activity, this);
                    //11
                    break;
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
                    makeToast(R.string.error_wrong_server);
                    break;
            }
        }
        loginImpl.login();
        loginProgress = true;
        switchButtonState(false);
    }

    private void switchButtonState(boolean newState) {
        binding.cardViewMain.cardView2.setEnabled(newState);
        binding.slot1.setEnabled(newState);
        binding.slot2.setEnabled(newState);
        binding.slot3.setEnabled(newState);
        binding.radioIOS.setEnabled(newState);
        binding.radioAndroid.setEnabled(newState);
        binding.radioPc.setEnabled(newState);
        binding.checkBoxWDJ.setEnabled(newState);
        binding.tokenCheckBox.setEnabled(newState);
        binding.btnScan.setEnabled(newState);
        binding.btnSelpic.setEnabled(newState);

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
                        makeToast(R.string.error_scan);
                    }
                }
            }
            if (requestCode == REQ_CODE_SCAN_GALLERY) {

                Bitmap bitmap;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(),data.getData());
                    List<String> result = WeChatQRCodeDetector.detectAndDecode(bitmap);
                if (result.size() >= 1) {
                        Intent resultIntent = new Intent();
                        Bundle bundle = resultIntent.getExtras();
                        if (bundle == null) {
                            bundle = new Bundle();
                        }
                        bundle.putString(Constant.INTENT_EXTRA_KEY_QR_SCAN, result.get(0));

                        resultIntent.putExtras(bundle);
                        onActivityResult(REQ_QR_CODE, RESULT_OK, resultIntent);
                    } else {
                        Log.makeToast("未找到二维码");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                final Uri uri = data.getData();
//
//                ProgressDialog mProgress = new ProgressDialog(getContext());
//                mProgress.setMessage("正在扫描...");
//                mProgress.setCancelable(false);
//                mProgress.show();
//                activity.runOnUiThread(() -> {
//                    List<String> result = scanningImage(uri);
//                    mProgress.dismiss();
//                    if (result != null) {
//                        Intent resultIntent = new Intent();
//                        Bundle bundle = resultIntent.getExtras();
//                        if (bundle == null) {
//                            bundle = new Bundle();
//                        }
//                        bundle.putString(Constant.INTENT_EXTRA_KEY_QR_SCAN, result.get(0));
//
//                        resultIntent.putExtras(bundle);
//                        onActivityResult(REQ_QR_CODE, RESULT_OK, resultIntent);
//                    } else {
//                        Log.makeToast(com.google.zxing.R.string.note_identify_failed);
//                    }
//                });
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
        Intent intent = new Intent(context, ScannerActivity.class);
        startActivityForResult(intent, com.github.haocen2004.login_simulation.util.Constant.REQ_QR_CODE);
    }

    @Override
    public void onClick(View view) {
        if (binding.btnScan.equals(view)) {
            if (Objects.equals(pref.getString("server_type", ""), "Official") && pref.getBoolean("use_token", false) && activity.getSharedPreferences("official_user_" + pref.getInt("official_slot", 1), Context.MODE_PRIVATE).getBoolean("has_token", false)) {
//                makeToast("Token 登录模式");
                isOfficial = true;
                startQrCode();
                return;
            }
            try {
                if (loginImpl.isLogin()) {
                    if (loginImpl.getRole().is_setup()) {
                        startQrCode();
                    } else {
                        makeToast(R.string.error_oa_process);
                    }
                } else {
                    makeToast(R.string.error_not_login);
                }
            } catch (NullPointerException e) {
//                    e.printStackTrace();
                makeToast(R.string.error_not_login);
            }
        } else if (binding.cardViewMain.cardView2.equals(view)) {
            doLogin();
        }

    }



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

    private void makeToast(Integer result) {
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
            final AlertDialog.Builder normalDialog = new AlertDialog.Builder(context);
            normalDialog.setTitle("权限说明");
            normalDialog.setMessage("使用扫码器需要以下权限:\n1.使用摄像头\n用于扫描登录二维码\n\n2.读取设备文件\n用于提供相册扫码\n可选：显示悬浮窗和获取屏幕内容\n仅在使用悬浮窗扫码功能时申请\n\n其他权限为各家SDK适配所需\n可不授予权限");
            normalDialog.setPositiveButton("我已知晓并授权使用",
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
                    activity.getSharedPreferences("official_user_" + pref.getInt("official_slot", 1), Context.MODE_PRIVATE).edit().clear().apply();
                    makeToast(R.string.cache_delete);
                    onLoginFailed();
                } else if (loginImpl.isLogin()) {
                    loginImpl.logout();
                    onLoginFailed();
                } else {
                    makeToast(R.string.error_not_login);
                }
            } catch (Exception e) {
                makeToast(R.string.error_not_login);
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
                normalDialog.setMessage("使用悬浮窗扫码器需要开启自动确认\n是否立即启用？");
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
        if (loginProgress) {
            Log.makeToast("当前正在登陆进程中，无法切换此项目");
            Logger.d(TAG, "登陆中 已阻止切换服务器或账户槽位");
            return;
        }
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
        View viewById = activity.findViewById(checkedId);
        if (binding.slot1.equals(viewById)) {
            app_pref.edit().putInt("official_slot", 1).apply();
            Logger.d(TAG, "onCheckedChanged: Switch To Slot 1");
            currSlot = checkedId;
        } else if (binding.slot2.equals(viewById)) {
            app_pref.edit().putInt("official_slot", 2).apply();
            Logger.d(TAG, "onCheckedChanged: Switch To Slot 2");
            currSlot = checkedId;
        } else if (binding.slot3.equals(viewById)) {
            app_pref.edit().putInt("official_slot", 3).apply();
            Logger.d(TAG, "onCheckedChanged: Switch To Slot 3");
            currSlot = checkedId;
        } else if (binding.radioPc.equals(viewById)) {
            app_pref.edit().putInt("official_type", 1).apply();
            Logger.d(TAG, "onCheckedChanged: Switch To PC");
            resetOfficialServerType();
            currType = checkedId;
        } else if (binding.radioAndroid.equals(viewById)) {
            app_pref.edit().putInt("official_type", 0).apply();
            Logger.d(TAG, "onCheckedChanged: Switch To Android");
            resetOfficialServerType();
            currType = checkedId;
        } else if (binding.radioIOS.equals(viewById)) {
            app_pref.edit().putInt("official_type", 2).apply();
            Logger.d(TAG, "onCheckedChanged: Switch To IOS");
            resetOfficialServerType();
            currType = checkedId;
        }
        try {
            if (loginImpl.isLogin() || accSwitch) {
                accSwitch = true;
                loginImpl = new Official(activity, this);
                refreshView();
                makeToast("切换后需重新登录");
            }
        } catch (Exception ignore) {
        }
//        Toast.makeText(getContext(), i+"", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoginSucceed() {
        spCheckHandle.postDelayed(() -> {
            pref.edit().putBoolean("last_login_succeed", true).apply();
            QRScanner qrScanner;
            if (isOfficial) {
                qrScanner = new QRScanner(activity, true);
            } else {
                qrScanner = new QRScanner(activity, loginImpl.getRole());
            }
            if (pref.getBoolean("socket_helper", false)) {
                socketHelper.setQrScanner(qrScanner);
                socketHelper.start();
            }
            loginProgress = false;
            makeToast(R.string.login_succeed);
            refreshView();
            switchButtonState(true);
        }, 500);
    }

    @Override
    public void onLoginFailed() {
        spCheckHandle.post(() -> {
            binding.cardViewMain.progressBar.setVisibility(View.INVISIBLE);
            binding.cardViewMain.imageViewChecked.setVisibility(View.VISIBLE);
            binding.cardViewMain.imageViewChecked.setImageResource(R.drawable.ic_baseline_close_24);
            pref.edit().putBoolean("last_login_succeed", false).apply();
            loginProgress = false;
            loginImpl = null;
            refreshView();
            switchButtonState(true);
        });
//        makeToast(R);
    }
}

package com.github.haocen2004.login_simulation.activity;

import static com.github.haocen2004.login_simulation.data.Constant.INTENT_EXTRA_KEY_QR_SCAN;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.haocen2004.login_simulation.utils.Logger;
import com.king.mlkit.vision.camera.AnalyzeResult;
import com.king.wechat.qrcode.scanning.WeChatCameraScanActivity;

import java.util.List;

public class ScannerActivity extends WeChatCameraScanActivity {

    String TAG = "ScannerActivity";

    ActivityManager activityManager = ActivityManager.getInstance();

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityManager.addActivity(this);
    }

    @Override
    protected void onPause() {
        activityManager.removeActivity(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        activityManager.addActivity(this);
    }

    @Override
    protected void onStop() {
        activityManager.removeActivity(this);
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        activityManager.addActivity(this);
    }

    @Override
    protected void onDestroy() {
        activityManager.removeActivity(this);
        super.onDestroy();
    }

    @Override
    public void onScanResultCallback(@NonNull AnalyzeResult<List<String>> result) {
        if (!result.getResult().isEmpty()) {
            getCameraScan().setAnalyzeImage(false);
            Logger.d(TAG, result.getResult().toString());
            String[] text = result.getResult().toArray(new String[0]);
            Intent intent = new Intent();
            intent.putExtra(INTENT_EXTRA_KEY_QR_SCAN, text);
            setResult(RESULT_OK, intent);
            finish();
        }
    }
}

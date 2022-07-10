package com.github.haocen2004.login_simulation.activity;

import static com.github.haocen2004.login_simulation.util.Constant.INTENT_EXTRA_KEY_QR_SCAN;

import android.content.Intent;

import androidx.annotation.NonNull;

import com.github.haocen2004.login_simulation.util.Logger;
import com.king.mlkit.vision.camera.AnalyzeResult;
import com.king.wechat.qrcode.scanning.WeChatCameraScanActivity;

import java.util.List;

public class ScannerActivity extends WeChatCameraScanActivity {

    String TAG = "ScannerActivity";

    @Override
    public void onScanResultCallback(@NonNull AnalyzeResult<List<String>> result) {
        if(!result.getResult().isEmpty()){
            getCameraScan().setAnalyzeImage(false);
            Logger.d(TAG,result.getResult().toString());
            String[] text = result.getResult().toArray(new String[0]);
            Intent intent = new Intent();
            intent.putExtra(INTENT_EXTRA_KEY_QR_SCAN,text);
            setResult(RESULT_OK,intent);
            finish();
        }
    }
}

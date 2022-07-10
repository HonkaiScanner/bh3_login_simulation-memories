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
            //停止分析
            getCameraScan().setAnalyzeImage(false);
//            setAnalyzeImage(false);
            Logger.d(TAG,result.getResult().toString());
            //一般需求都是识别一个码，所以这里取第0个就可以；有识别多个码的需求，可以取全部
            String[] text = result.getResult().toArray(new String[0]);
            Intent intent = new Intent();
            intent.putExtra(INTENT_EXTRA_KEY_QR_SCAN,text);
            setResult(RESULT_OK,intent);
            finish();
        }
    }
}

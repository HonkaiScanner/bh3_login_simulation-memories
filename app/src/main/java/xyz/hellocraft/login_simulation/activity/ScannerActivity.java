package xyz.hellocraft.login_simulation.activity;

import static xyz.hellocraft.login_simulation.data.Constant.INTENT_EXTRA_KEY_QR_SCAN;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.github.haocen2004.login_simulation.R;
import com.king.camera.scan.AnalyzeResult;
import com.king.view.viewfinderview.ViewfinderView;
import com.king.wechat.qrcode.scanning.WeChatCameraScanActivity;

import java.util.List;

import xyz.hellocraft.login_simulation.data.Constant;
import xyz.hellocraft.login_simulation.utils.Logger;
import xyz.hellocraft.login_simulation.utils.Tools;

public class ScannerActivity extends WeChatCameraScanActivity {

    String TAG = "ScannerActivity";

    ActivityManager activityManager = ActivityManager.getInstance();

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityManager.addActivity(this);
        viewfinderView.setViewfinderStyle(ViewfinderView.ViewfinderStyle.POPULAR);
        viewfinderView.setLaserStyle(ViewfinderView.LaserStyle.NONE);

        if (Constant.FA) {

            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.sparkle_small_scan);

            if (drawable != null) {

                Bitmap bitmap = Bitmap.createBitmap(
                        drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight(),
                        Bitmap.Config.ARGB_8888
                );
                Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);
                Bitmap transparent = Tools.setBitmapAlpha(bitmap, 45);

                viewfinderView.setMaskColor(0x00000000);
                viewfinderView.setFrameBitmap(transparent);
                viewfinderView.setViewfinderStyle(ViewfinderView.ViewfinderStyle.CLASSIC);
                viewfinderView.setFrameGravity(ViewfinderView.FrameGravity.TOP);
                viewfinderView.setFrameWidth(300);
                viewfinderView.setFrameHeight(300);
            }
        }
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

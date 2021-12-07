package com.github.haocen2004.login_simulation.util;

import static com.github.haocen2004.login_simulation.util.Constant.BAG_ALTER_NOTIFICATION;
import static com.github.haocen2004.login_simulation.util.Constant.REQ_PERM_RECORD;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.haocen2004.login_simulation.R;
import com.github.haocen2004.login_simulation.activity.MainActivity;
import com.hjq.xtoast.XToast;
import com.hjq.xtoast.draggable.SpringDraggable;
import com.king.wechat.qrcode.WeChatQRCodeDetector;

import java.nio.ByteBuffer;
import java.util.Objects;

//import com.google.zxing.BinaryBitmap;
//import com.google.zxing.ChecksumException;
//import com.google.zxing.DecodeHintType;
//import com.google.zxing.FormatException;
//import com.google.zxing.NotFoundException;
//import com.google.zxing.common.HybridBinarizer;
//import com.google.zxing.decoding.RGBLuminanceSource;
//import com.google.zxing.qrcode.QRCodeReader;

public class FabScanner extends Service {
    private static FabScanner INSTANCE;
    private final String TAG = "FabScanner";
    boolean isScreenCaptureStarted;
    private MediaProjectionManager mProjectionManager = null;
    private MediaProjection sMediaProjection;
    private Activity activity;
    private Logger Log;
    private boolean hasData;
    //    OnImageCaptureScreenListener listener;
    private int mDensity;
    private Display mDisplay;
    private int mWidth;
    private int mHeight;
    private ImageReader mImageReader;
    private VirtualDisplay mVirtualDisplay;
    private Handler mHandler;
    private Bitmap mbitmap;
    private String url;
    private QRScanner qrScanner;
    private Fragment fragment;
    private int mResultCode;
    private Intent mResultData;
    private boolean needStop;

    public FabScanner() {
    }

    public FabScanner(Fragment fragment) {
        this.fragment = fragment;
        this.activity = fragment.getActivity();
        Log = Logger.getLogger(activity);
        mProjectionManager = (MediaProjectionManager) activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        hasData = false;
        isScreenCaptureStarted = false;
        needStop = false;

        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                mHandler = new Handler();
                Looper.loop();
            }
        }.start();

        INSTANCE = this;
    }

    public static FabScanner getINSTANCE() {
        return INSTANCE;
    }

    public void setQrScanner(QRScanner qrScanner) {
        this.qrScanner = qrScanner;
    }


    public void setData(int resultCode, Intent data) {
        mResultCode = resultCode;
        mResultData = data;
        sMediaProjection = mProjectionManager.getMediaProjection(mResultCode, mResultData);
        hasData = true;
        showAlertScanner();

    }

    public void setsMediaProjection(MediaProjection sMediaProjection) {
        this.sMediaProjection = sMediaProjection;
        hasData = true;
        showAlertScanner();
    }

    @SuppressLint("WrongConstant")
    public void showAlertScanner() {
        if (!hasData) {
            fragment.startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQ_PERM_RECORD);
        } else {
            new XToast<>(activity.getApplication())
                    .setView(R.layout.fab_scanner)
                    .setGravity(Gravity.END | Gravity.BOTTOM)
                    .setYOffset(200)
                    // 设置指定的拖拽规则
                    .setDraggable(new SpringDraggable())
                    .setOnClickListener((XToast.OnClickListener<View>) (toast, view1) -> {
                        if (needStop) {
                            toast.cancel();
                            stopForeground(true);
                            return;
                        }

                        isScreenCaptureStarted = true;
                        WindowManager window = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
                        mDisplay = window.getDefaultDisplay();
                        final DisplayMetrics metrics = new DisplayMetrics();
                        mDisplay.getRealMetrics(metrics);
                        mDensity = metrics.densityDpi;
                        mWidth = metrics.widthPixels;
                        mHeight = metrics.heightPixels;

                        //start capture reader
                        mImageReader = ImageReader.newInstance(mWidth, mHeight, 1, 2);
                        try {
                            mVirtualDisplay = sMediaProjection.createVirtualDisplay(
                                    "ScreenShot",
                                    mWidth,
                                    mHeight,
                                    mDensity,
                                    DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                                    mImageReader.getSurface(),
                                    null,
                                    mHandler);
                        } catch (Exception e) {
                            try {
                                e.printStackTrace();
                                Log.makeToast("悬浮窗进程异常！");
                                toast.cancel();
                                stopForeground(true);
                            } catch (Exception ignore) {
                            }
                        }
                        mImageReader.setOnImageAvailableListener(reader -> {

                            if (isScreenCaptureStarted) {

                                Bitmap bitmap = null;
                                try (Image image = reader.acquireLatestImage()) {
                                    if (image != null) {

                                        int width = image.getWidth();
                                        int height = image.getHeight();

                                        final Image.Plane[] planes = image.getPlanes();
                                        final ByteBuffer buffer = planes[0].getBuffer();
                                        int pixelStride = planes[0].getPixelStride();
                                        int rowStride = planes[0].getRowStride();
                                        int rowPadding = rowStride - pixelStride * width;
                                        bitmap = Bitmap.createBitmap(
                                                width + rowPadding / pixelStride
                                                , height, Bitmap.Config.ARGB_8888);
                                        bitmap.copyPixelsFromBuffer(buffer);
                                        mbitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
                                        url = WeChatQRCodeDetector.detectAndDecode(bitmap).get(0);
//                                        Hashtable<DecodeHintType, String> hints = new Hashtable<>();
//                                        hints.put(DecodeHintType.CHARACTER_SET, "UTF8");
//
//                                        RGBLuminanceSource source = new RGBLuminanceSource(mbitmap);
//                                        BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
//                                        QRCodeReader reader2 = new QRCodeReader();
//                                        try {
//                                            url = reader2.decode(bitmap1, hints).getText();
//                                        } catch (NotFoundException | ChecksumException | FormatException e) {
//                                            url = "";
//                                            e.printStackTrace();
//                                        }

                                        if (qrScanner.parseUrl(url)) {
                                            Log.makeToast("扫码成功\n处理中....");
                                            qrScanner.start();
                                            stopProjection();
                                            stopForeground(true);
                                            needStop = true;
                                            toast.cancel();
                                        } else {
//                                            Log.makeToast("未找到二维码");  toast 由 qrScanner 发出
                                            isScreenCaptureStarted = false;
                                        }

                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Log.makeToast("未找到二维码");
                                    isScreenCaptureStarted = false;
                                } finally {
                                    if (null != bitmap) {
                                        bitmap.recycle();
                                    }
                                }
                            }
                        }, mHandler);
                        sMediaProjection.registerCallback(new MediaProjectionStopCallback(), mHandler);
                    })
                    .show();
//            Logger.setUseSnackbar(false);
        }
    }

    private void stopProjection() {
        isScreenCaptureStarted = false;
        Logger.d(TAG, "Screen captured");
        mHandler.post(() -> {
            if (sMediaProjection != null) {
                sMediaProjection.stop();
            }
        });
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        mResultCode = intent.getIntExtra("code", -1);
        mResultData = intent.getParcelableExtra("data");
        if (mProjectionManager == null) {
            mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        }
        sMediaProjection = mProjectionManager.getMediaProjection(mResultCode, Objects.requireNonNull(mResultData));

        FabScanner.getINSTANCE().setsMediaProjection(sMediaProjection);
        return super.onStartCommand(intent, flags, startId);
    }

    private void createNotificationChannel() {
        Notification.Builder builder = new Notification.Builder(this.getApplicationContext());
        Intent nfIntent = new Intent(this, MainActivity.class);

        builder.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, 0))
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher))
                .setContentTitle("扫码器悬浮窗扫码后台进程")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText("安卓10及以上适配所需")
                .setWhen(System.currentTimeMillis());

        /*以下是对Android 8.0的适配*/
        //普通notification适配
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId("scanner_alert_channel");
        }
        //前台服务notification适配
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel("scanner_alert_channel", "扫码器悬浮窗后台进程", NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
        }

        Notification notification = builder.build();
        notification.defaults = Notification.DEFAULT_SOUND;
        startForeground(BAG_ALTER_NOTIFICATION, notification);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }

    private class MediaProjectionStopCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            mHandler.post(() -> {
                if (mVirtualDisplay != null) {
                    mVirtualDisplay.release();
                }
                if (mImageReader != null) {
                    mImageReader.setOnImageAvailableListener(null, null);
                }
                sMediaProjection.unregisterCallback(MediaProjectionStopCallback.this);
            });
        }
    }
}

package com.github.haocen2004.login_simulation.util;

import static android.app.Activity.RESULT_OK;
import static com.github.haocen2004.login_simulation.util.Constant.BAG_ALTER_NOTIFICATION;
import static com.github.haocen2004.login_simulation.util.Constant.DEBUG_MODE;

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
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.github.haocen2004.login_simulation.R;
import com.github.haocen2004.login_simulation.activity.MainActivity;
import com.github.haocen2004.login_simulation.data.dialog.ButtonData;
import com.github.haocen2004.login_simulation.data.dialog.DialogData;
import com.github.haocen2004.login_simulation.data.dialog.DialogLiveData;
import com.hjq.xtoast.XToast;
import com.hjq.xtoast.draggable.SpringDraggable;
import com.king.wechat.qrcode.WeChatQRCodeDetector;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.List;
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
    private String[] url;
    private QRScanner qrScanner;
    private Fragment fragment;
    private int mResultCode;
    private Intent mResultData;
    private boolean needStop;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private XToast<?> toastInst;
    private int counter = 0;

    public FabScanner() {
    }

    public void setActivityResultLauncher(ActivityResultLauncher<Intent> activityResultLauncher) {
        this.activityResultLauncher = activityResultLauncher;
    }

    public ActivityResultCallback<ActivityResult> getResultApiCallback() {
        return callback -> {
            if (callback.getResultCode() == RESULT_OK) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Intent service = new Intent(activity, FabScanner.class);
                    service.putExtra("code", callback.getResultCode());
                    service.putExtra("data", callback.getData());
//                    service.putExtra("fragment");
                    activity.startForegroundService(service);
                } else {
                    mResultCode = callback.getResultCode();
                    mResultData = callback.getData();
                    sMediaProjection = mProjectionManager.getMediaProjection(mResultCode, mResultData);
                    hasData = true;
                    showAlertScanner();

                }
            }
        };
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
                try {
                    Looper.prepare();
                    mHandler = new Handler();
                    Looper.loop();
                } catch (Exception e) {
                    mHandler = new Handler();
                }
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


    public void setsMediaProjection(MediaProjection sMediaProjection) {
        this.sMediaProjection = sMediaProjection;
        hasData = true;
        showAlertScanner();
    }

    private boolean showMIUIalert = true;

    @SuppressLint("WrongConstant")
    public void showAlertScanner() {
        if (showMIUIalert && Tools.isMIUI(activity)) {
            String alertMsg = "检测到MIUI系统\n如果是MIUI13+需要同时在左上角权限提醒中允许屏幕共享\n否则扫码器将无法正确获取屏幕内容\n\n该内容每次都会出现";
            DialogData dialogData = new DialogData("悬浮窗扫码 - MIUI 额外提醒", alertMsg);
            dialogData.setPositiveButtonData(new ButtonData("我已知晓") {
                @Override
                public void callback(DialogHelper dialogHelper) {
                    showMIUIalert = false;
                    showAlertScanner();
                    super.callback(dialogHelper);
                }
            });
            DialogLiveData.getINSTANCE(null).addNewDialog(dialogData);
        } else {
            if (!hasData) {

                activityResultLauncher.launch(mProjectionManager.createScreenCaptureIntent());

//                fragment.startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQ_PERM_RECORD);
            } else if (toastInst == null) {
                counter++;
                toastInst = new XToast<>(activity.getApplication())
                        .setView(R.layout.fab_scanner)
                        .setGravity(Gravity.END | Gravity.BOTTOM)
                        .setYOffset(200)
                        .setDraggable(new SpringDraggable())
                        .setOnClickListener((toast, view1) -> {
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
                                        DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
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
                                            Bitmap.createBitmap(bitmap, 0, 0, width, height);
                                            if (DEBUG_MODE) {
                                                try {
                                                    String path = activity.getExternalFilesDir(null) + "/screenshot/";
                                                    File dir = new File(path);
                                                    if (!dir.exists()) {
                                                        dir.mkdirs();
                                                    }
                                                    File file = new File(path + System.currentTimeMillis() + ".jpg");
                                                    FileOutputStream out = new FileOutputStream(file);
                                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                                                    out.flush();
                                                    out.close();
                                                    //保存图片后发送广播通知更新数据库
//                                            Uri uri = Uri.fromFile(file);
//                                            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            List<String> urls = WeChatQRCodeDetector.detectAndDecode(bitmap);
                                            url = urls.toArray(new String[0]);
                                            Logger.d(TAG, urls.toString());
                                            if (qrScanner.parseUrl(url)) {
                                                Toast.makeText(activity, "扫码成功\n处理中....", Toast.LENGTH_SHORT).show();
                                                qrScanner.setFabMode(true);
                                                qrScanner.start();
                                                if (!PreferenceManager.getDefaultSharedPreferences(fragment.requireContext()).getBoolean("keep_capture", false)) {
                                                    stopProjection();
                                                    stopForeground(true);
                                                    needStop = true;
                                                    toast.cancel();
                                                    toast.recycle();
                                                } else {
                                                    Logger.d(TAG, "keep capture is true,continue.");
                                                    isScreenCaptureStarted = false;
                                                }
                                            } else {
//                                            Log.makeToast("未找到二维码");  toast 由 qrScanner 发出
                                                isScreenCaptureStarted = false;
                                            }
                                            mVirtualDisplay.release();
                                            mVirtualDisplay = null;
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
                        });
                toastInst.show();
//            Logger.setUseSnackbar(false);
            } else {
                counter++;
                Log.makeToast("你只可以打开一个悬浮窗！");
            }
        }
    }

    private void stopProjection() {
        isScreenCaptureStarted = false;
        Logger.d(TAG, "Screen captured");
        mHandler.post(() -> {
            if (sMediaProjection != null) {
                sMediaProjection.stop();
                sMediaProjection = null;
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

    @SuppressLint("InlinedApi")
    private void createNotificationChannel() {
        Notification.Builder builder = new Notification.Builder(this.getApplicationContext());
        Intent nfIntent = new Intent(this, MainActivity.class);
        nfIntent.addFlags(Intent.FLAG_ACTIVITY_MATCH_EXTERNAL);
        builder.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, PendingIntent.FLAG_IMMUTABLE))
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher))
                .setContentTitle("扫码器悬浮窗扫码后台进程")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText("安卓10及以上适配所需")
                .setWhen(System.currentTimeMillis());

        /*以下是对Android 8.0的适配*/
        //普通notification适配
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            builder.setChannelId("scanner_alert_channel");
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

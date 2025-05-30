package xyz.hellocraft.login_simulation.utils;

import static xyz.hellocraft.login_simulation.utils.Tools.getString;
import static xyz.hellocraft.login_simulation.utils.Tools.saveString;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.UiModeManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.ViewConfiguration;
import android.view.WindowManager;

import androidx.core.os.EnvironmentCompat;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import kotlin.ExceptionsKt;
import kotlin.Unit;
import kotlin.jvm.internal.Ref;
import kotlin.text.Regex;


public final class XDeviceUtils {
    public static final XDeviceUtils INSTANCE = new XDeviceUtils();
    private static final String TAG = "XDeviceUtils";
    private static final int EXCEPTION_CODE = -99;
    private static int cachedIsRooted = EXCEPTION_CODE;

    private XDeviceUtils() {
    }

    public static String getCpuModel(XDeviceUtils xDeviceUtils, String str, int i, Object obj) {
        if ((i & 1) != 0) {
            str = "hardware";
        }
        return xDeviceUtils.getCpuModel(str);
    }

    public static String getAndroidID$default(XDeviceUtils xDeviceUtils, Context context, boolean z, int i, Object obj) {
        if ((i & 2) != 0) {
            z = false;
        }
        return xDeviceUtils.getAndroidID(context, z);
    }

    public static String bytesToMegabytes(long bytes) {
        if (bytes <= 0) {
            return EnvironmentCompat.MEDIA_UNKNOWN;
        }
        long j = 1024;
        return String.valueOf((bytes / j) / j);
    }

    public int getScreenResolutionX(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public int getScreenResolutionY(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    public String getNetworkType(Context context) {

        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                Network[] allNetworks = connectivityManager.getAllNetworks();
                if (!(activeNetworkInfo != null && activeNetworkInfo.isConnected())) {
                    return "other";
                }
                for (Network network : allNetworks) {
                    NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
                    if (networkCapabilities != null) {
                        if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                            return "WiFi";
                        }
                        if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                            int valueOf = activeNetworkInfo.getSubtype();
                            if (valueOf == 20) {
                                return "5G";
                            }

                            switch (valueOf) {
                                case 1:
                                case 2:
                                case 4:
                                case 7:
                                case 11:
                                    return "2G";
                                case 3:
                                case 5:
                                case 6:
                                case 8:
                                case 9:
                                case 10:
                                case 12:
                                case 14:
                                case 15:
                                    return "3G";
                                case 13:
                                    return "4G";
                                default:
                                    return "other";
                            }

                        }
                    }
                }
            }
        } catch (Throwable th) {
            Log.w(TAG, ExceptionsKt.stackTraceToString(th));
        }
        return "other";
    }

    public int isProxy() {
        try {
            return (System.getProperty("http.proxyHost") == null && System.getProperty("https.proxyHost") == null) ? 0 : 1;
        } catch (Throwable th) {
            Log.w(TAG, ExceptionsKt.stackTraceToString(th));
            return EXCEPTION_CODE;
        }
    }

    public int isEmulator(Context context) {
        return 0;
    }

    public String getCpuModel(String type) {
        String str;
        String replace$default;
        String str2 = null;
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader("/proc/cpuinfo"));
            while (true) {
                String readLine = bufferedReader.readLine();
                if (readLine == null) {
                    break;
                }
                Locale ROOT = Locale.ROOT;
                String lowerCase = readLine.toLowerCase(ROOT);
                Object[] array = new Regex(":\\s+").split(lowerCase, 2).toArray(new String[0]);
                String[] strArr = (String[]) array;
                if (strArr[0].startsWith(type)) {
                    str2 = strArr[1];
                    break;
                }
            }
        } catch (Throwable th) {
            Log.w(TAG, ExceptionsKt.stackTraceToString(th));
        }
        String str3 = str2;
        String cpuModelFromSystemProperties = getCpuModelFromSystemProperties();
        String str4 = Build.HARDWARE;
        String str5 = "";
        if (str3 == null) {
            str = "";
        } else {
            str = str3.replace("|", "");
        }
        String replace$default2 = cpuModelFromSystemProperties.replace("|", "");
        if (str4 != null) {
            replace$default = str4.replace("|", "");
            str5 = replace$default;
        }
        return str + '|' + replace$default2 + '|' + str5;
    }

    private String getCpuModelFromSystemProperties() {
        try {
            @SuppressLint("PrivateApi") Class<?> cls = Class.forName("android.os.SystemProperties");
            Object invoke = cls.getDeclaredMethod("get", String.class, String.class).invoke(cls, "ro.soc.model", "");
            if (invoke != null) {
                return (String) invoke;
            }
            throw new NullPointerException("null cannot be cast to non-null type kotlin.String");
        } catch (Throwable th) {
            Log.w(TAG, ExceptionsKt.stackTraceToString(th));
            return "";
        }
    }

    public String getRomCapacity() {
        long capacity = Runtime.getRuntime().maxMemory();
        return bytesToMegabytes(capacity);
    }

    public String getRomRemain() {
        Runtime runtime = Runtime.getRuntime();
        long freeMemorySize = runtime.maxMemory() - (runtime.totalMemory() - runtime.freeMemory());
        return bytesToMegabytes(freeMemorySize);
    }

    public String getScreenSize(Context context) {
        Object systemService = context.getSystemService(Context.WINDOW_SERVICE);
        Display defaultDisplay = ((WindowManager) systemService).getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        defaultDisplay.getMetrics(displayMetrics);
        int i = displayMetrics.widthPixels;
        int i2 = displayMetrics.heightPixels;
        return String.valueOf(i) +
                'x' +
                i2;
    }

    public String getDeviceInfo() {
        return Build.FINGERPRINT;
    }

    public String getAppMemory() {
        return bytesToMegabytes(Runtime.getRuntime().maxMemory());

    }

    public String syncGetSensorValues(Context context, int type) {
        String cacheValues = getString(context, "syncGetSensorValues_" + type);
        if (!TextUtils.isEmpty(cacheValues)) {
            try {
                JSONObject jsonObject = new JSONObject(cacheValues);
                long ts = jsonObject.getLong("ts");
                // cache 7 days
                if (System.currentTimeMillis() - ts < 604800000L) {
                    return jsonObject.getString("values");
                }
            } catch (Exception ignore) {
            }
        }
        Object systemService = null;
        final Ref.ObjectRef objectRef = new Ref.ObjectRef();
        objectRef.element = "";
        try {
            systemService = context.getSystemService(Context.SENSOR_SERVICE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (systemService == null) {
            throw new NullPointerException("null cannot be cast to non-null type android.hardware.SensorManager");
        }
        SensorManager sensorManager = (SensorManager) systemService;
        Sensor defaultSensor = sensorManager.getDefaultSensor(type);
        if (defaultSensor == null) {
            return (String) objectRef.element;
        }
        final ReentrantLock reentrantLock = new ReentrantLock();
        final Condition newCondition = reentrantLock.newCondition();
        SensorEventListener sensorEventListener = new SensorEventListener() { // from class: com.mihoyo.platform.sdk.devicefp.CommonRequiredParams$syncGetSensorValues$listener$1
            @Override // android.hardware.SensorEventListener
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }

            /* JADX WARN: Type inference failed for: r6v3, types: [T, java.lang.String] */
            @Override // android.hardware.SensorEventListener
            public void onSensorChanged(SensorEvent event) {
                if (event == null) {
                    return;
                }
                Ref.ObjectRef<String> objectRef2 = objectRef;
                StringBuilder sb = new StringBuilder();
                sb.append(event.values[0]);
                sb.append('x');
                sb.append(event.values[1]);
                sb.append('x');
                sb.append(event.values[2]);
                objectRef2.element = sb.toString();
                ReentrantLock reentrantLock2 = reentrantLock;
                Condition condition = newCondition;
                reentrantLock2.lock();
                try {
                    condition.signal();
                    Unit unit = Unit.INSTANCE;
                } finally {
                    reentrantLock2.unlock();
                }
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("values", sb.toString());
                    jsonObject.put("ts", System.currentTimeMillis());
                    saveString(context, "syncGetSensorValues_" + type, jsonObject.toString());
                } catch (Exception ignored) {
                }
            }
        };
        if (sensorManager.registerListener(sensorEventListener, defaultSensor, 3)) {
            ReentrantLock reentrantLock2 = reentrantLock;
            reentrantLock2.lock();
            try {
                newCondition.await(2L, TimeUnit.SECONDS);
                reentrantLock2.unlock();
            } catch (Throwable th) {
                reentrantLock2.unlock();
            }
        }
        sensorManager.unregisterListener(sensorEventListener, defaultSensor);
        return (String) objectRef.element;
    }


    public int getCpuCores() {
        try {
            return Runtime.getRuntime().availableProcessors();
        } catch (Throwable th) {
            Log.w(TAG, ExceptionsKt.stackTraceToString(th));
            return 1;
        }
    }

    public long getBoot2NowTime() {
        return SystemClock.elapsedRealtime() / 1000;
    }

    public int getBrightness(Context context) {

        try {
            ContentResolver contentResolver = context.getContentResolver();
            return Settings.System.getInt(contentResolver, "screen_brightness");
        } catch (Throwable unused) {
            return EXCEPTION_CODE;
        }
    }

    @SuppressLint("HardwareIds")
    public String getAndroidID(Context context, boolean allowInvalid) {
        String id = null;
        String str = null;
        boolean z;

        try {
            id = Settings.Secure.getString(context.getContentResolver(), "android_id");
            str = id;
        } catch (Throwable th) {
            Log.w(TAG, ExceptionsKt.stackTraceToString(th));
        }
        if (TextUtils.isEmpty(str)) {
            return id;
        }
        if (allowInvalid) {
            return "";
        }
        return null;
    }

    public long getTotalRomSpace() {
        try {
            return Environment.getDataDirectory().getTotalSpace();
        } catch (Throwable th) {
            Log.w(TAG, ExceptionsKt.stackTraceToString(th));
            return -99L;
        }
    }

    public long getRomSpaceRemaining() {
        try {
            return Environment.getDataDirectory().getFreeSpace();
        } catch (Throwable th) {
            Log.w(TAG, ExceptionsKt.stackTraceToString(th));
            return -99L;
        }
    }

    public long getTotalRamSpace(Context context) {

        try {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
            if (activityManager == null) {
                return -99L;
            }
            activityManager.getMemoryInfo(memoryInfo);
            return memoryInfo.totalMem;
        } catch (Throwable th) {
            Log.w(TAG, ExceptionsKt.stackTraceToString(th));
            return -99L;
        }
    }

    public long getRamSpaceRemaining(Context context) {

        try {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
            if (activityManager == null) {
                return -99L;
            }
            activityManager.getMemoryInfo(memoryInfo);
            return memoryInfo.availMem;
        } catch (Throwable th) {
            Log.w(TAG, ExceptionsKt.stackTraceToString(th));
            return -99L;
        }
    }

    public String getVolumePercent(Context context) {

        try {
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            return audioManager != null ? String.valueOf(audioManager.getStreamVolume(1)) : EnvironmentCompat.MEDIA_UNKNOWN;
        } catch (Throwable th) {
            Log.w(TAG, ExceptionsKt.stackTraceToString(th));
            return EnvironmentCompat.MEDIA_UNKNOWN;
        }
    }

    public float getFontScalePercent(Context context) {

        return context.getResources().getConfiguration().fontScale;
    }

    public String getOperatorType(Context context) {

        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String simOperator = telephonyManager != null ? telephonyManager.getSimOperator() : null;
            if (TextUtils.isEmpty(simOperator)) {
                return EnvironmentCompat.MEDIA_UNKNOWN;
            }
            if (!"46001".equals(simOperator) && !"46006".equals(simOperator) && !"46009".equals(simOperator)) {
                if (!"46000".equals(simOperator) && !"46002".equals(simOperator) && !"46004".equals(simOperator) && !"46007".equals(simOperator)) {
                    if (!"46003".equals(simOperator) && !"46005".equals(simOperator)) {
                        if (!"46011".equals(simOperator)) {
                            return EnvironmentCompat.MEDIA_UNKNOWN;
                        }
                    }
                    return "中国电信";
                }
                return "中国移动";
            }
            return "中国联通";
        } catch (Throwable th) {
            Log.w(TAG, ExceptionsKt.stackTraceToString(th));
            return EnvironmentCompat.MEDIA_UNKNOWN;
        }
    }

    public int isRooted(Context context) {

        int i = cachedIsRooted;
        if (i != EXCEPTION_CODE) {
            return i;
        }
        try {
            boolean isEmulatorByGoogle = isEmulatorByGoogle(context);
            String str = Build.TAGS;
            int i2 = 0;
            if (!isEmulatorByGoogle && str != null && str.contains("test-keys")) {
                cachedIsRooted = 1;
            } else if (new File("/system/app/Superuser.apk").exists()) {
                cachedIsRooted = 1;
            } else {
                File file = new File("/system/xbin/su");
                File file2 = new File("/system/bin/su");
                if (!isEmulatorByGoogle && (file.exists() || file2.exists())) {
                    i2 = 1;
                }
                cachedIsRooted = i2;
            }
        } catch (Throwable th) {
            Log.w(TAG, ExceptionsKt.stackTraceToString(th));
        }
        return cachedIsRooted;
    }

    private boolean isEmulatorByGoogle(Context context) {
        String androidID$default = getAndroidID$default(this, context, false, 2, null);
        if (!"sdk".equals(Build.PRODUCT) && !"google_sdk".equals(Build.PRODUCT)) {
            return androidID$default.isEmpty() || androidID$default.equals(EnvironmentCompat.MEDIA_UNKNOWN);
        }
        return true;
    }

    public int hasOpenDebugMode(Context context) {

        try {
            return Settings.Secure.getInt(context.getContentResolver(), "adb_enabled", 0) > 0 ? 1 : 0;
        } catch (Throwable th) {
            Log.w(TAG, ExceptionsKt.stackTraceToString(th));
            return EXCEPTION_CODE;
        }
    }

    public int isTabletDevice(Context context) {

        return (context.getResources().getConfiguration().screenLayout & 15) >= 3 ? 1 : 0;
    }

    public int getSimCardState(Context context) {

        try {
            Object systemService = context.getSystemService(Context.TELEPHONY_SERVICE);
            TelephonyManager telephonyManager = systemService instanceof TelephonyManager ? (TelephonyManager) systemService : null;
            if (telephonyManager != null) {
                return telephonyManager.getSimState();
            }
            return -1;
        } catch (Throwable th) {
            Log.w(TAG, ExceptionsKt.stackTraceToString(th));
            return EXCEPTION_CODE;
        }
    }

    public String getUiModeType(Context context) {

        try {
            Object systemService = context.getSystemService(Context.UI_MODE_SERVICE);
            UiModeManager uiModeManager = systemService instanceof UiModeManager ? (UiModeManager) systemService : null;
            Integer valueOf = uiModeManager != null ? uiModeManager.getCurrentModeType() : null;
            if (valueOf != null && valueOf == 0) {
                return "UI_MODE_TYPE_UNDEFINED";
            }
            if (valueOf != null && valueOf == 1) {
                return "UI_MODE_TYPE_NORMAL";
            }
            if (valueOf != null && valueOf == 2) {
                return "UI_MODE_TYPE_DESK";
            }
            if (valueOf != null && valueOf == 3) {
                return "UI_MODE_TYPE_CAR";
            }
            if (valueOf != null && valueOf == 4) {
                return "UI_MODE_TYPE_TELEVISION";
            }
            if (valueOf != null && valueOf == 5) {
                return "UI_MODE_TYPE_APPLIANCE";
            }
            if (valueOf != null && valueOf == 6) {
                return "UI_MODE_TYPE_WATCH";
            }
            return valueOf == null ? EnvironmentCompat.MEDIA_UNKNOWN : valueOf == 7 ? "UI_MODE_TYPE_VR_HEADSET" : EnvironmentCompat.MEDIA_UNKNOWN;
        } catch (Throwable th) {
            Log.w(TAG, ExceptionsKt.stackTraceToString(th));
            return EnvironmentCompat.MEDIA_UNKNOWN;
        }
    }

    public long getSDCardTotalSize() {
        try {
            if (!"mounted".equals(Environment.getExternalStorageState())) {
                return -99L;
            }
            StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
            return (statFs.getBlockSizeLong() * statFs.getBlockCountLong()) / 1048576;
        } catch (Throwable th) {
            Log.w(TAG, ExceptionsKt.stackTraceToString(th));
            return -99L;
        }
    }

    public long getSDCardAvailableSize() {
        try {
            if (!"mounted".equals(Environment.getExternalStorageState())) {
                return -99L;
            }
            StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
            return (statFs.getBlockSizeLong() * statFs.getAvailableBlocksLong()) / 1048576;
        } catch (Throwable th) {
            Log.w(TAG, ExceptionsKt.stackTraceToString(th));
            return -99L;
        }
    }

    public int hasPhysicalButtons(Context context) {

        try {
            return (ViewConfiguration.get(context).hasPermanentMenuKey() && KeyCharacterMap.deviceHasKey(4)) ? 1 : 0;
        } catch (Throwable th) {
            Log.w(TAG, ExceptionsKt.stackTraceToString(th));
            return EXCEPTION_CODE;
        }
    }

    public int isMockLocationEnabled(Context context) {

        try {
            return Settings.Secure.getInt(context.getContentResolver(), "mock_location", 0) != 0 ? 1 : 0;
        } catch (Throwable th) {
            Log.w(TAG, ExceptionsKt.stackTraceToString(th));
            return EXCEPTION_CODE;
        }
    }

    public int getPhoneRingMode(Context context) {

        try {
            Object systemService = context.getSystemService(Context.AUDIO_SERVICE);
            AudioManager audioManager = systemService instanceof AudioManager ? (AudioManager) systemService : null;
            if (audioManager != null) {
                return audioManager.getRingerMode();
            }
            return -1;
        } catch (Throwable th) {
            Log.w(TAG, ExceptionsKt.stackTraceToString(th));
            return EXCEPTION_CODE;
        }
    }

    public int isAirplaneModeOn(Context context) {

        try {
            return Settings.Global.getInt(context.getContentResolver(), "airplane_mode_on", 0) != 0 ? 1 : 0;
        } catch (Throwable th) {
            Log.w(TAG, ExceptionsKt.stackTraceToString(th));
            return EXCEPTION_CODE;
        }
    }

    public int getBatteryStatus(Context context) {

        try {
            Object systemService = context.getSystemService(Context.BATTERY_SERVICE);
            BatteryManager batteryManager = systemService instanceof BatteryManager ? (BatteryManager) systemService : null;
            if (batteryManager != null) {
                return batteryManager.getIntProperty(4);
            }
            return -1;
        } catch (Throwable th) {
            Log.w(TAG, ExceptionsKt.stackTraceToString(th));
            return EXCEPTION_CODE;
        }
    }

    public int getBatteryChargingStatus(Context context) {
        return 1;
    }

    public long getAppInstallTimeDiff(Context context) {

        try {
            PackageManager packageManager = context.getPackageManager();
            String packageName = context.getPackageName();
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            return packageInfo.firstInstallTime;
        } catch (Throwable th) {
            Log.w(TAG, ExceptionsKt.stackTraceToString(th));
            return -99L;
        }
    }

    public long getAppUpdateTimeDiff(Context context) {

        try {
            PackageManager packageManager = context.getPackageManager();
            String packageName = context.getPackageName();
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            return packageInfo.lastUpdateTime;
        } catch (Throwable th) {
            Log.w(TAG, ExceptionsKt.stackTraceToString(th));
            return -99L;
        }
    }

    public String getSystemProperty(String name) {
        BufferedReader bufferedReader;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("getprop " + name).getInputStream()), 1024);
            try {
                String readLine = bufferedReader.readLine();
                bufferedReader.close();
                bufferedReader.close();
                return readLine;
            } catch (Throwable unused) {
                bufferedReader.close();
            }
        } catch (Throwable unused2) {
            bufferedReader = null;
        }
        return null;
    }

    public String getDeviceName(Context context) {
        String name;

        try {
            if (Build.VERSION.SDK_INT > 31) {
                name = Settings.Global.getString(context.getContentResolver(), "device_name");
            } else {
                name = Settings.Secure.getString(context.getContentResolver(), "bluetooth_name");
            }
            if (TextUtils.isEmpty(name)) {
                name = EnvironmentCompat.MEDIA_UNKNOWN;
            }
            return name;
        } catch (Throwable th) {
            Log.w(TAG, ExceptionsKt.stackTraceToString(th));
            return EnvironmentCompat.MEDIA_UNKNOWN;
        }
    }

    public String getDeviceBrand() {
        String str = Build.BRAND;
        String str2 = str;
        if (TextUtils.isEmpty(str2)) {
            str = null;
        }
        return str == null ? EnvironmentCompat.MEDIA_UNKNOWN : str;
    }

    public String getDeviceManufacturer() {
        String str = Build.MANUFACTURER;
        String str2 = str;
        if (TextUtils.isEmpty(str2)) {
            str = null;
        }
        return str == null ? EnvironmentCompat.MEDIA_UNKNOWN : str;
    }

    public String getDeviceModel() {
        String str = Build.MODEL;
        String str2 = str;
        if (TextUtils.isEmpty(str2)) {
            str = null;
        }
        return str == null ? EnvironmentCompat.MEDIA_UNKNOWN : str;
    }

    public String getDeviceVersion() {
        String str = Build.VERSION.RELEASE;
        String str2 = str;
        if (TextUtils.isEmpty(str2)) {
            str = null;
        }
        return str == null ? EnvironmentCompat.MEDIA_UNKNOWN : str;
    }

    public String getDeviceBootLoaderVersion() {
        String str = Build.BOOTLOADER;
        String str2 = str;
        if (TextUtils.isEmpty(str2)) {
            str = null;
        }
        return str == null ? EnvironmentCompat.MEDIA_UNKNOWN : str;
    }

    public String getDeviceInternalVersion() {
        String str = Build.DISPLAY;
        String str2 = str;
        if (TextUtils.isEmpty(str2)) {
            str = null;
        }
        return str == null ? EnvironmentCompat.MEDIA_UNKNOWN : str;
    }

    public int getAPILevel() {
        return Build.VERSION.SDK_INT;
    }

    public String getSerial() {
        @SuppressLint("HardwareIds") String str = Build.SERIAL;
        String str2 = str;
        if (TextUtils.isEmpty(str2)) {
            str = null;
        }
        return str == null ? EnvironmentCompat.MEDIA_UNKNOWN : str;
    }

    public String getBoard() {
        String str = Build.BOARD;
        String str2 = str;
        if (TextUtils.isEmpty(str2)) {
            str = null;
        }
        return str == null ? EnvironmentCompat.MEDIA_UNKNOWN : str;
    }

    public String getHardware() {
        String str = Build.HARDWARE;
        String str2 = str;
        if (TextUtils.isEmpty(str2)) {
            str = null;
        }
        return str == null ? EnvironmentCompat.MEDIA_UNKNOWN : str;
    }

    public String getCPUType() {
        String str = Build.CPU_ABI;
        String str2 = str;
        if (TextUtils.isEmpty(str2)) {
            str = null;
        }
        return str == null ? EnvironmentCompat.MEDIA_UNKNOWN : str;
    }

    public String getDeviceType() {
        String str = Build.DEVICE;
        String str2 = str;
        if (TextUtils.isEmpty(str2)) {
            str = null;
        }
        return str == null ? EnvironmentCompat.MEDIA_UNKNOWN : str;
    }

    public String getBuildHost() {
        String str = Build.HOST;
        String str2 = str;
        if (TextUtils.isEmpty(str2)) {
            str = null;
        }
        return str == null ? EnvironmentCompat.MEDIA_UNKNOWN : str;
    }

    public String getProductName() {
        String str = Build.PRODUCT;
        String str2 = str;
        if (TextUtils.isEmpty(str2)) {
            str = null;
        }
        return str == null ? EnvironmentCompat.MEDIA_UNKNOWN : str;
    }

    public String getVersionCodeName() {
        String str = Build.VERSION.CODENAME;
        String str2 = str;
        if (TextUtils.isEmpty(str2)) {
            str = null;
        }
        return str == null ? EnvironmentCompat.MEDIA_UNKNOWN : str;
    }

    public String getBuildTags() {
        String str = Build.TAGS;
        String str2 = str;
        if (TextUtils.isEmpty(str2)) {
            str = null;
        }
        return str == null ? EnvironmentCompat.MEDIA_UNKNOWN : str;
    }

    public String getBuildType() {
        String str = Build.TYPE;
        String str2 = str;
        if (TextUtils.isEmpty(str2)) {
            str = null;
        }
        return str == null ? EnvironmentCompat.MEDIA_UNKNOWN : str;
    }

    public String getBuildUser() {
        String str = Build.USER;
        String str2 = str;
        if (TextUtils.isEmpty(str2)) {
            str = null;
        }
        return str == null ? EnvironmentCompat.MEDIA_UNKNOWN : str;
    }

    public String getBuildTime() {
        return String.valueOf(Build.TIME);
    }
}
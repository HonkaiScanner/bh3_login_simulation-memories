package xyz.hellocraft.login_simulation.utils;

import static android.util.Log.getStackTraceString;
import static xyz.hellocraft.login_simulation.utils.Tools.getString;
import static xyz.hellocraft.login_simulation.utils.Tools.saveString;

import android.content.Context;
import android.os.Build;

import com.github.gzuliyujiang.oaid.DeviceID;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DeviceFpManager {

    private static DeviceFpManager instance;
    private static String deviceFp = null;
    private static boolean refreshFp = false;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private CompletableFuture<String> completableFuture;
    private boolean isRequestInProgress = false;

    private DeviceFpManager() {
    }

    public static synchronized DeviceFpManager getInstance() {
        if (instance == null) {
            instance = new DeviceFpManager();
        }
        return instance;
    }

    public static Map<String, String> getDSPRODHeader(Context context, String body) {

        Map<String, String> baseHeader = Network.getBaseHeader();
        baseHeader.put("x-rpc-app_id", "bll8iq97cem8");
        baseHeader.put("x-rpc-client_type", "2");
        baseHeader.put("x-rpc-device_id", Tools.getDeviceID(context));
        baseHeader.put("x-rpc-device_fp", DeviceFpManager.getInstance().getDeviceFp(context));
        baseHeader.put("x-rpc-device_name", XDeviceUtils.INSTANCE.getDeviceName(context));
        baseHeader.put("x-rpc-device_model", XDeviceUtils.INSTANCE.getDeviceModel());
        baseHeader.put("x-rpc-sys_version", XDeviceUtils.INSTANCE.getDeviceVersion());
        baseHeader.put("x-rpc-game_biz", "bbs_cn");
        baseHeader.put("x-rpc-app_version", "2.107.0");
        baseHeader.put("x-rpc-sdk_version", "2.42.0");
        baseHeader.put("x-rpc-account_version", "2.42.0");
        baseHeader.put("DS", Encrypt.ds2PRODSign(body));
        return baseHeader;
    }

    public synchronized String getDeviceFp(Context context) {

        if (refreshFp && deviceFp != null) {
            Logger.d("DeviceFp", "has cache device_fp: " + deviceFp);
            return deviceFp;
        }
        String tempFp = getString(context, "device_fp");
        if (tempFp.length() < 10) {
            tempFp = "1145141919810";
        }
        if (isRequestInProgress) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Logger.d("DeviceFp", "Request already in progress");
                    return completableFuture.get(); // 如果请求正在进行，等待并返回结果
                }
            } catch (Exception e) {
                Logger.e("DeviceFp", "Error while waiting for device_fp: " + getStackTraceString(e));
                return tempFp;
            }
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            String finalTempFp = tempFp;
            String finalTempFp1 = tempFp;
            completableFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return getDeviceFpNetwork(context, finalTempFp1);
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                    return finalTempFp;
                } finally {
                    isRequestInProgress = false;
                }
            }, executorService);
            try {
                return completableFuture.get();  // 获取并返回结果
            } catch (Exception e) {
                e.printStackTrace();
                return tempFp;
            }
        } else {
            return getDeviceFpNetwork(context, tempFp);
        }


    }

    private String getDeviceFpNetwork(Context context, String tempFp) {

        Logger.d("DeviceFp", "Generating RequestMap");

        String seed_id = getString(context, "fp_seed_id");
        String seed_time = getString(context, "fp_seed_time");
        if (seed_id.length() < 16) {
            seed_id = UUID.randomUUID().toString();
            seed_time = System.currentTimeMillis() + "";
            saveString(context, "fp_seed_id", seed_id);
            saveString(context, "fp_seed_time", seed_time);
        }
        Logger.dd("DeviceFp", "Getting oaid");
        String oaid = getString(context, "oaid");
        if (oaid.length() < 16) {
            oaid = DeviceID.getOAID();
            saveString(context, "oaid", oaid);
        }
        Logger.dd("DeviceFp", "Getting aaid");
        String aaid = getString(context, "aaid");
        if (aaid.length() < 36) {
            aaid = DeviceID.getGUID(context);
            saveString(context, "aaid", aaid);
        }
        Logger.dd("DeviceFp", "Getting vaid");
        String vaid = getString(context, "vaid");
        if (vaid.length() < 16) {
            vaid = DeviceID.getAndroidID(context);
            saveString(context, "vaid", vaid);
        }

        HashMap<String, Object> paramMap = new HashMap<>();

        Logger.dd("DeviceFp", "Getting proxyStatus");
        paramMap.put("proxyStatus", XDeviceUtils.INSTANCE.isProxy());
        Logger.dd("DeviceFp", "Getting isRoot");
        paramMap.put("isRoot", XDeviceUtils.INSTANCE.isRooted(context));
        Logger.dd("DeviceFp", "Getting romCapacity");
        paramMap.put("romCapacity", XDeviceUtils.INSTANCE.getRomCapacity());
        Logger.dd("DeviceFp", "Getting deviceName");
        paramMap.put("deviceName", XDeviceUtils.INSTANCE.getDeviceName(context));
        Logger.dd("DeviceFp", "Getting productName");
        paramMap.put("productName", XDeviceUtils.INSTANCE.getProductName());
        Logger.dd("DeviceFp", "Getting romRemain");
        paramMap.put("romRemain", XDeviceUtils.INSTANCE.getRomRemain());
        Logger.dd("DeviceFp", "Getting hostname");
        paramMap.put("hostname", XDeviceUtils.INSTANCE.getBuildHost());
        Logger.dd("DeviceFp", "Getting screenSize");
        paramMap.put("screenSize", XDeviceUtils.INSTANCE.getScreenSize(context));
        Logger.dd("DeviceFp", "Getting isTablet");
        paramMap.put("isTablet", XDeviceUtils.INSTANCE.isTabletDevice(context));
        paramMap.put("aaid", aaid);
        Logger.dd("DeviceFp", "Getting model");
        paramMap.put("model", XDeviceUtils.INSTANCE.getDeviceModel());
        Logger.dd("DeviceFp", "Getting brand");
        paramMap.put("brand", XDeviceUtils.INSTANCE.getDeviceBrand());
        Logger.dd("DeviceFp", "Getting hardware");
        paramMap.put("hardware", XDeviceUtils.INSTANCE.getHardware());
        Logger.dd("DeviceFp", "Getting deviceType");
        paramMap.put("deviceType", XDeviceUtils.INSTANCE.getDeviceType());
        Logger.dd("DeviceFp", "Getting devId");
        paramMap.put("devId", XDeviceUtils.INSTANCE.getVersionCodeName());
        Logger.dd("DeviceFp", "Getting serialNumber");
        paramMap.put("serialNumber", XDeviceUtils.INSTANCE.getSerial());
        Logger.dd("DeviceFp", "Getting sdCapacity");
        paramMap.put("sdCapacity", XDeviceUtils.INSTANCE.getSDCardTotalSize());
        Logger.dd("DeviceFp", "Getting buildTime");
        paramMap.put("buildTime", XDeviceUtils.INSTANCE.getBuildTime());
        Logger.dd("DeviceFp", "Getting buildUser");
        paramMap.put("buildUser", XDeviceUtils.INSTANCE.getBuildUser());
        Logger.dd("DeviceFp", "Getting simState");
        paramMap.put("simState", XDeviceUtils.INSTANCE.getSimCardState(context));
        Logger.dd("DeviceFp", "Getting ramRemain");
        paramMap.put("ramRemain", XDeviceUtils.bytesToMegabytes(XDeviceUtils.INSTANCE.getRomSpaceRemaining()));
        Logger.dd("DeviceFp", "Getting appUpdateTimeDiff");
        paramMap.put("appUpdateTimeDiff", XDeviceUtils.INSTANCE.getAppUpdateTimeDiff(context));
        Logger.dd("DeviceFp", "Getting deviceInfo");
        paramMap.put("deviceInfo", XDeviceUtils.INSTANCE.getDeviceInfo());
        paramMap.put("vaid", vaid);
        Logger.dd("DeviceFp", "Getting buildType");
        paramMap.put("buildType", XDeviceUtils.INSTANCE.getBuildType());
        Logger.dd("DeviceFp", "Getting sdkVersion");
        paramMap.put("sdkVersion", XDeviceUtils.INSTANCE.getAPILevel());
        Logger.dd("DeviceFp", "Getting ui_mode");
        paramMap.put("ui_mode", XDeviceUtils.INSTANCE.getUiModeType(context));
        Logger.dd("DeviceFp", "Getting isMockLocation");
        paramMap.put("isMockLocation", XDeviceUtils.INSTANCE.isMockLocationEnabled(context));
        Logger.dd("DeviceFp", "Getting cpuType");
        paramMap.put("cpuType", XDeviceUtils.INSTANCE.getCPUType());
        Logger.dd("DeviceFp", "Getting isAirMode");
        paramMap.put("isAirMode", XDeviceUtils.INSTANCE.isAirplaneModeOn(context));
        Logger.dd("DeviceFp", "Getting ringMode");
        paramMap.put("ringMode", XDeviceUtils.INSTANCE.getPhoneRingMode(context));
        Logger.dd("DeviceFp", "Getting chargeStatus");
        paramMap.put("chargeStatus", XDeviceUtils.INSTANCE.getBatteryChargingStatus(context));
        Logger.dd("DeviceFp", "Getting manufacturer");
        paramMap.put("manufacturer", XDeviceUtils.INSTANCE.getDeviceManufacturer());
        Logger.dd("DeviceFp", "Getting emulatorStatus");
        paramMap.put("emulatorStatus", XDeviceUtils.INSTANCE.isEmulator(context));
        Logger.dd("DeviceFp", "Getting appMemory");
        paramMap.put("appMemory", XDeviceUtils.INSTANCE.getAppMemory());
        Logger.dd("DeviceFp", "Getting osVersion");
        paramMap.put("osVersion", XDeviceUtils.INSTANCE.getDeviceVersion());
        Logger.dd("DeviceFp", "Getting vendor");
        paramMap.put("vendor", XDeviceUtils.INSTANCE.getOperatorType(context));
        Logger.dd("DeviceFp", "Getting accelerometer");
        paramMap.put("accelerometer", XDeviceUtils.INSTANCE.syncGetSensorValues(context, 1));
        Logger.dd("DeviceFp", "Getting sdRemain");
        paramMap.put("sdRemain", XDeviceUtils.INSTANCE.getSDCardAvailableSize());
        Logger.dd("DeviceFp", "Getting buildTags");
        paramMap.put("buildTags", XDeviceUtils.INSTANCE.getBuildTags());
        paramMap.put("packageName", "com.miHoYo.enterprise.NGHSoD");
        Logger.dd("DeviceFp", "Getting networkType");
        paramMap.put("networkType", XDeviceUtils.INSTANCE.getNetworkType(context));
        paramMap.put("oaid", oaid);
        Logger.dd("DeviceFp", "Getting debugStatus");
        paramMap.put("debugStatus", XDeviceUtils.INSTANCE.hasOpenDebugMode(context));
        Logger.dd("DeviceFp", "Getting ramCapacity");
        paramMap.put("ramCapacity", XDeviceUtils.bytesToMegabytes(XDeviceUtils.INSTANCE.getTotalRomSpace()));
        Logger.dd("DeviceFp", "Getting magnetometer");
        paramMap.put("magnetometer", XDeviceUtils.INSTANCE.syncGetSensorValues(context, 2));
        Logger.dd("DeviceFp", "Getting display");
        paramMap.put("display", XDeviceUtils.INSTANCE.getDeviceInternalVersion());
        Logger.dd("DeviceFp", "Getting appInstallTimeDiff");
        paramMap.put("appInstallTimeDiff", XDeviceUtils.INSTANCE.getAppInstallTimeDiff(context));
        paramMap.put("packageVersion", "2.28.0");
        Logger.dd("DeviceFp", "Getting gyroscope");
        paramMap.put("gyroscope", XDeviceUtils.INSTANCE.syncGetSensorValues(context, 4));
        Logger.dd("DeviceFp", "Getting batteryStatus");
        paramMap.put("batteryStatus", XDeviceUtils.INSTANCE.getBatteryStatus(context));
        Logger.dd("DeviceFp", "Getting hasKeyboard");
        paramMap.put("hasKeyboard", XDeviceUtils.INSTANCE.hasPhysicalButtons(context));
        Logger.dd("DeviceFp", "Getting board");
        paramMap.put("board", XDeviceUtils.INSTANCE.getBoard());

        Logger.d("DeviceFp", "RequestMap Generated");

        JSONObject getFpParam = new JSONObject();

        try {
            getFpParam.put("device_id", Tools.getDeviceID(context));
            getFpParam.put("seed_id", seed_id);
            getFpParam.put("seed_time", seed_time);
            getFpParam.put("platform", "2");
            getFpParam.put("device_fp", tempFp);
            getFpParam.put("app_name", "bh3_cn");
            getFpParam.put("ext_fields", new JSONObject(paramMap).toString());

            Logger.d("DeviceFp", "Requesting device_fp");
            String feedback = Network.sendPost("https://public-data-api.mihoyo.com/device-fp/api/getFp", getFpParam.toString(), 2);
            Logger.d("DeviceFp", "device_fp response: " + feedback);
            JSONObject feedbackJSON = new JSONObject(feedback);
            if (feedbackJSON.getInt("retcode") == 0) {
                deviceFp = feedbackJSON.getJSONObject("data").getString("device_fp");
                saveString(context, "device_fp", deviceFp);
                refreshFp = true;
            }

        } catch (Exception ignore) {
        }


        return tempFp;
    }
}

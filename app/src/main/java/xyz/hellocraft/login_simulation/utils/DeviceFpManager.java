package xyz.hellocraft.login_simulation.utils;

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
        baseHeader.put("x-rpc-app_version", "2.73.1");
        baseHeader.put("x-rpc-sdk_version", "2.20.2");
        baseHeader.put("x-rpc-account_version", "2.20.2");
        baseHeader.put("DS", Encrypt.ds2PRODSign(body));
        return baseHeader;
    }

    public synchronized String getDeviceFp(Context context) {

        if (refreshFp && deviceFp != null) {
            return deviceFp;
        }
        String tempFp = getString(context, "device_fp");
        if (tempFp.length() < 10) {
            tempFp = "1145141919810";
        }
        if (isRequestInProgress) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    return completableFuture.get(); // 如果请求正在进行，等待并返回结果
                }
            } catch (Exception e) {
                e.printStackTrace();
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

        String seed_id = getString(context, "fp_seed_id");
        String seed_time = getString(context, "fp_seed_time");
        if (seed_id.length() < 16) {
            seed_id = UUID.randomUUID().toString();
            seed_time = System.currentTimeMillis() + "";
            saveString(context, "fp_seed_id", seed_id);
            saveString(context, "fp_seed_time", seed_time);
        }
        String oaid = getString(context, "oaid");
        if (oaid.length() < 16) {
            oaid = DeviceID.getOAID();
            saveString(context, "oaid", oaid);
        }
        String aaid = getString(context, "aaid");
        if (aaid.length() < 36) {
            aaid = DeviceID.getGUID(context);
            saveString(context, "aaid", aaid);
        }
        String vaid = getString(context, "vaid");
        if (vaid.length() < 16) {
            vaid = DeviceID.getAndroidID(context);
            saveString(context, "vaid", vaid);
        }

        HashMap<String, Object> paramMap = new HashMap<>();

        paramMap.put("proxyStatus", XDeviceUtils.INSTANCE.isProxy());
        paramMap.put("isRoot", XDeviceUtils.INSTANCE.isRooted(context));
        paramMap.put("romCapacity", XDeviceUtils.INSTANCE.getRomCapacity());
        paramMap.put("deviceName", XDeviceUtils.INSTANCE.getDeviceName(context));
        paramMap.put("productName", XDeviceUtils.INSTANCE.getProductName());
        paramMap.put("romRemain", XDeviceUtils.INSTANCE.getRomRemain());
        paramMap.put("hostname", XDeviceUtils.INSTANCE.getBuildHost());
        paramMap.put("screenSize", XDeviceUtils.INSTANCE.getScreenSize(context));
        paramMap.put("isTablet", XDeviceUtils.INSTANCE.isTabletDevice(context));
        paramMap.put("aaid", aaid);
        paramMap.put("model", XDeviceUtils.INSTANCE.getDeviceModel());
        paramMap.put("brand", XDeviceUtils.INSTANCE.getDeviceBrand());
        paramMap.put("hardware", XDeviceUtils.INSTANCE.getHardware());
        paramMap.put("deviceType", XDeviceUtils.INSTANCE.getDeviceType());
        paramMap.put("devId", XDeviceUtils.INSTANCE.getVersionCodeName());
        paramMap.put("serialNumber", XDeviceUtils.INSTANCE.getSerial());
        paramMap.put("sdCapacity", XDeviceUtils.INSTANCE.getSDCardTotalSize());
        paramMap.put("buildTime", XDeviceUtils.INSTANCE.getBuildTime());
        paramMap.put("buildUser", XDeviceUtils.INSTANCE.getBuildUser());
        paramMap.put("simState", XDeviceUtils.INSTANCE.getSimCardState(context));
        paramMap.put("ramRemain", XDeviceUtils.bytesToMegabytes(XDeviceUtils.INSTANCE.getRomSpaceRemaining()));
        paramMap.put("appUpdateTimeDiff", XDeviceUtils.INSTANCE.getAppUpdateTimeDiff(context));
        paramMap.put("deviceInfo", XDeviceUtils.INSTANCE.getDeviceInfo());
        paramMap.put("vaid", vaid);
        paramMap.put("buildType", XDeviceUtils.INSTANCE.getBuildType());
        paramMap.put("sdkVersion", XDeviceUtils.INSTANCE.getAPILevel());
        paramMap.put("ui_mode", XDeviceUtils.INSTANCE.getUiModeType(context));
        paramMap.put("isMockLocation", XDeviceUtils.INSTANCE.isMockLocationEnabled(context));
        paramMap.put("cpuType", XDeviceUtils.INSTANCE.getCPUType());
        paramMap.put("isAirMode", XDeviceUtils.INSTANCE.isAirplaneModeOn(context));
        paramMap.put("ringMode", XDeviceUtils.INSTANCE.getPhoneRingMode(context));
        paramMap.put("chargeStatus", XDeviceUtils.INSTANCE.getBatteryChargingStatus(context));
        paramMap.put("manufacturer", XDeviceUtils.INSTANCE.getDeviceManufacturer());
        paramMap.put("emulatorStatus", XDeviceUtils.INSTANCE.isEmulator(context));
        paramMap.put("appMemory", XDeviceUtils.INSTANCE.getAppMemory());
        paramMap.put("osVersion", XDeviceUtils.INSTANCE.getDeviceVersion());
        paramMap.put("vendor", XDeviceUtils.INSTANCE.getOperatorType(context));
        paramMap.put("accelerometer", XDeviceUtils.INSTANCE.syncGetSensorValues(context, 1));
        paramMap.put("sdRemain", XDeviceUtils.INSTANCE.getSDCardAvailableSize());
        paramMap.put("buildTags", XDeviceUtils.INSTANCE.getBuildTags());
        paramMap.put("packageName", "com.miHoYo.enterprise.NGHSoD");
        paramMap.put("networkType", XDeviceUtils.INSTANCE.getNetworkType(context));
        paramMap.put("oaid", oaid);
        paramMap.put("debugStatus", XDeviceUtils.INSTANCE.hasOpenDebugMode(context));
        paramMap.put("ramCapacity", XDeviceUtils.bytesToMegabytes(XDeviceUtils.INSTANCE.getTotalRomSpace()));
        paramMap.put("magnetometer", XDeviceUtils.INSTANCE.syncGetSensorValues(context, 2));
        paramMap.put("display", XDeviceUtils.INSTANCE.getDeviceInternalVersion());
        paramMap.put("appInstallTimeDiff", XDeviceUtils.INSTANCE.getAppInstallTimeDiff(context));
        paramMap.put("packageVersion", "2.28.0");
        paramMap.put("gyroscope", XDeviceUtils.INSTANCE.syncGetSensorValues(context, 4));
        paramMap.put("batteryStatus", XDeviceUtils.INSTANCE.getBatteryStatus(context));
        paramMap.put("hasKeyboard", XDeviceUtils.INSTANCE.hasPhysicalButtons(context));
        paramMap.put("board", XDeviceUtils.INSTANCE.getBoard());

        JSONObject getFpParam = new JSONObject();

        try {
            getFpParam.put("device_id", Tools.getDeviceID(context));
            getFpParam.put("seed_id", seed_id);
            getFpParam.put("seed_time", seed_time);
            getFpParam.put("platform", "2");
            getFpParam.put("device_fp", tempFp);
            getFpParam.put("app_name", "bh3_cn");
            getFpParam.put("ext_fields", new JSONObject(paramMap).toString());

            String feedback = Network.sendPost("https://public-data-api.mihoyo.com/device-fp/api/getFp", getFpParam.toString());
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

package com.github.haocen2004.login_simulation.util;

/**
 * 常量
 */
public class Constant {
    // request参数
    public static final int REQ_QR_CODE = 11002; // 打开扫描界面请求码
    public static final int REQ_PERM_CAMERA = 11003; // 打开摄像头
    public static final int REQ_PERM_EXTERNAL_STORAGE = 11004; // 读写文件
    public static final int REQ_CODE_SCAN_GALLERY = 11005; // 直接相册扫码
    public static final int REQ_PERM_WINDOW = 11006;  // 悬浮窗
    public static final int REQ_PERM_RECORD = 11007;  // 屏幕捕获
    public static final int BAG_ALTER_NOTIFICATION = 12001;  // 屏幕捕获后台通知

    public static final String INTENT_EXTRA_KEY_QR_SCAN = "qr_scan_result";
    public static final String BH_APP_KEY = "0ebc517adb1b62c6b408df153331f9aa";
    public static final String BH_PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDDvekdPMHN3AYhm/vktJT+YJr7cI5DcsNKqdsx5DZX0gDuWFuIjzdwButrIYPNmRJ1G8ybDIF7oDW2eEpm5sMbL9zs\n9ExXCdvqrn51qELbqj0XxtMTIpaCHFSI50PfPpTFV9Xt/hmyVwokoOXFlAEgCn+Q\nCgGs52bFoYMtyi+xEQIDAQAB\n";
    //    public static final String BH_APP_KEY = "86d0629f8498e55344a68d9ac1ee2c34";
    public static final String BILI_APP_KEY = "dbf8f1b4496f430b8a3c0f436a35b931";
    public static final String VIVO_APP_KEY = "94c93e8ac604d1909943862f12803ac9";

    public static String SP_URL = "https://vmh6lryy.lc-cn-n1-shared.com";
    public static String BH_VER = "4.7.0";
    public static String MDK_VERSION = "1.17.1";
    public static String OFFICIAL_TYPE = "android01";
    public static boolean CHECK_VER = true;
    public static boolean HAS_ACCOUNT = false;
}

package com.github.haocen2004.login_simulation.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import static com.github.haocen2004.login_simulation.util.Constant.BH_APP_KEY;
import static com.github.haocen2004.login_simulation.util.Network.sendPost;

public class Tools {

    public static String getOAServer(RoleData roleData) {
//        http://106.14.51.73/query_gameserver?version=4.2.0_gf_pc&t=1598631898&uid=21097880
//        https://global2.bh3.com/query_dispatch  version=4.2.0_gf_android_xiaomi
//        https://global1.bh3.com/query_dispatch?version=4.2.0_gf_pc&t=1598673811
        try {
            String feedback = sendPost("https://global2.bh3.com/query_dispatch?version=" + roleData.getOa_req_key() + "&t=" + System.currentTimeMillis(), "");
            System.out.println(feedback);
            JSONObject json1 = new JSONObject(feedback);
            JSONArray jsonArray = json1.getJSONArray("region_list");
            JSONObject json2 = jsonArray.getJSONObject(0);
            String url = json2.getString("dispatch_url");
            feedback = sendPost(url + "?version=" + roleData.getOa_req_key() + "&t=" + System.currentTimeMillis(), "");
            System.out.println(feedback);
            return feedback;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void openUrl(String url, Activity activity) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.setData(uri);
        activity.startActivity(intent);
    }

    public static String bh3Sign(Map<String, Object> paramMap) {
        ArrayList<Comparable> arrayList = new ArrayList(paramMap.keySet());
        Collections.sort(arrayList);
        StringBuilder stringBuilder = new StringBuilder();
        for (Comparable str : arrayList) {
            stringBuilder.append(str);
            stringBuilder.append("=");
            stringBuilder.append(paramMap.get(str));
            stringBuilder.append("&");
        }
        return sha256HMAC(stringBuilder.toString().substring(0, stringBuilder.length() - 1), BH_APP_KEY);
    }

    public static String getDeviceID(Context paramContext) {
        String str = getString(paramContext, "device_id");
        if (TextUtils.isEmpty(str)) {
            str = Settings.System.getString(paramContext.getContentResolver(), "android_id");
            if (TextUtils.isEmpty(str)) {
                str = getDeviceModel() +
                        getSystemVersion() +
                        System.currentTimeMillis();
            }
            saveString(paramContext, "device_id", str);
        }
        return str;
    }

    public static String getDeviceModel() {
        return Build.MODEL;
    }

    public static String getSystemVersion() {
        return Build.VERSION.RELEASE;
    }
    private static String byteArrayToHexString(byte[] paramArrayOfbyte) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b = 0; paramArrayOfbyte != null && b < paramArrayOfbyte.length; b++) {
            String str = Integer.toHexString(0xFF & paramArrayOfbyte[b]);
            if (str.length() == 1)
                stringBuilder.append('0');
            stringBuilder.append(str);
        }
        return stringBuilder.toString().toLowerCase();
    }

    public static String sha256HMAC(String paramString1, String paramString2) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("sha256HMAC:");
        stringBuilder.append(paramString1);
        stringBuilder.append(" secret ");
        stringBuilder.append(paramString2);
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(paramString2.getBytes(), "HmacSHA256"));
            return byteArrayToHexString(mac.doFinal(paramString1.getBytes()));
        } catch (Exception exception) {
            return "";
        }
    }
    private static String name = "mihoyo_sdk_preference";

    public static String getString(Context paramContext, String paramString) {
        return paramContext.getSharedPreferences(name, 0).getString(paramString, "");
    }

    public static boolean saveString(Context paramContext, String paramString1, String paramString2) {
        return paramContext.getSharedPreferences(name, 0).edit().putString(paramString1, paramString2).commit();
    }

    public static String encryptByPublicKey(String paramString1, String paramString2) {
        try {
            return encode(encryptByPublicKey(paramString1.getBytes(), decode(paramString2)));
        } catch (Exception exception) {
            exception.printStackTrace();
            return "";
        }
    }

    public static byte[] encryptByPublicKey(byte[] paramArrayOfbyte1, byte[] paramArrayOfbyte2) throws Exception {
        KeyFactory keyFactory;
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(paramArrayOfbyte2);
        if (Build.VERSION.SDK_INT >= 28) {
            keyFactory = KeyFactory.getInstance("RSA");
        } else {
            keyFactory = KeyFactory.getInstance("RSA", "BC");
        }
        PublicKey publicKey = keyFactory.generatePublic(x509EncodedKeySpec);
        Cipher cipher = Cipher.getInstance("RSA/None/PKCS1Padding");
        cipher.init(1, publicKey);
        return cipher.doFinal(paramArrayOfbyte1);
    }
    private static byte[] base64DecodeChars;

    private static char[] base64EncodeChars = new char[] {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
            'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
            'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd',
            'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
            'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
            'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', '+', '/' };

    static {
        base64DecodeChars = new byte[] {
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, 62, -1, -1, -1, 63, 52, 53,
                54, 55, 56, 57, 58, 59, 60, 61, -1, -1,
                -1, -1, -1, -1, -1, 0, 1, 2, 3, 4,
                5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
                15, 16, 17, 18, 19, 20, 21, 22, 23, 24,
                25, -1, -1, -1, -1, -1, -1, 26, 27, 28,
                29, 30, 31, 32, 33, 34, 35, 36, 37, 38,
                39, 40, 41, 42, 43, 44, 45, 46, 47, 48,
                49, 50, 51, -1, -1, -1, -1, -1 };
    }

    public static byte[] decode(String paramString) {
        try {
            return decodePrivate(paramString);
        } catch (UnsupportedEncodingException unsupportedEncodingException) {
            unsupportedEncodingException.printStackTrace();
            return new byte[0];
        }
    }

    private static byte[] decodePrivate(String paramString) throws UnsupportedEncodingException {
        StringBuilder stringBuffer = new StringBuilder();
        byte[] arrayOfByte = paramString.getBytes(StandardCharsets.US_ASCII);
        int i = arrayOfByte.length;
        int j = 0;
        while (j < i) {
            int k;
            byte b1;
            byte b2;
            while (true) {
                byte[] arrayOfByte1 = base64DecodeChars;
                k = j + 1;
                b1 = arrayOfByte1[arrayOfByte[j]];
                if (k >= i || b1 != -1)
                    break;
                j = k;
            }
            if (b1 == -1)
                break;
            while (true) {
                byte[] arrayOfByte1 = base64DecodeChars;
                j = k + 1;
                b2 = arrayOfByte1[arrayOfByte[k]];
                if (j >= i || b2 != -1)
                    break;
                k = j;
            }
            if (b2 == -1)
                break;
            stringBuffer.append((char)(b1 << 2 | (b2 & 0x30) >>> 4));

            for (k = j;; k = j) {
                j = k + 1;
                k = arrayOfByte[k];
                if (k == 61)
                    return stringBuffer.toString().getBytes("iso8859-1");
                b1 = base64DecodeChars[k];
                if (j >= i || b1 != -1)
                    break;
            }
            if (b1 == -1)
                break;
            stringBuffer.append((char)((b2 & 0xF) << 4 | (b1 & 0x3C) >>> 2));
            for (k = j;; k = j) {
                j = k + 1;
                k = arrayOfByte[k];
                if (k == 61)
                    return stringBuffer.toString().getBytes("iso8859-1");
                k = base64DecodeChars[k];
                if (j >= i || k != -1)
                    break;
            }
            if (k == -1)
                break;
            stringBuffer.append((char)(k | (b1 & 0x3) << 6));
        }
        return stringBuffer.toString().getBytes("iso8859-1");
    }

    public static String encode(byte[] paramArrayOfbyte) {
        StringBuilder stringBuffer = new StringBuilder();
        int i = paramArrayOfbyte.length;
        for (int j = 0; j < i; j++) {
            int k = j + 1;
            int m = paramArrayOfbyte[j] & 0xFF;
            if (k == i) {
                stringBuffer.append(base64EncodeChars[m >>> 2]);
                stringBuffer.append(base64EncodeChars[(m & 0x3) << 4]);
                stringBuffer.append("==");
                break;
            }
            j = k + 1;
            k = paramArrayOfbyte[k] & 0xFF;
            if (j == i) {
                stringBuffer.append(base64EncodeChars[m >>> 2]);
                stringBuffer.append(base64EncodeChars[(m & 0x3) << 4 | (k & 0xF0) >>> 4]);
                stringBuffer.append(base64EncodeChars[(k & 0xF) << 2]);
                stringBuffer.append("=");
                break;
            }
            int n = paramArrayOfbyte[j] & 0xFF;
            stringBuffer.append(base64EncodeChars[m >>> 2]);
            stringBuffer.append(base64EncodeChars[(m & 0x3) << 4 | (k & 0xF0) >>> 4]);
            stringBuffer.append(base64EncodeChars[(k & 0xF) << 2 | (n & 0xC0) >>> 6]);
            stringBuffer.append(base64EncodeChars[n & 0x3F]);
        }
        return stringBuffer.toString();
    }
}

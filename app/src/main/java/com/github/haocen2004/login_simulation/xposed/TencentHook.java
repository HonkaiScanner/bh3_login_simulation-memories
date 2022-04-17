package com.github.haocen2004.login_simulation.xposed;

import android.app.Activity;
import android.os.Bundle;

import com.qq.taf.jce.HexUtil;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class TencentHook {
    public static String[] a() throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        String signature = "";
        String sb2 = String.valueOf(System.currentTimeMillis() / 1000);
        String[] strArr = {"", "", ""};
        String raw_signature = "6203756aea8a1b7d35bb0222a2444ec3";
        String package_name = "com.tencent.tmgp.bh3";
        try {
//            messageDigest.reset();
            messageDigest.update((package_name + "_" + raw_signature + "_" + sb2).getBytes());
            signature = HexUtil.bytes2HexStr(messageDigest.digest());
        } catch (Exception e3) {
            XposedBridge.log(e3);
        }
        strArr[0] = raw_signature;
        strArr[1] = signature;
        strArr[2] = sb2;
        return strArr;
    }

    public TencentHook(XC_LoadPackage.LoadPackageParam lpparam) {

        Class<?> clazz = XposedHelpers.findClass("com.tencent.open.virtual.OpenSdkVirtualUtil", lpparam.classLoader);
        Class<?> clazz2 = XposedHelpers.findClass("com.tencent.open.agent.strategy.SSOLoginAction", lpparam.classLoader);
        Class<?> clazz3 = XposedHelpers.findClass("com.tencent.open.agent.util.AuthorityUtil", lpparam.classLoader);

        try {
            XposedHelpers.findAndHookMethod(clazz3, "c", Activity.class, new XC_MethodHook() {

                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    String package_name = param.getResult().toString();
                    XposedBridge.log("hooked AuthorityUtil.c,package_name:" + package_name);
                    if (package_name.contains("com.github.haocen2004.bh3_login_simulation")) {
                        param.setResult("com.tencent.tmgp.bh3");
                    }
                }
            });
        } catch (Exception e) {
            XposedBridge.log(e);
        }
        try {
            XposedHelpers.findAndHookMethod(clazz2, "d", Bundle.class, String.class, new XC_MethodHook() {

                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    String package_name = param.getResult().toString();
                    XposedBridge.log("hooked SSOLoginAction.d,package_name:" + package_name);
                    if (package_name.contains("com.github.haocen2004.bh3_login_simulation")) {
                        param.setResult("com.tencent.tmgp.bh3");
                    }
                }
            });
        } catch (Exception e) {
            XposedBridge.log(e);
        }
        XposedHelpers.findAndHookMethod(clazz, "a", String.class, new XC_MethodHook() {

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                String package_name = param.args[0].toString();
                XposedBridge.log("hooked OpenSdkVirtualUtil.a,package_name:" + package_name);
                String[] strings = (String[]) param.getResult();
                for (String string : strings) {
                    XposedBridge.log("old:" + string);
                }
                if (strings[0].equals("87870b21663deebd4f0ad96e6ac5414d") || package_name.contains("com.github.haocen2004.bh3_login_simulation") || package_name.contains("com.tencent.tmgp.bh3")) {
                    XposedBridge.log("replace need,param: " + package_name);
                    String[] new_s = a();
                    for (String string : new_s) {
                        XposedBridge.log("new:" + string);
                    }
                    param.setResult(new_s);
                    XposedBridge.log("replaced.");


                }
//
//ret:87870b21663deebd4f0ad96e6ac5414d
//ret:27AC59E4DF2A6446FD2E3619DF7BB46F
//ret:1650184540
                //C72B17A4ED30C124D45204A58D358BDF, timestr=1650184588
                //21D1144D6FED4D26C303DF19C9F8A778
            }
        });


    }


}
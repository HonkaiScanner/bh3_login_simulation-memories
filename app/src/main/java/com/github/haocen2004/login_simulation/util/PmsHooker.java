package com.github.haocen2004.login_simulation.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Locale;

// code from https://github.com/fourbrother/HookPmsSignature/blob/master/src/cn/wjdiankong/hookpms/PmsHookBinderInvocationHandler.java

public class PmsHooker implements InvocationHandler {
    private Object base;

    public static void startHook(Context context) {
        try {

            Log.d("PMSHook", "start to hook");
            // 获取全局的ActivityThread对象
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Method currentActivityThreadMethod =
                    activityThreadClass.getDeclaredMethod("currentActivityThread");
            Object currentActivityThread = currentActivityThreadMethod.invoke(null);
            // 获取ActivityThread里面原始的sPackageManager
            Field sPackageManagerField = activityThreadClass.getDeclaredField("sPackageManager");
            sPackageManagerField.setAccessible(true);
            Object sPackageManager = sPackageManagerField.get(currentActivityThread);
            // 准备好代{过}{滤}理对象, 用来替换原始的对象
            Class<?> iPackageManagerInterface = Class.forName("android.content.pm.IPackageManager");
            Object proxy = Proxy.newProxyInstance(
                    iPackageManagerInterface.getClassLoader(),
                    new Class<?>[]{iPackageManagerInterface},
                    new PmsHooker(sPackageManager, 0));
            // 1. 替换掉ActivityThread里面的 sPackageManager 字段
            sPackageManagerField.set(currentActivityThread, proxy);
            // 2. 替换 ApplicationPackageManager里面的 mPM对象
            PackageManager pm = context.getPackageManager();
            Field mPmField = pm.getClass().getDeclaredField("mPM");
            mPmField.setAccessible(true);
            mPmField.set(pm, proxy);

        } catch (Exception e) {
            Log.d("PMSHook", "pms hook failed.");
        }

    }

    public PmsHooker(Object base, int hashCode) {
        try {
            this.base = base;
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("PMSHook", "error:" + e.getMessage());
        }
    }

    public static String getPackageNameFilter(String rawPackageName) {
        StackTraceElement[] arr = Thread.currentThread().getStackTrace();
        boolean oppoChange = false;
        boolean forceKeep = false;
        for (StackTraceElement el : arr) {
            String className = el.getClassName().toLowerCase(Locale.ROOT);
            if (className.contains("nearme")) {
                oppoChange = true;
            }
            if (className.contains("oppo")) {
                oppoChange = true;
            }
            if (className.contains("heytap")) {
                oppoChange = true;
            }
            if (className.contains("intent")) {
                forceKeep = true;
            }
        }
        if (oppoChange && !forceKeep) {
            return "com.miHoYo.bh3.nearme.gamecenter";
        }
        return rawPackageName;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {


        if ("getApplicationInfo".equals(method.getName()) && args != null && args.length > 0) {
            Log.d("PMSHook", method.getName() + ": " + args[0]);
        } else {
            Log.d("PMSHook", method.getName());
        }
        StackTraceElement[] arr = Thread.currentThread().getStackTrace();
        for (StackTraceElement el : arr) {
            String className = el.getClassName();
            if (className.contains("nearme") || className.contains("heytap") || className.contains("oppo")) {
                if ("getApplicationInfo".equals(method.getName())) {
                    if (args != null && args[0].equals("com.miHoYo.bh3.nearme.gamecenter")) {
                        args[0] = "com.github.haocen2004.bh3_login_simulation";
                    }
                    return replace(method, args, "com.miHoYo.bh3.nearme.gamecenter");
                }
                if ("getPackageInfo".equals(method.getName())) {
                    Log.d("PMSHook", (String) args[0]);
                    if (args[0].equals("com.miHoYo.bh3.nearme.gamecenter")) {
                        args[0] = "com.github.haocen2004.bh3_login_simulation";
                        return oppoReplace(method, args, true);
                    }

//                Log.d("stackTrace","at "+el.getClassName()+"\t" +el.getMethodName()+"\t"+el.getLineNumber());

                }
            }
        }
        return method.invoke(base, args);
    }

    private ApplicationInfo replace(Method method, Object[] args, String newPackageName) throws InvocationTargetException, IllegalAccessException {
        ApplicationInfo info = (ApplicationInfo) method.invoke(base, args);
        if (info != null) {
            String oldName = info.packageName;
            info.packageName = newPackageName;
            Log.d("PMSHook", "replace packageName from " + oldName + " to " + info.packageName);
        }
        return info;
    }

    private PackageInfo replace(Method method, Object[] args, String newPackageName, String newSign, boolean includeSign) throws InvocationTargetException, IllegalAccessException {
        PackageInfo info = (PackageInfo) method.invoke(base, args);
        if (info != null) {
            if (includeSign) {
                Signature sign = new Signature(newSign);
                info.signatures[0] = sign;
            }
            String oldName = info.packageName;
            info.packageName = newPackageName;
            Log.d("PMSHook", "replace packageName from " + oldName + " to " + info.packageName);
        }
        return info;
    }

    private PackageInfo oppoReplace(Method method, Object[] args, boolean includeSign) throws InvocationTargetException, IllegalAccessException {

        String Sign = "MIICPzCCAaigAwIBAgIETs3gMjANBgkqhkiG9w0BAQUFADBjMQswCQYDVQQGEwI4NjESMBAGA1UE\n" +
                "CBMJZ3Vhbmdkb25nMREwDwYDVQQHEwhzaGVuemhlbjENMAsGA1UEChMEb3BwbzENMAsGA1UECxME\n" +
                "b3BwbzEPMA0GA1UEAxMGbmVhcm1lMCAXDTExMTEyNDA2MTIwMloYDzIwNjYwODI3MDYxMjAyWjBj\n" +
                "MQswCQYDVQQGEwI4NjESMBAGA1UECBMJZ3Vhbmdkb25nMREwDwYDVQQHEwhzaGVuemhlbjENMAsG\n" +
                "A1UEChMEb3BwbzENMAsGA1UECxMEb3BwbzEPMA0GA1UEAxMGbmVhcm1lMIGfMA0GCSqGSIb3DQEB\n" +
                "AQUAA4GNADCBiQKBgQCZR4BtPo+jrI8rA8gLr5QMhFQyVz5UYNwiLNUkqt1d+987r/gL6tYMzDcx\n" +
                "INAU+8ur9I+PMl+EjmS2GHcrozwB4wv3AILQeahD8vlbhcD2K8I985OVlLN4rdoPEilCe0IbRQhH\n" +
                "lWGN5+RFPGI5MGtap239jO0AZPst4J2m7KznWwIDAQABMA0GCSqGSIb3DQEBBQUAA4GBAA9t/QuN\n" +
                "29AA63z0lBeaHWfPRLjVVozI1/ly3zSBycS9i41bbMhH80ydz3ILhHuLQBaR7FtkaKZFpaMMBd/e\n" +
                "r64GO49bSGHw8szh2lQAPfDsibc/RSZexiwntD1N/yT19UjCbXp/z7yT944Ibzdi2ddlznO19h9N\n" +
                "GNCSXudsGP+p";
        String newPackageName = "com.miHoYo.bh3.nearme.gamecenter";
        return replace(method, args, newPackageName, Sign, includeSign);
    }
}

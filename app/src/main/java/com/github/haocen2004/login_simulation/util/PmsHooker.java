package com.github.haocen2004.login_simulation.util;

import android.annotation.SuppressLint;
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
    private static final String TAG = "PMSHook";

    public PmsHooker(Object base, int hashCode) {
        try {
            this.base = base;
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "error:" + e.getMessage());
        }
    }

    public static void startHook(Context context) {
        try {

            Log.d(TAG, "start to hook");
            // 获取全局的ActivityThread对象
            @SuppressLint("PrivateApi") Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            @SuppressLint("DiscouragedPrivateApi") Method currentActivityThreadMethod =
                    activityThreadClass.getDeclaredMethod("currentActivityThread");
            Object currentActivityThread = currentActivityThreadMethod.invoke(null);
            // 获取ActivityThread里面原始的sPackageManager
            @SuppressLint("DiscouragedPrivateApi") Field sPackageManagerField = activityThreadClass.getDeclaredField("sPackageManager");
            sPackageManagerField.setAccessible(true);
            Object sPackageManager = sPackageManagerField.get(currentActivityThread);
            // 准备好代{过}{滤}理对象, 用来替换原始的对象
            @SuppressLint("PrivateApi") Class<?> iPackageManagerInterface = Class.forName("android.content.pm.IPackageManager");
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
            Log.d(TAG, "pms hook failed.");
        }

    }

    public static String getPackageNameFilter(String rawPackageName) {
        StackTraceElement[] arr = Thread.currentThread().getStackTrace();
        boolean oppoChange = false;
        boolean flymeChange = false;
        boolean forceKeep = false;
        for (StackTraceElement el : arr) {
            String className = el.getClassName().toLowerCase(Locale.ROOT);
            String methodName = el.getMethodName().toLowerCase(Locale.ROOT);
            if (className.contains("nearme") || className.contains("heytap") || className.contains("oppo")) {
                oppoChange = true;
            }
            if (className.contains("meizu")) {
                flymeChange = true;
            }
            if (className.contains("intent") || className.contains("component") || methodName.contains("verifyofficialpack")) {
                forceKeep = true;
                printStackTrace();
            }
        }
        if (!forceKeep) {
            if (oppoChange) {
                return "com.miHoYo.bh3.nearme.gamecenter";
            }
            if (flymeChange) {
                return "com.miHoYo.bh3.mz";
            }
        }

        return rawPackageName;
    }

    private static void printStackTrace() {
        Log.d("printStack", "--------------->");
        StackTraceElement[] arr = Thread.currentThread().getStackTrace();
        for (StackTraceElement el : arr) {
            Log.d("printStack",
                    "at " + el.getClassName() + "." + el.getMethodName() + "(" + el
                            .getFileName() + ":" + el.getLineNumber() + ")"
            );
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        StackTraceElement[] arr = Thread.currentThread().getStackTrace();
        boolean oppoChange = false;
        boolean flymeChange = false;
        boolean forceKeep = false;
        for (StackTraceElement el : arr) {
            String className = el.getClassName().toLowerCase(Locale.ROOT);
            String methodName = el.getMethodName().toLowerCase(Locale.ROOT);
            if (className.contains("nearme") || className.contains("heytap") || className.contains("oppo")) {
                oppoChange = true;
            }
            if (className.contains("meizu")) {
                flymeChange = true;
            }
            if (className.contains("intent") || className.contains("component") || methodName.contains("verifyofficialpack")) {
                forceKeep = true;
                printStackTrace();
            }
        }
        if (!forceKeep) {
            if (oppoChange) {
                if ("getApplicationInfo".equals(method.getName())) {
                    if (args != null && args[0].equals("com.miHoYo.bh3.nearme.gamecenter")) {
                        args[0] = "com.github.haocen2004.bh3_login_simulation";
                    }
                    return replace(method, args, "com.miHoYo.bh3.nearme.gamecenter");
                }
                if ("getPackageInfo".equals(method.getName())) {
                    Log.d(TAG, (String) args[0]);
                    if (args[0].equals("com.miHoYo.bh3.nearme.gamecenter")) {
                        args[0] = "com.github.haocen2004.bh3_login_simulation";
                        return oppoReplace(method, args, true);
                    }
                }
            }
            if (flymeChange) {
                if ("getApplicationInfo".equals(method.getName())) {
                    if (args != null && args[0].equals("com.miHoYo.bh3.mz")) {
                        args[0] = "com.github.haocen2004.bh3_login_simulation";
                    }
                    return replace(method, args, "com.miHoYo.bh3.mz");
                }
                if ("getPackageInfo".equals(method.getName())) {
                    Log.d(TAG, (String) args[0]);
                    if (args[0].equals("com.miHoYo.bh3.mz")) {
                        args[0] = "com.github.haocen2004.bh3_login_simulation";
                        return meizuReplace(method, args, true);
                    }
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
            Log.d(TAG, "replace packageName from " + oldName + " to " + info.packageName);
        }
        return info;
    }

    private PackageInfo replace(Method method, Object[] args, String newPackageName, String newSign, boolean includeSign) throws InvocationTargetException, IllegalAccessException {
        PackageInfo info = (PackageInfo) method.invoke(base, args);
        if (info != null) {
            if (includeSign) {
                try {
                    Signature sign = new Signature(newSign);
                    info.signatures[0] = sign;
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(TAG, "sign Replace failed");
                }
            }
            String oldName = info.packageName;
            info.packageName = newPackageName;
            Log.d(TAG, "replace packageName from " + oldName + " to " + info.packageName);
        }
        return info;
    }

    private PackageInfo meizuReplace(Method method, Object[] args, boolean includeSign) throws InvocationTargetException, IllegalAccessException {
        String sign = "308203CB308202B3A00302010202047AE0FC6D300D06092A864886F70D01010B0500308194310B300906035504061302434E31123010060355040813094775616E67646F6E67310F300D060355040713065A687568616931153013060355040A130C47616D652043656E7465722E31293027060355040B13205A6875686169204D65697A7520546563686E6F6C6F677920436F2E2C4C74642E311E301C0603550403131567616D652E7369676E65722E6D65697A752E636F6D3020170D3134303431393033313631385A180F33303133303832303033313631385A308194310B300906035504061302434E31123010060355040813094775616E67646F6E67310F300D060355040713065A687568616931153013060355040A130C47616D652043656E7465722E31293027060355040B13205A6875686169204D65697A7520546563686E6F6C6F677920436F2E2C4C74642E311E301C0603550403131567616D652E7369676E65722E6D65697A752E636F6D30820122300D06092A864886F70D01010105000382010F003082010A0282010100A331685E5C0D891C1E69A7C51611B066E80A10A82594E02C42995244B0A3A9A803CC25D570C40441C43E69935821254D7C085A01C02A2A20F33977DF41BEA03CD41076CFF541AC96528EBFFA5BA19CF71F9A32B0C1EEDD17D5DF4F7F11CC5CAD20D977B2ED7A835AB9CBB8A29B005EBD71236DCAFE70E3E48143669E0CB39BB3867144367C816333739D2699574AB7BCC8CD382B4FD4FF9274A5D2061130EE1C745A2EC229BA9AE349CAB1874117AE7926ED46155ECCF5AC7062AABF1E0E29400DB4B9EA6068D4BB8A1CDC78C4A4F6255CF33D597A462B945291C468503CCDA8F476B8AF9E84D5B3EC1DA3BDB402A7AB8FC88CAEF62C86312B3463DDFB48930D0203010001A321301F301D0603551D0E04160414E68E0EE7A2A72C7C26A5E0A696890C1277BDB834300D06092A864886F70D01010B050003820101006524D708E30239215D588BF700D4193C5432047F87B03654E485FFAEA0F102CBE21D75734AB7B3A29C708543AD97C9FDEA85437299D7938EAE8EEC02CA9C02771443646F974222E75BA419BAB201BF1ACA8DD1527F28951D639C913A5CDDE058AC49587CEC33339BCDAC2C4C3387993F79DEECD1BA124A03FCF0783A0CC392CC0A8960CC06940DB4F0E07BF5F1F75AAB642EE51EB94566A17B4388A389A26D4A110C80C0FE2E7D2DBE643536CAAC4E8CA1906A30FB4CB9223A203B47E154993A830216017297E3BBB79EAD28BE19575A425E7A197E5BDA6C6F2278B011AAD6E6E8BB91716618EA4EB660E949AE32CE822C158112341F8D6222C77488639A9FD7";
        String newPackageName = "com.miHoYo.bh3.mz";
        return replace(method, args, newPackageName, sign, includeSign);
    }

    private PackageInfo oppoReplace(Method method, Object[] args, boolean includeSign) throws InvocationTargetException, IllegalAccessException {

        String sign = "3082023F308201A8A00302010202044ECDE032300D06092A864886F70D01010505003063310B300906035504061302383631123010060355040813096775616E67646F6E673111300F060355040713087368656E7A68656E310D300B060355040A13046F70706F310D300B060355040B13046F70706F310F300D060355040313066E6561726D653020170D3131313132343036313230325A180F32303636303832373036313230325A3063310B300906035504061302383631123010060355040813096775616E67646F6E673111300F060355040713087368656E7A68656E310D300B060355040A13046F70706F310D300B060355040B13046F70706F310F300D060355040313066E6561726D6530819F300D06092A864886F70D010101050003818D00308189028181009947806D3E8FA3AC8F2B03C80BAF940C845432573E5460DC222CD524AADD5DFBDF3BAFF80BEAD60CCC373120D014FBCBABF48F8F325F848E64B618772BA33C01E30BF70082D079A843F2F95B85C0F62BC23DF3939594B378ADDA0F1229427B421B45084795618DE7E4453C6239306B5AA76DFD8CED0064FB2DE09DA6ECACE75B0203010001300D06092A864886F70D0101050500038181000F6DFD0B8DDBD000EB7CF494179A1D67CF44B8D5568CC8D7F972DF3481C9C4BD8B8D5B6CC847F34C9DCF720B847B8B401691EC5B6468A645A5A30C05DFDEAFAE063B8F5B4861F0F2CCE1DA54003DF0EC89B73F45265EC62C27B43D4DFF24F5F548C26D7A7FCFBC93F78E086F3762D9D765CE73B5F61F4D18D0925EE76C18FFA9";
        String newPackageName = "com.miHoYo.bh3.nearme.gamecenter";
        return replace(method, args, newPackageName, sign, includeSign);
    }
}

package com.github.haocen2004.login_simulation.xposed;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class SelfHook {
    public SelfHook(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedBridge.log("Hook " + lpparam.packageName);
        Class<?> clazz3 = XposedHelpers.findClass("com.github.haocen2004.login_simulation.login.Tencent", lpparam.classLoader);

        try {
            XposedHelpers.findAndHookMethod(clazz3, "getHooked", new XC_MethodHook() {

                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    param.setResult(true);
                    XposedBridge.log("Self Hooked Success.");
                }
            });
        } catch (Exception e) {
            XposedBridge.log(e);
        }
    }
}

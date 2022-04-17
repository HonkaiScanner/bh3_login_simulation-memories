package com.github.haocen2004.login_simulation.xposed;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        switch (lpparam.packageName) {
            case "com.tencent.mobileqq":
                new TencentHook(lpparam);
                break;
            case "com.github.haocen2004.bh3_login_simulation":
            case "com.github.haocen2004.bh3_login_simulation.dev":
                new SelfHook(lpparam);
                break;
        }
    }
}
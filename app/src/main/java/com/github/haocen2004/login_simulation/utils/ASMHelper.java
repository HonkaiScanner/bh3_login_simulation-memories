package com.github.haocen2004.login_simulation.utils;

import static com.github.haocen2004.login_simulation.data.Constant.MI_ADV_MODE;

import androidx.annotation.Keep;

public class ASMHelper {

    @Keep
    public static String getMiPackageName() {
        if (MI_ADV_MODE) {
            return "com.xiaomi.gamecenter.sdk.service";
        }
        return "com.xiaomi.gamecenter.sdk.service.ban";
    }
}

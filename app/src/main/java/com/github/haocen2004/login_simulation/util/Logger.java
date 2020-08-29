package com.github.haocen2004.login_simulation.util;

import android.util.Log;

public class Logger {

    private static String tag = "login_simulation";

    private static String generateTag() {
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[4];
        String str1 = stackTraceElement.getClassName();
        String str2 = str1.substring(1 + str1.lastIndexOf("."));
        Object[] arrayOfObject = new Object[3];
        arrayOfObject[0] = str2;
        arrayOfObject[1] = stackTraceElement.getMethodName();
        arrayOfObject[2] = stackTraceElement.getLineNumber();
        String str3 = String.format("%s.%s(L:%d)", arrayOfObject);
        if (tag != null) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(tag);
            stringBuilder.append(" ");
            stringBuilder.append(str3);
            return stringBuilder.toString();
        }
        return str3;
    }

    public static void info(String msg){
        Log.i(generateTag(),msg);
    }
    public static void warning(String msg){
        Log.w(generateTag(),msg);
    }
    public static void debug(String msg){
        Log.d(generateTag(),msg);
    }

}

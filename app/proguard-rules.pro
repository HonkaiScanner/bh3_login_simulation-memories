# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-optimizationpasses 5          # 指定代码的压缩级别
-dontusemixedcaseclassnames   # 是否使用大小写混合
-verbose
-printconfiguration ~/tmp/full-r8-config.txt

-keep class com.nearme.** { *; }
-keep class com.bun.miitmdid.core.** {*;}
-keep class audio.mve.** { *; }
-keep class com.hh.** { *; }
-keep class com.bilibili.** { *; }
-keep class bsgamesdkhttp.** { *; }
-keep class bsgamesdkio.** { *; }
-keep class com.bsgamesdk.** { *; }

-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}

-keepattributes Signature
-dontwarn com.jcraft.jzlib.**
-keep class com.jcraft.jzlib.**  { *;}

-dontwarn sun.misc.**
-keep class sun.misc.** { *;}

-dontwarn retrofit2.**
-keep class retrofit2.** { *;}

-dontwarn io.reactivex.**
-keep class io.reactivex.** { *;}

-dontwarn sun.security.**
-keep class sun.security.** { *; }

-dontwarn com.google.**
-keep class com.google.** { *;}

-dontwarn cn.leancloud.**
-keep class cn.leancloud.** { *;}

-keep public class android.net.http.SslError
-keep public class android.webkit.WebViewClient

-dontwarn android.webkit.WebView
-dontwarn android.net.http.SslError
-dontwarn android.webkit.WebViewClient

-dontwarn android.support.**

-dontwarn org.apache.**
-keep class org.apache.** { *;}

-dontwarn okhttp3.**
-keep class okhttp3.** { *;}
-keep interface okhttp3.** { *; }

-dontwarn okio.**
-keep class okio.** { *;}

-keepattributes *Annotation*

-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
 <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder {
  *** rewind();
}

-dontwarn com.geetest.sdk.**
-keep class com.geetest.sdk.**{*;}

-keep class com.nearme.** { *; }
-dontwarn com.nearme.**


-keepattributes InnerClasses,Signature,Exceptions,Deprecated,*Annotation*
-dontwarn android.**
-dontwarn com.google.**
-keep class android.** {*;}
-keep class com.google.** {*;}
-keep class com.android.** {*;}
-dontwarn org.apache.**
-keep class org.apache.** { *; }
-keep class sun.misc.Unsafe { *; }
-keep class com.google.** {*;}
-keep public class android.arch.core.internal.FastSafeIterableMap
-keep public class android.arch.core.util.Function
-keep public class android.arch.lifecycle.Lifecycle
-keep public class android.arch.lifecycle.Observer
-keep public class android.arch.lifecycle.ReportFragment
-keep public class android.arch.lifecycle.ViewModel
-keep public class android.support.v4.app.Fragment
-keep public class android.support.annotation.AnimatorRes
-keep public class android.support.v4.app.ActivityCompat
-keep public class android.support.design.widget.CoordinatorLayout
-keep public class android.support.v4.app.AppLaunchChecker
-keep public class android.support.v4.app.BackStackState
#-libraryjars libs/alipaySdk.jar
-dontwarn com.alipay.**
-keep class com.alipay.** {*;}
-keep class com.ut.device.** {*;}
-keep class com.ta.utdid2.** {*;}
#-libraryjars libs/eventbus-3.jar
-keep class org.greenrobot.eventbus.** { *; }
-keep class de.greenrobot.event.** { *; }
-keep class de.greenrobot.dao.** {*;}
-keepclassmembers class ** {
public void onEvent*(**);
void onEvent*(**);
}
-keepclassmembers class ** {
@org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }
# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
<init>(java.lang.Throwable);
}
#-libraryjars libs/wechat.jar
-keep class com.tencent.** {*;}
#-libraryjars libs/glide.jar
-keep class com.bumptech.glide.** {*;}
-dontwarn com.xiaomi.**
-keep class com.xiaomi.** {*;}
-keep class com.mi.** {*;}
-keep class com.wali.** {*;}
-keep class cn.com.wali.** {*;}
-keep class miui.net.**{*;}
-keep class org.xiaomi.** {*;}

-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,LocalVariable*Table,*Annotation*,Synthetic,EnclosingMethod
-keepclasseswithmembers class * extends cn.gundam.sdk.shell.even.SDKEventReceiver
-keep class cn.uc.**{
    <methods>;
    <fields>;
}
-keep class cn.gundam.**{
    <methods>;
    <fields>;
}
-keep class android.**{
    <methods>;
    <fields>;
}
-keep class org.json.**{
    <methods>;
    <fields>;
}

-keep class com.bun.miitmdid.core.** {*;}

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}


-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keepattributes InnerClasses
-keep class com.tencent.bugly.**{*;}
-keep class com.tencent.stat.**{*;}
-keep class com.tencent.smtt.**{*;}
-keep class com.tencent.beacon.**{*;}
-keep class com.tencent.mm.**{*;}
-keep class com.tencent.apkupdate.**{*;}
-keep class com.tencent.tmassistantsdk.**{*;}
-keep class org.apache.http.** { * ;}
-keep class com.qq.jce.**{*;}
-keep class com.qq.taf.**{*;}
-keep class com.tencent.connect.**{*;}
-keep class com.tencent.map.**{*;}
-keep class com.tencent.open.**{*;}
-keep class com.tencent.qqconnect.**{*;}
-keep class com.tencent.mobileqq.**{*;}
-keep class com.tencent.tauth.**{*;}
-keep class com.tencent.feedback.**{*;}
-keep class common.**{*;}
-keep class exceptionupload.**{*;}
-keep class mqq.**{*;}
-keep class qimei.**{*;}
-keep class strategy.**{*;}
-keep class userinfo.**{*;}
-keep class com.pay.**{*;}
-keep class com.demon.plugin.**{*;}
-keep class com.tencent.midas.**{*;}
-keep class oicq.wlogin_sdk.**{*;}
-keep class com.tencent.ysdk.**{*;}
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep class com.tencent.android.tpush.** {* ;}
-keep class com.tencent.mid.** {* ;}
-keep class com.qq.taf.jce.** {*;}
-keep class com.tencent.a.**{*;}
-keep class com.tencent.wxop.**{*;}
-keep class com.jg.**{*;}
-keep class com.tencent.qqdownloader.pay.**{*;}
-keepclasseswithmembernames class * {
    native <methods>;
}

-keep public class com.tencent.safecloud.**{*;}
-keep class com.bun.miitmdid.** {*;}
-keep class com.tencent.pipe.** {*;}

-dontwarn java.nio.file.Files
-dontwarn java.nio.file.Path
-dontwarn java.nio.file.OpenOption
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# 设备指纹SDK
-keep public class com.tencent.safecloud.**{*;}
-keep class com.tencent.tgpa.** {*;}
-keep public class qfc.**{*;}

#HttpDNS相关
-keep class com.tencent.msdk.dns.**{*;}

# 防沉迷接口
-keep class com.tencent.ysdk.module.AntiAddiction.impl.AntiAddictModule {public static *;}
-keep interface com.tencent.ysdk.module.AntiAddiction.listener.AntiAddictListener {*;}
-keep interface com.tencent.ysdk.module.AntiAddiction.listener.LoginAntiAddictCallBack {*;}
-keep class com.tencent.ysdk.module.AntiAddiction.model.AntiAddictRet {*;}
-keep class com.tencent.ysdk.module.AntiAddiction.AntiAddictionApi {*;}
-keep interface com.tencent.ysdk.module.AntiAddiction.AntiAddictionInterface {*;}
-keep interface com.tencent.ysdk.module.AntiAddiction.listener.AntiRegisterWindowCloseListener {*;}
-keep interface com.tencent.ysdk.module.AntiAddiction.listener.QueryCertificationCallback {*;}
-keep class com.tencent.ysdk.module.AntiAddiction.model.CertificationRect {*;}
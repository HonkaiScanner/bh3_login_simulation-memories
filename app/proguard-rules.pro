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

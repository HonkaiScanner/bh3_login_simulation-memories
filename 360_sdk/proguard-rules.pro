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
-keep class cn.pp.** { *; }
-keep class cn.pp.** { *; }
-keep class org.json.alipay.** { *; }
-keep class com.alipay.** {*;}
-keep class com.nearme.atlas.** {*;}
-keep class com.qihoo.** {*;}
-keep class com.qihoo360.** { *; }
-keep class com.qihoopp.** { *; }
-keep class com.qihoosdk.** {*;}
-keep class qihoohttp.** {*;}
-keepnames class qihoohttp.** {*;}
-keep class com.heepay.plugin.**{*;}
-keepnames class com.heepay.plugin.**{*;}
-keep class com.yeepay.safekeyboard.** { *; }
-keep class com.ipaynow.** {*;}
-keep class com.ta.utdid2.** {*;}
-keep class com.ut.device.** {*;}
-keep class com.tencent.** {*;}
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-keep class okhttp3.** {*;}
-keepnames class okhttp3.** {*;}
-keep class com.cloudplay.**{*;}
-keep class com.qihoo.antispam.holmes.**{*;}
	-keep class com.qihoo360.qos.**{ *;}
-keep class com.qihoo.gamecenter.sdk.common.stat.qos.**{*;}
	-keep class com.qihoo360.qos.staticmsa.** {*;}
	-keep class com.bun.miitmdid.core.** {*;}
	-keep class com.bun.supplier.** {*;}
-dontwarn com.cmic.sso.sdk.**
	-keep class com.cmic.** {*;}
	-keep class com.cmic.sso.sdk.** {*;}
	-keep class cn.com.chinatelecom.**{*;}
	-keep class cn.com.chinatelecom.account.api.**{*;}
	-keep class com.unicom.sdklibrary.** {*;}
	-keep interface com.unicom.sdklibrary.ResultListener {*;}
-keep class com.qihoo.gamecenter.sdk.common.isp.**{*;}
-keep class com.qihoo.gamecenter.sdk.common.view.PrivacyModel { *;}
-keep class com.qihoo.gamecenter.sdk.common.view.PrivacyName { *;}
-keepclassmembers class * extends android.webkit.WebChromeClient {
	  public void openFileChooser(...);
	  public boolean onShowFileChooser(...);
}
-dontwarn cn.pp.**
-dontwarn com.qihoo.**
-dontwarn com.qihoo360.**
-dontwarn com.qihoopp.**
-dontwarn com.qihoosdk.**
-dontwarn qihoohttp.**
-dontwarn com.alipay.**
-dontwarn com.alipay.android.app.**
-dontwarn com.yeepay.safekeyboard.**
-dontwarn com.heepay.plugin.**
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-dontwarn android.**
	-dontwarn java.net.**
	-dontwarn com.qihoo.antispam.holmes.**
-dontwarn com.bun.miitmdid.core.**
-dontwarn com.bun.supplier.**

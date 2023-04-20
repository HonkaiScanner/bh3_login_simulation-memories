import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("com.huawei.agconnect")
    id("kotlin-kapt")
    kotlin("android")
    kotlin("plugin.serialization")
}
apply<ASMPlugin>()

//tasks {
//    withType<JavaCompile> {
//        options.compilerArgs.add("-Xlint:deprecation")
//    }
//}

android {

    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a", "x86")
            isUniversalApk = true
        }
    }

    lint {
        disable.add("RestrictedApi")
    }

    signingConfigs {
        if (!System.getenv("CI").toBoolean()) {

            create("debugSign") {

                val properties = Properties()

                val keystorePropertiesFile = rootProject.file("local.properties")
                storeFile = file(System.getenv("KEYSTORE") ?: "keystore.jks")
                properties.load(FileInputStream(keystorePropertiesFile))

                storePassword = properties.getProperty("sign.pwn")
                keyAlias = properties.getProperty("sign.keyAlias")
                keyPassword = properties.getProperty("sign.pwn")

            }
        }

        create("releaseSign") {
            storeFile = file(System.getenv("KEYSTORE") ?: "keystore.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }
    compileSdk = 33
    buildToolsVersion = "33.0.1"

    defaultConfig {
        applicationId = "com.github.haocen2004.bh3_login_simulation"
        minSdk = 23
        targetSdk = 33
        versionCode = 71
        versionName = "1.8.0"
        // versionCode = System.currentTimeMillis().toString().substring(7, 12).toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
        signingConfig = signingConfigs.getByName("releaseSign")
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            if (System.getenv("CI").toBoolean()) {
                versionNameSuffix = System.getenv("NAME_SUFFIX")
            } else {
                versionNameSuffix = "-snapshot-23w17a.dev"
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("debug") {
            versionNameSuffix = "-dev"
            // applicationIdSuffix = ".dev"
            isMinifyEnabled = false
            isShrinkResources = false
            if (System.getenv("CI") == null) {
                signingConfig = signingConfigs.getByName("debugSign")
            }

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("libs")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        jvmToolchain(11)
    }

    namespace = "com.github.haocen2004.login_simulation"

    if (!System.getenv("CI").toBoolean()) {

        applicationVariants.all {
            println(versionName)

            buildOutputs.all {

                val variantOutputImpl =
                    this as com.android.build.gradle.internal.api.BaseVariantOutputImpl

                val variantName: String = variantOutputImpl.name

                val outputFileName = "Scanner-${versionName}-${variantName}.apk"

                variantOutputImpl.outputFileName = outputFileName

                println(variantOutputImpl.outputFileName)
            }
        }


    }

}


configurations {
    all {
        exclude(group = "com.huawei.hms", module = "hmscoreinstaller")
        exclude(group = "androidx.appcompat", module = "appcompat")
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.so", "*.aar"))))
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.preference:preference:1.2.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.navigation:navigation-fragment:2.5.3")
    implementation("androidx.navigation:navigation-ui:2.5.3")
    implementation("androidx.viewpager2:viewpager2:1.1.0-beta01")
    implementation("com.github.jenly1314.WeChatQRCode:opencv:1.2.1")
    implementation("com.github.jenly1314.WeChatQRCode:opencv-armv7a:1.2.1")
    implementation("com.github.jenly1314.WeChatQRCode:opencv-armv64:1.2.1")
    implementation("com.github.jenly1314.WeChatQRCode:opencv-x86:1.2.1")
    implementation("com.github.jenly1314.WeChatQRCode:opencv-x86_64:1.2.1")
    implementation("com.github.jenly1314.WeChatQRCode:wechat-qrcode:1.2.1")
    implementation("com.github.jenly1314.WeChatQRCode:wechat-qrcode-scanning:1.2.1")
    implementation("com.github.jenly1314.MLKit:mlkit-camera-core:1.2.0")
    implementation(project(":mi_sdk"))
//    implementation(project(":360_sdk"))
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("com.tencent.bugly:crashreport:4.1.9")
    implementation("cn.leancloud:storage-android:8.2.18")
    implementation("cn.leancloud:realtime-android:8.2.18")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
    implementation("androidx.room:room-runtime:2.5.1")
    implementation("androidx.room:room-ktx:2.5.1")
//    implementation("com.google.android.material:material:1.7.0")
    implementation("dev.rikka.rikkax.material:material:2.7.0")
    implementation("dev.rikka.rikkax.material:material-preference:2.0.0")
    implementation("com.github.bumptech.glide:glide:4.15.1")
    implementation("com.github.getActivity:ToastUtils:9.1")
    implementation("com.github.getActivity:XToast:8.9")
    implementation("com.geetest.sensebot:sensebot:4.3.9.1")
    implementation("com.drakeet.about:about:2.5.2")
    implementation("com.drakeet.multitype:multitype:4.3.0")
//    implementation("com.huawei.agconnect:agconnect-core:1.7.1.300")
    implementation("com.huawei.hms:hwid:6.8.0.300")
    implementation("com.huawei.hms:game:6.8.0.300")
    implementation("androidx.paging:paging-runtime:3.1.1")

    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")
    annotationProcessor("androidx.room:room-compiler:2.5.1")
//    testImplementation 'junit:junit:4.13.2'
//    androidTestImplementation 'androidx.test.ext:junit:1.1.4'
//    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.0'

    kapt("androidx.room:room-compiler:2.5.1")
}

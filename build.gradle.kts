buildscript {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://maven.cnb.cool/tencent-tds/shiply-public/-/packages/") }
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://developer.huawei.com/repo/") }
        maven { url = uri("https://developer.hihonor.com/repo") }
    }
    dependencies {
        classpath(kotlin("gradle-plugin", version = "1.7.20"))
        classpath("com.android.tools.build:gradle:8.5.2")
        classpath("com.huawei.agconnect:agcp:1.9.1.301")
        classpath("com.github.megatronking.stringfog:gradle-plugin:5.2.0")
        classpath("com.github.megatronking.stringfog:xor:5.0.0")
// NOTE: Do not place your application dependencies here; they belong
// in the individual module build.gradle files
    }
}

plugins {
    id("com.android.application") apply false
    id("com.android.library") apply false
    kotlin("android") apply false
}

tasks.register("clean", Delete::class.java) {
    delete(rootProject.layout.buildDirectory)
}
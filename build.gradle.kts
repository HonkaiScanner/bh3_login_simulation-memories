buildscript {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://developer.huawei.com/repo/") }
    }
    dependencies {
        classpath(kotlin("gradle-plugin", version = "1.7.20"))
        classpath("com.android.tools.build:gradle:7.4.1")
        classpath("com.tencent.bugly:symtabfileuploader:2.2.1")
        classpath("com.huawei.agconnect:agcp:1.7.3.300")
        classpath("dev.rikka.tools.materialthemebuilder:gradle-plugin:1.3.3")
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
    delete(rootProject.buildDir)
}
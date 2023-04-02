enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://developer.huawei.com/repo/") }
    }
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    plugins {
        id("com.android.application") version "7.4.1"
        id("com.android.library") version "7.4.1"
        id("com.google.devtools.ksp") version "1.7.20-1.0.7"
        id("dev.rikka.tools.autoresconfig") version "1.2.1"
        id("dev.rikka.tools.materialthemebuilder") version "1.3.3"
        id("dev.rikka.tools.refine") version "3.1.1"
        id("org.jetbrains.kotlin.android") version "1.7.20"
        kotlin("plugin.serialization") version "1.7.20"
    }
}

include(":app", ":mi_sdk")//, ':360_sdk'
rootProject.name = "bh3_login_simulation"

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.0"
}

repositories {
    google()
    mavenCentral()
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.apiVersion = "1.4"
}

dependencies {
    implementation("com.android.tools.build:gradle-api:8.5.2")
    implementation(kotlin("stdlib"))
    implementation("org.ow2.asm:asm-util:9.6")
    gradleApi()
}
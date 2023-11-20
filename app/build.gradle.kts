plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = 33

    namespace = "me.kyuubiran.potato"

    defaultConfig {
        applicationId = "me.kyuubiran.potato"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0.0"

        ndk.abiFilters.apply {
            add("armeabi-v7a")
            add("arm64-v8a")
//            add("x86")
//            add("x86_64")
        }
    }

    buildTypes {
        named("release") {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles("proguard-rules.pro")
        }
    }


    androidResources {
        additionalParameters += arrayOf("--allow-reserved-package-id", "--package-id", "0x45")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
}

dependencies {
    implementation("com.github.kyuubiran:EzXHelper:2.0.7")
    implementation("org.luckypray:dexkit:2.0.0-rc8")

    compileOnly("de.robv.android.xposed:api:82")
}

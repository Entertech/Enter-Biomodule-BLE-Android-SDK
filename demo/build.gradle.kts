plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    signingConfigs {
        create("config") {
            keyAlias = "entertech"
            keyPassword = "123456"
            storeFile = file("../entertech.jks")
            storePassword = "123456"
        }
    }

    packagingOptions {
        exclude("META-INF/*.kotlin_module")
        exclude("META-INF/kotlinx_coroutines_core.version")
        exclude("META-INF/proguard/androidx-annotations.pro")
    }

    namespace = "cn.entertech.flowtimeble"
    compileSdk = 35

    defaultConfig {
        applicationId = "cn.entertech.flowtimeble"
        minSdk = 24
        targetSdk = 35
        versionCode = 3061
        versionName = "3.0.6.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    implementation(libs.kotlin.stdlib.jdk7)
//    implementation(project(":ble-device-headband"))
//    implementation(project(":ble-device-eyehead"))
//    implementation(project(":ble-device-tag"))
//    implementation(project(":ble-device-cushion"))
    implementation (libs.ble.device.headband)
    implementation (libs.ble.device.tag)
    implementation (libs.ble.device.eyehead)
    implementation (libs.ble.device.cushion)
    implementation(libs.androidx.activity)
    testImplementation(libs.junit.v412)
    androidTestImplementation(libs.androidx.junit.v115)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.androidx.core.ktx.v190)
    implementation(libs.kotlin.stdlib)
    implementation(libs.androidx.appcompat.v100)
    implementation(libs.androidx.constraintlayout.v214)
    implementation(libs.material.v190)

//    implementation(libs.mpandroidchart)
    implementation(libs.device)
    implementation(libs.base)
    implementation(libs.dfu)
    implementation(libs.log.local)
}
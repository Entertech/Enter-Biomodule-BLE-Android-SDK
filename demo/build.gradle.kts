plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    packagingOptions {
        exclude("META-INF/*.kotlin_module")
        exclude("META-INF/kotlinx_coroutines_core.version")
        exclude("META-INF/proguard/androidx-annotations.pro")
    }

    namespace = "cn.entertech.flowtimeble"
    compileSdk = 34

    defaultConfig {
        applicationId = "cn.entertech.flowtimeble"
        minSdk = 24
        targetSdk = 34
        versionCode = 2051
        versionName = "2.0.5.1"
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
    buildFeatures {
        viewBinding = true
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(libs.kotlin.stdlib.jdk7)

    testImplementation(libs.junit.v412)
    androidTestImplementation(libs.androidx.junit.v115)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation ("cn.entertech.android:device:0.0.2")
//    api project(':bleuisdk')
//    implementation project(':ble')
    implementation ("cn.entertech.android:ble-device:3.0.3-dev-local")
    implementation ("cn.entertech.android:ble-device-api:3.0.3-dev-local")
    implementation ("cn.entertech.android:log-local:1.1.0")
    implementation(libs.androidx.core.ktx.v190)
    implementation(libs.kotlin.stdlib)
    implementation(libs.androidx.appcompat.v100)
    implementation(libs.androidx.constraintlayout.v214)
    implementation(libs.material.v190)

    implementation (libs.meditate)
//    implementation(libs.mpandroidchart)
    implementation(libs.device)
    implementation(libs.base)
    implementation(libs.dfu)
    implementation(libs.log.local)
}
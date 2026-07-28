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
        versionCode = 3072
        versionName = "3.0.7.2"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    flavorDimensions += "product"
    flavorDimensions += "function"

    productFlavors {
        create("demo") {
            dimension = "product"
        }
        //用于研究测试
        create("qa") {
            dimension = "product"
            applicationIdSuffix = ".test"
            versionNameSuffix = "-test"
        }

        create("hr") {
            dimension = "function"
        }

        create("all") {
            dimension = "function"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
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

androidComponents {
    beforeVariants(selector().all()) { variantBuilder ->
        val product = variantBuilder.productFlavors
            .firstOrNull { it.first == "product" }
            ?.second
        val function = variantBuilder.productFlavors
            .firstOrNull { it.first == "function" }
            ?.second

        variantBuilder.enable = when {
            product == "demo" && function == "all" -> true
            product == "qa" && function == "hr" -> true
            else -> false
        }
    }

    onVariants(
        selector()
            .withFlavor("product", "qa")
            .withFlavor("function", "hr")
    ) { variant ->
        variant.applicationId.set("cn.entertech.flowtimeble.qahr")
    }
}

dependencies {
    implementation(libs.kotlin.stdlib.jdk7)
//    implementation(project(":ble-device-headband"))
//    implementation(project(":ble-device-eyehead"))
//    implementation(project(":ble-device-tag"))
//    implementation(project(":ble-device-cushion"))
    implementation (libs.ble.device.headband)
    implementation (libs.ble.device.api)
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

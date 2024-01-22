# Ble SDK [![Maven Central](https://maven-badges.herokuapp.com/maven-central/cn.entertech.android/biomoduleble/badge.svg)](https://maven-badges.herokuapp.com/maven-central/cn.entertech.android/biomoduleble)

## Description

The basic Ble SDK can easily collect raw brainwaves, heart rate and other data from the hardware side.

## Getting Started

### Gradle
Add the following dependencies under the build.gradle file in the project root directory:
```groovy
repositories {
  mavenCentral()
}
```

Add the following dependencies under the build.gradle file in the required module:

```groovy
implementation 'cn.entertech.android:biomoduleble:1.5.5'
```

### Permissions

```xml
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
```

## How to use

### 1.Connect to device

**Code**
There are two ways to connect devices: scan & connect to the device with the strongest signal; connect to a paired device

#### 1.1 scan & connect to the device with the strongest signal

**Code**

```kotlin
val biomoduleBleManager = BiomoduleBleManager.getInstance(context)
biomoduleBleManager.connectDevice(fun(mac: String) {
            BleLogUtil.i(TAG, "connect success $mac")
        }, 
        { msg ->
            BleLogUtil.i(TAG, "connect failed")
        }, 
        ConnectionBleStrategy.SCAN_AND_CONNECT_HIGH_SIGNAL)

```

#### 1.2 Connect a paired device

**Code**

```kotlin
val biomoduleBleManager = BiomoduleBleManager.getInstance(context)
biomoduleBleManager.connectDevice(fun(mac: String) {
            BleLogUtil.i(TAG, "connect success $mac")
        }, 
        { msg ->
            BleLogUtil.i(TAG, "connect failed")
        }, 
        ConnectionBleStrategy.CONNECT_BONDED)
```


### 2.Add data listener

**Code**

```kotlin
//heart rate data listener
var heartRateListener = fun(heartRate: Int) {
    BleLogUtil.d("heart rate:" + heartRate)
}
biomoduleBleManager.addHeartRateListener(heartRateListener)
//raw brainwave data listener
var rawDataListener = fun(data:ByteArray){
    BleLogUtil.d(Arrays.toString(data))
}
biomoduleBleManager.addRawDataListener(rawDataListener)
```

> Note: You need to remove listener after using this sdk, otherwise memory overflow will occurfor. For example: `biomoduleBleManager.removeRawDataListener(rawDataListener)`

### 3.Collect brainwave and heart rate data

**Code**

```kotlin
//invoke `stopHeartAndBrainCollection()` if you want to stop collecting
biomoduleBleManager.startHeartAndBrainCollection()
```

For more detail ble function,please refer to [Ble Detail API](../Ble_Detail_API.md)

# Change Notes

[Change Notes](https://github.com/Entertech/Enter-Biomodule-BLE-Android-SDK/wiki/biomoduleble--%E6%9B%B4%E6%96%B0%E6%97%A5%E5%BF%97)

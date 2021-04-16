# Ble SDK [![Download](https://api.bintray.com/packages/hzentertech/maven/biomoduleble/images/download.svg?version=1.4.0)](https://bintray.com/hzentertech/maven/biomoduleble/1.4.0/link)

## Description

The basic Ble SDK can easily collect raw brainwaves, heart rate and other data from the hardware side.

## Getting Started

### Gradle

Add the following dependencies under the build.gradle file in the required module:

```groovy
implementation 'cn.entertech:biomoduleble:1.4.0'
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

```kotlin
var biomoduleBleManager = BiomoduleBleManager.getInstance(context)
//Connect to the nearest device according to the signal strength, 
// if you need to connect to the specified device,
// you can call the `scanMacAndConnect` method to connect to the mac address
biomoduleBleManager.scanNearDeviceAndConnect(fun() {
            Logger.d("scan success")
        }, fun(e: Exception) {
            Logger.d("scan failed：$e")
        }, fun(mac: String) {
            Logger.d("connect success $mac")
        }) { msg ->
            Logger.d("connect failed")
        }
```

### 2.Add data listener

**Code**

```kotlin
//heart rate data listener
var heartRateListener = fun(heartRate: Int) {
    Logger.d("heart rate:" + heartRate)
}
biomoduleBleManager.addHeartRateListener(heartRateListener)
//raw brainwave data listener
var rawDataListener = fun(data:ByteArray){
    Logger.d(Arrays.toString(data))
}
biomoduleBleManager.addRawDataListener(rawDataListener)
```

> Note: You need to remove listener after using this sdk, otherwise memory overflow will occurfor. For example: `biomoduleBleManager.removeRawDataListener(rawDataListener)`

### 3.Collect brainwave and heart rate data

**Code**

```kotlin
//invoke `stopBrainCollection()` if you want to stop collecting
biomoduleBleManager.startBrainCollection()
```

For more detail ble function,please refer to [Ble Detail API](../Ble_Detail_API.md)

# Change Notes

[Change Notes](https://github.com/Entertech/Enter-Biomodule-BLE-Android-SDK/wiki/biomoduleble--%E6%9B%B4%E6%96%B0%E6%97%A5%E5%BF%97)

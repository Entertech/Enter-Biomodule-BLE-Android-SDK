# Device Management Interface SDK (Integrate On Demand)[![Maven Central](https://maven-badges.herokuapp.com/maven-central/cn.entertech.android/biomodulebleui/badge.svg)](https://maven-badges.herokuapp.com/maven-central/cn.entertech.android/biomodulebleui)

## Description

The device management interface SDK includes the basic ble function, and also includes the device management interface. If there is no special customization requirement for your APP, you can directly access the device management interface.

## Getting Started

### Gradle

Add the following dependencies to the module's build.gradle file:

```groovy
implementation 'cn.entertech.android:biomoduleble:1.4.0'  //basic ble function
implementation 'cn.entertech.android:biomodulebleui:1.0.8' //the device management interface
```

Add the following dependency address under the build.gradle file in the project root directory

```groovy
allprojects {
    repositories {
        maven {
            url "https://dl.bintray.com/hzentertech/maven"
        }
    }
}
```

### Permissions

```xml
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.INTERNET" />
```

## Initialization

If there is no special requirement for the device management interface, you can directly use the device management interface SDK provided by us, and you can set the DeviceUIConfig class to configure related properties.

```kotlin
var deviceUIConfig = DeviceUIConfig.getInstance(this)
deviceUIConfig.init(isDeviceBind, isMultipleDevice, deviceCount)
```

**Parameter Description**

| Parameters | Type | Description |
| ---------------- | ------- | -------------------------------------------------------- |
| isDeviceBind | Boolean | Whether to bind the device, if so, the previous device will be automatically connected each time the device is connected |
| isMultipleDevice | Boolean | Whether to support multiple connections |
| deviceCount | Int | The number of device connections, up to 4 devices |

## Device management entrance

The external entrance provided by the device management interface: DeviceManagerActivity

**Display**

<img src="https://github.com/Entertech/Enter-Biomodule-BLE-Android-SDK/blob/master/docimage/%E8%AE%BE%E5%A4%87%E8%BF%9E%E6%8E%A5.jpeg" width="200"/><img src="https://github.com/Entertech/Enter-Biomodule-BLE-Android-SDK/blob/master/docimage/%E8%AE%BE%E5%A4%87%E8%BF%9E%E6%8E%A5%E6%88%90%E5%8A%9F.jpeg" width="200"/><img src="https://github.com/Entertech/Enter-Biomodule-BLE-Android-SDK/blob/master/docimage/%E8%AE%BE%E5%A4%87%E8%BF%9E%E6%8E%A5%E5%A4%B1%E8%B4%A5.jpeg" width="200"/>


## DFU Configuration

```kotlin
deviceUIConfig.updateFirmware(newVersion,path,isForceUpdate)
```
**Parameter Description**

| Parameters | Type | Description |
| ---------- | ------- | -------------------------- |
| newVersion | String | New firmware version number Format: a.b.c |
| path | String | The path of the firmware upgrade package |
| isForceUpdate | Boolean | Whether to force the update, if not, it will be automatically judged according to the version number |

**Display**

<img src="https://github.com/Entertech/Enter-Biomodule-BLE-Android-SDK/blob/master/docimage/%E5%9B%BA%E4%BB%B6%E5%8D%87%E7%BA%A71.jpeg" width="200"/><img src="https://github.com/Entertech/Enter-Biomodule-BLE-Android-SDK/blob/master/docimage/%E5%9B%BA%E4%BB%B6%E5%8D%87%E7%BA%A72.jpeg" width="200"/><img src="https://github.com/Entertech/Enter-Biomodule-BLE-Android-SDK/blob/master/docimage/%E5%9B%BA%E4%BB%B6%E5%8D%87%E7%BA%A73.jpeg" width="200"/>

# Change Notes

[Change Notes](https://github.com/Entertech/Enter-Biomodule-BLE-Android-SDK/wiki/biomodulebleui--%E6%9B%B4%E6%96%B0%E6%97%A5%E5%BF%97)
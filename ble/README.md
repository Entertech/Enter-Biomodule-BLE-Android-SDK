[English Readme](https://github.com/Entertech/Enter-Biomodule-BLE-Android-SDK/blob/master/ble/README_EN.md)
# Ble基础SDK [![Maven Central](https://maven-badges.herokuapp.com/maven-central/cn.entertech.android/biomoduleble/badge.svg)](https://maven-badges.herokuapp.com/maven-central/cn.entertech.android/biomoduleble)

## 说明

基础的Ble SDK可以方便从硬件端采集到原始脑波、心率等数据.

## 集成

### gradle自动依赖
在项目根目录的build.gradle文件下添加以下依赖：
```groovy
repositories {
  mavenCentral()
}
```

在所需的module中的build.gradle文件下添加以下依赖：

```groovy
implementation 'cn.entertech.android:biomoduleble:1.5.5'
```

### 注意事项

并且需要申明蓝牙相关权限：

```xml
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
```

## 快速接入

### 1.连接设备

分两种连接设备方式：扫描&连接信号最强设备；连接已配对的设备;

#### 1.1 扫描&连接信号最强设备
**代码示例**
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

#### 1.2 连接已配对的设备
**代码示例**
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

### 2.添加数据监听

**代码示例**

```kotlin
//心率数据监听
var heartRateListener = fun(heartRate: Int) {
    BleLogUtil.d("心率数据" + heartRate)
}
biomoduleBleManager.addHeartRateListener(heartRateListener)
//原始脑波数据监听
var rawDataListener = fun(data:ByteArray){
    BleLogUtil.d(Arrays.toString(data))
}
biomoduleBleManager.addRawDataListener(rawDataListener)
```

> 注意：如果在界面退出时需要调用对象的移除监听方法，否则会出现内存溢出，如biomoduleBleManager.removeRawDataListener(rawDataListener)

### 3.采集脑波和心率数据

**代码示例**

```kotlin
//如果想要停止采集调用stopHeartAndBrainCollection()
biomoduleBleManager.startHeartAndBrainCollection()
```

更多详细的蓝牙ble方法可以参考[Ble详细API说明](<https://github.com/Entertech/Enter-Biomodule-BLE-Android-SDK/blob/master/Ble%E8%AF%A6%E7%BB%86API%E8%AF%B4%E6%98%8E.md>)

# 更新日志

[biomoduleble 更新日志](https://github.com/Entertech/Enter-Biomodule-BLE-Android-SDK/wiki/biomoduleble--%E6%9B%B4%E6%96%B0%E6%97%A5%E5%BF%97)

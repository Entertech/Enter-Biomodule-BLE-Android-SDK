[English Readme](https://github.com/Entertech/Enter-Biomodule-BLE-Android-SDK/blob/master/ble/README_EN.md)
# Ble基础SDK [![Download](https://api.bintray.com/packages/hzentertech/maven/biomoduleble/images/download.svg?version=1.3.9)](https://bintray.com/hzentertech/maven/biomoduleble/1.3.9/link)

## 说明

基础的Ble SDK可以方便从硬件端采集到原始脑波、心率等数据.

## 集成

### gradle自动依赖

在所需的module中的build.gradle文件下添加以下依赖即可：

```groovy
implementation 'cn.entertech:biomoduleble:1.3.9'
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

**代码示例**

```kotlin
var biomoduleBleManager = BiomoduleBleManager.getInstance(context)
//根据信号强弱连接最近的设备，如果需要连接指定设备可调用scanMacAndConnect方法传入mac地址连接
biomoduleBleManager.scanNearDeviceAndConnect(fun() {
            Logger.d("扫描成功")
        }, fun(e: Exception) {
            Logger.d("扫描失败：$e")
        }, fun(mac: String) {
            Logger.d("连接成功$mac")
        }) { msg ->
            Logger.d("连接失败")
        }
```

### 2.添加数据监听

**代码示例**

```kotlin
//心率数据监听
var heartRateListener = fun(heartRate: Int) {
    Logger.d("心率数据" + heartRate)
}
biomoduleBleManager.addHeartRateListener(heartRateListener)
//原始脑波数据监听
var rawDataListener = fun(data:ByteArray){
    Logger.d(Arrays.toString(data))
}
biomoduleBleManager.addRawDataListener(rawDataListener)
```

> 注意：如果在界面退出时需要调用对象的移除监听方法，否则会出现内存溢出，如biomoduleBleManager.removeRawDataListener(rawDataListener)

### 3.采集脑波和心率数据

**代码示例**

```kotlin
//如果想要停止采集调用stopBrainCollection()
biomoduleBleManager.startBrainCollection()
```

更多详细的蓝牙ble方法可以参考[Ble详细API说明](<https://github.com/Entertech/Enter-Biomodule-BLE-Android-SDK/blob/master/Ble%E8%AF%A6%E7%BB%86API%E8%AF%B4%E6%98%8E.md>)

# 更新日志

[biomoduleble 更新日志](https://github.com/Entertech/Enter-Biomodule-BLE-Android-SDK/wiki/biomoduleble--%E6%9B%B4%E6%96%B0%E6%97%A5%E5%BF%97)

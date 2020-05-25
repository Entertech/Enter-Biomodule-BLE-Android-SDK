# 设备管理界面 SDK（按需接入）[![Download](https://api.bintray.com/packages/hzentertech/maven/biomodulebleui/images/download.svg?version=1.0.6)](https://bintray.com/hzentertech/maven/biomodulebleui/1.0.6/link)

## 说明

设备管理界面SDK，包含了基础ble功能，另外还包含设备管理界面，如果对您的APP对界面没有特殊的定制化需要可直接接入设备管理界面

## 集成

在module的build.gradle文件中加入以下依赖：

```groovy
implementation 'cn.entertech:biomoduleble:1.3.6'  //ble基础功能
implementation 'cn.entertech:biomodulebleui:1.0.6' //ble 设备管理界面
```

在项目根目录的build.gradle文件下添加以下依赖地址

```groovy
allprojects {
    repositories {
        maven {
            url "https://dl.bintray.com/hzentertech/maven"
        }
    }
}
```

## 权限申请

```xml
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.INTERNET" />
```

## 初始化

如果对设备管理界面没有特殊要求可以直接我们提供的设备管理界面SDK，可以设置DeviceUIConfig这个类，进行相关属性的配置。

**代码示例**

```kotlin
var deviceUIConfig = DeviceUIConfig.getInstance(this)
deviceUIConfig.init(isDeviceBind, isMultipleDevice, deviceCount)
```

**参数说明**

| 参数             | 类型    | 说明                                                     |
| ---------------- | ------- | -------------------------------------------------------- |
| isDeviceBind     | Boolean | 是否绑定设备，如果是则每次连接设备时会自动连接之前的设备 |
| isMultipleDevice | Boolean | 是否支持多连接                                           |
| deviceCount      | Int     | 设备连接个数，最多可设备4个                              |

## 设备管理入口

设备管理界面对外提供的入口：DeviceManagerActivity

**效果**

<img src="https://github.com/Entertech/Enter-Biomodule-BLE-Android-SDK/blob/master/docimage/%E8%AE%BE%E5%A4%87%E8%BF%9E%E6%8E%A5.jpeg" width="200"/><img src="https://github.com/Entertech/Enter-Biomodule-BLE-Android-SDK/blob/master/docimage/%E8%AE%BE%E5%A4%87%E8%BF%9E%E6%8E%A5%E6%88%90%E5%8A%9F.jpeg" width="200"/><img src="https://github.com/Entertech/Enter-Biomodule-BLE-Android-SDK/blob/master/docimage/%E8%AE%BE%E5%A4%87%E8%BF%9E%E6%8E%A5%E5%A4%B1%E8%B4%A5.jpeg" width="200"/>


## 固件更新配置

**代码示例**

```kotlin
deviceUIConfig.updateFirmware(newVersion,path,isForceUpdate)
```

**参数说明**

| 参数       | 类型    | 说明                       |
| ---------- | ------- | -------------------------- |
| newVersion | String  | 新固件版本号 格式：a.b.c   |
| path       | String  | 固件升级包的路径           |
| isForceUpdate   | Boolean | 是否强制更新，如果否会根据版本号自动判断           |

**效果**

<img src="https://github.com/Entertech/Enter-Biomodule-BLE-Android-SDK/blob/master/docimage/%E5%9B%BA%E4%BB%B6%E5%8D%87%E7%BA%A71.jpeg" width="200"/><img src="https://github.com/Entertech/Enter-Biomodule-BLE-Android-SDK/blob/master/docimage/%E5%9B%BA%E4%BB%B6%E5%8D%87%E7%BA%A72.jpeg" width="200"/><img src="https://github.com/Entertech/Enter-Biomodule-BLE-Android-SDK/blob/master/docimage/%E5%9B%BA%E4%BB%B6%E5%8D%87%E7%BA%A73.jpeg" width="200"/>

# 更新日志

[biomodulebleui 更新日志](https://github.com/Entertech/Enter-Biomodule-BLE-Android-SDK/wiki/biomodulebleui--%E6%9B%B4%E6%96%B0%E6%97%A5%E5%BF%97)
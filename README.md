# 简介

回车情感云可以根据用户的脑波数据和心率数据来进行高级情绪情感数据分析的一个云算法平台，同时能给出包括：放松度、注意力、愉悦值，压力值、激动度（内测）在内的多种情绪情感值。详情请查看[官网](https://www.entertech.cn)。

在开始开发前，请先查看回车情感云的[开发文档](https://docs.affectivecloud.com)，了解情感云平台的架构和所能提供的服务具体说明，确定好你的应用中所需要的服务。你还需要联系管理员注册好测试应用，然后再进行开发。

为了方便你进行 Android 平台的快速开发，我们提供了情感云快速开发 SDK，通过本 SDK 你可以快速地将情感云的能力集成到你的 app 中。

# Demo

**下载**

[demo.apk](https://fir.im/gb3a?release_id=5ddf4415f945484f9475bcd2)

**说明**

更多demo的操作说明可以查看[回车生物电采集模块演示 demo](https://github.com/Entertech/Enter-AffectiveCloud-Android-SDK/tree/master/demo)

另外我们还提供了一个心流演示应用，其集成了蓝牙SDK，蓝牙设备管理SDK，情感云SDK等功能，详细请查看[心流演示应用](https://github.com/Entertech/Enter-Affective-Cloud-Demo-Android)

# 集成

### Gradle 自动集成
在module的build.gradle文件下添加以下依赖
```groovy
implementation 'cn.entertech:affectivecloud:1.3.2'
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
### 权限申请

```xml
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
```

# 快速接入

SDK提供了快速接入情感云的管理类`EnterAffectiveCloudManager`，使用该类只需要几步就可以完成客户端与情感云平台的数据交互。

##  1.初始化

```kotlin
//基础服务
var availableBioServices = listOf(Service.EEG, Service.HR)
//情感云服务(需向官方申请)
var availableAffectiveServices = listOf(Service.ATTENTION, Service.PRESSURE, Service.AROUSAL, Service.SLEEP)
//基础服务订阅参数
var biodataSubscribeParams = BiodataSubscribeParams.Builder()
            .requestAllEEGData()//订阅所有eeg数据
            .requestAllHrData()//订阅所有心率相关数据
            .build()
//情感服务订阅参数
var affectiveSubscribeParams = AffectiveSubscribeParams.Builder()
            .requestAllSleepData()//订阅所有sleep服务数据
            .requestAttention()//订阅attention数据
            .requestRelaxation()//订阅relaxation数据
            .requestPressure()//订阅pressure数据
            .requestPleasure()//订阅pleasure数据
            .build()
//配置项
var enterAffectiveCloudConfig = EnterAffectiveCloudConfig.Builder(APP_KEY, APP_SECRET, USER_ID)
            .url(websocketAddress)//配置websocket地址
            .timeout(10000)//配置websocket连接超时时间 单位：ms
            .availableBiodataServices(availableBioServices)//可用的基础服务
            .availableAffectiveServices(availableAffectiveServices)//可用的情感服务
            .biodataSubscribeParams(biodataSubscribeParams!!)//基础服务订阅参数
            .affectiveSubscribeParams(affectiveSubscribeParams!!)//情感服务订阅参数
            .build()
//创建管理类
var enterAffectiveCloudManager = EnterAffectiveCloudManager(enterAffectiveCloudConfig)
//初始化SDK
enterAffectiveCloudManager?.init(object : Callback {
            override fun onError(error: Error?) {
                messageReceiveFragment.appendMessageToScreen("初始化失败：${error.toString()}")
            }
            override fun onSuccess() {
                messageReceiveFragment.appendMessageToScreen("初始化成功")
            }
        })
```

**参数说明**

|       参数       |  类型  |                                             说明                                             |
| :--------------: | :----: | :------------------------------------------------------------------------------------------: |
| websocketAddress | String | 情感云服务器链接，详见[链接](https://docs.affectivecloud.com/🎙接口协议/1.%20综述.html#正式) |

|    参数    |  类型  |                                                                       说明                                                                        |
| :--------: | :----: | :-----------------------------------------------------------------------------------------------------------------------------------------------: |
|  APP_KEY   | String |                                                             由我们后台生成的：App Key                                                             |
| APP_SECRET | String |                                                           由我们后台生成的：App Secret                                                            |
|  USER_ID   | String | 你 app 当前用户的 id，如手机号、id 号，昵称等，需要保证唯一性。详见[userID](https://docs.affectivecloud.com/🎙接口协议/3.%20会话协议.html#userID) |

## 2.添加实时数据监听

注意如果要想实时返回数据，需要在上面步骤中配置订阅相应数据字段，否则监听将无数据返回。

```kotlin
enterAffectiveCloudManager!!.addBiodataRealtimeListener {
            messageReceiveFragment.appendMessageToScreen("基础服务实时数据：${it.toString()}")
        }
enterAffectiveCloudManager!!.addAffectiveDataRealtimeListener {
            messageReceiveFragment.appendMessageToScreen("情感服务实时数据：${it.toString()}")
        }
```

## 3.上传数据

**上传脑波数据**

```kotlin
enterAffectiveCloudManager?.appendEEGData(bytes)
```

**上传心率数据**

```kotlin
enterAffectiveCloudManager?.appendHeartRateData(heartRate)
```

> 注意：上面数据都直接由硬件返回，硬件有数据返回就调用相应的方法，传入数据即可，无需做其他处理。

## 4.获取报表

相应返回的 report 字段，由之前配置决定。具体字段的详细描述见[生物数据基础报表参数](https://docs.affectivecloud.com/🎙接口协议/4.%20生物数据基础分析服务协议.html#biodata-report)和[情感计算报表参数](https://docs.affectivecloud.com/🎙接口协议/5.%20情感计算服务协议.html#affective-report)。

```kotlin
enterAffectiveCloudManager?.getBiodataReport(object : Callback2<HashMap<Any, Any?>> {
            override fun onSuccess(t: HashMap<Any, Any?>?) {
                messageReceiveFragment.appendMessageToScreen("基础报表：${t.toString()}")
            }
            override fun onError(error: Error?) {
                messageReceiveFragment.appendMessageToScreen("基础报表出错：${error.toString()}")
            }
        })
enterAffectiveCloudManager?.getAffectiveDataReport(object : Callback2<HashMap<Any, Any?>> {
            override fun onSuccess(t: HashMap<Any, Any?>?) {
                messageReceiveFragment.appendMessageToScreen("情感报表：${t.toString()}")
            }
            override fun onError(error: Error?) {
                messageReceiveFragment.appendMessageToScreen("情感报表出错：${error.toString()}")
            }
        })
```

## 5.资源释放

注意，每次使用完情感云服务都需调用如下`release`方法来释放资源，否则会面临持续扣费的风险

```kotlin
enterAffectiveCloudManager?.release(object : Callback {
            override fun onSuccess() {
                messageReceiveFragment.appendMessageToScreen("情感云已成功断开！")
            }
            override fun onError(error: Error?) {
                messageReceiveFragment.appendMessageToScreen("情感云断开失败：${error}")
            }
        })
```


# 详细API功能说明

如果你需要根据不同场景灵活使用情感云服务，可以使用`EnterAffectiveCloudApi`来调用相应API，该类封装了所有情感云服务对外的接口。更加详情的情感云API可以查看[情感云详细API功能说明](https://github.com/Entertech/Enter-AffectiveCloud-Android-SDK/blob/master/media/%E6%83%85%E6%84%9F%E4%BA%91%E8%AF%A6%E7%BB%86API%E5%8A%9F%E8%83%BD%E8%AF%B4%E6%98%8E.md)

# 更新日志
[更新日志](https://github.com/Entertech/Enter-AffectiveCloud-Android-SDK/wiki/%E6%9B%B4%E6%96%B0%E6%97%A5%E5%BF%97)


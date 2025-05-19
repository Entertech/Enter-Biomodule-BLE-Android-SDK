# Headband SDK 接入说明

## 集成

#### gradle自动依赖

在项目根目录的build.gradle文件下添加以下依赖：

```groovy
repositories {
    mavenCentral()
}
```

在所需的module中的build.gradle文件下添加以下依赖：

    implementation ("cn.entertech.android:ble-device-headband:3.0.4")

## 详细API说明

### 获取Headband蓝牙管理类

**方法说明**

该类集成了蓝牙的所有操作

**示例代码**

```kotlin
biomoduleBleManager = HeadbandManger(context)
```

### 设备连接

#### 连接附近信号最强的设备（未知设备mac地址）

**方法说明**

扫描并连接附近信号最强的设备

**示例代码**

```kotlin
biomoduleBleManager.connectDevice(
    fun(mac: String) {
        BleLogUtil.i(TAG, "connect success $mac")
    },
    { msg ->
        BleLogUtil.i(TAG, "connect failed")
    },
    ConnectionBleStrategy.SCAN_AND_CONNECT_HIGH_SIGNAL
)
```

**参数说明**

| 参数                    | 类型                           | 说明     |
|-----------------------|------------------------------|--------|
| successConnect        | ((String) -> Unit)?          | 连接成功回调 |
| failure               | ((String) -> Unit)           | 连接失败回调 |
| connectionBleStrategy | ConnectionBleStrategy        | 连接类型   |
| filter                | (String?,String?) -> Boolean | 过滤逻辑   |

#### 根据指定mac连接（已知设备mac地址）

**方法说明**

连接指定mac地址设备，需要传入mac地址

**示例代码**

```kotlin
  biomoduleBleManager.scanMacAndConnect(mac, fun(mac: String) {
    BleLogUtil.d("连接成功$mac")
}) { msg ->
    BleLogUtil.d("连接失败")
}
```

**参数说明**

| 参数              | 类型             | 说明      |
|-----------------|----------------|---------|
| mac             | String         | 设备mac地址 |
| successCallBack | (String)->Unit | 成功回调    |
| failedCallBack  | (String)->Unit | 失败回调    |

### 设备断开

**方法说明**

断开与设备的连接

**示例代码**

```kotlin
biomoduleBleManager.disConnect()
```

### 获取设备连接状态

**方法说明**

获取当前设备连接状态

**示例代码**

```kotlin
biomoduleBleManager.isConnected()
```

**返回值说明**

| 参数          | 类型      | 说明                    |
|-------------|---------|-----------------------|
| isConnected | Boolean | 设备已连接为true，未连接为false。 |

### 下发收集数据指令

**方法说明**
订阅任何数据前都需要下发收集数据指令

**示例代码**

```kotlin
biomoduleBleManager.startCollectBrainAndHrData(success = {
    showMsg("收集数据指令发送成功 ")
}, failure = { _, it ->
    showMsg("收集数据指令发送失败： 失败原因：$it ")
})
```

### 下发停止收集数据指令

**方法说明**
不需要接受数据时，可以下发停止收集数据指令
**示例代码**

```kotlin
biomoduleBleManager.stopCollectBrainAndHrData(success = {
    showMsg("停止收集数据指令发送成功 ")
}, failure = { _, it ->
    showMsg("停止收集数据指令发送失败： 失败原因：$it ")
})
```

### 电量数据

#### 读取电量数据

    fun readBatteryValue(success: (Int) -> Unit, failure: ((String) -> Unit)?)

#### 读取电量原始数据

    fun readBatteryByteArray(
            success: (ByteArray) -> Unit = {},
            failure: ((String) -> Unit)? = null
        )

#### 订阅电量数据

    notifyBatteryValue(success: (Int) -> Unit, failure: ((String) -> Unit)?)

#### 订阅电量原始数据

    fun notifyBattery(
            success: (ByteArray) -> Unit,
            failure: ((String) -> Unit)?
        )

### 脑波数据

#### 订阅原始脑波数据

**示例代码**

```kotlin

biomoduleBleManager.notifyBrainWave(success = {
    Log.d("", "订阅到的原始数据：$it")
}, failure = {
    Log.d("", "订阅失败：$it")
})
```

### 心率数据

#### 订阅心率数据

**示例代码**

```kotlin
fun notifyHRValue(
    success: (HeadbandHrBean) -> Unit,
    failure: ((String) -> Unit)? = null,
    castHrValue: (ByteArray) -> HeadbandHrBean = getDefaultCastHrValue()
) {
} 
```

**参数说明**

| 参数          | 类型                            | 说明                                           |
|-------------|-------------------------------|----------------------------------------------|
| success     | (HeadbandHrBean) -> Unit      | 获取心率数据的回调                                    |
| failure     | (String)->Unit                | 失败回调                                         |
| castHrValue | (ByteArray) -> HeadbandHrBean | 原始数据转换成可利用数据逻辑，默认使用getDefaultCastHrValue()方法 |

#### headband心率数据类：HeadbandHrBean

```kotlin
data class HeadbandHrBean(val rawData: ByteArray, val hr: Int) 
```

**参数说明**

| 参数      | 类型        | 说明      |
|---------|-----------|---------|
| rawData | ByteArray | 心率原始数据  |
| hr      | Int       | 心率可视化数据 |

### 佩戴状态数据

#### 订阅佩戴状态数据

    fun notifyContact(
            success: ((ByteArray) -> Unit) = {},
            failure: (String) -> Unit = {}
        )

**参数说明**

| 参数      | 类型                  | 说明                        |
|---------|---------------------|---------------------------|
| success | (ByteArray) -> Unit | 佩戴状态原始数据   第一个字节为0，表示佩戴好了 |
| failure | (String) -> Unit    | 订阅失败回调                    |



### DFU（设备固件升级）

支持DFU（Device Firmware
Update），即设备远程固件升级。由于本SDK底层依赖Android-DFU-Library库，因此如有需要DFU，需在你的build.gradle文件中添加如下依赖：

```groovy
implementation 'com.github.santa-cat:Android-DFU-Library:v1.6.1'
```

#### DFU的使用

首先需要定义一个服务并继承DfuBaseService，如下：

    public class DfuService extends DfuBaseService {
    	@Override
    	protected Class<? extends Activity> getNotificationTarget() {
     	   return NotificationActivity.class;//当点击固件升级中的通知栏时，会打开
                                            //NotificationActivity这个Activity
    	}

    	@Override
    	protected boolean isDebug() {
     	   //该方法表示是否打印更多的调试日志
    	    return BuildConfig.DEBUG;//BuildConfig.DEBUG为false，如果要打印更多的日志则直接返回true
    	}
    }

NotificationActivity类代码如下：

    public class NotificationActivity extends Activity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (isTaskRoot()) {
                // 这里声明你要跳转的Activity
                final Intent intent = new Intent(this, MyActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtras(getIntent().getExtras()); 
                startActivity(intent);
            }
            finish();
        }
    }

> *注意：别忘了在AndroidManifest.xml文件中注册以上service和Activity*

接下来需要创建一个远程升级的监听，用来监听升级过程中各个过程：

```kotlin
private val mDfuProgressListener = object : DfuProgressListenerAdapter() {

    //设备正在连接
    override fun onDeviceConnecting(deviceAddress: String?) {
    }

    //设备已连接
    override fun onDeviceConnected(deviceAddress: String?) {
    }

    //正准备开始升级
    override fun onDfuProcessStarting(deviceAddress: String?) {
    }

    //设备开始升级
    override fun onDfuProcessStarting(deviceAddress: String?) {
    }

    override fun onEnablingDfuMode(deviceAddress: String?) {
    }

    //固件验证
    override fun onFirmwareValidating(deviceAddress: String?) {
    }

    //设备正在断开
    override fun onDeviceDisconnecting(deviceAddress: String?) {
    }

    //升级完成
    override fun onDfuCompleted(deviceAddress: String?) {
    }

    //由于意外原因，升级流产，升级失败
    override fun onDfuAborted(deviceAddress: String?) {
    }

    //升级中进度回调
    override fun onProgressChanged(
        deviceAddress: String?,
        percent: Int,
        speed: Float,
        avgSpeed: Float,
        currentPart: Int,
        partsTotal: Int
    ) {
    }

    //升级失败
    override fun onError(deviceAddress: String?, error: Int, errorType: Int, message: String?) {
    }
}
```

#### 注册监听

要在相应的生命周期中注册及取消注册该监听

**代码示例**

```kotlin
fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_device_update_tip)
    DfuServiceListenerHelper.registerProgressListener(this, mDfuProgressListener)
}
fun onDestroy() {
    super.onDestroy()
    DfuServiceListenerHelper.unregisterProgressListener(this, mDfuProgressListener)
}
```

#### 开始升级

```kotlin
val starter = DfuServiceInitiator(deviceMac) //传入设备mac地址
    .setDeviceName(deviceName) //传入设备名称
    .setkeepBond(true) //是否保持设备绑定
//调用此方法使Nordic nrf52832进入bootloader模式
starter.setUnsafeExperimentalButtonlessServiceInSecureDfuEnabled(true)
//设置文件路径
starter.setZip(FileUtil.getFirmwareDir() + "/firmware.zip")
//开始升级
starter.start(this, DfuService::class.java)
```

对于Android 8.0及以上的系统，如果你想让DFU服务能够在通知栏中展示进度条，你需要创建一个通知频道。最简单的方式就是调用以下方法：

```kotlin
DfuServiceInitiator.createDfuNotificationChannel(context);
```

### C#调用说明

> 该SDK大部分方法都能被C#调用，但是涉及到数组的传递时，C#会调用失败，如这里的脑波数据传递。因此如需在C#设置脑波数据监听，可采用如下方式：
>
> ```c#
> //c#端定义脑波监听类
> class RawBrainDataCallback : AndroidJavaProxy {
> 	public RawBrainDataCallback():base("kotlin.jvm.functions.Function1"){
> 	}
> 	public void invoke(AndroidJavaObject jo){
> 		AndroidJavaObject bufferObject = jo.Get<AndroidJavaObject>("Buffer");
> 		byte[] buffer = AndroidJNIHelper.ConvertFromJNIArray<byte[]>(bufferObject.GetRawObject());//这里的buffer数据便是返回的脑波数组
>     }
> }
> AndroidJavaClass biomoduleBleManagerJc = new AndroidJavaClass("cn.entertech.ble.BiomoduleBleManager");
> var companion = biomoduleBleManagerJc.GetStatic<AndroidJavaObject>("Companion");
> var biomoduleBleManager = companion.Call<AndroidJavaObject>("getInstance",currentActivity);
> //实例化脑波数据监听回调
> RawBrainDataCallback rawBrainDataCallback = new RawBrainDataCallback();
> biomoduleBleManager.Call("addRawDataListener4CSharp",rawBrainDataCallback);
> ```


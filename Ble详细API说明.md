# 详细API说明

### 获取蓝牙管理类

**方法说明**

该类集成了蓝牙的所有操作

**示例代码**

```kotlin
biomoduleBleManager = BiomoduleBleManager.getInstance(context)
```

### 设备连接

#### 连接附近信号最强的设备（未知设备mac地址）

**方法说明**

扫描并连接附近信号最强的设备，需传入用户id，如不需要用户绑定功能则传入固定值

**示例代码**

```kotlin
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

**参数说明**

| 参数                   | 类型                | 说明         |
| ---------------------- | ------------------- | ------------ |
| scanSuccessCallBack    | () -> Unit          | 扫描成功回调 |
| scanFailCallBack       | (Exception) -> Unit | 扫描失败回调 |
| connectSuccessCallBack | (String) ->Unit     | 连接成功回调 |
| failedCallBack         | (String)->Unit      | 失败回调     |

#### 根据指定mac连接（已知设备mac地址）

**方法说明**

连接指定mac地址设备，需要传入用户id、mac地址

**示例代码**

```kotlin
  biomoduleBleManager.scanMacAndConnect(mac, fun(mac: String) {
    Logger.d("连接成功$mac")
  }){msg->
    Logger.d("连接失败")
  }
```

**参数说明**

| 参数            | 类型           | 说明        |
| --------------- | -------------- | ----------- |
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
boolean isConnected = biomoduleBleManager.isConnected()
```

**返回值说明**

| 参数        | 类型    | 说明                              |
| ----------- | ------- | --------------------------------- |
| isConnected | Boolean | 设备已连接为true，未连接为false。 |

### 设置监听接口

**监听接口生命周期需要管理，不需要监听了，请调用remove**

#### 添加原始脑波监听

**方法说明**

添加原始脑波监听，通过该监听可从硬件中获取原始脑波数据

**示例代码**

```kotlin
  var rawDataListener = fun(data:ByteArray){
        Logger.d(Arrays.toString(data))
  }
  biomoduleBleManager.addRawDataListener(rawDataListener)
```

**参数说明**

| 参数            | 类型                | 说明         |
| --------------- | ------------------- | ------------ |
| rawDataListener | （ByteArray）->Unit | 原始脑波回调 |

> **原始脑波数据说明**
>
> 从脑波回调中返回的原始脑波数据是一个长度为20的字节数组，前两个字节为包编号，后18个字节为有效脑波数据，其中脑波数据分两个通道，依次为：一通道、二通道、一通道、二通道......
>
> **正常数据示例**
>
> [0, -94, 21, -36, 125, 21, -12, -75, 22, 8, 61, 22, 10, -72, 22, 15, -19,20,10,8]
>
> **异常数据示例（未检测到脑波数据）**
>
> [0, 101, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,-1,-1,-1]

#### 移除原始脑波监听

**方法说明**

如果不想受到脑波数据，移除监听即可

**示例代码**

```kotlin
biomoduleBleManager.removeRawDataListener(rawDataListener)
```

**参数说明**

| 参数            | 类型                | 说明         |
| --------------- | ------------------- | ------------ |
| rawDataListener | （ByteArray）->Unit | 原始脑波回调 |


#### 添加心率监听

**方法说明**

添加心率监听，通过该监听可从硬件中获取心率数据

**示例代码**

```kotlin
var heartRateListener = fun(heartRate: Int) {
    Logger.d("heart rate data is " + heartRate)
}
biomoduleBleManager.addHeartRateListener(heartRateListener)
```

**参数说明**

| 参数              | 类型          | 说明             |
| ----------------- | ------------- | ---------------- |
| heartRateListener | （Int）->Unit | 心率数据获取回调 |

#### 移除心率监听

**方法说明**

如果不想收到心率，移除监听即可

**示例代码**

```kotlin
biomoduleBleManager.removeHeartRateListener(heartRateListener)
```

**参数说明**

| 参数              | 类型          | 说明         |
| ----------------- | ------------- | ------------ |
| heartRateListener | （Int）->Unit | 心率数据回调 |

#### 添加佩戴信号监听

**方法说明**

添加该监听，可实时获取设备佩戴质量

**代码示例**

```kotlin
contactListener = fun(state: Int) {
   Logger.d("Whether the wearing contact is good:"+ state == 0);
}
biomoduleBleManager.addContactListener(contactListener)
    
```

**参数说明**

| 参数            | 类型                   | 说明                                                         |
| --------------- | ---------------------- | ------------------------------------------------------------ |
| contactListener | （Int）->Unit | 佩戴信号回调。0:接触良好，其他值：未正常佩戴 |

#### 移除佩戴信号监听

**方法说明**

移除该监听，则不会受到佩戴信号

**代码示例**

```kotlin
biomoduleBleManager.removeContactListener(contactListener)
```

**参数说明**

| 参数            | 类型                   | 说明         |
| --------------- | ---------------------- | ------------ |
| contactListener | （Int）->Unit | 佩戴信号回调 |

#### 添加电量监听

**方法说明**

添加电量监听，添加后会每隔30秒回调一次

**代码示例**

```kotlin
var batteryListener = fun(byte: Byte) {
   Logger.d("battery = $byte")
}  
biomoduleBleManager.addBatteryListener(batteryListener)
```

**参数说明**

| 参数            | 类型            | 说明     |
| --------------- | --------------- | -------- |
| batteryListener | （Byte）-> Unit | 电量回调 |

#### 移除电量监听

**方法说明**

移除后，将不会收到电量回调

**代码示例**

```kotlin
biomoduleBleManager.removeBatteryListener(batteryListener)
```

**参数说明**

| 参数            | 类型            | 说明     |
| --------------- | --------------- | -------- |
| batteryListener | （Byte）-> Unit | 电量回调 |

#### 添加电池电压监听

**方法说明**

添加电池电压监听，添加后会每隔30秒回调一次

**代码示例**

```kotlin
var batteryVoltageListener = fun(voltage: Double) {
   Logger.d("battery voltage = $voltage")
}  
biomoduleBleManager.addBatteryVoltageListener(batteryVoltageListener)
```

**参数说明**

| 参数            | 类型            | 说明     |
| --------------- | --------------- | -------- |
| batteryVoltageListener | （Double）-> Unit | 电池电压回调 |

#### 移除电池电压监听

**方法说明**

移除后，将不会收到电池电压回调

**代码示例**

```kotlin
biomoduleBleManager.removeBatteryVoltageListener(batteryVoltageListener)
```

**参数说明**

| 参数            | 类型            | 说明     |
| --------------- | --------------- | -------- |
| batteryVoltageListener | （Double）-> Unit | 电池电压回调 |

可以根据电池电压计算电池电量，

默认电池规格（型号401015，容量 40mAh,额定电压3.7V）计算公式如下：

```
已知电压为x（单位：V）
【1】剩余电量百分比q（单位：%；取值范围：0~100）表达式：
	q =  a1*exp(-((x-b1)/c1)^2) + a2*exp(-((x-b2)/c2)^2) + a3*exp(-((x-b3)/c3)^2)	# 高斯拟合曲线
	q = q*1.13 - 5  		# 藏电，上限藏8，下限藏5

	q = max([min([q, 100]), 0])	# 取值范围限制在0~100
	其中参数值如下:
       		a1 =       99.84
       		b1 =       4.244
       		c1 =      0.3781
       		a2 =       21.38
       		b2 =       3.953
       		c2 =      0.1685
       		a3 =       15.21
       		b3 =       3.813
       		c3 =     0.09208
【2】剩余使用时长t（单位：min）表达式：
	t = 3.84*q  		# 原时长估计因子为4.52，保守估计为85%，故改成3.84
```

### 采集与停止

#### 开始脑波数据采集

**方法说明**

开始脑波数据采集，调用这个接口开始采集脑波数据

**示例代码**

```kotlin
biomoduleBleManager.startBrainCollection()
```

#### 停止脑波数据采集

**方法说明**

停止采集，调用该方法停止采集脑波数据

**示例代码**

```kotlin
biomoduleBleManager.stopBrainCollection()
```

#### 开始心率数据采集

**方法说明**

开始心率数据采集，调用这个接口开始采集心率数据

**示例代码**

```kotlin
biomoduleBleManager.startHeartRateCollection()
```

#### 停止心率数据采集

**方法说明**

停止采集，调用该方法停止采集心率数据

**示例代码**

```kotlin
biomoduleBleManager.stopHeartRateCollection()
```

#### 开始脑波和心率数据同时采集

**方法说明**

开始心率数据采集，调用这个接口开始同时采集脑波和心率数据

**示例代码**

```kotlin
biomoduleBleManager.startHeartAndBrainCollection()
```

#### 停止脑波和心率数据采集

**方法说明**

停止采集，调用该方法停止采集脑波和心率数据

**示例代码**

```kotlin
biomoduleBleManager.stopHeartAndBrainCollection()
```



### DFU（设备固件升级）

支持DFU（Device Firmware Update），即设备远程固件升级。由于本SDK底层依赖Android-DFU-Library库，因此如有需要DFU，需在你的build.gradle文件中添加如下依赖：

```groovy
implementation 'com.github.santa-cat:Android-DFU-Library:v1.6.1'
```

#### DFU的使用

首先需要定义一个服务并继承DfuBaseService，如下：

```
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
```

NotificationActivity类代码如下：

```
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
```

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
        override fun onProgressChanged(deviceAddress: String?, percent: Int, speed: Float, avgSpeed: Float, currentPart: Int, partsTotal: Int) {
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
override fun onCreate(savedInstanceState: Bundle?) {
   super.onCreate(savedInstanceState)
   setContentView(R.layout.activity_device_update_tip)
   DfuServiceListenerHelper.registerProgressListener(this, mDfuProgressListener)
}
override fun onDestroy() {
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
### 多设备连接

新增MultipleBiomoduleBleManager类，可以支持多设备的连接。MultipleBiomoduleBleManager与BiomoduleBleManager功能接口相同，唯一区别是实例化方式不同，BiomoduleBleManager采用单例模式，而MultipleBiomoduleBleManager可以任意实例化，每一个MultipleBiomoduleBleManager对应管理一个ble设备，但是连接设备的个数会受手机终端限制，不同终端限制数量会不同，需根据具体情况分析。

**代码示例**

```kotlin
//实例化ble管理类
var multipleBiomoduleBleManager = MultipleBiomoduleBleManager()
//接下来所有的接口调用方式均与BiomoduleBleManager类相同，这里不在赘述。
...
```
 ### C#调用说明
>
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


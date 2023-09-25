# Detailed API description

### Get Bluetooth Management Class

**Method Description**
This class integrates all the operations of Bluetooth
**Sample Code**

```kotlin

biomoduleBleManager = BiomoduleBleManager.getInstance(context)

```

### Device connection
#### Connect to a matched device (unknown device mac address)

**Method Description** 

Connect a paired device

**Sample Code**

```kotlin
biomoduleBleManager.connectDevice(fun(mac: String) {
            BleLogUtil.i(TAG, "connect success $mac")
        }, 
        { msg ->
            BleLogUtil.i(TAG, "connect failed")
        }, 
        ConnectionBleStrategy.CONNECT_BONDED)

```

#### Connect to the device with the strongest signal nearby (unknown device mac address)

**Method Description**

Scan and connect to the device with the strongest signal nearby, you need to pass in the user id , if the user binding function is not required, pass in a fixed value

**Sample Code**

```kotlin
  biomoduleBleManager.connectDevice(fun(mac: String) {
            BleLogUtil.i(TAG, "connect success $mac")
        }, 
        { msg ->
            BleLogUtil.i(TAG, "connect failed")
        }, 
        ConnectionBleStrategy.SCAN_AND_CONNECT_HIGH_SIGNAL)
```
**Parameter description**

| Parameter                   | Type                         | Description        |
| ---------------------- | ----------------------------| -----------|
| successConnect         | ((String) -> Unit)?         | Connect success callback |
| failure                | ((String) -> Unit)          | Connect failed callback |
| connectionBleStrategy  | ConnectionBleStrategy       | Connect  type  ｜
| filter                 | (String?,String?) -> Boolean| filter logic   ｜

#### According to the specified mac connection (known device mac address)

**Method Description**

To connect to a device with a specified mac address, you need to pass in the user id and mac address

**Sample Code**

```kotlin

  biomoduleBleManager.scanMacAndConnect(mac, fun(mac: String) {
    Logger.d(" Connected successfully $mac")
  }){msg->
    Logger.d(" Connection failed ")
  }

```
**Parameter description**

| Parameter            | Type           | Description  |
| --------------- | -------------- | ----------- |
| mac | String | device mac address |
| successCallBack | (String)->Unit | Success Callback    |
| failedCallBack | (String)->Unit | Failed Callback    |


### Device disconnect

**Method Description**

Disconnect from the device

**Sample Code**

```kotlin

biomoduleBleManager.disConnect()

```

### Get device connection status

**Method Description**

Get the current device connection status

**Sample Code**

```kotlin

boolean isConnected = biomoduleBleManager.isConnected()

```

**Return value description**


| Parameter        | Type    | Description  |
| ----------- | ------- | ------------- |
| isConnected | Boolean | The device is connected to true , not connected to false . |


### Set up the listening interface

**The monitoring interface life cycle needs to be managed, no monitoring is needed, please call remove**


#### Add original brainwave monitoring

**Method Description**

Add original brain wave monitoring, through which the original brain wave data can be obtained from the hardware



**Sample Code**

```kotlin

  var rawDataListener = fun(data:ByteArray){

        Logger.d(Arrays.toString(data))

  }
  biomoduleBleManager.addRawDataListener(rawDataListener)

```


**Parameter description**

| Parameter    | Type    | Description   |
| --------------- | ----------------- | ------------ |
| rawDataListener | ( ByteArray ) ->Unit | Raw brainwave callback |


> **Explanation of raw brainwave data**
>
> The original brainwave data returned from the brainwave callback is a byte array with a length of 20, the first two bytes are the packet number, the last 18 bytes are valid brainwave data, of which the brainwave data is divided into two left and right aisle,
> In order: left, left, left, right, right, right, left, left, left, right, right, right, left, left, left, right, right, right

> **Example of normal data**
>
> [0, -94, 21, -36, 125, 21, -12, -75, 22, 8, 61, 22, 10, -72, 22, 15, -19,20,10,8]
>
> **Example of abnormal data (no brain wave data detected)**
>
> [0, 101, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 ,-1,-1,-1]

#### Remove the original brainwave monitor

**Method Description**

If you don’t want to receive brainwave data, just remove the monitor


**Sample Code**



```kotlin

biomoduleBleManager.removeRawDataListener(rawDataListener)

```

**Parameter description**

| Parameter     | Type   | Description   |
| --------------- | ------------------- | ------------ |
| rawDataListener | ( ByteArray ) ->Unit | Raw brainwave callback |

#### Add heart rate monitor
**Method Description**

Add a heart rate monitor, through which heart rate data can be obtained from the hardware

**Sample Code**

```kotlin

var heartRateListener = fun(heartRate: Int) {
    Logger.d("heart rate data is "+ heartRate)
}

biomoduleBleManager.addHeartRateListener(heartRateListener)

```
**Parameter description**

| Parameter              | Type    | Description  |
| ----------------- | ------------- | ---------------- |
| heartRateListener | ( Int ) ->Unit | Heart rate data acquisition callback |

#### Remove heart rate monitor

**Method Description**

If you don’t want to receive your heart rate, just remove the monitor

**Sample Code**



```kotlin

biomoduleBleManager.removeHeartRateListener(heartRateListener)

```
**Parameter description**

| Parameter    | Type  | Description  |
| ----------------- | ------------- | ------------ |
| heartRateListener | ( Int ) ->Unit | Heart Rate Data Callback |

#### Add wearing signal monitoring

**Method Description**

Add this monitor, you can get the quality of equipment wearing in real time

**Code Example**



```kotlin

contactListener = fun(state: Int) {
    Logger.d("Whether the wearing contact is good:"+ state == 0);
}

biomoduleBleManager.addContactListener(contactListener)


```
**Parameter description**

| Parameter            | Type        | Description    |
| --------------- | ---------------------- | --------------- |
| contactListener | ( Int ) ->Unit | Wear signal callback.  0 means normal wearing, other values ​​mean abnormal wearing |

#### Remove wearing signal monitor
**Method Description**

Remove the monitor, you won’t receive the wear signal

**Code Example**

```kotlin

biomoduleBleManager.removeContactListener(contactListener)

```
**Parameter description**


| Parameter      | Type   | Description         |
| --------------- | ---------------------- | ----------- |
| contactListener | ( Int ) ->Unit | Wear signal callback |


#### Add battery monitor

**Method Description**

Add battery monitor, after adding it will call back every 30 seconds

**Code Example**

```kotlin

var batteryListener = fun(byte: Byte) {

   Logger.d("battery = $byte")

}

biomoduleBleManager.addBatteryListener(batteryListener)

```
**Parameter description**

| Parameter      | Type      | Description     |
| --------------- | --------------- | -------- |
| batteryListener | ( Byte ) -> Unit | Battery Callback |

#### Remove battery monitor
**Method Description**
After removal, you will not receive a battery callback

**Code Example**
```kotlin

biomoduleBleManager.removeBatteryListener(batteryListener)

```

**Parameter description**

| Parameter            | Type     | Description     |
| --------------- | --------------- | -------- |
| batteryListener | ( Byte ) -> Unit | Battery Callback |

#### Add battery voltage monitor
**Method Description**

Add battery voltage monitor, it will be called back every 30 seconds after adding

**Code Example**

```kotlin

var batteryVoltageListener = fun(voltage: Double) {

   Logger.d("battery voltage = $voltage")

}
biomoduleBleManager.addBatteryVoltageListener(batteryVoltageListener)

```
**Parameter description**

| Parameter            | Type            | Description     |
| --------------- | --------------- | -------- |
| batteryVoltageListener | ( Double ) -> Unit | Battery Voltage Callback |



#### Remove battery voltage monitor

**Method Description**

After removal, you will not receive battery voltage callback

**Code Example**



```kotlin

biomoduleBleManager.removeBatteryVoltageListener(batteryVoltageListener)

```
**Parameter description**

| Parameter            | Type            | Description     |
| --------------- | --------------- | -------- |
| batteryVoltageListener | ( Double ) -> Unit | Battery Voltage Callback |

The battery power can be calculated based on the battery voltage,The default battery specifications (model 401015 , capacity 40mAh, rated voltage 4.1V ) calculation formula is as follows:
```
The known voltage is x (unit: V)
[1] Remaining power percentage q (unit: %; value range: 0~100) expression:
q = a1*exp(-((x-b1)/c1)^2) + a2*exp(-((x-b2)/c2)^2) + a3*exp(-((x-b3)/ c3)^2) # Gaussian fitting curve
q = q*1.13-5 # Tibetan electricity, the upper limit is 8 and the lower limit is 5
To
q = max([min([q, 100]), 0]) # The value range is limited to 0~100
The parameter values ​​are as follows:
       a1 = 99.84
       b1 = 4.244
       c1 = 0.3781
       a2 = 21.38
       b2 = 3.953
       c2 = 0.1685
       a3 = 15.21
       b3 = 3.813
       c3 = 0.09208
[2] Remaining use time t (unit: min) expression:
t = 3.84*q # The original time length estimation factor is 4.52, and the conservative estimate is 85%, so it is changed to 3.84
```

### Collection and stop

#### Start brainwave data collection

**Method Description**

Start brain wave data collection, call this interface to start collecting brain wave data


**Sample Code**

```kotlin

biomoduleBleManager.startBrainCollection()

```
#### Stop brainwave data collection

**Method Description**

Stop collecting, call this method to stop collecting brain wave data


**Sample Code**

```kotlin

biomoduleBleManager.stopBrainCollection()

```
#### Start heart rate data collection

**Method Description**

Start heart rate data collection, call this interface to start collecting heart rate data
**Sample Code**
```kotlin

biomoduleBleManager.startHeartRateCollection()

```
#### Stop heart rate data collection
**Method Description**
Stop collecting, call this method to stop collecting heart rate data
**Sample Code**
```kotlin

biomoduleBleManager.stopHeartRateCollection()

```
#### Start brainwave and heart rate data collection at the same time
**Method Description**

Start heart rate data collection, call this interface to start collecting brain wave and heart rate data at the same time

**Sample Code**

```kotlin

biomoduleBleManager.startHeartAndBrainCollection()

```
#### Stop brainwave and heart rate data collection

**Method Description**

Stop collecting, call this method to stop collecting brain wave and heart rate data
**Sample Code**

```kotlin

biomoduleBleManager.stopHeartAndBrainCollection()

```

### DFU (device firmware upgrade)

Support DFU ( Device Firmware Update ), that is, device remote firmware upgrade. Since the bottom layer of this SDK depends on the Android-DFU-Library library, if you need DFU , you need to add the following dependencies to your build.gradle file:

```groovy

implementation'com.github.santa-cat:Android-DFU-Library:v1.6.1'

```
#### Use of DFU

First, you need to define a service and inherit DfuBaseService , as follows:


```
public class DfuService extends DfuBaseService {

              @Override

              protected Class<? extends Activity> getNotificationTarget() {

                 return NotificationActivity.class;// When you click the notification bar in the firmware upgrade, it will open

                                        //NotificationActivity this Activity

              }
              @Override

              protected boolean isDebug() {

                 // This method indicates whether to print more debug logs

                  return BuildConfig.DEBUG;//BuildConfig.DEBUG is false , if you want to print more logs, return true directly
              }
}
```
The NotificationActivity class code is as follows:

```
public class NotificationActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isTaskRoot()) {
            // Declare the Activity you want to jump here
            final Intent intent = new Intent(this, MyActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtras(getIntent().getExtras());
            startActivity(intent);
        }
        finish();
    }
}
```
> * Note: Don’t forget to register the above service and Activity in the AndroidManifest.xml file *

Next, you need to create a remote upgrade monitor to monitor the various processes in the upgrade process:

```kotlin
private val mDfuProgressListener = object: DfuProgressListenerAdapter() {
        //The device is connecting
        override fun onDeviceConnecting(deviceAddress: String?) {
        }
        //The device is connected
        override fun onDeviceConnected(deviceAddress: String?) {
        }
         // Preparing to start the upgrade
        override fun onDfuProcessStarting(deviceAddress: String?) {
        }
        //The equipment starts to upgrade
        override fun onDfuProcessStarting(deviceAddress: String?) {
        }
        override fun onEnablingDfuMode(deviceAddress: String?) {
        }
         // Firmware verification
        override fun onFirmwareValidating(deviceAddress: String?) {
        }
        //The device is disconnecting
        override fun onDeviceDisconnecting(deviceAddress: String?) {
        }
        // Upgrade completed
        override fun onDfuCompleted(deviceAddress: String?) {
        }
        // Due to unexpected reasons, the upgrade was aborted and the upgrade failed
        override fun onDfuAborted(deviceAddress: String?) {
        }
         // Progress callback during upgrade
        override fun onProgressChanged(deviceAddress: String?, percent: Int, speed: Float, avgSpeed: Float, currentPart: Int, partsTotal: Int) {
        }
         // Upgrade failed
        override fun onError(deviceAddress: String?, error: Int, errorType: Int, message: String?) {
        }
    }
```
#### Register to monitor

To register and unregister the monitor in the corresponding life cycle

**Code Example**
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
#### Start upgrade

```kotlin
val starter = DfuServiceInitiator(deviceMac) // Incoming device mac address
          .setDeviceName(deviceName) // Incoming device name
           .setkeepBond(true) // Whether to keep device binding
// Call this method to make Nordic nrf52832 enter bootloader mode
starter.setUnsafeExperimentalButtonlessServiceInSecureDfuEnabled(true)
// Set file path
starter.setZip(FileUtil.getFirmwareDir() + "/firmware.zip")
// Start to upgrade
starter.start(this, DfuService::class.java)
```

For Android 8.0 and above systems, if you want the DFU service to be able to display the progress bar in the notification bar, you need to create a notification channel. The easiest way is to call the following method:

```kotlin
DfuServiceInitiator.createDfuNotificationChannel(context);
```

### Multi-device connection

Added the MultipleBiomoduleBleManager class to support the connection of multiple devices. MultipleBiomoduleBleManager and BiomoduleBleManager have the same functional interface. The only difference is that the instantiation method is different. BiomoduleBleManager adopts singleton mode, while MultipleBiomoduleBleManager can be instantiated arbitrarily. Each MultipleBiomoduleBleManager corresponds to one ble device, but the number of connected devices will be limited by the mobile terminal. The number of terminal limits will be different, and it needs to be analyzed according to the specific situation.

**Code Example**

```kotlin
// Instantiate the ble management class
var multipleBiomoduleBleManager = MultipleBiomoduleBleManager()
// All the following interface calling methods are the same as the BiomoduleBleManager class, so I won’t repeat them here.
...
```
### C# Calling instructions

>
> Most of the SDK methods can be called by C# , but when it comes to the transfer of arrays, C# will fail to call, such as the brainwave data transfer here. Therefore, if you need to set up brainwave data monitoring in C# , you can use the following methods:
>
> ```c#
> //c# defines the brainwave monitoring class
> class RawBrainDataCallback: AndroidJavaProxy {
>     public RawBrainDataCallback():base("kotlin.jvm.functions.Function1"){
>     }
>     public void invoke(AndroidJavaObject jo){
>         AndroidJavaObject bufferObject = jo.Get<AndroidJavaObject>("Buffer");
>         byte[] buffer = AndroidJNIHelper.ConvertFromJNIArray<byte[]>(bufferObject.GetRawObject());// The buffer data here is the returned brainwave array
>     }
>}
> AndroidJavaClass biomoduleBleManagerJc = new AndroidJavaClass("cn.entertech.ble.BiomoduleBleManager");
> var companion = biomoduleBleManagerJc.GetStatic<AndroidJavaObject>("Companion");
> var biomoduleBleManager = companion.Call<AndroidJavaObject>("getInstance",currentActivity);
> // Instantiate brainwave data monitoring callback
> RawBrainDataCallback rawBrainDataCallback = new RawBrainDataCallback();
> biomoduleBleManager.Call("addRawDataListener4CSharp",rawBrainDataCallback);
> ```




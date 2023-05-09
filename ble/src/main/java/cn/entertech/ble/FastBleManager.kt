package cn.entertech.ble

import android.app.Application
import android.bluetooth.BluetoothGatt
import android.content.Context
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import cn.entertech.ble.utils.*
import com.clj.fastble.BleManager
import com.clj.fastble.callback.*
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.clj.fastble.scan.BleScanRuleConfig
import com.clj.fastble.utils.HexUtil
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

class FastBleManager constructor(context: Context) {
    private var curBleDevice: BleDevice? = null

    init {
        BleManager.getInstance().init(context as Application?)
        setScanRule("")
        BleManager.getInstance()
            .enableLog(true)
            .setReConnectCount(1, 5000)
            .setSplitWriteNum(20)
            .setConnectOverTime(10000).operateTimeout = 5000
    }


    private fun setScanRule(mac: String) {
        val uuids: Array<String>?
        val str_uuid = "0000FF10-1212-abcd-1523-785feabcd123"
        uuids = if (TextUtils.isEmpty(str_uuid)) {
            null
        } else {
            str_uuid.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        }
        var serviceUuids: Array<UUID?>? = null
        if (uuids != null && uuids.size > 0) {
            serviceUuids = arrayOfNulls(uuids.size)
            for (i in uuids.indices) {
                val name = uuids[i]
                val components = name.split("-".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                if (components.size != 5) {
                    serviceUuids[i] = null
                } else {
                    serviceUuids[i] = UUID.fromString(uuids[i])
                }
            }
        }


        val scanRuleConfig = BleScanRuleConfig.Builder()
            .setServiceUuids(serviceUuids) // 只扫描指定的服务的设备，可选
            .setScanTimeOut(10000) // 扫描超时时间，可选，默认10秒
            .build()
        BleManager.getInstance().initScanRule(scanRuleConfig)
    }


    fun isConnected(): Boolean {
        return BleManager.getInstance().isConnected(curBleDevice)
    }


    fun disConnect(){
        BleManager.getInstance().disconnect(curBleDevice)
        curBleDevice=null;
    }


    fun scanMacAndConnect(mac: String, timeout: Long = RxBleManager.SCAN_TIMEOUT, success: ((String) -> Unit)?, failure: ((String) -> Unit)?) {
        setScanRule("")
        BleManager.getInstance().scanAndConnect(object : BleScanAndConnectCallback() {
            override fun onScanStarted(success: Boolean) {
                Log.d("cpTest", "onScanStarted")
            }

            override fun onScanning(bleDevice: BleDevice) {
                Log.d("cpTest", "onScanning")
            }

            override fun onScanFinished(scanResult: BleDevice) {
                Log.d("cpTest", "onScanFinished")
            }

            override fun onStartConnect() {
                Log.d("cpTest", "onStartConnect")
            }

            override fun onConnectFail(bleDevice: BleDevice, exception: com.clj.fastble.exception.BleException) {
                Log.d("cpTest", "onConnect fail " + bleDevice.mac)
                if (curBleDevice != null) {
                    mHandler.postDelayed(reConnectRunnable, 5000)
                }
                disConnectListeners.forEach {
                    it.invoke("conn error:${exception}")
                }

            }

            override fun onConnectSuccess(bleDevice: BleDevice, gatt: BluetoothGatt, status: Int) {
                Log.d("cpTest", "onConnect success first " + bleDevice.mac)
                curBleDevice = bleDevice
         //       startHeartAndBrainCollection()
                initNotification()
                connectListeners.forEach {
                    it.invoke(bleDevice.mac)
                }

                FileUtils.writeLocalFile(bleDevice.mac)

            }

            override fun onDisConnected(
                isActiveDisConnected: Boolean,
                device: BleDevice,
                gatt: BluetoothGatt,
                status: Int
            ) {
                stopNotify()
                stopHeartAndBrainCollection()
                if (curBleDevice != null) {
                    mHandler.postDelayed(reConnectRunnable, 5000)
                }
                disConnectListeners.forEach {
                    it.invoke("conn error disconnect")
                }
            }
        })
    }

    fun scanNearDeviceAndConnect(successScan: (() -> Unit)?, failScan: ((Exception) -> Unit)?,
                                 successConnect: ((String) -> Unit)?, failure: ((String) -> Unit)?) {
        Log.d("cpTest","start scan")
        setScanRule("")
        BleManager.getInstance().scanAndConnect(object : BleScanAndConnectCallback() {
            override fun onScanStarted(success: Boolean) {
                Log.d("cpTest", "onScanStarted")
            }

            override fun onScanning(bleDevice: BleDevice) {
                Log.d("cpTest", "onScanning")
            }

            override fun onScanFinished(scanResult: BleDevice) {
                Log.d("cpTest", "onScanFinished")
            }

            override fun onStartConnect() {
                Log.d("cpTest", "onStartConnect")
            }

            override fun onConnectFail(bleDevice: BleDevice, exception: com.clj.fastble.exception.BleException) {
                Log.d("cpTest", "onConnectFail " + bleDevice.mac)
                if (curBleDevice != null) {
                    mHandler.postDelayed(reConnectRunnable, 5000)
                }
                disConnectListeners.forEach {
                    it.invoke("conn error disconnect")
                }

            }

            override fun onConnectSuccess(bleDevice: BleDevice, gatt: BluetoothGatt, status: Int) {
                Log.d("cpTest", "onConnect success" + bleDevice.mac)
                curBleDevice = bleDevice
                initNotification()
            //    startHeartAndBrainCollection()
                connectListeners.forEach {
                    it.invoke(bleDevice.mac)
                }
                FileUtils.writeLocalFile(bleDevice.mac)

            }

            override fun onDisConnected(
                isActiveDisConnected: Boolean,
                device: BleDevice,
                gatt: BluetoothGatt,
                status: Int
            ) {
                stopNotify()
                if (curBleDevice != null) {
                    mHandler.postDelayed(reConnectRunnable, 5000)
                }
                disConnectListeners.forEach {
                    it.invoke("disconnect  ${device.mac}")
                }
            }
        })
    }


    private val mHandler = Handler()
    var reConnectRunnable = Runnable {
        if (BleManager.getInstance().getConnectState(curBleDevice) == 0) {
            BleManager.getInstance().connect(curBleDevice, bleGattCallback)
        }
    }



    var bleGattCallback: BleGattCallback = object : BleGattCallback() {
        override fun onStartConnect() {

        }

        override fun onConnectFail(bleDevice: BleDevice, e: com.clj.fastble.exception.BleException) {
            Log.d("cpTest", "onConnectFail")
            if (curBleDevice != null) {
                mHandler.postDelayed(reConnectRunnable, 5000)
            }
            disConnectListeners.forEach {
                it.invoke("conn error disconnect")
            }
        }

        override fun onConnectSuccess(bleDevice: BleDevice, bluetoothGatt: BluetoothGatt, i: Int) {
            curBleDevice = bleDevice
       //     startHeartAndBrainCollection()
            initNotification()
            connectListeners.forEach {
                it.invoke(bleDevice.mac)
            }
            FileUtils.writeLocalFile(bleDevice.mac)
        }

        override fun onDisConnected(
            b: Boolean,
            bleDevice: BleDevice,
            bluetoothGatt: BluetoothGatt,
            i: Int
        ) {
            stopNotify()
            if (curBleDevice != null) {
                mHandler.postDelayed(reConnectRunnable, 5000)
            }
            disConnectListeners.forEach {
                it.invoke("conn error disconnect")
            }
        }
    }



    enum class Command(val value: ByteArray) {
        START_BRAIN_COLLECT(ByteArray(1) { 0x01 }),
        STOP_BRAIN_COLLECT(ByteArray(1) { 0x02 }),
        START_HEART_RATE_COLLECT(ByteArray(1) { 0x03 }),
        STOP_HEART_RATE_COLLECT(ByteArray(1) { 0x04 }),
        START_HEART_AND_BRAIN_COLLECT(ByteArray(1) { 0x05 }),
        STOP_HEART_AND_BRAIN_COLLECT(ByteArray(1) { 0x06 }),
        START_CONTACT(ByteArray(1) { 0x07 }),
        STOP_CONTACT(ByteArray(1) { 0x08 }),
        DISCONNECTED(ByteArray(1) { 0x49 }),
        FIND_CONNECTED_DEVICE(ByteArray(1) { 0x79 })
    }


    /**
     * start collect all data
     */
    fun startHeartAndBrainCollection() {
        BleManager.getInstance().write(
            curBleDevice, "0000FF20-1212-abcd-1523-785feabcd123",
            "0000FF21-1212-abcd-1523-785feabcd123",
            Command.START_HEART_AND_BRAIN_COLLECT.value,bleWriteCallback
        )
    }


    /**
     * write command
     */
    fun command(command: RxBleManager.Command, success: ((ByteArray) -> Unit)? = null) {
        BleManager.getInstance().write(
            curBleDevice, "0000FF20-1212-abcd-1523-785feabcd123",
            "0000FF21-1212-abcd-1523-785feabcd123",
            command.value,bleWriteCallback
        )
    }


    fun read(serviceId: String,characterId: String, success: (ByteArray) -> Unit, failure: ((String) -> Unit)?) {
        BleManager.getInstance().read(curBleDevice, serviceId, characterId, object : BleReadCallback() {
            override fun onReadSuccess(bytes: ByteArray) {
                success.invoke(bytes)
            }
            override fun onReadFailure(e: BleException) {
                failure?.invoke(e.toString())
            }
        })
    }

    /**
     * stop collect all data
     */
    fun stopHeartAndBrainCollection() {
        BleManager.getInstance().write(curBleDevice,"0000FF20-1212-abcd-1523-785feabcd123",
            "0000FF21-1212-abcd-1523-785feabcd123"
            ,Command.STOP_HEART_AND_BRAIN_COLLECT.value,bleWriteCallback)
    }


    var bleWriteCallback: BleWriteCallback = object : BleWriteCallback() {
        override fun onWriteSuccess(i: Int, i1: Int, bytes: ByteArray) {
            Log.d("cpTest11","write command success")
        }
        override fun onWriteFailure(e: com.clj.fastble.exception.BleException) {
            Log.d("cpTest11","write command fail "+e.toString())
        }
    }


    private fun initNotification(){
        Thread.sleep(200)
        notifyBrainWave()
        Thread.sleep(100)
        notifyBattery()
        Thread.sleep(100)
        notifyContact()
        Thread.sleep(100)
        notifyHrRate()
        Thread.sleep(200)
     //   startHeartAndBrainCollection()
    }


    val rawDataListeners = CopyOnWriteArrayList<(ByteArray) -> Unit>()
    val rawDataListeners4CSharp = CopyOnWriteArrayList<(ByteArrayBean) -> Unit>()
    val contactListeners = CopyOnWriteArrayList<(Int) -> Unit>()
    val batteryListeners = CopyOnWriteArrayList<(NapBattery) -> Unit>()
    val batteryVoltageListeners = CopyOnWriteArrayList<(Double) -> Unit>()
    val heartRateListeners = CopyOnWriteArrayList<(Int) -> Unit>()

    private fun notifyBrainWave() {
        BleManager.getInstance().notify(curBleDevice,
            "0000FF30-1212-abcd-1523-785feabcd123",
            "0000FF31-1212-abcd-1523-785feabcd123",
            object : BleNotifyCallback() {
                override fun onNotifySuccess() {
                    Log.d("cpTest", "notifyBrainWave success")
               //     startHeartAndBrainCollection()
                }

                override fun onNotifyFailure(e: com.clj.fastble.exception.BleException) {
                    Log.d("cpTest", "nofity failure")
                }

                override fun onCharacteristicChanged(bytes: ByteArray) {
                     Log.d("cpTest", "read data " + HexUtil.encodeHexStr(bytes))
                    rawDataListeners.forEach { listener ->
                        listener.invoke(bytes)
                    }
                }
            }
        )
    }

    private fun notifyBattery() {
        BleManager.getInstance().notify(curBleDevice,
            "0000180F-0000-1000-8000-00805F9B34FB",
            "00002A19-0000-1000-8000-00805F9B34FB",
            object : BleNotifyCallback() {
                override fun onNotifySuccess() {
                    Log.d("cpTest", "notify battery success")
              //        startHeartAndBrainCollection()
                }

                override fun onNotifyFailure(e: com.clj.fastble.exception.BleException) {
                    Log.d("cpTest", "nofity battery failure")
                }

                override fun onCharacteristicChanged(bytes: ByteArray) {
                    Log.d("cpTest", "battery change")
                    batteryListeners.forEach { listener ->
                        listener.invoke(BatteryUtil.getMinutesLeft(bytes[0]))
                    }

                }
            }
        )
    }



    private fun notifyContact() {
        BleManager.getInstance().notify(curBleDevice,
            "0000FF30-1212-abcd-1523-785feabcd123",
            "0000FF32-1212-abcd-1523-785feabcd123",
            object : BleNotifyCallback() {
                override fun onNotifySuccess() {
                    Log.d("cpTest", "nofity contract success")

                }

                override fun onNotifyFailure(e: com.clj.fastble.exception.BleException) {
                    Log.d("cpTest", "nofity contract failure "+e.toString())
                }

                override fun onCharacteristicChanged(bytes: ByteArray) {
                    Log.d("cpTest", "read contract data " + HexUtil.encodeHexStr(bytes))
                    contactListeners.forEach { listener ->
                        listener.invoke(CharUtil.converUnchart(bytes[0]))
                    }
                }
            }
        )
    }


    private fun notifyHrRate() {
        BleManager.getInstance().notify(curBleDevice,
            "0000FF50-1212-abcd-1523-785feabcd123",
            "0000FF51-1212-abcd-1523-785feabcd123",
            object : BleNotifyCallback() {
                override fun onNotifySuccess() {
                    Log.d("cpTest", "nofity hr rate success")
                }

                override fun onNotifyFailure(e: com.clj.fastble.exception.BleException) {
                    Log.d("cpTest", "nofity  hr rate failure")
                }

                override fun onCharacteristicChanged(bytes: ByteArray) {
                    Log.d("cpTest", "read HrData " + HexUtil.encodeHexStr(bytes))
                    heartRateListeners.forEach { listener ->
                        listener.invoke(CharUtil.converUnchart(bytes[0]))
                    }
                }
            }
        )
    }



    private fun stopNotify() {
        BleManager.getInstance().stopNotify(
            curBleDevice,
            "0000FF30-1212-abcd-1523-785feabcd123",
            "0000FF31-1212-abcd-1523-785feabcd123"
        )
        Thread.sleep(100)
        BleManager.getInstance().stopNotify(
            curBleDevice,
            "0000180F-0000-1000-8000-00805F9B34FB",
            "00002A19-0000-1000-8000-00805F9B34FB"
        )

        Thread.sleep(100)
        BleManager.getInstance().stopNotify(
            curBleDevice,
            "0000FF30-1212-abcd-1523-785feabcd123",
            "0000FF32-1212-abcd-1523-785feabcd123",
        )

        Thread.sleep(100)
        BleManager.getInstance().stopNotify(
            curBleDevice,
            "0000FF50-1212-abcd-1523-785feabcd123",
             "0000FF51-1212-abcd-1523-785feabcd123",
        )
    }



    val disConnectListeners = mutableListOf<(String) -> Unit>()
    val connectListeners = mutableListOf<(String) -> Unit>()

    /**
     * add device disconnect listener
     */
    fun addDisConnectListener(listener: (String) -> Unit) {
        disConnectListeners.add(listener)
    }

    /**
     * remove device disconnect listener
     */
    fun removeDisConnectListener(listener: (String) -> Unit) {
        disConnectListeners.remove(listener)
    }

    /**
     * add device connect listener
     */
    fun addConnectListener(listener: (String) -> Unit) {
        connectListeners.add(listener)
    }

    /**
     * remove device connect listener
     */
    fun removeConnectListener(listener: (String) -> Unit) {
        connectListeners.remove(listener)
    }


    /**
     * add raw brain data listener
     */
    fun addRawDataListener(listener: (ByteArray) -> Unit) {
        rawDataListeners.add(listener)

    }

    /**
     * add raw brain data listener
     */
    fun addRawDataListener4CSharp(listener: (ByteArrayBean) -> Unit) {
        rawDataListeners4CSharp.add(listener)
    }

    fun removeRawDataListener4CSharp(listener: (ByteArrayBean) -> Unit) {
        rawDataListeners4CSharp.remove(listener)
    }


    /**
     * remove raw brain data listener
     */
    fun removeRawDataListener(listener: (ByteArray) -> Unit) {
        rawDataListeners.remove(listener)
    }

    /**
     * add device contact listener
     */
    fun addContactListener(listener: (Int) -> Unit) {
        contactListeners.add(listener)
    }

    /**
     * remove device contact listener
     */
    fun removeContactListener(listener: (Int) -> Unit) {
        contactListeners.remove(listener)
    }

    /**
     * add device battery listener
     */
    fun addBatteryListener(listener: (NapBattery) -> Unit) {
        batteryListeners.add(listener)
    }

    /**
     * remove device battery listener
     */
    fun removeBatteryListener(listener: (NapBattery) -> Unit) {
        batteryListeners.remove(listener)
    }

    /**
     * add device battery voltage listener
     */
    fun addBatteryVoltageListener(listener: (Double) -> Unit) {
        batteryVoltageListeners.add(listener)
    }

    /**
     * remove device battery voltage listener
     */
    fun removeBatteryVoltageListener(listener: (Double) -> Unit) {
        batteryVoltageListeners.remove(listener)
    }


    /**
     * add device heart rate listener
     */
    fun addHeartRateListener(listener: (Int) -> Unit) {
        heartRateListeners.add(listener)
    }

    /**
     * remove device heart rate listener
     */
    fun removeHeartRateListener(listener: (Int) -> Unit) {
        heartRateListeners.remove(listener)
    }

}
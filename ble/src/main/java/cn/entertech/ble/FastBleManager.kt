package cn.entertech.ble

import android.app.Application
import android.bluetooth.BluetoothGatt
import android.content.Context
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import com.clj.fastble.BleManager
import com.clj.fastble.callback.*
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.clj.fastble.scan.BleScanRuleConfig
import com.clj.fastble.utils.HexUtil
import java.util.*

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
                Log.d("cpTest", "onConnect success " + bleDevice.mac)
                if (curBleDevice != null) {
                    mHandler.postDelayed(reConnectRunnable, 5000)
                }

            }

            override fun onConnectSuccess(bleDevice: BleDevice, gatt: BluetoothGatt, status: Int) {
                Log.d("cpTest", "onConnect success" + bleDevice.mac)
                curBleDevice = bleDevice
                initNotification()

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
                Log.d("cpTest", "onConnect success " + bleDevice.mac)
                if (curBleDevice != null) {
                    mHandler.postDelayed(reConnectRunnable, 5000)
                }

            }

            override fun onConnectSuccess(bleDevice: BleDevice, gatt: BluetoothGatt, status: Int) {
                Log.d("cpTest", "onConnect success" + bleDevice.mac)

                curBleDevice = bleDevice
                initNotification()

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

        }

        override fun onConnectSuccess(bleDevice: BleDevice, bluetoothGatt: BluetoothGatt, i: Int) {
            curBleDevice = bleDevice
            startHeartAndBrainCollection()
            initNotification()
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
            ,Command.STOP_BRAIN_COLLECT.value,bleWriteCallback)
    }


    var bleWriteCallback: BleWriteCallback = object : BleWriteCallback() {
        override fun onWriteSuccess(i: Int, i1: Int, bytes: ByteArray) {
            Log.d("cpTest","write success")
        }
        override fun onWriteFailure(e: com.clj.fastble.exception.BleException) {
            Log.d("cpTest","write fail")
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
    }



    private fun notifyBrainWave() {
        BleManager.getInstance().notify(curBleDevice,
            "0000FF30-1212-abcd-1523-785feabcd123",
            "0000FF31-1212-abcd-1523-785feabcd123",
            object : BleNotifyCallback() {
                override fun onNotifySuccess() {
                    Log.d("cpTest", "nofity success")
                    startHeartAndBrainCollection()
                }

                override fun onNotifyFailure(e: com.clj.fastble.exception.BleException) {
                    Log.d("cpTest", "nofity failure")
                }

                override fun onCharacteristicChanged(bytes: ByteArray) {
                    Log.d("cpTest", "read data " + HexUtil.encodeHexStr(bytes))
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
                    Log.d("cpTest", "nofity success")
                    //   startHeartAndBrainCollection()
                }

                override fun onNotifyFailure(e: com.clj.fastble.exception.BleException) {
                    Log.d("cpTest", "nofity failure")
                }

                override fun onCharacteristicChanged(bytes: ByteArray) {


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
                    Log.d("cpTest", "nofity success")
                    startHeartAndBrainCollection()
                }

                override fun onNotifyFailure(e: com.clj.fastble.exception.BleException) {
                    Log.d("cpTest", "nofity failure")
                }

                override fun onCharacteristicChanged(bytes: ByteArray) {
                    Log.d("cpTest", "read data " + HexUtil.encodeHexStr(bytes))

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
                    startHeartAndBrainCollection()
                }

                override fun onNotifyFailure(e: com.clj.fastble.exception.BleException) {
                    Log.d("cpTest", "nofity  hr rate failure")
                }

                override fun onCharacteristicChanged(bytes: ByteArray) {
                    Log.d("cpTest", "read data " + HexUtil.encodeHexStr(bytes))
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

}
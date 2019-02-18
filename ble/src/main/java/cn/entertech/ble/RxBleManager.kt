package cn.entertech.ble

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.os.ParcelUuid
import android.util.Log
import cn.entertech.ble.util.*
import com.orhanobut.logger.Logger
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.RxBleDevice
import com.polidea.rxandroidble2.exceptions.BleException
import com.polidea.rxandroidble2.scan.ScanFilter
import com.polidea.rxandroidble2.scan.ScanResult
import com.polidea.rxandroidble2.scan.ScanSettings
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import java.util.*
import kotlin.concurrent.schedule

import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit


/**
 * Created by EnterTech on 2017/10/30.
 */
class RxBleManager private constructor(context: Context) {

    private var rxBleClient: RxBleClient
    private var rxBleDevice: RxBleDevice? = null
    private var subscription: Disposable? = null
    private var rxBleConnection: RxBleConnection? = null
    private var isShakeHandPassed = false
    private lateinit var handlerThread: HandlerThread
    private lateinit var handler: Handler
    private lateinit var scanNearSubscription: Disposable
    private lateinit var scanSubscription: Disposable
    private val SCAN_TIMEOUT: Long = 10000
    private val DURATION_OF_SORT: Long = 5000
    private val CONNECT_TASK_DELAY: Long = 2000

    init {
        rxBleClient = RxBleClient.create(context)

        handlerThread = HandlerThread("shake_hand")
        handlerThread.start()
        handler = Handler(handlerThread.looper)
        RxJavaPlugins.setErrorHandler { error ->
            if (error is UndeliverableException && error.cause is BleException) {
                return@setErrorHandler // ignore BleExceptions as they were surely delivered at least once
            }
            // add other custom handlers if needed
            throw error
        }
    }

    companion object {
        @Volatile
        private var instance: RxBleManager? = null

        fun getInstance(context: Context): RxBleManager {
            if (instance == null) {
                synchronized(RxBleManager::class) {
                    if (instance == null) {
                        instance = RxBleManager(context.applicationContext)
                    }
                }
            }

            return instance!!
        }
    }


    /**
     * is device connected
     */
    fun isConnected(): Boolean {
        rxBleDevice?.let {
            return it.connectionState == RxBleConnection.RxBleConnectionState.CONNECTED
        }

        return false
    }


    /**
     * connect close device
     */
    fun scanNearDeviceAndConnect(successScan: (() -> Unit)?,
                                 successConnect: ((String) -> Unit)?, failure: ((String) -> Unit)?) {

        BleUtil.removePairDevice()
        if (isConnected()) disConnect()
        var scanStartTime = System.currentTimeMillis()
        var isScanSuccess = false
        var nearScanResult: ScanResult? = null
        scanNearSubscription = rxBleClient.scanBleDevices(
                ScanSettings.Builder()
                        .build(),
                ScanFilter.Builder().setServiceUuid(ParcelUuid(UUID.fromString(NapBleDevice.NAPTIME.uuid)))
                        .build()

        ).timeout(SCAN_TIMEOUT, TimeUnit.MILLISECONDS).subscribe(
                { scanResult ->
                    if (null == nearScanResult || scanResult.rssi > nearScanResult!!.rssi) {
                        nearScanResult = scanResult
                    }
                    if (System.currentTimeMillis() >= scanStartTime + DURATION_OF_SORT) {
                        scanNearSubscription.dispose()
                        if (!isScanSuccess) {
                            isScanSuccess = true
                            successScan?.invoke()
                            Timer().schedule(CONNECT_TASK_DELAY) {
                                handler.post {
                                    if (null == scanResult) {
                                        failure?.invoke("scan error 1")
                                    } else {
                                        connect(nearScanResult!!, true, successConnect, failure)
                                    }
                                }
                            }
                        }
                    }
                }, {
            it.printStackTrace()
        })
    }


    /**
     * connect device
     */
    fun connect(scanResult: ScanResult, auto: Boolean = true, success: ((String) -> Unit)?, failure: ((String) -> Unit)?) {
        rxBleDevice = rxBleClient.getBleDevice(scanResult.bleDevice.macAddress)

        subscription = rxBleDevice!!.establishConnection(false)
                .subscribe({ rxBleConnection ->
                    this.rxBleConnection = rxBleConnection
                    Logger.d("conn succ")
                    success?.invoke(scanResult.bleDevice.macAddress)
                }, { throwable ->
                    isConnecting = false
                    disConnectListeners.forEach {
                        it.invoke("conn error")
                    }
                    failure?.invoke("conn error")
                })
    }


    /**
     * is device connecting
     */
    fun isConnecting(): Boolean {
        return isConnecting
    }

    private var isConnecting = false
    /**
     * connect device by mac address
     */
    fun scanMacAndConnect(mac: String, success: ((String) -> Unit)?, failure: ((String) -> Unit)?, timeout: Long = 5000, auto: Boolean = true) {
        BleUtil.removePairDevice()
        var isScanSuccess = false
        isConnecting = true
        scanSubscription = rxBleClient.scanBleDevices(
                ScanSettings.Builder()
                        .build(),
                ScanFilter.Builder().setDeviceAddress(mac)
                        .build()
        ).timeout(timeout, TimeUnit.MILLISECONDS)
                .subscribe(
                        { scanResult ->
                            if (!isScanSuccess) {
                                isScanSuccess = true
                                Timer().schedule(2000) {
                                    handler.post {
                                        if (null == scanResult) {
                                            failure?.invoke("scan error 1")
                                            isConnecting = false
                                        } else {
                                            scanSubscription.dispose()
                                            connect(scanResult, true, success, failure)
                                        }
                                    }
                                }
                            }
                        }, {
                    isConnecting = false
                    it.printStackTrace()
                    failure?.invoke("scan error")
                })
    }

    /**
     * disconnect device
     */
    fun disConnect() {
        subscription?.dispose()
        disConnectListeners.forEach {
            it.invoke("disconnect")
        }
    }

    enum class Command(val value: ByteArray) {
        START_BRAIN_COLLECT(ByteArray(1) { 0x01 }),
        STOP_BRAIN_COLLECT(ByteArray(1) { 0x02 }),
        START_HEART_RATE_COLLECT(ByteArray(1) { 0x03 }),
        STOP_HEART_RATE_COLLECT(ByteArray(1) { 0x04 }),
        START_HEART_AND_BRAIN_COLLECT(ByteArray(1) { 0x05 }),
        STOP_HEART_AND_BRAIN_COLLECT(ByteArray(1) { 0x06 }),
        DISCONNECTED(ByteArray(1) { 0x49 })
    }

    /**
     * write command
     */
    fun command(command: Command, success: (() -> Unit)? = null) {
        write(NapBleCharacter.CMD_DOWNLOAD.uuid, command.value, fun() {
            Logger.d("succ command")
            success?.invoke()
        }, fun(_) {
            Logger.d("fail command")
        })
    }

    /**
     * notify command
     */
    fun notifyCommand(command: (Command) -> Unit) {
        notify(NapBleCharacter.CMD_UPLOAD.uuid, fun(bytes: ByteArray) {

        }, null)
    }

    /**
     * read battery
     */
    fun readBattery(success: (Byte) -> Unit, failure: ((String) -> Unit)?) {
        read(NapBleCharacter.BATTERY_LEVEL.uuid, fun(bytes: ByteArray) {
//            success.invoke(BatteryUtil.getMinutesLeft(bytes[0]).percent.toByte())
            success.invoke(bytes[0])
        }, failure)
    }

    /**
     * notify battery
     */
    fun notifyBattery(success: (Byte) -> Unit, failure: ((String) -> Unit)? = null): Disposable? {
        return notify(NapBleCharacter.BATTERY_LEVEL.uuid, fun(bytes: ByteArray) {
            success.invoke(bytes[0])
        }, failure)
    }


    /**
     * notify heart rate
     */
    fun notifyHeartRate(success: (ByteArray) -> Unit, failure: ((String) -> Unit)? = null): Disposable? {
        return notify(NapBleCharacter.HEART_RATE.uuid, fun(bytes: ByteArray) {
            Log.d("###","heart rate is "+bytes)
            success.invoke(bytes)
        }, failure)
    }

    /**
     * notify brain
     */
    fun notifyBrainWave(onNotify: (ByteArray) -> Unit): Disposable? {
        return notify(NapBleCharacter.EEG_DATA.uuid, onNotify, null)
    }

    /**
     * read device serial
     */
    fun readDeviceSerial(success: (String) -> Unit, failure: ((String) -> Unit)?) {
        readDeviceInfo(NapBleCharacter.DEVICE_SERIAL.uuid, success, failure)
    }

    /**
     * read device firmware
     */
    fun readDeviceFirmware(success: (String) -> Unit, failure: ((String) -> Unit)?) {
        readDeviceInfo(NapBleCharacter.DEVICE_FIRMWARE.uuid, success, failure)
    }

    /**
     * read device hardware
     */
    fun readDeviceHardware(success: (String) -> Unit, failure: ((String) -> Unit)?) {
        readDeviceInfo(NapBleCharacter.DEVICE_HARDWARE.uuid, success, failure)
    }

    /**
     * read device manufacturer
     */
    fun readDeviceManufacturer(success: (String) -> Unit, failure: ((String) -> Unit)?) {
        readDeviceInfo(NapBleCharacter.DEVICE_MANUFACTURER.uuid, success, failure)
    }

    /**
     * read device info
     */
    private fun readDeviceInfo(characterId: String, success: (String) -> Unit, failure: ((String) -> Unit)?) {
        read(characterId, fun(bytes: ByteArray) {
            success.invoke(String(bytes, StandardCharsets.UTF_8))
        }, failure)
    }

//    //write DFU command
//    fun writeDFU(data: ByteArray, callback: BleCharacterCallback) {
//        bleManager.writeDevice(NapBleService.DFU.uuid, NapBleCharacter.DFU_CTRL.uuid, data, callback)
//    }
//
//    //notify DFU
//    fun notifyDFU(callback: BleCharacterCallback) {
//        bleManager.notify(NapBleService.DFU.uuid, NapBleCharacter.DFU_CTRL.uuid, callback)
//    }
//
//    //write no response
//    fun write(data: ByteArray) {
//        bleManager.writeDevice(NapBleService.DFU.uuid, NapBleCharacter.DFU_PKT.uuid, data, null)
//    }

    /**
     * read characteristic
     */
    fun read(characterId: String, success: (ByteArray) -> Unit, failure: ((String) -> Unit)?) {
        rxBleConnection?.let {
            it.readCharacteristic(UUID.fromString(characterId))
                    .subscribe(
                            { characteristicValue ->
                                success.invoke(characteristicValue)
                            },
                            { throwable ->
                                // Handle an error here.
                                Logger.d("read error $throwable")
                                failure?.invoke("read error $throwable")
                            }
                    )
        }
    }

    /**
     * write characteristic
     */
    private fun write(characterId: String, bytes: ByteArray, success: (() -> Unit)? = null, failure: ((String) -> Unit)?) {
        rxBleConnection?.let {
            it.writeCharacteristic(UUID.fromString(characterId), bytes)
                    .subscribe(
                            { characteristicValue ->
                                success?.invoke()
                            },
                            { throwable ->
                                // Handle an error here.
                                Logger.d("write error $throwable")
                                failure?.invoke("write error")
                            }
                    )
        }
    }

    /**
     * notify characteristic
     */
    private fun notify(characterId: String, success: (ByteArray) -> Unit, failure: ((String) -> Unit)?): Disposable? {
        return rxBleConnection?.let {
            it.setupNotification(UUID.fromString(characterId))
                    .flatMap({ notificationObservable -> notificationObservable })
                    .subscribe(
                            { characteristicValue ->
                                //                                Logger.d(Arrays.toString(characteristicValue))
                                success.invoke(characteristicValue)
                            },
                            { throwable ->
                                // Handle an error here.
                                Logger.d("notify error $throwable ")
//                                failure?.invoke("notify error")
                            }
                    )

        }
    }

    /**
     * notify contact
     */
    fun notifyContact(onNotify: (Byte) -> Unit): Disposable? {
        return notify(NapBleCharacter.CONTACT_DATE.uuid, fun(bytes: ByteArray) {
//            Logger.d("check contact ${bytes[0]}")
            onNotify.invoke(bytes[0])
        }, null)
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
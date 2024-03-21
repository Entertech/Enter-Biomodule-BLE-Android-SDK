package cn.entertech.ble

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.os.ParcelUuid
import cn.entertech.ble.uid.device.BaseBleDeviceFactory
import cn.entertech.ble.uid.service.IBatteryService
import cn.entertech.ble.uid.service.IEegService
import cn.entertech.ble.uid.service.IHrsService
import cn.entertech.ble.utils.*
import cn.entertech.ble.utils.CharUtil.converUnchart
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
import io.reactivex.schedulers.Schedulers
import java.util.*
import kotlin.concurrent.schedule

import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit


/**
 * Created by EnterTech on 2017/10/30.
 */
class RxBleManager constructor(
    context: Context,
    private val bleFactory: BaseBleDeviceFactory
) {

    private var rxBleClient: RxBleClient
    private var rxBleDevice: RxBleDevice? = null
    private var rxBleConnection: RxBleConnection? = null

    private var connectDeviceDisposable: Disposable? = null
    private var handlerThread: HandlerThread
    private var handler: Handler
    private var scanNearSubscription: Disposable? = null
    private var scanSubscription: Disposable? = null
    private val disConnectListeners = mutableListOf<(String) -> Unit>()
    private val connectListeners = mutableListOf<(String) -> Unit>()
    private var isConnecting = false

    companion object {
        const val SCAN_TIMEOUT: Long = 20000
        private const val TAG = "RxBleManager"
        private const val DURATION_OF_SORT: Long = 3000
        private const val CONNECT_TASK_DELAY: Long = 1000
    }

    init {
        rxBleClient = RxBleClient.create(context)
        handlerThread = HandlerThread("shake_hand")
        handlerThread.start()
        handler = Handler(handlerThread.looper)
        RxJavaPlugins.setErrorHandler { error ->
            if (error is UndeliverableException && error.cause is BleException) {
                return@setErrorHandler // ignore BleExceptions as they were surely delivered at least once
            }
            throw error
        }
    }


    /**
     * is device connecting
     */
    fun isConnecting(): Boolean {
        return isConnecting
    }

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
     * is device connected
     */
    fun isConnected(): Boolean {
        rxBleDevice?.let {
            return it.connectionState == RxBleConnection.RxBleConnectionState.CONNECTED
        }

        return false
    }


    fun getDevice(): RxBleDevice? {
        return rxBleDevice
    }

    /**
     * 连接已配对的设备
     * */
    fun connectBondedDevice(
        successConnect: ((String) -> Unit)?,
        failure: ((String) -> Unit)?,
        filter: (String?, String?) -> Boolean = { _, _ -> true }
    ) {
        val bondedDevices = rxBleClient.bondedDevices?.toTypedArray() ?: emptyArray()
        BleLogUtil.d(TAG, "connectBondedDevice : deviceSize ${bondedDevices.size}")
        val filterDevices =
            bondedDevices.filter {
                filter(it.name, it.macAddress)
            }
        BleLogUtil.d(TAG, "connectBondedDevice : filter deviceSize ${filterDevices.size}")
        if (filterDevices.isEmpty()) {
            failure?.invoke("no bonded device")
        } else {
            disConnect()
            val targetDevice = filterDevices[0]
            /*connect(targetDevice, successConnect) {
                BleLogUtil.e(TAG, "connectBondedDevice error $it")
                scanMacAndConnect(
                    targetDevice.macAddress,
                    success = successConnect,
                    failure = failure
                )
            }*/
            connect(targetDevice, successConnect, failure)
        }
    }


    /**
     * connect close device
     */
    fun scanNearDeviceAndConnect(
        scanTimeout: Long = SCAN_TIMEOUT,
        successConnect: ((String) -> Unit)?,
        failure: ((String) -> Unit)?
    ) {
        BleUtil.removePairDevice()
        disConnect()
        val scanStartTime = System.currentTimeMillis()
        var isScanSuccess = false
        var nearScanResult: ScanResult? = null
        isConnecting = true
        scanNearSubscription = rxBleClient.scanBleDevices(
            ScanSettings.Builder()
                .build(),
            ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(UUID.fromString(bleFactory.getBroadcastUUid())))
                .build()

        ).timeout(scanTimeout, TimeUnit.MILLISECONDS).subscribe(
            { scanResult ->
                if (null == nearScanResult || scanResult.rssi > nearScanResult!!.rssi) {
                    nearScanResult = scanResult
                }
                if (System.currentTimeMillis() >= scanStartTime + DURATION_OF_SORT) {
                    scanNearSubscription?.dispose()
                    if (!isScanSuccess) {
                        isScanSuccess = true
                        handler.postDelayed({
                            nearScanResult?.apply {
                                connect(this, successConnect, failure)
                            } ?: kotlin.run {
                                isConnecting = false
                                failure?.invoke("scan error 1")
                            }
                        }, CONNECT_TASK_DELAY)

                    }
                } else {
                    BleLogUtil.d(TAG, "smaller than DURATION_OF_SORT")
                }
            }, {
                failure?.invoke("scan failure ${it.message}")
                scanNearSubscription?.dispose()
                it.printStackTrace()
            })
    }

    fun stopConnectDevice() {
        scanNearSubscription?.dispose()
        scanNearSubscription = null
        scanSubscription?.dispose()
        scanSubscription = null
        connectDeviceDisposable?.dispose()
        connectDeviceDisposable = null
        handler.removeCallbacksAndMessages(null)
    }


    fun connect(device: RxBleDevice, success: ((String) -> Unit)?, failure: ((String) -> Unit)?) {
        isConnecting = true
        //不懂为啥要多写下面这一行
        rxBleDevice = rxBleClient.getBleDevice(device.macAddress)
        connectDeviceDisposable = rxBleDevice?.establishConnection(false)
            ?.subscribe({ rxBleConnection ->
                this.rxBleConnection = rxBleConnection
                BleLogUtil.d(TAG, "conn succ")
                isConnecting = false
                success?.invoke(device.macAddress)
                connectListeners.forEach {
                    it.invoke(device.macAddress)
                }
            }, { throwable ->
                isConnecting = false
                disConnectListeners.forEach {
                    it.invoke("conn error:${throwable}")
                }
                failure?.invoke("conn error:${throwable}")
            })
    }

    /**
     * connect device
     */
    fun connect(
        scanResult: ScanResult,
        success: ((String) -> Unit)?,
        failure: ((String) -> Unit)?
    ) {
        connect(scanResult.bleDevice, success, failure)
    }

    /**
     * connect device by mac address
     */
    fun scanMacAndConnect(
        mac: String,
        timeout: Long = SCAN_TIMEOUT,
        success: ((String) -> Unit)?,
        failure: ((String) -> Unit)?
    ) {
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
                    scanSubscription?.dispose()
                    if (!isScanSuccess) {
                        isScanSuccess = true
                        handler.postDelayed({
                            scanResult?.apply {
                                connect(scanResult, success, failure)
                            } ?: kotlin.run {
                                isConnecting = false
                                failure?.invoke("scan error 1")
                            }
                        }, CONNECT_TASK_DELAY)
                    }
                }, {
                    isConnecting = false
                    scanSubscription?.dispose()
                    it.printStackTrace()
                    failure?.invoke("scan error")
                })
    }

    /**
     * disconnect device
     */
    fun disConnect(isForce: Boolean = false) {
        BleLogUtil.d(TAG, "disConnect")
        val isConnected = isConnected()
        connectDeviceDisposable?.dispose()
        if (isConnected || isForce) {
            disConnectListeners.forEach {
                it.invoke("disconnect")
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
     * write command
     */
    fun command(
        command: Command,
        success: ((ByteArray) -> Unit)? = null,
        failure: ((String) -> Unit)? = null
    ) {
        write(
            bleFactory.getCharacteristicCommandUploadUUid(),
            command.value,
            fun(characteristicValue) {
                BleLogUtil.i(TAG, "succ command")
                success?.invoke(characteristicValue)
            },
            fun(errorMsg) {
                BleLogUtil.i(TAG, "fail command")
                failure?.invoke(errorMsg)
            })
    }


    /**
     * write command
     */
    fun command(
        bytes: ByteArray, success: (() -> Unit)? = null,
        failure: ((String) -> Unit)? = null
    ) {
        write(bleFactory.getCharacteristicCommandUploadUUid(), bytes, fun(characteristicValue) {
            BleLogUtil.i(TAG, "succ command")
            success?.invoke()
        }, fun(errorMsg) {
            BleLogUtil.i(TAG, "fail command")
            failure?.invoke(errorMsg)
        })
    }

    /**
     * read battery
     */
    fun readBattery(success: (Byte) -> Unit, failure: ((String) -> Unit)?) {
        if (bleFactory is IBatteryService) {
            read(bleFactory.getCharacteristicBatteryLevelUUid(), fun(bytes: ByteArray) {
//            success.invoke(BatteryUtil.getMinutesLeft(bytes[0]).percent.toByte())
                success.invoke(bytes[0])
            }, failure)
        }

    }

    /**
     * notify battery
     */
    fun notifyBattery(success: (Byte) -> Unit, failure: ((String) -> Unit)? = null): Disposable? {
        if (bleFactory !is IBatteryService) {
            return null
        }
        return notify(bleFactory.getCharacteristicBatteryLevelUUid(), fun(bytes: ByteArray) {
            success.invoke(bytes[0])
        }, failure)
    }


    /**
     * notify battery voltage
     */
    fun notifyBatteryVoltage(
        success: (Byte) -> Unit,
        failure: ((String) -> Unit)? = null
    ): Disposable? {
        if (bleFactory !is IBatteryService) {
            return null
        }
        return notify(bleFactory.getCharacteristicBatteryLevelUUid(), fun(bytes: ByteArray) {
            success.invoke(bytes[0])
        }, failure)
    }


    /**
     * notify heart rate
     */
    fun notifyHeartRate(success: (Byte) -> Unit, failure: ((String) -> Unit)? = null): Disposable? {
        if (bleFactory !is IHrsService) {
            return null
        }
        return notify(bleFactory.getCharacteristicHrUUid(), fun(bytes: ByteArray) {
            if (bytes.isNotEmpty()) {
                success.invoke(bytes[0])
            } else {
                success.invoke(0)
            }
        }, failure)
    }

    /**
     * notify brain
     */
    fun notifyBrainWave(onNotify: (ByteArray) -> Unit): Disposable? {
        if (bleFactory !is IEegService) {
            return null
        }
        return notify(bleFactory.getCharacteristicEEGUUid(), onNotify, null)
    }

    /**
     * read device serial
     */
    fun readDeviceSerial(success: (String) -> Unit, failure: ((String) -> Unit)?) {
        readDeviceInfo(bleFactory.getCharacteristicDeviceSerialUUid(), success, failure)
    }

    /**
     * read device firmware
     */
    fun readDeviceFirmware(success: (String) -> Unit, failure: ((String) -> Unit)?) {
        readDeviceInfo(bleFactory.getCharacteristicDeviceFirmwareUUid(), success, failure)
    }

    /**
     * read device hardware
     */
    fun readDeviceHardware(success: (String) -> Unit, failure: ((String) -> Unit)?) {
        readDeviceInfo(bleFactory.getCharacteristicDeviceHardwareUUid(), success, failure)
    }

    /**
     * read device manufacturer
     */
    fun readDeviceManufacturer(success: (String) -> Unit, failure: ((String) -> Unit)?) {
        readDeviceInfo(bleFactory.getDeviceManufacturerUuid(), success, failure)
    }

    /**
     * read device mac
     */
    fun readDeviceMac(success: (String) -> Unit, failure: ((String) -> Unit)?) {
        readDeviceInfo(bleFactory.getCharacteristicDeviceMacUUid(), success, failure)
    }

    /**
     * read device info
     */
    private fun readDeviceInfo(
        characterId: String,
        success: (String) -> Unit,
        failure: ((String) -> Unit)?
    ) {
        read(characterId, fun(bytes: ByteArray) {
            success.invoke(String(bytes, StandardCharsets.UTF_8))
        }, failure)
    }

    fun readBatteryByteArray(success: (ByteArray) -> Unit, failure: ((String) -> Unit)?) {
        if (bleFactory !is IBatteryService) {
            return
        }
        read(bleFactory.getCharacteristicBatteryLevelUUid(), success, failure)
    }

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
                        BleLogUtil.i(TAG, "read error $throwable")
                        failure?.invoke("read error $throwable")
                    }
                )
        }
    }

    /**
     * write characteristic
     */
    private fun write(
        characterId: String,
        bytes: ByteArray,
        success: ((ByteArray) -> Unit)? = null,
        failure: ((String) -> Unit)?
    ) {
        rxBleConnection?.let {
            it.writeCharacteristic(UUID.fromString(characterId), bytes)
                .subscribe(
                    { characteristicValue ->
                        success?.invoke(characteristicValue)
                    },
                    { throwable ->
                        // Handle an error here.
                        BleLogUtil.i(TAG, "write error $throwable")
                        failure?.invoke("write error")
                    }
                )
        }
    }

    /**
     * notify characteristic
     */
    private fun notify(
        characterId: String,
        success: (ByteArray) -> Unit,
        failure: ((String) -> Unit)?
    ): Disposable? {
        BleLogUtil.d(TAG, "notify characterId $characterId")
        return rxBleConnection?.let {
            it.setupNotification(UUID.fromString(characterId))
                .flatMap { notificationObservable -> notificationObservable }
                .subscribeOn(Schedulers.io())
                .subscribe(
                    { characteristicValue ->
                        success.invoke(characteristicValue)
                    },
                    { throwable ->
                        // Handle an error here.
                        BleLogUtil.e(TAG, "notify characterId  error $throwable ")
                        failure?.invoke("notify error")
                    }
                )

        }
    }

    /**
     * notify contact
     */
    fun notifyContact(onNotify: (Int) -> Unit): Disposable? {
        if (bleFactory !is IEegService) {
            return null
        }
        return notify(bleFactory.getCharacteristicContactDateMacUUid(), fun(bytes: ByteArray) {
//            BleLogUtil.d("check contact ${converUnchart(bytes[0])}")
            onNotify.invoke(converUnchart(bytes[0]))
        }, null)
    }

}
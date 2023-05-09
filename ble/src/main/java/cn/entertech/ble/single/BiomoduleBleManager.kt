package cn.entertech.ble.single

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import cn.entertech.ble.ContactState
import cn.entertech.ble.FastBleManager
import cn.entertech.ble.RxBleManager
import cn.entertech.ble.RxBleManager.Companion.SCAN_TIMEOUT
import cn.entertech.ble.toEnum
import cn.entertech.ble.utils.*
import io.reactivex.disposables.Disposable
import java.nio.charset.StandardCharsets
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.Exception

class BiomoduleBleManager private constructor(context: Context) {
    val fastBleManager: FastBleManager
    private var handler: Handler
    private var handlerThread: HandlerThread
    val rawDataListeners = CopyOnWriteArrayList<(ByteArray) -> Unit>()
    val rawDataListeners4CSharp = CopyOnWriteArrayList<(ByteArrayBean) -> Unit>()
    val contactListeners = CopyOnWriteArrayList<(Int) -> Unit>()
    val batteryListeners = CopyOnWriteArrayList<(NapBattery) -> Unit>()
    val batteryVoltageListeners = CopyOnWriteArrayList<(Double) -> Unit>()
    val heartRateListeners = CopyOnWriteArrayList<(Int) -> Unit>()
    var brainWaveDisposable: Disposable? = null
    var batteryDisposable: Disposable? = null
    var heartRateDisposable: Disposable? = null
    var contactDisposable: Disposable? = null


    init {
        fastBleManager = FastBleManager(context)
        handlerThread = HandlerThread("notify_thread")
        handlerThread.start()
        handler = Handler(handlerThread.looper)
    }

    companion object {
        @Volatile
        var mBleDeviceManager: BiomoduleBleManager? = null

        fun getInstance(context: Context): BiomoduleBleManager {
            if (mBleDeviceManager == null) {
                synchronized(BiomoduleBleManager::class.java) {
                    if (mBleDeviceManager == null) {
                        mBleDeviceManager = BiomoduleBleManager(context)
                    }
                }
            }
            return mBleDeviceManager!!
        }
    }


    /**
     * is device connect
     */
    fun isConnected(): Boolean {
        return fastBleManager.isConnected()
    }


    /**
     * add device disconnect listener
     */
    fun addDisConnectListener(listener: (String) -> Unit) {
        fastBleManager.addDisConnectListener(listener)
    }

    /**
     * remove device disconnect listener
     */
    fun removeDisConnectListener(listener: (String) -> Unit) {
        fastBleManager.removeDisConnectListener(listener)
    }

    /**
     * add device connect listener
     */
    fun addConnectListener(listener: (String) -> Unit) {
        fastBleManager.addConnectListener(listener)
    }

    /**
     * remove device connect listener
     */
    fun removeConnectListener(listener: (String) -> Unit) {
        fastBleManager.removeConnectListener(listener)
    }



    /**
     * stop notify brain
     */
    fun stopNotifyBrainWave() {
        brainWaveDisposable?.dispose()
    }


    fun notifyBrainWave(){

    }
    /**
     * notify battery
     */
    fun notifyBattery() {

    }

    /**
     * notify heart rate
     */
    fun notifyHeartRate() {

    }

    /**
     * stop notify heart rate
     */
    fun stopNotifyHeartRate() {
        heartRateDisposable?.dispose()
    }


    /**
     * stop notify battery
     */
    fun stopNotifyBattery() {
        batteryDisposable?.dispose()
    }

    /**
     * notify contact
     */
    fun notifyContact() {

    }

    /**
     * stop notify contact
     */
    fun stopNotifyContact() {
        contactDisposable?.dispose()
    }


    /**
     * notify after connect
     */
    private fun initNotifications() {
        Thread.sleep(200)
        notifyBrainWave()
        Thread.sleep(100)
        notifyBattery()
        Thread.sleep(100)
        notifyContact()
        Thread.sleep(100)
        notifyHeartRate()
    }

    /**
     * connect close device
     */
    fun scanNearDeviceAndConnect(
        successScan: (() -> Unit)?,
        failScan: ((Exception) -> Unit),
        successConnect: ((String) -> Unit)?,
        failure: ((String) -> Unit)?
    ) {
        fastBleManager.scanNearDeviceAndConnect(successScan, failScan, fun(mac: String) {
            initNotifications()
            successConnect?.invoke(mac)
        }, failure)
    }

    /**
     * connect device by mac address
     */
    fun scanMacAndConnect(
        mac: String,
        scanTimeout: Long = SCAN_TIMEOUT,
        successConnect: ((String) -> Unit)?,
        failure: ((String) -> Unit)?
    ) {
        fastBleManager.scanMacAndConnect(mac, scanTimeout, fun(mac: String) {
            initNotifications()
            successConnect?.invoke(mac)
        }, failure)
    }

    /**
     * disconnect device
     */
    fun disConnect() {
        fastBleManager.disConnect()
    }

    /**
     * find connected device
     */
    fun findConnectedDevice() {
    //    rxBleManager.command(RxBleManager.Command.FIND_CONNECTED_DEVICE)
    }

    /**
     * add raw brain data listener
     */
    fun addRawDataListener(listener: (ByteArray) -> Unit) {
        rawDataListeners.add(listener)
        fastBleManager.addRawDataListener(listener)

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
        fastBleManager.removeRawDataListener(listener)
    }

    /**
     * add device contact listener
     */
    fun addContactListener(listener: (Int) -> Unit) {
        contactListeners.add(listener)
        fastBleManager.addContactListener(listener)
    }

    /**
     * remove device contact listener
     */
    fun removeContactListener(listener: (Int) -> Unit) {
        contactListeners.remove(listener)
        fastBleManager.removeContactListener(listener)
    }

    /**
     * add device battery listener
     */
    fun addBatteryListener(listener: (NapBattery) -> Unit) {
        batteryListeners.add(listener)
        Log.d("cpTest","addBatteryListener ")
        fastBleManager.addBatteryListener(listener)
    }

    /**
     * remove device battery listener
     */
    fun removeBatteryListener(listener: (NapBattery) -> Unit) {
        batteryListeners.remove(listener)
        fastBleManager.removeBatteryListener(listener)
    }

    /**
     * add device battery voltage listener
     */
    fun addBatteryVoltageListener(listener: (Double) -> Unit) {
        batteryVoltageListeners.add(listener)
        fastBleManager.addBatteryVoltageListener(listener)
    }

    /**
     * remove device battery voltage listener
     */
    fun removeBatteryVoltageListener(listener: (Double) -> Unit) {
        batteryVoltageListeners.remove(listener)
        fastBleManager.removeBatteryVoltageListener(listener)
    }


    /**
     * add device heart rate listener
     */
    fun addHeartRateListener(listener: (Int) -> Unit) {
        heartRateListeners.add(listener)
        fastBleManager.addHeartRateListener(listener)
    }

    /**
     * remove device heart rate listener
     */
    fun removeHeartRateListener(listener: (Int) -> Unit) {
        heartRateListeners.remove(listener)
        fastBleManager.removeHeartRateListener(listener)
    }

    fun startContact() {
        //rxBleManager.command(RxBleManager.Command.START_CONTACT)
        fastBleManager.command(RxBleManager.Command.START_CONTACT)
    }

    fun stopContact() {
        fastBleManager.command(RxBleManager.Command.STOP_CONTACT)
    }

    /**
     * start collect brain data
     */
    fun startBrainCollection() {
        fastBleManager.command(RxBleManager.Command.START_BRAIN_COLLECT)
    }

    /**
     * stop collect brain data
     */
    fun stopBrainCollection() {
        fastBleManager.command(RxBleManager.Command.STOP_BRAIN_COLLECT)
    }

    /**
     * start collect heart rate data
     */
    fun startHeartRateCollection() {
        fastBleManager.command(RxBleManager.Command.START_HEART_RATE_COLLECT)
    }

    /**
     * stop collect heart rate data
     */
    fun stopHeartRateCollection() {

        fastBleManager.command(RxBleManager.Command.STOP_HEART_RATE_COLLECT)
    }

    /**
     * start collect all data
     */
    fun startHeartAndBrainCollection() {
        fastBleManager.startHeartAndBrainCollection()
    }

    /**
     * stop collect all data
     */
    fun stopHeartAndBrainCollection() {
        fastBleManager.stopHeartAndBrainCollection()
    }

    //read battery（readDeviceInfo）
    fun readBattery(success: (NapBattery) -> Unit, failure: ((String) -> Unit)?) {
        fastBleManager.read(NapBleService.BATTERY.uuid,NapBleCharacter.BATTERY_LEVEL.uuid, fun(bytes: ByteArray) {
            success.invoke(BatteryUtil.getMinutesLeft(bytes[0]))
        }, failure)
    }

    /**
     * read device serial（readDeviceInfo）
     */
    fun readDeviceSerial(success: (String) -> Unit, failure: ((String) -> Unit)?) {
        fastBleManager.read(NapBleService.DEVICE_INFO.uuid,NapBleCharacter.DEVICE_SERIAL.uuid, fun(bytes: ByteArray) {
            success.invoke(String(bytes, StandardCharsets.UTF_8))
        }, failure)
    }

    /**
     * read device firmware（readDeviceInfo）
     */
    fun readDeviceFirmware(success: (String) -> Unit, failure: ((String) -> Unit)?) {
        fastBleManager.read(NapBleService.DEVICE_INFO.uuid,NapBleCharacter.DEVICE_FIRMWARE.uuid, fun(bytes: ByteArray) {
            success.invoke(String(bytes, StandardCharsets.UTF_8))
        }, failure)
    }

    /**
     * read device hardware（readDeviceInfo）
     */
    fun readDeviceHardware(success: (String) -> Unit, failure: ((String) -> Unit)?) {
        fastBleManager.read(NapBleService.DEVICE_INFO.uuid,NapBleCharacter.DEVICE_HARDWARE.uuid, fun(bytes: ByteArray) {
            success.invoke(String(bytes, StandardCharsets.UTF_8))
        }, failure)
    }

    /**
     * read device manufacturer（readDeviceInfo）
     */
    fun readDeviceManufacturer(success: (String) -> Unit, failure: ((String) -> Unit)?) {
        fastBleManager.read(NapBleService.DEVICE_INFO.uuid,NapBleCharacter.DEVICE_MANUFACTURER.uuid, fun(bytes: ByteArray) {
            success.invoke(String(bytes, StandardCharsets.UTF_8))
        }, failure)
    }

    fun readDeviceMac(success: (String) -> Unit, failure: ((String) -> Unit)?) {
        fastBleManager.read(NapBleService.DEVICE_INFO.uuid,NapBleCharacter.DEVICE_MAC.uuid, fun(bytes: ByteArray) {
            success.invoke(String(bytes, StandardCharsets.UTF_8))
        }, failure)

    }





}
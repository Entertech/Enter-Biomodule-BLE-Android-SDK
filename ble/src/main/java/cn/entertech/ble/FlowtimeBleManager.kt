package cn.entertech.ble

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import cn.entertech.ble.util.NapBattery
import cn.entertech.ble.util.BatteryUtil
import cn.entertech.ble.util.NapBleCharacter
import io.reactivex.disposables.Disposable

class FlowtimeBleManager private constructor(context: Context) {
    val rxBleManager: RxBleManager
    private lateinit var handler: Handler
    private lateinit var handlerThread: HandlerThread
    val rawDataListeners = mutableListOf<(ByteArray) -> Unit>()
    val contactListeners = mutableListOf<(ContactState) -> Unit>()
    val batteryListeners = mutableListOf<(NapBattery) -> Unit>()
    val heartRateListeners = mutableListOf<(ByteArray) -> Unit>()
    var brainWaveDisposable: Disposable? = null
    var batteryDisposable: Disposable? = null
    var heartRateDisposable: Disposable? = null
    var contactDisposable: Disposable? = null


    init {
        rxBleManager = RxBleManager.getInstance(context)
        handlerThread = HandlerThread("notify_thread")
        handlerThread.start()
        handler = Handler(handlerThread.looper)
    }

    companion object {
        @Volatile
        var mBleDeviceManager: FlowtimeBleManager? = null

        fun getInstance(context: Context): FlowtimeBleManager {
            if (mBleDeviceManager == null) {
                synchronized(FlowtimeBleManager::class.java) {
                    if (mBleDeviceManager == null) {
                        mBleDeviceManager = FlowtimeBleManager(context)
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
        return rxBleManager.isConnected()
    }

    /**
     * is device connecting
     */
    fun isConnecting(): Boolean {
        return rxBleManager.isConnecting()
    }

    /**
     * add device disconnect listener
     */
    fun addDisConnectListener(listener: (String) -> Unit) {
        rxBleManager.addDisConnectListener(listener)
    }

    /**
     * remove device disconnect listener
     */
    fun removeDisConnectListener(listener: (String) -> Unit) {
        rxBleManager.removeDisConnectListener(listener)
    }

    /**
     * add device connect listener
     */
    fun addConnectListener(listener: (String) -> Unit) {
        rxBleManager.addConnectListener(listener)
    }

    /**
     * remove device connect listener
     */
    fun removeConnectListener(listener: (String) -> Unit) {
        rxBleManager.removeConnectListener(listener)
    }

    /**
     * notify brain
     */
    fun notifyBrainWave() {
        brainWaveDisposable = rxBleManager.notifyBrainWave { bytes ->
            bytes.let {
                handler.post {
                    rawDataListeners.forEach { listener ->
                        listener.invoke(it)
                    }
                }

            }
        }
    }

    /**
     * stop notify brain
     */
    fun stopNotifyBrainWave() {
        brainWaveDisposable?.dispose()
    }

    /**
     * notify battery
     */
    fun notifyBattery() {
        batteryDisposable = rxBleManager.notifyBattery(fun(byte: Byte) {
            handler.post {
                byte.let {
                    batteryListeners.forEach { listener ->
                        listener.invoke(BatteryUtil.getMinutesLeft(it))
                    }
                }
            }
        })
    }

    /**
     * notify heart rate
     */
    fun notifyHeartRate() {
        heartRateDisposable = rxBleManager.notifyHeartRate(fun(bytes: ByteArray) {
            handler.post {
                bytes.let {
                    heartRateListeners.forEach { listener ->
                        listener.invoke(bytes)
                    }
                }
            }
        })
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
        contactDisposable = rxBleManager.notifyContact { byte ->
            handler.post {
                byte.let {
                    contactListeners.forEach { listener ->
                        listener.invoke(toEnum(it))
                    }
                }
            }
        }
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
    fun scanNearDeviceAndConnect(successScan: (() -> Unit)?, successConnect: ((String) -> Unit)?, failure: ((String) -> Unit)?) {
        rxBleManager.scanNearDeviceAndConnect(successScan, fun(mac: String) {
            initNotifications()
            successConnect?.invoke(mac)
        }, failure)
    }

    /**
     * connect device by mac address
     */
    fun scanMacAndConnect(mac: String, successConnect: ((String) -> Unit)?, failure: ((String) -> Unit)?) {
        rxBleManager.scanMacAndConnect( mac, fun(mac: String) {
            initNotifications()
            successConnect?.invoke(mac)
        }, failure)
    }

    /**
     * disconnect device
     */
    fun disConnect() {
        rxBleManager.command(RxBleManager.Command.DISCONNECTED) {
            rxBleManager.disConnect()
        }
    }

    /**
     * add raw brain data listener
     */
    fun addRawDataListener(listener: (ByteArray) -> Unit) {
        rawDataListeners.add(listener)
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
    fun addContactListener(listener: (ContactState) -> Unit) {
        contactListeners.add(listener)
    }

    /**
     * remove device contact listener
     */
    open fun removeContactListener(listener: (ContactState) -> Unit) {
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
     * add device heart rate listener
     */
    fun addHeartRateListener(listener: (ByteArray) -> Unit) {
        heartRateListeners.add(listener)
    }

    /**
     * remove device heart rate listener
     */
    fun removeHeartRateListener(listener: (ByteArray) -> Unit) {
        heartRateListeners.remove(listener)
    }

    /**
     * start collect brain data
     */
    fun startBrainCollection() {
        rxBleManager.command(RxBleManager.Command.START_BRAIN_COLLECT)
    }

    /**
     * stop collect brain data
     */
    fun stopBrainCollection() {
        rxBleManager.command(RxBleManager.Command.STOP_BRAIN_COLLECT)
    }

    /**
     * start collect heart rate data
     */
    fun startHeartRateCollection() {
        rxBleManager.command(RxBleManager.Command.START_HEART_RATE_COLLECT)
    }

    /**
     * stop collect heart rate data
     */
    fun stopHeartRateCollection() {
        rxBleManager.command(RxBleManager.Command.STOP_HEART_RATE_COLLECT)
    }

    /**
     * start collect all data
     */
    fun startHeartAndBrainCollection() {
        rxBleManager.command(RxBleManager.Command.START_HEART_AND_BRAIN_COLLECT)
    }

    /**
     * stop collect all data
     */
    fun stopHeartAndBrainCollection() {
        rxBleManager.command(RxBleManager.Command.STOP_HEART_AND_BRAIN_COLLECT)
    }

    //read battery（readDeviceInfo）
    fun readBattery(success: (NapBattery) -> Unit, failure: ((String) -> Unit)?) {
        rxBleManager.read(NapBleCharacter.BATTERY_LEVEL.uuid, fun(bytes: ByteArray) {
//            success.invoke(BatteryUtil.getMinutesLeft(bytes[0]).percent.toByte())
            success.invoke(BatteryUtil.getMinutesLeft(bytes[0]))
        }, failure)
    }

    /**
     * read device serial（readDeviceInfo）
     */
    fun readDeviceSerial(success: (String) -> Unit, failure: ((String) -> Unit)?) {
        rxBleManager.readDeviceSerial(success, failure)
    }

    /**
     * read device firmware（readDeviceInfo）
     */
    fun readDeviceFirmware(success: (String) -> Unit, failure: ((String) -> Unit)?) {
        rxBleManager.readDeviceFirmware(success, failure)
    }

    /**
     * read device hardware（readDeviceInfo）
     */
    fun readDeviceHardware(success: (String) -> Unit, failure: ((String) -> Unit)?) {
        rxBleManager.readDeviceHardware(success, failure)
    }

    /**
     * read device manufacturer（readDeviceInfo）
     */
    fun readDeviceManufacturer(success: (String) -> Unit, failure: ((String) -> Unit)?) {
        rxBleManager.readDeviceManufacturer(success, failure)
    }


}
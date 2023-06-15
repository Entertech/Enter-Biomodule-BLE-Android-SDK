package cn.entertech.ble.single

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import cn.entertech.ble.RxBleManager
import cn.entertech.ble.RxBleManager.Companion.SCAN_TIMEOUT
import cn.entertech.ble.utils.*
import io.reactivex.disposables.Disposable
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.Exception

/**
 * 单设备
 * */
class BiomoduleBleManager private constructor(context: Context) {
    val rxBleManager: RxBleManager
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
        rxBleManager = RxBleManager(context)
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
        private const val TAG="BiomoduleBleManager"
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
                    FirmwareFixHelper.getInstance(rxBleManager).fixFirmware(it)
                    rawDataListeners.forEach { listener ->
                        listener.invoke(it)
                    }
                    rawDataListeners4CSharp.forEach { listener ->
                        listener.invoke(ByteArrayBean(bytes))
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
                    castBattery(it, { napBattery ->
                        batteryListeners.forEach { listener ->
                            listener.invoke(napBattery)
                        }
                    }, null)

                    batteryVoltageListeners.forEach { listener ->
                        listener.invoke(BatteryUtil.getBatteryVoltage(it))
                    }
                }
            }
        })
    }

    /**
     * notify heart rate
     */
    fun notifyHeartRate() {
        heartRateDisposable = rxBleManager.notifyHeartRate(fun(heartRate: Int) {
            handler.post {
                heartRate.let {
                    heartRateListeners.forEach { listener ->
                        listener.invoke(heartRate)
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
                        listener.invoke(it)
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
    fun scanNearDeviceAndConnect(
        successScan: (() -> Unit)?,
        failScan: ((Exception) -> Unit),
        successConnect: ((String) -> Unit)?,
        failure: ((String) -> Unit)?
    ) {
        rxBleManager.scanNearDeviceAndConnect(successScan, failScan, fun(mac: String) {
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
        rxBleManager.scanMacAndConnect(mac, scanTimeout, fun(mac: String) {
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
     * find connected device
     */
    fun findConnectedDevice() {
        rxBleManager.command(RxBleManager.Command.FIND_CONNECTED_DEVICE)
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

    fun startContact() {
        rxBleManager.command(RxBleManager.Command.START_CONTACT)
    }

    fun stopContact() {
        rxBleManager.command(RxBleManager.Command.STOP_CONTACT)
    }

    /**
     * start collect brain data
     */
    fun startBrainCollection() {
        FirmwareFixHelper.getInstance(rxBleManager).startFix()
        rxBleManager.command(RxBleManager.Command.START_BRAIN_COLLECT)
    }

    /**
     * stop collect brain data
     */
    fun stopBrainCollection() {
        FirmwareFixHelper.getInstance(rxBleManager).stopFix()
        rxBleManager.command(RxBleManager.Command.STOP_BRAIN_COLLECT)
    }

    /**
     * start collect heart rate data
     */
    fun startHeartRateCollection() {
        FirmwareFixHelper.getInstance(rxBleManager).startFix()
        rxBleManager.command(RxBleManager.Command.START_HEART_RATE_COLLECT)
    }

    /**
     * stop collect heart rate data
     */
    fun stopHeartRateCollection() {
        FirmwareFixHelper.getInstance(rxBleManager).stopFix()
        rxBleManager.command(RxBleManager.Command.STOP_HEART_RATE_COLLECT)
    }

    /**
     * start collect all data
     */
    fun startHeartAndBrainCollection() {
        FirmwareFixHelper.getInstance(rxBleManager).startFix()
        rxBleManager.command(RxBleManager.Command.START_HEART_AND_BRAIN_COLLECT)
    }

    /**
     * stop collect all data
     */
    fun stopHeartAndBrainCollection() {
        FirmwareFixHelper.getInstance(rxBleManager).stopFix()
        rxBleManager.command(RxBleManager.Command.STOP_HEART_AND_BRAIN_COLLECT)
    }

    //read battery（readDeviceInfo）
    fun readBattery(success: (NapBattery) -> Unit, failure: ((String) -> Unit)?) {
        rxBleManager.read(NapBleCharacter.BATTERY_LEVEL.uuid, fun(bytes: ByteArray) {
            castBattery(bytes[0],success,failure)
        }, failure)
    }

    /**
     * 电量转换
     * */
    private fun castBattery(byte: Byte,success: (NapBattery) -> Unit,failure: ((String) -> Unit)?){
        readDeviceHardware(fun(version){
            BleLogUtil.d(TAG,"currentVersion: $version")
            when(BatteryUtil.compareBleVersion(version,"3.0.0")){
                BatteryUtil.COMPARE_VERSION_VALUE_ERROR_FORMAT->{
                    BleLogUtil.e(TAG,"ble version error")
                    failure?.invoke("")
                }
                //当前版本小于3.0.0
                BatteryUtil.COMPARE_VERSION_VALUE_SMALL->{
                    BleLogUtil.d(TAG,"use old battery")
                    success.invoke(BatteryUtil.getMinutesLeftOld(byte))
                }
                else->{
                    BleLogUtil.d(TAG,"use new battery")
                    success.invoke(BatteryUtil.getMinutesLeft(byte))
                }
            }
        }){
            failure?.invoke("")
        }
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

    fun readDeviceMac(success: (String) -> Unit, failure: ((String) -> Unit)?) {
        rxBleManager.readDeviceMac(success, failure)
    }
}
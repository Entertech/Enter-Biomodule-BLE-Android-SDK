package cn.entertech.ble


import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import cn.entertech.ble.RxBleManager.Companion.SCAN_TIMEOUT
import cn.entertech.ble.fix.BaseFirmwareFixStrategy
import cn.entertech.ble.fix.Firmware0FixHelper
import cn.entertech.ble.utils.BatteryUtil
import cn.entertech.ble.utils.BleLogUtil
import cn.entertech.ble.utils.ByteArrayBean
import cn.entertech.ble.fix.Firmware128FixHelper
import cn.entertech.ble.fix.Firmware255FixHelper
import cn.entertech.ble.fix.IFixTriggerCallback
import cn.entertech.ble.utils.NapBattery
import cn.entertech.ble.utils.NapBleCharacter
import io.reactivex.disposables.Disposable
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 单设备
 * */
abstract class BaseBleConnectManager constructor(
    context: Context,
    private val fixStrategies: List<BaseFirmwareFixStrategy> = listOf(
        Firmware128FixHelper,
        Firmware255FixHelper,
        Firmware0FixHelper
    )
) : IFixTriggerCallback {
    private val rxBleManager: RxBleManager
    private var handler: Handler
    private val mainHandler by lazy {
        Handler(Looper.getMainLooper())
    }
    private var handlerThread: HandlerThread
    private val rawDataListeners = CopyOnWriteArrayList<(ByteArray) -> Unit>()

    /**
     * 皮肤电阻率
     * */
    private val skinConductivityServiceListener = CopyOnWriteArrayList<(ByteArray) -> Unit>()
    private val rawDataListeners4CSharp = CopyOnWriteArrayList<(ByteArrayBean) -> Unit>()
    private val contactListeners = CopyOnWriteArrayList<(Int) -> Unit>()
    private val batteryListeners = CopyOnWriteArrayList<(NapBattery) -> Unit>()
    private val batteryVoltageListeners = CopyOnWriteArrayList<(Double) -> Unit>()
    private val heartRateListeners = CopyOnWriteArrayList<(Int) -> Unit>()
    private var brainWaveDisposable: Disposable? = null
    private var batteryDisposable: Disposable? = null
    private var heartRateDisposable: Disposable? = null
    private var contactDisposable: Disposable? = null


    init {
        rxBleManager = RxBleManager(context)
        handlerThread = HandlerThread("notify_thread")
        handlerThread.start()
        handler = Handler(handlerThread.looper)
    }

    companion object {
        private const val TAG = "BaseBleConnectManager"
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

    var lastNotifyBrainWaveLogTime = 0L


    /**
     * notify brain
     */
    fun notifyBrainWave() {
        brainWaveDisposable = rxBleManager.notifyBrainWave { bytes ->
            if (System.currentTimeMillis() - lastNotifyBrainWaveLogTime > 1000 * 20L) {
                BleLogUtil.d(TAG, "notifyBrainWave")
                lastNotifyBrainWaveLogTime = System.currentTimeMillis()
            }
            handler.post {
                skinConductivityServiceListener.forEach { listener ->
                    listener(bytes)
                }
                bytes.forEach { byte ->
                    fixStrategies.forEach {
                        it.fixFirmware(byte)
                    }
                }
                rawDataListeners.forEach { listener ->
                    listener.invoke(bytes)
                }
                rawDataListeners4CSharp.forEach { listener ->
                    listener.invoke(ByteArrayBean(bytes))
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

    var lastNotifyBatteryLogTime = 0L

    /**
     * notify battery
     */
    fun notifyBattery() {
        batteryDisposable = rxBleManager.notifyBattery(fun(byte: Byte) {
            handler.post {
                if (System.currentTimeMillis() - lastNotifyBatteryLogTime > 1000 * 20L) {
                    BleLogUtil.d(TAG, "notifyBattery")
                    lastNotifyBatteryLogTime = System.currentTimeMillis()
                }
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

    var lastNotifyHeartRateLogTime = 0L

    /**
     * notify heart rate
     */
    fun notifyHeartRate() {
        heartRateDisposable = rxBleManager.notifyHeartRate(fun(heartRate: Int) {
            handler.post {
                if (System.currentTimeMillis() - lastNotifyHeartRateLogTime > 1000 * 20L) {
                    BleLogUtil.d(TAG, "notifyHeartRate")
                    lastNotifyHeartRateLogTime = System.currentTimeMillis()
                }
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

    var lastNotifyContactLogTime = 0L

    /**
     * notify contact
     */
    fun notifyContact() {
        contactDisposable = rxBleManager.notifyContact { byte ->
            handler.post {
                byte.let {
                    if (System.currentTimeMillis() - lastNotifyContactLogTime > 1000 * 20L) {
                        BleLogUtil.d(TAG, "notifyContact")
                        lastNotifyContactLogTime = System.currentTimeMillis()
                    }
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
     * 扫描附近的设备，选择信号最好的，然后连接
     */
    private fun scanNearAndConnectDevice(
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
     * 连接已配对的设备
     * */
    private fun connectBondedDevice(
        successConnect: ((String) -> Unit)?,
        failure: ((String) -> Unit)?,
        filter: (String?, String?) -> Boolean = { _, _ -> true }
    ) {
        rxBleManager.connectBondedDevice(successConnect, failure, filter)
    }


    /**
     * 该方法为连接设备入口
     * @param connectionBleStrategy 连接策略
     * @param successConnect 连接成功回调 mac地址
     * @param failure 连接失败回调 失败原因
     * @param filter 过滤逻辑， true 表示保留 false 表示舍弃 仅在[ConnectionBleStrategy.CONNECT_BONDED]模式下生效
     */
    fun connectDevice(
        successConnect: ((String) -> Unit)?, failure: ((String) -> Unit)?,
        connectionBleStrategy: ConnectionBleStrategy = ConnectionBleStrategy.SCAN_AND_CONNECT_HIGH_SIGNAL,
        filter: (String?, String?) -> Boolean = { _, _ -> true }
    ) {
        when (connectionBleStrategy) {
            ConnectionBleStrategy.SCAN_AND_CONNECT_HIGH_SIGNAL -> {
                rxBleManager.scanNearDeviceAndConnect({ }, {
                    failure?.invoke(it.message ?: "scan failure")
                }, fun(mac: String) {
                    initNotifications()
                    successConnect?.invoke(mac)
                }, failure)
            }

            ConnectionBleStrategy.CONNECT_BONDED -> {
                connectBondedDevice({
                    initNotifications()
                    successConnect?.invoke(it)
                }, failure, filter)
            }
        }
    }

    /**
     * 为c#添加的方法，c#不能直接使用枚举
     * */
    fun connectDevice(
        successConnect: ((String) -> Unit)?, failure: ((String) -> Unit)?,
        connectionBleStrategy: Int,
        filter: (String?, String?) -> Boolean = { _, _ -> true }
    ) {
        connectDevice(
            successConnect,
            failure,
            ConnectionBleStrategy.SCAN_AND_CONNECT_HIGH_SIGNAL.getConnectionBleStrategy(
                connectionBleStrategy
            ),
            filter
        )
    }

    /**
     * 该方法为连接设备入口，实现与方法名称无关 具体连接实现[ConnectionBleStrategy]
     * @param successScan invalid param
     * @param failScan invalid param
     */
    @Deprecated("该方法名称有误导性 建议用connectDevice 替代", ReplaceWith("connectDevice"))
    fun scanNearDeviceAndConnect(
        successScan: (() -> Unit)? = null,
        failScan: ((Exception) -> Unit)? = null,
        successConnect: ((String) -> Unit)? = null,
        failure: ((String) -> Unit)? = null,
        connectionBleStrategy: Int = ConnectionBleStrategy.SCAN_AND_CONNECT_HIGH_SIGNAL.flag,
        filter: (String?, String?) -> Boolean = { _, _ -> true }
    ) {
        connectDevice(
            successConnect,
            failure,
            ConnectionBleStrategy.SCAN_AND_CONNECT_HIGH_SIGNAL.getConnectionBleStrategy(
                connectionBleStrategy
            ),
            filter
        )
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
    fun disConnect(success: () -> Unit = {}, failure: ((String) -> Unit)? = null) {
        rxBleManager.command(RxBleManager.Command.DISCONNECTED, { _ ->
            rxBleManager.disConnect(true)
            success()
        }) { errorMsg ->
            failure?.invoke(errorMsg)
        }
    }

    fun command(
        byteArray: ByteArray,
        success: () -> Unit = {},
        failure: ((String) -> Unit)? = null
    ) {
        rxBleManager.command(byteArray, success, failure)
    }

    /**
     * find connected device
     */
    fun findConnectedDevice(
        success: ((ByteArray) -> Unit)? = null,
        failure: ((String) -> Unit)? = null
    ) {
        rxBleManager.command(RxBleManager.Command.FIND_CONNECTED_DEVICE, success, failure)
    }

    fun addSkinConductivityServiceListener(listener: (ByteArray) -> Unit) {
        skinConductivityServiceListener.add(listener)
    }

    fun removeSkinConductivityServiceListener(listener: (ByteArray) -> Unit) {
        skinConductivityServiceListener.remove(listener)
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

    fun getDevice() = rxBleManager.getDevice()

    /**
     * remove device heart rate listener
     */
    fun removeHeartRateListener(listener: (Int) -> Unit) {
        heartRateListeners.remove(listener)
    }

    fun startContact(
        success: ((ByteArray) -> Unit)? = null,
        failure: ((String) -> Unit)? = null
    ) {
        BleLogUtil.d(TAG, "startContact")
        rxBleManager.command(RxBleManager.Command.START_CONTACT, success, failure)
    }

    fun stopContact(
        success: ((ByteArray) -> Unit)? = null,
        failure: ((String) -> Unit)? = null
    ) {
        BleLogUtil.d(TAG, "stopContact")
        rxBleManager.command(RxBleManager.Command.STOP_CONTACT, success, failure)
    }

    /**
     * start collect brain data
     */
    fun startBrainCollection(
        success: ((ByteArray) -> Unit)? = null,
        failure: ((String) -> Unit)? = null
    ) {
        BleLogUtil.d(TAG, "startBrainCollection")
        startFix(this)
        rxBleManager.command(RxBleManager.Command.START_BRAIN_COLLECT, success, failure)
    }

    /**
     * stop collect brain data
     */
    fun stopBrainCollection(
        success: ((ByteArray) -> Unit)? = null,
        failure: ((String) -> Unit)? = null
    ) {
        BleLogUtil.d(TAG, "stopBrainCollection")
        stopFix()
        rxBleManager.command(RxBleManager.Command.STOP_BRAIN_COLLECT, success, failure)
    }

    /**
     * start collect heart rate data
     */
    fun startHeartRateCollection(
        success: ((ByteArray) -> Unit)? = null,
        failure: ((String) -> Unit)? = null
    ) {
        BleLogUtil.d(TAG, "startHeartRateCollection")
        startFix(this)
        rxBleManager.command(RxBleManager.Command.START_HEART_RATE_COLLECT, success, failure)
    }

    /**
     * stop collect heart rate data
     */
    fun stopHeartRateCollection(
        success: ((ByteArray) -> Unit)? = null,
        failure: ((String) -> Unit)? = null
    ) {
        BleLogUtil.d(TAG, "stopHeartRateCollection")
        stopFix()
        rxBleManager.command(RxBleManager.Command.STOP_HEART_RATE_COLLECT, success, failure)
    }

    /**
     * start collect all data
     */
    fun startHeartAndBrainCollection(
        success: ((ByteArray) -> Unit)? = null,
        failure: ((String) -> Unit)? = null
    ) {
        BleLogUtil.d(TAG, "startHeartAndBrainCollection")
        startFix(this)
        rxBleManager.command(RxBleManager.Command.START_HEART_AND_BRAIN_COLLECT, success, failure)
    }

    /**
     * stop collect all data
     */
    fun stopHeartAndBrainCollection(
        success: ((ByteArray) -> Unit)? = null,
        failure: ((String) -> Unit)? = null
    ) {
        BleLogUtil.d(TAG, "stopHeartAndBrainCollection")
        stopFix()
        rxBleManager.command(RxBleManager.Command.STOP_HEART_AND_BRAIN_COLLECT, success, failure)
    }

    //read battery（readDeviceInfo）
    fun readBattery(success: (NapBattery) -> Unit, failure: ((String) -> Unit)?) {
        rxBleManager.read(NapBleCharacter.BATTERY_LEVEL.uuid, fun(bytes: ByteArray) {
            castBattery(bytes[0], success, failure)
        }, failure)
    }

    /**
     * 电量转换
     * */
    private fun castBattery(
        byte: Byte,
        success: (NapBattery) -> Unit,
        failure: ((String) -> Unit)?
    ) {
        readDeviceHardware(fun(version) {

            BleLogUtil.d(TAG, "currentVersion: $version")
            when (BatteryUtil.compareBleVersion(version, "3.0.0")) {
                BatteryUtil.COMPARE_VERSION_VALUE_ERROR_FORMAT -> {
                    BleLogUtil.e(TAG, "ble version error")
                    failure?.invoke("")
                }
                //当前版本小于3.0.0
                BatteryUtil.COMPARE_VERSION_VALUE_SMALL -> {
                    BleLogUtil.d(TAG, "use old battery")
                    success.invoke(BatteryUtil.getMinutesLeftOld(byte))
                }

                else -> {
                    BleLogUtil.d(TAG, "use new battery")
                    success.invoke(BatteryUtil.getMinutesLeft(byte))
                }
            }
        }) {
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

    override fun fixTrigger() {
        mainHandler.post {
            stopHeartAndBrainCollection()
            startHeartAndBrainCollection()
        }
    }

    private fun stopFix(){
        fixStrategies.forEach { 
            it.stopFix()
        }
    }
    
    private fun startFix(callback: IFixTriggerCallback){
        fixStrategies.forEach {
            it.startFix(callback)
        }
    }

    fun stopScanNearDevice(){
        rxBleManager.stopScanNearDevice()
    }
}
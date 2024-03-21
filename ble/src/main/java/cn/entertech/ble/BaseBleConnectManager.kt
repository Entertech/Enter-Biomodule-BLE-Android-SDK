package cn.entertech.ble


import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.ParcelUuid
import cn.entertech.ble.fix.BaseFirmwareFixStrategy
import cn.entertech.ble.fix.Firmware128FixHelper
import cn.entertech.ble.fix.Firmware255FixHelper
import cn.entertech.ble.fix.IFixTriggerCallback
import cn.entertech.ble.uid.characteristic.BluetoothCharacteristic
import cn.entertech.ble.uid.device.BaseBleDeviceFactory
import cn.entertech.ble.uid.property.BluetoothProperty
import cn.entertech.ble.uid.service.IBatteryService
import cn.entertech.ble.uid.service.IEegService
import cn.entertech.ble.uid.service.IHrsService
import cn.entertech.ble.utils.BatteryUtil
import cn.entertech.ble.utils.BleLogUtil
import cn.entertech.ble.utils.BleUtil
import cn.entertech.ble.utils.ByteArrayBean
import cn.entertech.ble.utils.CharUtil.converUnchart
import cn.entertech.ble.utils.NapBattery
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
import java.nio.charset.StandardCharsets
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit

/**
 * 单设备
 * */
abstract class BaseBleConnectManager constructor(
    context: Context,
    val bleFactory: BaseBleDeviceFactory,
    private val fixStrategies: List<BaseFirmwareFixStrategy> = listOf(
        Firmware128FixHelper,
        Firmware255FixHelper
    )
) : IFixTriggerCallback {
    companion object {
        private const val TAG = "BaseBleConnectManager"
        const val SCAN_TIMEOUT: Long = 20000
        private const val DURATION_OF_SORT: Long = 3000
        private const val CONNECT_TASK_DELAY: Long = 1000
    }

    private val mainHandler by lazy {
        Handler(Looper.getMainLooper())
    }
    private val rawDataListeners = CopyOnWriteArrayList<(ByteArray) -> Unit>()
    private val rawDataListeners4CSharp = CopyOnWriteArrayList<(ByteArrayBean) -> Unit>()
    private val contactListeners = CopyOnWriteArrayList<(Int) -> Unit>()
    private val batteryListeners = CopyOnWriteArrayList<(NapBattery) -> Unit>()
    private val batteryVoltageListeners = CopyOnWriteArrayList<(Double) -> Unit>()
    private val heartRateListeners = CopyOnWriteArrayList<(Int) -> Unit>()
    private var brainWaveDisposable: Disposable? = null
    private var batteryDisposable: Disposable? = null
    private var heartRateDisposable: Disposable? = null
    private var contactDisposable: Disposable? = null

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

    fun connectDevice(builder: BluetoothConnectBuilder) {
        connectDevice(
            builder.connectSuccess,
            builder.connectFailure,
            connectionBleStrategy = builder.connectionBleStrategy,
            mac = builder.mac,
            connectTimeOut = builder.scanTimeout,
            filter = builder.filter
        )
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
     * 该方法为连接设备入口
     * @param connectionBleStrategy 连接策略
     * @param successConnect 连接成功回调 mac地址
     * @param failure 连接失败回调 失败原因
     * @param filter 过滤逻辑， true 表示保留 false 表示舍弃 仅在[ConnectionBleStrategy.CONNECT_BONDED]模式下生效
     */
    fun connectDevice(
        successConnect: ((String) -> Unit)?, failure: ((String) -> Unit)?,
        connectionBleStrategy: ConnectionBleStrategy = ConnectionBleStrategy.SCAN_AND_CONNECT_HIGH_SIGNAL,
        mac: String = "",
        connectTimeOut: Long = 0,
        filter: (String?, String?) -> Boolean = { _, _ -> true }
    ) {
        when (connectionBleStrategy) {
            ConnectionBleStrategy.SCAN_AND_CONNECT_HIGH_SIGNAL -> {
                scanNearDeviceAndConnect(connectTimeOut, successConnect, failure)

            }

            ConnectionBleStrategy.CONNECT_BONDED -> {
                connectBondedDevice(successConnect, failure, filter)
            }

            ConnectionBleStrategy.CONNECT_DEVICE_MAC -> {
                scanMacAndConnect(
                    mac,
                    connectTimeOut,
                    successConnect,
                    failure
                )
            }

        }
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
        connectDevice(
            successConnect,
            failure,
            connectionBleStrategy,
            mac = "",
            connectTimeOut = 0,
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
     * disconnect device
     */
    fun disConnect(success: () -> Unit = {}, failure: ((String) -> Unit)? = null) {
        command(Command.DISCONNECTED, { _ ->
            disConnect(true)
            success()
        }) { errorMsg ->
            failure?.invoke(errorMsg)
        }
    }

    @Throws(IllegalStateException::class)
    fun write(
        character: BluetoothCharacteristic,
        bytes: ByteArray,
        success: ((ByteArray) -> Unit)? = null,
        failure: ((String) -> Unit)? = null
    ) {
        val properties = character.properties
        if (properties.contains(BluetoothProperty.BLUETOOTH_PROPERTY_WRITE) ||
            properties.contains(BluetoothProperty.BLUETOOTH_PROPERTY_WRITE_WITHOUT_RESPONSE)
        ) {
            write(character.uid, bytes, success, failure)
        } else {
            throw IllegalStateException("character is not support write")
        }
    }

    /**
     * write characteristic
     */
    private fun write(
        characterId: String,
        bytes: ByteArray,
        success: ((ByteArray) -> Unit)? = null,
        failure: ((String) -> Unit)? = null
    ): Disposable? {
        return rxBleConnection?.writeCharacteristic(UUID.fromString(characterId), bytes)
            ?.subscribe(
                { characteristicValue ->
                    success?.invoke(characteristicValue)
                },
                { throwable ->
                    // Handle an error here.
                    failure?.invoke("write error $throwable")
                }
            )
    }

    /**
     * read characteristic
     */
    @Throws(IllegalStateException::class)
    fun read(
        character: BluetoothCharacteristic,
        success: (ByteArray) -> Unit,
        failure: ((String) -> Unit)?
    ) {
        val properties = character.properties
        if (properties.contains(BluetoothProperty.BLUETOOTH_PROPERTY_READ)) {
            read(character.uid, success, failure)
        } else {
            throw IllegalStateException("character is not support read")
        }
    }


    /**
     * read characteristic
     */
    fun read(
        characterId: String,
        success: (ByteArray) -> Unit,
        failure: ((String) -> Unit)?
    ): Disposable? {
        return rxBleConnection?.readCharacteristic(UUID.fromString(characterId))
            ?.subscribe(
                { characteristicValue ->
                    success.invoke(characteristicValue)
                },
                { throwable ->
                    failure?.invoke("read error $throwable")
                }
            )
    }

    @Throws(IllegalStateException::class)
    fun notify(
        character: BluetoothCharacteristic,
        success: (ByteArray) -> Unit,
        failure: ((String) -> Unit)?
    ) {
        val properties = character.properties
        if (properties.contains(BluetoothProperty.BLUETOOTH_PROPERTY_NOTIFY)) {
            notify(character.uid, success, failure)
        } else {
            throw IllegalStateException("character is not support notify")
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
        return rxBleConnection?.setupNotification(UUID.fromString(characterId))
            ?.flatMap { notificationObservable -> notificationObservable }
            ?.subscribeOn(Schedulers.io())
            ?.subscribe(
                { characteristicValue ->
                    success.invoke(characteristicValue)
                },
                { throwable ->
                    failure?.invoke("notify error ${throwable.message}")
                }
            )
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
     * start collect brain data
     */
    fun startBrainCollection(
        success: ((ByteArray) -> Unit)? = null,
        failure: ((String) -> Unit)? = null
    ) {
        BleLogUtil.d(TAG, "startBrainCollection")
        startFix(this)
        command(Command.START_BRAIN_COLLECT, success, failure)
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
        command(Command.STOP_BRAIN_COLLECT, success, failure)
    }

    /**
     * notify brain
     */
    fun notifyBrainWave() {
        brainWaveDisposable = notifyBrainWave { bytes ->
            BleLogUtil.d(TAG, "notifyBrainWave")
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

    /**
     * stop notify brain
     */
    fun stopNotifyBrainWave() {
        brainWaveDisposable?.dispose()
    }


    //read battery（readDeviceInfo）
    fun readBattery(success: (NapBattery) -> Unit, failure: ((String) -> Unit)?) {
        readBatteryByteArray(fun(bytes: ByteArray) {
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
     * notify battery
     */
    fun notifyBattery() {
        batteryDisposable = notifyBattery(fun(byte: Byte) {
            BleLogUtil.d(TAG, "notifyBattery")
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
        })
    }


    /**
     * stop notify battery
     */
    fun stopNotifyBattery() {
        batteryDisposable?.dispose()
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
        command(Command.START_HEART_RATE_COLLECT, success, failure)
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
        command(Command.STOP_HEART_RATE_COLLECT, success, failure)
    }


    /**
     * notify heart rate
     */
    fun notifyHeartRate() {
        heartRateDisposable = notifyHeartRate(fun(heartRate: Byte) {
            BleLogUtil.d(TAG, "notifyHeartRate")
            heartRateListeners.forEach { listener ->
                listener.invoke(converUnchart(heartRate))
            }
        })
    }

    /**
     * stop notify heart rate
     */
    fun stopNotifyHeartRate() {
        heartRateDisposable?.dispose()
    }

    fun startContact(
        success: ((ByteArray) -> Unit)? = null,
        failure: ((String) -> Unit)? = null
    ) {
        BleLogUtil.d(TAG, "startContact")
        command(Command.START_CONTACT, success, failure)
    }

    fun stopContact(
        success: ((ByteArray) -> Unit)? = null,
        failure: ((String) -> Unit)? = null
    ) {
        BleLogUtil.d(TAG, "stopContact")
        command(Command.STOP_CONTACT, success, failure)
    }


    /**
     * notify contact
     */
    fun notifyContact() {
        contactDisposable = notifyContact { byte ->
            BleLogUtil.d(TAG, "notifyContact")
            contactListeners.forEach { listener ->
                listener.invoke(byte)
            }
        }
    }

    /**
     * stop notify contact
     */
    fun stopNotifyContact() {
        contactDisposable?.dispose()
    }


    fun command(
        byteArray: ByteArray,
        success: () -> Unit = {},
        failure: ((String) -> Unit)? = null
    ) {
        command(byteArray, success, failure)
    }

    /**
     * find connected device
     */
    fun findConnectedDevice(
        success: ((ByteArray) -> Unit)? = null,
        failure: ((String) -> Unit)? = null
    ) {
        command(Command.FIND_CONNECTED_DEVICE, success, failure)
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
        command(Command.START_HEART_AND_BRAIN_COLLECT, success, failure)
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
        command(Command.STOP_HEART_AND_BRAIN_COLLECT, success, failure)
    }

    override fun fixTrigger() {
        mainHandler.post {
            stopHeartAndBrainCollection()
            startHeartAndBrainCollection()
        }
    }

    private fun stopFix() {
        fixStrategies.forEach {
            it.stopFix()
        }
    }

    private fun startFix(callback: IFixTriggerCallback) {
        fixStrategies.forEach {
            it.startFix(callback)
        }
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
package cn.entertech.flowtimeble

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.entertech.ble.BaseBleConnectManager
import cn.entertech.ble.base.IBatteryFunction
import cn.entertech.ble.base.IDeviceInfoFunction
import cn.entertech.ble.base.IEegFunction
import cn.entertech.ble.base.IHrFunction
import cn.entertech.ble.log.BleLogUtil
import cn.entertech.ble.utils.NapBattery
import cn.entertech.bleuisdk.ui.DeviceUIConfig
import cn.entertech.bleuisdk.ui.activity.DeviceManagerActivity
import cn.entertech.device.DeviceType
import cn.entertech.flowtimeble.skin.SkinDataHelper
import cn.entertech.flowtimeble.skin.SkinDataType
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    private var bluetoothDeviceManager: BaseBleConnectManager? = null
    private var spinnerDeviceTypeList: Spinner? = null
    private var cbNeedReconnected: CheckBox? = null
    private var cbShowLog: CheckBox? = null
    private var scrollView_logs: RecyclerView? = null
    private var btnClearLog: Button? = null
    private var btnConnect: Button? = null
    private var btnSwapPersistenceState: Button? = null
    private val simple by lazy {
        SimpleDateFormat("yyyy/MM/dd  hh:mm:ss:SSS")
    }

    private val skinDataHelper = SkinDataHelper("brain_tag")
    private var lastReceiveDataTime = 0L
    private val adapter by lazy {
        LogAdapter()
    }
    private var rawListener = fun(bytes: ByteArray) {
        if (lastReceiveDataTime == 0L) {
            lastReceiveDataTime = System.currentTimeMillis()
            showMsg("收到数据了")
            return
        }
        if (System.currentTimeMillis() - lastReceiveDataTime > 10000) {
            showMsg("超过10s没收到数据")
            lastReceiveDataTime = 0
        } else {
            lastReceiveDataTime = System.currentTimeMillis()
        }
//        BleLogUtil.d(TAG,"firmware fixing hex " + HexDump.toHexString(bytes))
        skinDataHelper.saveData(SkinDataType.BRAIN_DATA, HexDump.toHexString(bytes))
//        showMsg("braindata: " + HexDump.toHexString(bytes))
//        BleLogUtil.d(TAG,"brain data is " + Arrays.toString(bytes))
    }

    private var heartRateListener = fun(heartRate: Int) {
        BleLogUtil.d(TAG, "heart rate data is $heartRate")
    }

    companion object {
        private const val TAG = "MainActivity"

        /**
         * 2s
         * */
        private const val RECONNECT_DELAY_TIME = 2000L

        /**
         * 30s
         * */
        private const val CHECK_CONNECT_TIME = 1000 * 30L
    }

    private val mainHandler by lazy {
        Handler(Looper.getMainLooper())
    }


    /**
     * 是否是持久性实验
     * true 是，
     * 连接失败/断开 都会尝试重连，
     * 连接成功后会每隔[CHECK_CONNECT_TIME]检查连接状态
     * 连接成功后会进行脑波心率数据的采集
     * */
    private var isPersistenceExperiment = false

    private var needLog = false
    private val reconnectRunnable: Runnable by lazy {
        Runnable {
            showMsg("reconnectRunnable needReConnected:   $needReConnected")
            if (needReConnected) {
                showMsg("start reconnect")
                connectDevice()
            }
        }
    }

    @Volatile
    private var needReConnected = false
    private val checkConnectRunnable by lazy {
        object : Runnable {
            override fun run() {
                BleLogUtil.i(
                    TAG,
                    "checkConnectRunnable biomoduleBleManager.isConnected()： ${bluetoothDeviceManager?.isConnected()}"
                )
                mainHandler.postDelayed(this, CHECK_CONNECT_TIME)
            }
        }
    }

    private lateinit var btnScanConnect: Button
    private val deviceTypes by lazy {
        listOf(
            DeviceType.DEVICE_TYPE_BRAIN_TAG,
            DeviceType.DEVICE_TYPE_HEADBAND,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        spinnerDeviceTypeList = findViewById(R.id.spinnerDeviceTypeList)
        // 创建 ArrayAdapter
        val arrayAdapter: ArrayAdapter<DeviceType> =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, deviceTypes)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDeviceTypeList?.adapter = arrayAdapter
        spinnerDeviceTypeList?.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                initBleManager(parent?.getItemAtPosition(position) as? DeviceType)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                initBleManager(null)
            }
        }

        cbNeedReconnected = findViewById(R.id.cbNeedReconnected)
        scrollView_logs = findViewById(R.id.scrollView_logs)
        scrollView_logs?.adapter = adapter
        scrollView_logs?.layoutManager = LinearLayoutManager(this)
        btnClearLog = findViewById(R.id.btnClearLog)
        btnConnect = findViewById(R.id.btnConnect)
        btnSwapPersistenceState = findViewById(R.id.btnSwapPersistenceState)
        cbShowLog = findViewById(R.id.cbStopLog)
        btnClearLog?.setOnClickListener {
            adapter.setData(ArrayList())
        }
        cbNeedReconnected?.isChecked = true
        cbShowLog?.isChecked = true
        needLog = cbShowLog?.isChecked ?: false
        cbNeedReconnected?.setOnCheckedChangeListener { _, isChecked ->
            needReConnected = isChecked
        }
        cbShowLog?.setOnCheckedChangeListener { _, isChecked ->
            needLog = isChecked
        }
        needReConnected = cbNeedReconnected?.isChecked ?: false

        initPermission()
        DeviceUIConfig.getInstance(this).init(false, false, 1)
        DeviceUIConfig.getInstance(this).updateFirmware(
            "1.2.0",
            "${Environment.getExternalStorageDirectory()}/dfufile.zip",
            true
        )
        btnScanConnect = findViewById(R.id.btnScanConnect)
        btnConnect?.setOnClickListener {
            onConnectBound()
        }
        initPersistenceState()
    }

    private fun initBleManager(deviceType: DeviceType?) {
        val currentDeviceType = deviceType ?: deviceTypes[0]
        bluetoothDeviceManager?.apply {
            removeDisConnectListener(disConnectedListener)
            removeConnectListener(connectedListener)
            removeRawDataListener(rawListener)
            disConnect()
        }
        showMsg("当前选择的设备类型： $currentDeviceType")
        bluetoothDeviceManager =
            BaseBleConnectManager.getBleManagerInstance(currentDeviceType, this)
        bluetoothDeviceManager?.addConnectListener(connectedListener)
        bluetoothDeviceManager?.addDisConnectListener(disConnectedListener)
        bluetoothDeviceManager?.addRawDataListener(rawListener)
    }


    private fun showMsg(msg: String) {
        BleLogUtil.d(TAG, msg)
        if (!needLog) {
            return
        }
        val realMsg = "->: ${simple.format(Date())} $msg\n"
        runOnUiThread {
            adapter.addItem(realMsg)
            scrollView_logs?.scrollToPosition(adapter.itemCount - 1)
        }

    }

    /**
     * Android6.0 auth
     */
    private fun initPermission() {
        val needPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
        val needRequestPermissions = ArrayList<String>()
        for (i in needPermission.indices) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    needPermission[i]
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                needRequestPermissions.add(needPermission[i])
            }
        }
        if (needRequestPermissions.size != 0) {
            val permissions = arrayOfNulls<String>(needRequestPermissions.size)
            for (i in needRequestPermissions.indices) {
                permissions[i] = needRequestPermissions[i]
            }
            ActivityCompat.requestPermissions(this@MainActivity, permissions, 1)
        }
    }

    fun onDeviceUI(@Suppress("UNUSED_PARAMETER") view: View) {
        startActivity(Intent(this@MainActivity, DeviceManagerActivity::class.java))
    }

    var contactListener = fun(contactState: Int) {
        BleLogUtil.i(TAG, "contace state is ${contactState}")
    }

    var connectedListener = fun(string: String) {
        showMsg("connectedListener connect success:   $string")
        runOnUiThread {
            Toast.makeText(this@MainActivity, "connect success", Toast.LENGTH_SHORT).show()
        }
    }
    var disConnectedListener = fun(string: String) {
        showMsg("disconnect:   $string")
        lastReceiveDataTime = 0L
        reconnect()
        runOnUiThread {
//            btnConnect?.setText(R.string.connectBonded)
            btnScanConnect.setText(R.string.connect)
            Toast.makeText(this@MainActivity, "disconnect ", Toast.LENGTH_SHORT).show()
        }
    }

    private fun reconnect() {
        mainHandler.removeCallbacks(reconnectRunnable)
        mainHandler.postDelayed(reconnectRunnable, RECONNECT_DELAY_TIME)
    }


    @OptIn(ExperimentalStdlibApi::class)
    fun onConnectBound() {
        mainHandler.removeCallbacks(reconnectRunnable)
        bluetoothDeviceManager?.connectDevice({
            BleLogUtil.i(TAG, "connect Bound success")
            btnConnect?.text = it
            if (isPersistenceExperiment) {
                mainHandler.post(checkConnectRunnable)
                onCollectBrainAndHeartStart()
            }
            runOnUiThread {
                Toast.makeText(this@MainActivity, "connect success ", Toast.LENGTH_SHORT).show()
            }

        }, {
            BleLogUtil.i(TAG, "connect Bound failed error $it ")
            if (isPersistenceExperiment) {
                reconnect()
            }
            runOnUiThread {
                Toast.makeText(
                    this@MainActivity, "connect Bound failed error $it ", Toast.LENGTH_SHORT
                ).show()
            }
        }, cn.entertech.ble.api.ConnectionBleStrategy.CONNECT_BONDED
        )

    }

    fun onConnect(@Suppress("UNUSED_PARAMETER") view: View) {
        connectDevice()
    }

    private fun connectDevice() {
        mainHandler.removeCallbacks(reconnectRunnable)
        if (bluetoothDeviceManager?.isConnected() == true) {
            showMsg("已连接  $bluetoothDeviceManager")
            return
        }

        if (bluetoothDeviceManager?.isConnecting() == true) {
            showMsg("正在连接中  $bluetoothDeviceManager")
            return
        }
        showMsg("开始寻找设备 ，准备连接 $bluetoothDeviceManager")
        bluetoothDeviceManager?.connectDevice(fun(mac: String) {
            showMsg("connect success $mac")
            mainHandler.postDelayed({
                skinDataHelper?.close()
                skinDataHelper?.initHelper()
                startCollection(false)
                startContact()
            }, 1000)
            runOnUiThread {
                btnScanConnect.text = mac
                Toast.makeText(this@MainActivity, "connect to device success", Toast.LENGTH_SHORT)
                    .show()
            }
        }, { msg ->
            showMsg("connect failed $msg")
            runOnUiThread {
                Toast.makeText(
                    this@MainActivity,
                    "failed to connect to device：${msg}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }, cn.entertech.ble.api.ConnectionBleStrategy.SCAN_AND_CONNECT_HIGH_SIGNAL)
    }

    private fun initPersistenceState() {
        btnSwapPersistenceState?.text = if (isPersistenceExperiment) {
            "当前是处于持续性实验状态"
        } else {
            "当前是处于非持续性实验状态"
        }
    }


    fun onSwapPersistenceState(@Suppress("UNUSED_PARAMETER") view: View) {
        isPersistenceExperiment = !isPersistenceExperiment
        initPersistenceState()
    }


    fun onDisconnect(@Suppress("UNUSED_PARAMETER") view: View) {
        bluetoothDeviceManager?.disConnect {
            btnConnect?.setText(R.string.connectBonded)
            btnScanConnect.setText(R.string.connect)
        }
    }


    fun onAddConnectedListener(@Suppress("UNUSED_PARAMETER") view: View) {
        bluetoothDeviceManager?.removeConnectListener(connectedListener)
        bluetoothDeviceManager?.addConnectListener(connectedListener)
    }

    fun onRemoveConnectedListener(@Suppress("UNUSED_PARAMETER") view: View) {
        bluetoothDeviceManager?.removeConnectListener(connectedListener)
    }

    fun onAddDisconnectedListener(@Suppress("UNUSED_PARAMETER") view: View) {
        bluetoothDeviceManager?.removeDisConnectListener(disConnectedListener)
        bluetoothDeviceManager?.addDisConnectListener(disConnectedListener)
    }

    fun onRemoveDisconnectedListener(@Suppress("UNUSED_PARAMETER") view: View) {
        bluetoothDeviceManager?.removeDisConnectListener(disConnectedListener)
    }

    fun onStopContact(@Suppress("UNUSED_PARAMETER") view: View) {
        stopContact()
    }

    fun onStartContact(@Suppress("UNUSED_PARAMETER") view: View) {
        stopContact()
        startContact()
    }


    private fun startContact() {
        showMsg("开始佩戴检测 ")
        val currentBleManger = bluetoothDeviceManager
        if (currentBleManger is IEegFunction) {
            currentBleManger.notifyContact({
                val result = if (it.isEmpty()) {
                    0
                } else {
                    it.contentToString()
                }
                skinDataHelper.saveData(SkinDataType.Contact, result.toString())
                showMsg("佩戴检测值： $result")
            }) {
                showMsg("佩戴检测失败： $it")
            }
        }

    }

    private fun stopContact() {
        showMsg("停止佩戴检测")
        val currentBleManger = bluetoothDeviceManager
        if (currentBleManger is IEegFunction) {
            currentBleManger.stopNotifyContact()
        }
    }

    fun onCollectHeartStart(@Suppress("UNUSED_PARAMETER") view: View) {
//        bluetoothDeviceManager?.startHeartRateCollection()
    }

    fun onCollectHeartStop(@Suppress("UNUSED_PARAMETER") view: View) {
//        bluetoothDeviceManager?.stopHeartRateCollection()
    }

    private fun startCollection(needStop: Boolean = true) {
        if (needStop) {
            stopCollection(success = {
                showMsg("开始收集数据")
                bluetoothDeviceManager?.startCollection({
                    showMsg("收集数据指令发送成功 ")
                }) {
                    showMsg("收集数据指令发送失败 $it")
                } ?: showMsg("收集数据指令发送失败 bluetoothDeviceManager is null")

            })
        } else {
            showMsg("开始收集数据")
            bluetoothDeviceManager?.startCollection({
                showMsg("收集数据指令发送成功 ")
            }) {
                showMsg("收集数据指令发送失败 $it")
            } ?: showMsg("收集数据指令发送失败 bluetoothDeviceManager is null")
        }
    }

    private fun stopCollection(success: () -> Unit = {}, failure: (String) -> Unit = {}) {
        showMsg("停止收集数据")
        bluetoothDeviceManager?.stopCollection({
            showMsg("停止数据指令发送成功 ")
            success()
        }) {
            showMsg("停止数据指令发送失败 $it")
            failure(it)
        }
    }

    fun onCollectBrainStart(@Suppress("UNUSED_PARAMETER") view: View) {
        startCollection()
    }

    fun onCollectBrainStop(@Suppress("UNUSED_PARAMETER") view: View) {
        stopCollection()
    }

    fun onCollectBrainAndHeartStart(@Suppress("UNUSED_PARAMETER") view: View) {
        onCollectBrainAndHeartStart()
    }

    fun onCollectBrainAndHeartStart() {
//        bluetoothDeviceManager?.startHeartAndBrainCollection()
    }

    fun onCollectBrainAndHeartStop(@Suppress("UNUSED_PARAMETER") view: View) {
//        bluetoothDeviceManager?.stopHeartAndBrainCollection()
    }


    fun onAddRawListener(@Suppress("UNUSED_PARAMETER") view: View) {
        bluetoothDeviceManager?.addRawDataListener(rawListener)
    }

    fun onRemoveRawListener(@Suppress("UNUSED_PARAMETER") view: View) {
        bluetoothDeviceManager?.removeRawDataListener(rawListener)
    }

    fun onAddHeartRateListener(@Suppress("UNUSED_PARAMETER") view: View) {
        bluetoothDeviceManager?.apply {
            if (this is IHrFunction) {
                addHeartRateListener(heartRateListener)
            }
        }
    }

    fun onRemoveHeartRateListener(@Suppress("UNUSED_PARAMETER") view: View) {
        bluetoothDeviceManager?.apply {
            if (this is IHrFunction) {
                removeHeartRateListener(heartRateListener)
            }
        }
    }

    fun onAddContactListener(@Suppress("UNUSED_PARAMETER") view: View) {
        bluetoothDeviceManager?.addContactListener(contactListener)
    }

    fun onRemoveContactListener(@Suppress("UNUSED_PARAMETER") view: View) {
        bluetoothDeviceManager?.removeContactListener(contactListener)
    }

    fun onBattery(@Suppress("UNUSED_PARAMETER") view: View) {
        val currentBleManger = bluetoothDeviceManager
        if (currentBleManger is IBatteryFunction) {
            currentBleManger.readBattery(fun(battery: NapBattery) {
                BleLogUtil.d(TAG, "battery = $battery")
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "电量:$battery", Toast.LENGTH_SHORT).show()
                }
            }, fun(error: String) {
                BleLogUtil.d(TAG, "error is $error")
            })
        } else {
            showMsg("当前连接设备不支持读取电量")
        }

    }

    fun onGetState(@Suppress("UNUSED_PARAMETER") view: View) {
        BleLogUtil.d(
            TAG,
            "biomoduleBleManager.isConnected()： ${bluetoothDeviceManager?.isConnected()}"
        )
        Toast.makeText(
            this, if (bluetoothDeviceManager?.isConnected() == true) {
                "connected"
            } else {
                "disconnect"
            }, Toast.LENGTH_SHORT
        ).show()
    }

    val batteryListener = fun(napBattery: NapBattery) {
        BleLogUtil.d(TAG, "battery = ${napBattery}")
    }
    val batteryVoltageListener = fun(voltage: Double) {
        BleLogUtil.d(TAG, "battery voltage = ${voltage}")
    }

    fun onAddBatteryListener(@Suppress("UNUSED_PARAMETER") view: View) {
//        bluetoothDeviceManager?.addBatteryListener(batteryListener)
    }

    fun onRemoveBatteryListener(@Suppress("UNUSED_PARAMETER") view: View) {
//        bluetoothDeviceManager?.removeBatteryListener(batteryListener)
    }

    fun onAddBatteryVoltageListener(@Suppress("UNUSED_PARAMETER") view: View) {
//        bluetoothDeviceManager?.addBatteryVoltageListener(batteryVoltageListener)
    }

    fun onRemoveBatteryVoltageListener(@Suppress("UNUSED_PARAMETER") view: View) {
//        bluetoothDeviceManager?.removeBatteryVoltageListener(batteryVoltageListener)
    }

    fun onReadHardware(@Suppress("UNUSED_PARAMETER") view: View) {
        val currentBleManager = bluetoothDeviceManager
        if (currentBleManager is IDeviceInfoFunction) {
            currentBleManager.readDeviceHardware(fun(hardware: String) {
                BleLogUtil.d(TAG, "hardware is $hardware")
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "hardware is：${hardware}", Toast.LENGTH_SHORT)
                        .show()
                }
            }, fun(error: String) {
                BleLogUtil.d(TAG, "error is $error")
            })
        }

    }

    fun onReadFirmware(@Suppress("UNUSED_PARAMETER") view: View) {
        bluetoothDeviceManager?.apply {
            if (this is IDeviceInfoFunction) {
                readDeviceFirmware(fun(firmware: String) {
                    BleLogUtil.d(TAG, "firmware is $firmware")
                    runOnUiThread {
                        Toast.makeText(
                            this@MainActivity,
                            "firmware is：${firmware}",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }, fun(error: String) {
                    BleLogUtil.d(TAG, "error is $error")
                })
            }

        }
    }

    fun onReadDeviceSerial(@Suppress("UNUSED_PARAMETER") view: View) {
        bluetoothDeviceManager?.apply {
            if (this is IDeviceInfoFunction) {
                readDeviceSerial(fun(serial: String) {
                    BleLogUtil.d(TAG, "serial is $serial")
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "serial is：${serial}", Toast.LENGTH_SHORT)
                            .show()
                    }
                }, fun(error: String) {
                    BleLogUtil.d(TAG, "error is $error")
                })
            }
        }
    }

    fun oReadDeviceManufacturer(@Suppress("UNUSED_PARAMETER") view: View) {
        bluetoothDeviceManager?.apply {
            if (this is IDeviceInfoFunction) {
                readDeviceManufacturer(fun(manufacturer: String) {
                    BleLogUtil.d(TAG, "manufacturer is $manufacturer")
                    runOnUiThread {
                        Toast.makeText(
                            this@MainActivity,
                            "manufacturer is：${manufacturer}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }, fun(error: String) {
                    BleLogUtil.d(TAG, "error is $error")
                })
            }
        }
    }

    fun showLog(view: View) {
//        startActivity(Intent(this, ShowLogActivity::class.java))
    }

    fun onFindConnectedDevice(@Suppress("UNUSED_PARAMETER") view: View) {
        bluetoothDeviceManager?.findConnectedDevice()
    }

    fun goToSkinConductivity(view: View) {
        if (bluetoothDeviceManager?.isConnected() != true) {
            Toast.makeText(applicationContext, "请先连接设备", Toast.LENGTH_SHORT).show()
        }
//        startActivity(Intent(this, SkinConductivity::class.java))
    }

    fun readSkinConductivity(view: View) {
//        startActivity(Intent(this, SkinConductivityRecordActivity::class.java))
    }

    override fun onDestroy() {
        bluetoothDeviceManager?.apply {
            removeRawDataListener(rawListener)
            removeContactListener(contactListener)
            if (this is IHrFunction) {
                removeHeartRateListener(heartRateListener)
            }
        }
//        bluetoothDeviceManager?.removeBatteryListener(batteryListener)

//        bluetoothDeviceManager?.stopHeartRateCollection()
//        bluetoothDeviceManager?.stopBrainCollection()
//        bluetoothDeviceManager?.stopHeartAndBrainCollection()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()

    }
}

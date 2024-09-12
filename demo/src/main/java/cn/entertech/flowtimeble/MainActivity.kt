package cn.entertech.flowtimeble

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.entertech.base.util.startActivity
import cn.entertech.ble.BaseBleConnectManager
import cn.entertech.ble.log.BleLogUtil
import cn.entertech.ble.multiple.MultipleBiomoduleBleManager
import cn.entertech.device.api.IDeviceType
import cn.entertech.flowtimeble.data.FileListActivity
import cn.entertech.flowtimeble.skin.SkinDataHelper
import cn.entertech.flowtimeble.skin.SkinDataType
import cn.entertech.flowtimeble.skin.SkinDevice
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.ConcurrentHashMap


class MainActivity : AppCompatActivity() {

    private val deviceManageMap by lazy {
        ConcurrentHashMap<String, BaseBleConnectManager?>()
    }
    private val disConnectedListenerMap by lazy {
        ConcurrentHashMap<String, (String) -> Unit>()
    }
    private val rawListenerMap by lazy {
        ConcurrentHashMap<String, (ByteArray) -> Unit>()
    }

    private val deviceContactMap by lazy {
        ConcurrentHashMap<String, Int>()
    }


    private val hrListenerMap by lazy {
        ConcurrentHashMap<String, (Int) -> Unit>()
    }
    private val deviceSaveHelperMap by lazy {
        ConcurrentHashMap<String, SkinDataHelper>()
    }

    private val hasNotifyMap by lazy {
        ConcurrentHashMap<String, Boolean>()
    }
    private val reconnectRunnableMap by lazy {
        ConcurrentHashMap<String, Runnable>()
    }

    private var spinnerDeviceTypeList: Spinner? = null
    private var cbNeedReconnected: CheckBox? = null
    private var cbShowLog: CheckBox? = null
    private var scrollView_logs: RecyclerView? = null
    private var btnClearLog: Button? = null
    private var btnConnect: Button? = null
    private var btnScanConnect2: Button? = null
    private var btnDisconnect2: Button? = null
    private var btnOpenLocalData: Button? = null
    private var btnStartCollection2: Button? = null
    private var btnStopCollection2: Button? = null
    private var btnStartCollection: Button? = null
    private var btnStopCollection: Button? = null
    private var btnOpenFile: Button? = null
    private var btnSwapPersistenceState: Button? = null
    private var tvDevice1: TextView? = null
    private var tvDevice2: TextView? = null
    private var tvDevice1WearStatus: TextView? = null
    private val simple by lazy {
        SimpleDateFormat("yyyy/MM/dd  hh:mm:ss:SSS")
    }

    private val adapter by lazy {
        LogAdapter()
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val DEVICE1 = "vr"
        private const val DEVICE2 = "hand"

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

    @Volatile
    private var needReConnected = false

    private var btnScanConnect: Button? = null
    private val deviceTypes by lazy {
        listOf(
            SkinDevice,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        spinnerDeviceTypeList = findViewById(R.id.spinnerDeviceTypeList)
        // 创建 ArrayAdapter
        val arrayAdapter: ArrayAdapter<IDeviceType> =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, deviceTypes)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDeviceTypeList?.adapter = arrayAdapter
        spinnerDeviceTypeList?.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    initBleManager(parent?.getItemAtPosition(position) as? IDeviceType)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    initBleManager(null)
                }
            }

        cbNeedReconnected = findViewById(R.id.cbNeedReconnected)
        tvDevice1 = findViewById(R.id.tvDevice1)
        tvDevice2 = findViewById(R.id.tvDevice2)
        tvDevice1WearStatus = findViewById(R.id.tvDevice1WearStatus)
        scrollView_logs = findViewById(R.id.scrollView_logs)
        btnScanConnect2 = findViewById(R.id.btnScanConnect2)
        btnDisconnect2 = findViewById(R.id.btnDisconnect2)
        btnOpenLocalData = findViewById(R.id.btnOpenLocalData)
        btnDisconnect2?.setOnClickListener {
            disconnect(DEVICE2)
        }
        btnOpenLocalData?.setOnClickListener {
            startActivity(FileListActivity::class.java, finishCurrent = false)
        }
        btnStartCollection2 = findViewById(R.id.btnStartCollection2)
        btnStopCollection2 = findViewById(R.id.btnStopCollection2)
        btnStartCollection = findViewById(R.id.btnStartCollection)
        btnStopCollection = findViewById(R.id.btnStopCollection)

        btnStartCollection2?.setOnClickListener {
            startCollectData(DEVICE2)
        }
        btnStopCollection2?.setOnClickListener {
            stopCollectData(DEVICE2)
        }
        btnStartCollection?.setOnClickListener {
            startCollectData(DEVICE1)
        }
        btnStopCollection?.setOnClickListener {
            stopCollectData(DEVICE1)
        }
        btnOpenFile = findViewById(R.id.btnOpenFile)
        btnOpenFile?.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.putExtra(
                "android.content.extra.FOLDER_NAME",
                App.getInstance().getExternalFilesDir("")?.absoluteFile
            );
            startActivity(Intent.createChooser(intent, "选择文件管理器"));
        }
        btnScanConnect2?.setOnClickListener {
            connectDevice(DEVICE2) {
                btnScanConnect2?.text = it
            }
        }
        scrollView_logs?.adapter = adapter
        scrollView_logs?.layoutManager = LinearLayoutManager(this)
        btnClearLog = findViewById(R.id.btnClearLog)
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

        btnScanConnect = findViewById(R.id.btnScanConnect)
        btnScanConnect?.setOnClickListener {
            connectDevice(DEVICE1) {
                btnScanConnect?.text = it
            }
        }
        btnConnect?.setOnClickListener {
//            onConnectBound()
        }
        initPersistenceState()
    }

    private fun initBleManager(deviceType: IDeviceType? = null, deviceName: String = "") {
        val currentDeviceType = deviceType ?: deviceTypes[0]
        showMsg("$deviceName 初始化")
        var bluetoothDeviceManager: BaseBleConnectManager? = deviceManageMap[deviceName]
        if (bluetoothDeviceManager == null) {
            bluetoothDeviceManager = MultipleBiomoduleBleManager(this)/*  if (currentDeviceType is DeviceType) {

          }*//*BaseBleConnectManager.getBleManagerInstance(currentDeviceType, this)
        } else if (currentDeviceType is SkinDevice) {
            SkinManage(this)
        } else {
            null
        }*/
            deviceManageMap[deviceName] = bluetoothDeviceManager
        }
        val disConnectedListener = initMap(disConnectedListenerMap, deviceName) {
            fun(string: String) {
                showMsg("$deviceName disconnect:   $string")
                reconnect(deviceName)
                runOnUiThread {
                    if (deviceName == DEVICE1) {
                        btnScanConnect?.setText(R.string.connect)
                    }

                    if (deviceName == DEVICE2) {
                        btnScanConnect2?.setText(R.string.connect)
                    }
                    Toast.makeText(this@MainActivity, "$deviceName disconnect ", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        val rawListener = initMap(rawListenerMap, deviceName) {
            fun(bytes: ByteArray) {
                val mSkinDataHelper = initMap(this@MainActivity.deviceSaveHelperMap, deviceName) {
                    SkinDataHelper(deviceName)
                }
                mSkinDataHelper?.saveData(SkinDataType.BRAIN_DATA, HexDump.toHexString(bytes))
                showMsg("braindata: " + HexDump.toHexString(bytes))
            }
        }/*   val hrListener = initMap(hrListenerMap, deviceName) {
               fun(hr: Int) {
                   val mSkinDataHelper = initMap(this@MainActivity.deviceSaveHelperMap, deviceName) {
                       SkinDataHelper(deviceName)
                   }
                   mSkinDataHelper?.saveData(SkinDataType.HR, "$hr")
               }
           }*/

        bluetoothDeviceManager?.apply {
            disConnectedListener?.let {
                removeDisConnectListener(it)
            }
            rawListener?.let {
                removeRawDataListener(it)
            }/*    hrListener?.let {
                    (this as? IHrFunction)?.addHeartRateListener(it)
                }*/
            disConnect()
        }
        disConnectedListener?.let {
            bluetoothDeviceManager?.addDisConnectListener(it)
        }
        rawListener?.let {
            bluetoothDeviceManager?.addRawDataListener(it)
        }/*  hrListener?.let {
              (bluetoothDeviceManager as? IHrFunction)?.addHeartRateListener(it)
          }*/
    }

    private fun <T> initMap(map: MutableMap<String, T>, key: String, init: () -> T): T? {
        var value = map[key]
        if (value == null) {
            value = init()
        }
        return value
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
                    this, needPermission[i]
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


    private fun reconnect(deviceName: String) {
        val reconnectRunnable = initMap(reconnectRunnableMap, deviceName) {
            Runnable {
                showMsg("reconnectRunnable needReConnected:   $needReConnected")
                if (needReConnected) {
                    showMsg("start reconnect $deviceName")
                    connectDevice(deviceName) {
                        if (deviceName == DEVICE2) {
                            btnScanConnect2?.text = it
                        }

                        if (deviceName == DEVICE1) {
                            btnScanConnect?.text = it
                        }
                    }
                }
            }
        }
        reconnectRunnable?.apply {
            mainHandler.removeCallbacks(reconnectRunnable)
            mainHandler.postDelayed(reconnectRunnable, RECONNECT_DELAY_TIME)
        }

    }

    private fun connectDevice(deviceName: String = "", successUi: (String) -> Unit) {
        reconnectRunnableMap[deviceName]?.apply {
            mainHandler.removeCallbacks(this)
        }
        var bluetoothDeviceManager = deviceManageMap[deviceName]
        if (bluetoothDeviceManager == null) {
            initBleManager(deviceName = deviceName)
            bluetoothDeviceManager = deviceManageMap[deviceName]
        }
        if (bluetoothDeviceManager?.isConnected() == true) {
            showMsg("已连接  $bluetoothDeviceManager")
            return
        }

        if (bluetoothDeviceManager?.isConnecting() == true) {
            showMsg("正在连接中  $bluetoothDeviceManager")
            return
        }
        showMsg("开始寻找设备 ，准备连接 $bluetoothDeviceManager")
        var skinDataHelper = deviceSaveHelperMap[deviceName]
        if (skinDataHelper == null) {
            skinDataHelper = SkinDataHelper(deviceName)
            deviceSaveHelperMap[deviceName] = skinDataHelper
        }
        skinDataHelper.close()
        bluetoothDeviceManager?.connectDevice(fun(mac: String) {
            showMsg("connect success $mac")
            runOnUiThread {
                successUi(mac)
            }
        }, { msg ->
            showMsg("connect failed $msg")
            runOnUiThread {
                Toast.makeText(
                    this@MainActivity, "failed to connect to device：${msg}", Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun startCollectData(deviceName: String, delayMillis: Long = 0) {
        val hasNotify = hasNotifyMap[deviceName]
        if (hasNotify == true) {
            return
        }
        hasNotifyMap[deviceName] = true
        if (deviceName == DEVICE2) {
            btnStartCollection2?.isClickable = false
        }
        if (deviceName == DEVICE1) {
            btnStartCollection?.isClickable = false
        }
        val skinDataHelper = initMap(deviceSaveHelperMap, deviceName) {
            SkinDataHelper(deviceName)
        }
        skinDataHelper?.close()
        skinDataHelper?.initHelper()
        mainHandler.postDelayed({
            startCollection(deviceName, false)
            notifyData(deviceName)
            if (deviceName == DEVICE1) {
                startContact(deviceName)
            }

        }, delayMillis)
    }

    private fun stopCollectData(deviceName: String, delayMillis: Long = 0) {
        showMsg("$deviceName 准备停止收集数据 ")
        hasNotifyMap[deviceName] = false
        if (deviceName == DEVICE2) {
            btnStartCollection2?.isClickable = true
        }
        if (deviceName == DEVICE1) {
            btnStartCollection?.isClickable = true
        }
        mainHandler.postDelayed({
            if (deviceName == DEVICE1) {
                stopContact(DEVICE1)
            }
            stopNotifyData(deviceName)
            stopCollection(deviceName)

        }, delayMillis)
    }

    private fun initPersistenceState() {
        btnSwapPersistenceState?.text = if (isPersistenceExperiment) {
            "当前是处于持续性实验状态"
        } else {
            "当前是处于非持续性实验状态"
        }
    }

    fun onDisconnect(@Suppress("UNUSED_PARAMETER") view: View) {
        disconnect(DEVICE1)
    }

    fun disconnect(deviceName: String) {
        deviceManageMap[deviceName]?.disConnect {
            runOnUiThread {
                hasNotifyMap[deviceName] = false
                if (deviceName == DEVICE2) {
                    btnStartCollection2?.isClickable = true
                }
                if (deviceName == DEVICE1) {
                    btnStartCollection?.isClickable = true
                }
                if (deviceName == DEVICE2) {
                    btnScanConnect2?.setText(R.string.connect)
                }

                if (deviceName == DEVICE1) {
                    btnScanConnect?.setText(R.string.connect)
                }
            }
        }
    }

    private fun notifyData(deviceName: String) {
        (deviceManageMap[deviceName])?.notifyBrainWave()
    }

    private fun stopNotifyData(deviceName: String) {
        (deviceManageMap[deviceName])?.stopNotifyBrainWave()
    }


    private fun startContact(deviceName: String) {
        showMsg("$deviceName 开始佩戴检测 ")
        val currentBleManger = deviceManageMap[deviceName]
        currentBleManger?.apply {
            notifyContact({ result ->
                BleLogUtil.d(TAG, "startContact: $result")
                var count = deviceContactMap[deviceName]
                if (count == null) {
                    count = 0
                }
                if (result == 0) {
                    count++
                    if (count >= 5) {
                        count = 5
                    }
                } else {
                    count = 0
                }
                deviceContactMap[deviceName] = count
                mainHandler.post {
                    if (deviceName == DEVICE1) {
                        tvDevice1WearStatus
                    } else {
                        null
                    }?.apply {
                        if (count >= 5) {
                            setTextColor(
                                ContextCompat.getColor(
                                    this@MainActivity, R.color.color_common_4cd964_ff
                                )
                            )
                            text = "已佩戴"
                        } else {
                            setTextColor(
                                ContextCompat.getColor(
                                    this@MainActivity, R.color.color_common_ff0000_ff
                                )
                            )
                            text = "未佩戴"
                        }
                    }
                    if (deviceName == DEVICE1) {
                        tvDevice1
                    } else {
                        tvDevice2
                    }?.setTextColor(
                        if (count >= 5) {
                            ContextCompat.getColor(
                                this@MainActivity, R.color.color_common_4cd964_ff
                            )
                        } else {
                            ContextCompat.getColor(
                                this@MainActivity, R.color.color_common_ff0000_ff
                            )
                        }
                    )
                }

//                showMsg("佩戴检测值： $result")
            }) {
                showMsg("佩戴检测失败： $it")
            }
        } ?: kotlin.run {
            showMsg("$deviceName startContact currentBleManger is null ")
        }
    }


    private fun stopContact(deviceName: String) {
        showMsg("停止佩戴检测")
        val currentBleManger = deviceManageMap[deviceName]
        currentBleManger?.stopNotifyContact()

    }

    private fun startCollection(deviceName: String, needStop: Boolean = true) {
        val bluetoothDeviceManager = deviceManageMap[deviceName]
        if (needStop) {
            stopCollection(deviceName, success = {
                showMsg("$deviceName 开始停止收集数据")
                bluetoothDeviceManager?.stopHeartAndBrainCollection({
                    showMsg("$deviceName 停止收集数据指令发送成功 ")
                }) {
                    showMsg("$deviceName 停止收集数据指令发送失败 $it")
                } ?: showMsg("$deviceName 停止收集数据指令发送失败 bluetoothDeviceManager is null")

            })
        } else {
            showMsg("$deviceName 开始收集数据")
            bluetoothDeviceManager?.startHeartAndBrainCollection({
                showMsg("$deviceName 收集数据指令发送成功 ")
            }) {
                showMsg("$deviceName 收集数据指令发送失败 $it")
            } ?: showMsg("$deviceName 收集数据指令发送失败 bluetoothDeviceManager is null")
        }
    }

    private fun stopCollection(
        deviceName: String, success: () -> Unit = {}, failure: (String) -> Unit = {}
    ) {
        val bluetoothDeviceManager = deviceManageMap[deviceName]
        showMsg("$deviceName 停止收集数据")
        bluetoothDeviceManager?.stopHeartAndBrainCollection({
            showMsg("$deviceName 停止数据指令发送成功 ")
            success()
        }) {
            showMsg("$deviceName 停止数据指令发送失败 $it")
            failure(it)
        }
    }

    fun showLog(view: View) {
//        startActivity(Intent(this, ShowLogActivity::class.java))
    }
}

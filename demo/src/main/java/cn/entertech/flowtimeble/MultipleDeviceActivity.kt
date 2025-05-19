package cn.entertech.flowtimeble

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.entertech.base.util.startActivity
import cn.entertech.ble.BaseBleConnectManager
import cn.entertech.ble.device.tag.BrainTagManager
import cn.entertech.ble.device.tag.bean.BrainTemperatureBean
import cn.entertech.ble.device.tag.function.IDeviceGyroFunction
import cn.entertech.ble.device.tag.function.IDeviceTemperatureFunction
import cn.entertech.ble.function.IDeviceEegFunction
import cn.entertech.ble.function.IDeviceHrFunction
import cn.entertech.ble.function.collect.ICollectBrainAndHrDataFunction
import cn.entertech.ble.log.BleLogUtil
import cn.entertech.ble.utils.CharUtil
import cn.entertech.device.DeviceType
import cn.entertech.device.api.IDeviceType
import cn.entertech.flowtimeble.data.FileListActivity
import cn.entertech.flowtimeble.device.BaseDeviceFactory
import cn.entertech.flowtimeble.device.tag.BrainTagFactory
import cn.entertech.flowtimeble.device.HandBandFactory
import cn.entertech.flowtimeble.log.LogAdapter
import cn.entertech.flowtimeble.skin.SkinDataHelper
import cn.entertech.flowtimeble.skin.SkinDataType
import cn.entertech.log.local.LogListActivity
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap


class MultipleDeviceActivity : AppCompatActivity() {

    private val deviceManageMap by lazy {
        ConcurrentHashMap<String, BaseBleConnectManager?>()
    }
    private val disConnectedListenerMap by lazy {
        ConcurrentHashMap<String, (String) -> Unit>()
    }

    private val deviceSaveHelperMap by lazy {
        ConcurrentHashMap<String, SkinDataHelper>()
    }

    private val deviceContactMap by lazy {
        ConcurrentHashMap<String, Int>()
    }

    private val hasNotifyMap by lazy {
        ConcurrentHashMap<String, Boolean>()
    }
    private val reconnectRunnableMap by lazy {
        ConcurrentHashMap<String, Runnable>()
    }

    /**
     * 使用 Intent 启动 SAF 选择器
     * 无法查看到Android/data文件夹
     * */
    private fun openFolderPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        folderPickerLauncher.launch(intent)
    }

    private val folderPickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val uri: Uri? = result.data?.data
                // 处理所选文件夹的 Uri
                Toast.makeText(this, "Selected Folder: $uri", Toast.LENGTH_SHORT).show()
            }
        }
    private var spinnerDeviceTypeList: Spinner? = null
    private var cbNeedReconnected: CheckBox? = null
    private var cbAutoNotifyData: CheckBox? = null
    private var cbShowLog: CheckBox? = null
    private var scrollView_logs: RecyclerView? = null
    private var btnClearLog: Button? = null
    private var btnConnect: Button? = null
    private var btnScanConnect2: Button? = null
    private var btnStartCollection2: Button? = null
    private var btnStopCollection2: Button? = null
    private var btnStartCollection: Button? = null
    private var btnStopCollection: Button? = null
    private var btnOpenLocalLog: Button? = null
    private var btnOpenLocalData: Button? = null
    private var btnOpenFile: Button? = null
    private var tvDevice1: TextView? = null
    private var tvDevice2: TextView? = null
    private var tvDevice1Battery: TextView? = null
    private var tvDevice2Battery: TextView? = null
    private val simple by lazy {
        SimpleDateFormat("yyyy/MM/dd  hh:mm:ss:SSS")
    }
    private var deviceFactory: BaseDeviceFactory? = null
    private val adapter by lazy {
        LogAdapter()
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

    private var needLog = false
    private var autoNotifyData = false

    @Volatile
    private var needReConnected = false

    private var btnScanConnect: Button? = null

    private val devicesMap: Map<String, IDeviceType> by lazy {
        mapOf(
            Pair("BrainTag", DeviceType.DEVICE_TYPE_BRAIN_TAG),
            Pair("Handband", DeviceType.DEVICE_TYPE_HEADBAND),
        )
    }
    private val deviceTypes by lazy {
        devicesMap.map {
            it.key
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.multiple_device_activity)
        spinnerDeviceTypeList = findViewById(R.id.spinnerDeviceTypeList)
        btnOpenLocalData = findViewById(R.id.btnOpenLocalData)
        tvDevice1 = findViewById(R.id.tvDevice1)
        tvDevice2 = findViewById(R.id.tvDevice2)
        tvDevice1Battery = findViewById(R.id.tvDevice1Battery)
        tvDevice2Battery = findViewById(R.id.tvDevice2Battery)
        // 创建 ArrayAdapter
        val arrayAdapter: ArrayAdapter<String> =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, deviceTypes)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDeviceTypeList?.adapter = arrayAdapter
        cbNeedReconnected = findViewById(R.id.cbNeedReconnected)
        scrollView_logs = findViewById(R.id.scrollView_logs)
        btnScanConnect2 = findViewById(R.id.btnScanConnect2)
        btnStartCollection2 = findViewById(R.id.btnStartCollection2)
        btnStopCollection2 = findViewById(R.id.btnStopCollection2)
        btnStartCollection = findViewById(R.id.btnStartCollection)
        btnStopCollection = findViewById(R.id.btnStopCollection)
        btnOpenLocalLog = findViewById(R.id.btnOpenLocalLog)
        cbAutoNotifyData = findViewById(R.id.cbAutoNotifyData)
        spinnerDeviceTypeList?.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    initDevice(position)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    deviceFactory = null
                }
            }
        initDevice(spinnerDeviceTypeList?.selectedItemPosition)
        btnOpenLocalLog?.setOnClickListener {
//            openFolderPicker()
            startActivity(LogListActivity::class.java, finishCurrent = false)
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
        scrollView_logs?.adapter = adapter
        scrollView_logs?.layoutManager = LinearLayoutManager(this)
        btnClearLog = findViewById(R.id.btnClearLog)
        cbShowLog = findViewById(R.id.cbStopLog)
        btnClearLog?.setOnClickListener {
            adapter.setData(ArrayList())
        }
        cbNeedReconnected?.isChecked = true
        cbShowLog?.isChecked = true
        cbAutoNotifyData?.isChecked = true
        needLog = cbShowLog?.isChecked ?: false
        autoNotifyData = cbAutoNotifyData?.isChecked ?: false
        cbNeedReconnected?.setOnCheckedChangeListener { _, isChecked ->
            needReConnected = isChecked
        }
        cbShowLog?.setOnCheckedChangeListener { _, isChecked ->
            needLog = isChecked
        }
        cbAutoNotifyData?.setOnCheckedChangeListener { _, isChecked ->
            autoNotifyData = isChecked
        }
        needReConnected = cbNeedReconnected?.isChecked ?: false

        initPermission()

        btnOpenLocalData?.setOnClickListener {
            startActivity(FileListActivity::class.java, finishCurrent = false)
        }
        btnStartCollection2?.setOnClickListener {
            startCollectData(deviceFactory?.getDeviceKeyList()?.get(1))
        }
        btnStopCollection2?.setOnClickListener {
            stopCollectData(deviceFactory?.getDeviceKeyList()?.get(1))
        }
        btnStartCollection?.setOnClickListener {
            startCollectData(deviceFactory?.getDeviceKeyList()?.get(0))
        }
        btnStopCollection?.setOnClickListener {
            stopCollectData(deviceFactory?.getDeviceKeyList()?.get(0))
        }

        btnScanConnect2?.setOnClickListener {
            connectDevice(deviceFactory?.getDeviceKeyList()?.get(1)) {
                btnScanConnect2?.text = it
            }
        }
        btnScanConnect = findViewById(R.id.btnScanConnect)
        btnScanConnect?.setOnClickListener {
            connectDevice(deviceFactory?.getDeviceKeyList()?.get(0)) {
                btnScanConnect?.text = it
            }
        }
        btnConnect?.setOnClickListener {
//            onConnectBound()
        }
    }

    private fun initBleManager(deviceName: String) {
        showMsg("$deviceName 初始化 deviceFactory: $deviceFactory")
        var bluetoothDeviceManager: BaseBleConnectManager? = deviceManageMap[deviceName]
        if (bluetoothDeviceManager == null) {
            bluetoothDeviceManager = deviceFactory?.createBleConnectManager(this)
            deviceManageMap[deviceName] = bluetoothDeviceManager
        }
        val disConnectedListener = initMap(disConnectedListenerMap, deviceName) {
            fun(string: String) {
                showMsg("$deviceName disconnect:   $string")
                reconnect(deviceName)
                runOnUiThread {
                    stopCollectData(deviceName)
                    if (deviceName == deviceFactory?.getDeviceKeyList()?.get(0)) {
                        btnScanConnect?.setText(R.string.connect)
                    }

                    if (deviceName == deviceFactory?.getDeviceKeyList()?.get(1)) {
                        btnScanConnect2?.setText(R.string.connect)
                    }
                    Toast.makeText(
                        this@MultipleDeviceActivity, "$deviceName disconnect ", Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        bluetoothDeviceManager?.apply {
            disConnectedListener?.let {
                removeDisConnectListener(it)
            }
            disConnect()
        }
        disConnectedListener?.let {
            bluetoothDeviceManager?.addDisConnectListener(it)
        }
    }

    private fun <T> initMap(map: MutableMap<String, T>, key: String, init: () -> T): T? {
        var value = map[key]
        if (value == null) {
            value = init()
            map[key] = value
        }
        return value
    }

    private fun initDevice(position: Int?) {
        if (position == null) {
            return
        }
        val currentDeviceType = devicesMap[deviceTypes[position]]
        if (currentDeviceType == DeviceType.DEVICE_TYPE_BRAIN_TAG) {
            deviceFactory = BrainTagFactory()
        }
        if (currentDeviceType == DeviceType.DEVICE_TYPE_HEADBAND) {
            deviceFactory = HandBandFactory()
        }

        deviceFactory?.apply {
            tvDevice1?.text = getDeviceInfo()[getDeviceKeyList()[0]]?.deviceName
            tvDevice2?.text = getDeviceInfo()[getDeviceKeyList()[1]]?.deviceName
        }
    }

    private fun showMsg(msg: String, needShow: Boolean = true) {
        val realMsg = "->: ${simple.format(Date())} $msg\n"
        BleLogUtil.d(TAG, realMsg)
        if (needLog && needShow) {
            runOnUiThread {
                adapter.addItem(realMsg)
                scrollView_logs?.scrollToPosition(adapter.itemCount - 1)
            }
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
            ActivityCompat.requestPermissions(this@MultipleDeviceActivity, permissions, 1)
        }
    }


    private fun reconnect(deviceName: String) {
        val reconnectRunnable = initMap(reconnectRunnableMap, deviceName) {
            Runnable {
                showMsg("reconnectRunnable needReConnected:   $needReConnected")
                if (needReConnected) {
                    showMsg("start reconnect $deviceName")
                    connectDevice(deviceName) {
                        if (deviceName == deviceFactory?.getDeviceKeyList()?.get(1)) {
                            btnScanConnect2?.text = it
                        }

                        if (deviceName == deviceFactory?.getDeviceKeyList()?.get(0)) {
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

    private fun connectDevice(deviceName: String? = "", successUi: (String) -> Unit) {
        if (deviceName == null) {
            return
        }
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
            notifyBattery(deviceName)
            runOnUiThread {
                successUi(mac)
            }
            if (autoNotifyData) {
                mainHandler.post {
                    startCollectData(deviceName, 100)
                }
            }
        }, { msg ->
            showMsg("connect failed $msg")
            runOnUiThread {
                Toast.makeText(
                    this@MultipleDeviceActivity,
                    "failed to connect to device：${msg}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }, cn.entertech.ble.api.ConnectionBleStrategy.SCAN_AND_CONNECT_HIGH_SIGNAL)
    }

    private fun startCollectData(deviceName: String?, delayMillis: Long = 0) {
        if (deviceName == null) {
            return
        }
        val hasNotify = hasNotifyMap[deviceName]
        if (hasNotify == true) {
            return
        }
        hasNotifyMap[deviceName] = true
        if (deviceName == deviceFactory?.getDeviceKeyList()?.get(1)) {
            btnStartCollection2?.isClickable = false
            btnStartCollection2?.isEnabled = false

        }
        if (deviceName == deviceFactory?.getDeviceKeyList()?.get(0)) {
            btnStartCollection?.isClickable = false
            btnStartCollection?.isEnabled = false
        }
        val skinDataHelper = initMap(deviceSaveHelperMap, deviceName) {
            SkinDataHelper(deviceName)
        }
        skinDataHelper?.close()
        skinDataHelper?.initHelper()
        mainHandler.postDelayed({
            startCollection(deviceName, false)
            startContact(deviceName)
            notifyBrainData(deviceName)
            notifyHrData(deviceName)
            notifyRrData(deviceName)
            notifyTemData(deviceName)
            notifySleepPositionData(deviceName)
            notifyExerciseLevelData(deviceName)
        }, delayMillis)
    }

    private fun stopCollectData(deviceName: String?, delayMillis: Long = 0) {
        if (deviceName == null) {
            return
        }
        showMsg("$deviceName 准备停止收集数据 ")
        mainHandler.postDelayed({
            hasNotifyMap[deviceName] = false
            if (deviceName == deviceFactory?.getDeviceKeyList()?.get(1)) {
                btnStartCollection2?.isClickable = true
                btnStartCollection2?.isEnabled = true
            }
            if (deviceName == deviceFactory?.getDeviceKeyList()?.get(0)) {
                btnStartCollection?.isClickable = true
                btnStartCollection?.isEnabled = true
            }
//            startContact(deviceName)
            stopNotifyBrainData(deviceName)
            stopNotifySleepPositionData(deviceName)
            stopNotifyExerciseLevelData(deviceName)
            stopNotifyTemData(deviceName)
            stopNotifyHrData(deviceName)
            stopCollection(deviceName)
        }, delayMillis)
    }

    fun onDisconnect(@Suppress("UNUSED_PARAMETER") view: View) {
        disconnect(deviceFactory?.getDeviceKeyList()?.get(0))
    }

    private fun disconnect(deviceName: String?) {
        if (deviceName == null) {
            return
        }
        deviceManageMap[deviceName]?.disConnect {
            runOnUiThread {
                hasNotifyMap[deviceName] = false
                if (deviceName == deviceFactory?.getDeviceKeyList()?.get(1)) {
                    btnStartCollection2?.isClickable = true
                    btnStartCollection2?.isEnabled = true
                }
                if (deviceName == deviceFactory?.getDeviceKeyList()?.get(0)) {
                    btnStartCollection?.isClickable = true
                    btnStartCollection?.isEnabled = true
                }
                if (deviceName == deviceFactory?.getDeviceKeyList()?.get(1)) {
                    btnScanConnect2?.setText(R.string.connect)
                }

                if (deviceName == deviceFactory?.getDeviceKeyList()?.get(0)) {
                    btnScanConnect?.setText(R.string.connect)
                }
            }
        }
    }

    private fun notifyBattery(deviceName: String) {
        showMsg("$deviceName 订阅电量数据")
        (deviceManageMap[deviceName] as? BrainTagManager)?.notifyBattery({
            BleLogUtil.d(TAG, "battery: ${it.contentToString()}")
            val battery = if (it.isEmpty()) {
                0
            } else {
                it[0]
            }
            mainHandler.post {
                if (deviceName == deviceFactory?.getDeviceKeyList()?.get(0)) {
                    tvDevice1Battery
                } else {
                    tvDevice2Battery
                }?.text = "电量：${CharUtil.converUnchart(battery)}"
            }

        }, {
            showMsg("$deviceName 订阅电量数据失败$it")
        }) ?: kotlin.run {
            showMsg("$deviceName 设备 不支持 电量 数据")
        }
    }

    private fun notifyBrainData(deviceName: String) {
        showMsg("$deviceName 订阅脑波数据")
        (deviceManageMap[deviceName] as? IDeviceEegFunction)?.notifyBrainWave({
            initMap(this@MultipleDeviceActivity.deviceSaveHelperMap, deviceName) {
                SkinDataHelper(deviceName)
            }?.saveData(SkinDataType.BRAIN_DATA, it)
        }, {
            showMsg("$deviceName 订阅脑波数据失败$it")
        }) ?: kotlin.run {
            showMsg("$deviceName 设备 不支持 eeg 数据")
        }
    }

    private fun stopNotifyBrainData(deviceName: String) {
        showMsg("$deviceName 取消订阅脑波数据")
        (deviceManageMap[deviceName] as? IDeviceEegFunction)?.stopNotifyBrainWave({
            showMsg("$deviceName 取消订阅脑波数据成功")
        }, {
            showMsg("$deviceName 取消订阅脑波数据失败$it")
        }) ?: kotlin.run {
            showMsg("$deviceName 设备 不支持 eeg 数据")
        }
    }

    private fun notifyHrData(deviceName: String) {
        showMsg("$deviceName 订阅心率数据")
        (deviceManageMap[deviceName] as? IDeviceHrFunction<*, *>)?.notifyHeartRate({
            initMap(this@MultipleDeviceActivity.deviceSaveHelperMap, deviceName) {
                SkinDataHelper(deviceName)
            }?.saveData(SkinDataType.HR, it)
        }, {
            showMsg("$deviceName 订阅心率数据失败 $it")
        }) ?: kotlin.run {
            showMsg("$deviceName 设备 不支持 心率 数据")
        }
    }

    private fun notifyRrData(deviceName: String) {
        showMsg("$deviceName 订阅呼吸率数据")
        (deviceManageMap[deviceName] as? IDeviceHrFunction<*, *>)?.notifyHeartRate({
            initMap(this@MultipleDeviceActivity.deviceSaveHelperMap, deviceName) {
                SkinDataHelper(deviceName)
            }?.saveData(SkinDataType.RR, it)
        }, {
            showMsg("$deviceName 订阅呼吸率数据失败 $it")
        }) ?: kotlin.run {
            showMsg("$deviceName 设备 不支持 心率 数据")
        }
    }

    private fun stopNotifyHrData(deviceName: String) {
        showMsg("$deviceName 取消订阅心率数据")
        (deviceManageMap[deviceName] as? IDeviceHrFunction<*, *>)?.stopNotifyHeartRate({
            showMsg("$deviceName 取消订阅心率数据成功")
        }, {
            showMsg("$deviceName 取消订阅心率数据失败")
        }) ?: kotlin.run {
            showMsg("$deviceName 设备 不支持 心率 数据")
        }
    }

    private fun notifyTemData(deviceName: String) {
        showMsg("$deviceName 订阅温度数据")
        (deviceManageMap[deviceName] as? IDeviceTemperatureFunction<*>)?.notifyTemperatureValue({
            if (it is BrainTemperatureBean) {
                initMap(this@MultipleDeviceActivity.deviceSaveHelperMap, deviceName) {
                    SkinDataHelper(deviceName)
                }?.saveData(SkinDataType.Temperature, it.raw)
                showMsg("Temperature: ${it.tem}", false)
            } else {
                showMsg("Temperature is error $it")
            }

        }, {
            showMsg("$deviceName 订阅温度数据失败")
        }) ?: kotlin.run {
            showMsg("$deviceName 设备 不支持 温度 数据")
        }
    }

    private fun stopNotifyTemData(deviceName: String) {
        showMsg("$deviceName 取消订阅温度数据")
        (deviceManageMap[deviceName] as? IDeviceTemperatureFunction<*>)?.stopNotifyTemperature({
            showMsg("$deviceName 取消订阅温度数据成功")
        }, {
            showMsg("$deviceName 取消订阅温度数据失败")
        }) ?: kotlin.run {
            showMsg("$deviceName 设备 不支持 温度 数据")
        }
    }

    private fun notifySleepPositionData(deviceName: String) {
        showMsg("$deviceName 订阅睡眠姿势数据")
        (deviceManageMap[deviceName] as? IDeviceGyroFunction<*, *>)?.notifySleepPosture({
            initMap(this@MultipleDeviceActivity.deviceSaveHelperMap, deviceName) {
                SkinDataHelper(deviceName)
            }?.saveData(SkinDataType.SleepPosture, it)
        }, {
            showMsg("$deviceName 订阅睡眠姿势数据失败")
        }) ?: kotlin.run {
            showMsg("$deviceName 设备 不支持 睡眠姿势 数据")
        }
    }

    private fun stopNotifySleepPositionData(deviceName: String) {
        showMsg("$deviceName 取消订阅睡眠姿势数据")
        (deviceManageMap[deviceName] as? IDeviceGyroFunction<*, *>)?.stopNotifySleepPosture({
            showMsg("$deviceName 取消订阅睡眠姿势数据成功")
        }, {
            showMsg("$deviceName 取消订阅睡眠姿势数据失败")
        }) ?: kotlin.run {
            showMsg("$deviceName 设备 不支持 睡眠姿势 数据")
        }
    }

    private fun notifyExerciseLevelData(deviceName: String) {
        showMsg("$deviceName 订阅运动水平数据")
        (deviceManageMap[deviceName] as? IDeviceGyroFunction<*, *>)?.notifyExerciseLevel({
            initMap(this@MultipleDeviceActivity.deviceSaveHelperMap, deviceName) {
                SkinDataHelper(deviceName)
            }?.saveData(SkinDataType.ExerciseLevel, it)
        }, {
            showMsg("$deviceName 订阅运动水平数据失败")
        }) ?: kotlin.run {
            showMsg("设备 不支持 运动水平 数据")
        }
    }

    private fun stopNotifyExerciseLevelData(deviceName: String) {
        showMsg("$deviceName 取消订阅运动水平数据")
        (deviceManageMap[deviceName] as? IDeviceGyroFunction<*, *>)?.stopNotifyExerciseLevel({
            showMsg("$deviceName 取消订阅运动水平数据成功")
        }, {
            showMsg("$deviceName 取消订阅运动水平数据失败")
        }) ?: kotlin.run {
            showMsg("$deviceName 设备 不支持 运动水平 数据")
        }
    }


    private fun startContact(deviceName: String) {
        showMsg("$deviceName 开始佩戴检测 ")
        val currentBleManger = deviceManageMap[deviceName]
        if (currentBleManger is IDeviceEegFunction) {
            currentBleManger.notifyContact({
                initMap(this@MultipleDeviceActivity.deviceSaveHelperMap, deviceName) {
                    SkinDataHelper(deviceName)
                }?.saveData(SkinDataType.Contact, it)
                val result = if (it.isEmpty()) {
                    -1
                } else {
                    CharUtil.converUnchart(it[0])
                }
                BleLogUtil.d(TAG, "startContact: $result  rawdata  ${it.contentToString()}")
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
                    if (deviceName == deviceFactory?.getDeviceKeyList()?.get(0)) {
                        tvDevice1
                    } else {
                        tvDevice2
                    }?.setTextColor(
                        if (count >= 5) {
                            ContextCompat.getColor(
                                this@MultipleDeviceActivity, cn.entertech.base.R.color.color_common_4cd964_ff
                            )
                        } else {
                            ContextCompat.getColor(
                                this@MultipleDeviceActivity, cn.entertech.base.R.color.color_common_ff0000_ff
                            )
                        }
                    )
                }

//                showMsg("佩戴检测值： $result")
            }) {
                showMsg("佩戴检测失败： $it")
            }
        } else {
            showMsg("$deviceName 设备不支持 佩戴检测")
        }

    }

    private fun stopContact(deviceName: String) {
        showMsg("$deviceName 停止佩戴检测")
        val currentBleManger = deviceManageMap[deviceName]
        if (currentBleManger is IDeviceEegFunction) {
            currentBleManger.stopNotifyContact({
                showMsg("$deviceName 停止佩戴检测成功")
            }, {
                showMsg("$deviceName 停止佩戴检测失败")
            })
        }
    }

    private fun startCollection(deviceName: String, needStop: Boolean = true) {
        val bluetoothDeviceManager = deviceManageMap[deviceName]
        if (needStop) {
            stopCollection(deviceName, success = {
                showMsg("$deviceName 开始停止收集数据")
                (bluetoothDeviceManager as? ICollectBrainAndHrDataFunction)?.apply {
                    stopCollectBrainAndHrData(Unit, success = {
                        showMsg("$deviceName 停止收集数据指令发送成功 ")
                    }, failure = { _, it ->
                        showMsg("$deviceName 停止收集数据指令发送失败 $it")
                    })
                } ?: kotlin.run {
                    showMsg("$deviceName 停止收集数据指令发送失败 bluetoothDeviceManager is not ICollectBrainAndHrDataFunction")
                }


            })
        } else {
            showMsg("$deviceName 开始收集数据")
            (bluetoothDeviceManager as? ICollectBrainAndHrDataFunction)?.apply {
                startCollectBrainAndHrData(Unit, success = {
                    showMsg("$deviceName 停止收集数据指令发送成功 ")
                }, failure = { _, it ->
                    showMsg("$deviceName 停止收集数据指令发送失败 $it")
                })
            } ?: kotlin.run {
                showMsg("$deviceName 停止收集数据指令发送失败 bluetoothDeviceManager is not ICollectBrainAndHrDataFunction")
            }
        }
    }

    private fun stopCollection(
        deviceName: String, success: () -> Unit = {}, failure: (String) -> Unit = {}
    ) {
        val bluetoothDeviceManager = deviceManageMap[deviceName]
        showMsg("$deviceName 停止收集数据")
        (bluetoothDeviceManager as? ICollectBrainAndHrDataFunction)?.apply {
            startCollectBrainAndHrData(Unit, success = {
                success()
            }, failure = { _, it ->
                failure("$deviceName 停止收集数据指令发送失败 $it")
            })
        } ?: kotlin.run {
            showMsg("$deviceName 停止收集数据指令发送失败 bluetoothDeviceManager is not ICollectBrainAndHrDataFunction")
            failure("$deviceName 停止收集数据指令发送失败 bluetoothDeviceManager is not ICollectBrainAndHrDataFunction")
        }
    }

    fun showLog(view: View) {
//        startActivity(Intent(this, ShowLogActivity::class.java))
    }
}

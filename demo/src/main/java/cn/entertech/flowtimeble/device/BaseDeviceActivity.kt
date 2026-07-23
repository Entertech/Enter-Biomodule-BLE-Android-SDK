package cn.entertech.flowtimeble.device

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.entertech.base.BaseActivity
import cn.entertech.base.util.startActivity
import cn.entertech.ble.BaseBleConnectManager
import cn.entertech.ble.api.ConnectionBleStrategy
import cn.entertech.ble.device.eyehead.EyeHeadManager
import cn.entertech.ble.function.IBatteryFunction
import cn.entertech.ble.function.IBrainWaveFunction
import cn.entertech.ble.function.IContactFunction
import cn.entertech.ble.function.IExerciseLevelFunction
import cn.entertech.ble.function.IHrFunction
import cn.entertech.ble.function.IInfoFunction
import cn.entertech.ble.function.ISleepPostureFunction
import cn.entertech.ble.function.ITemperatureFunction
import cn.entertech.ble.function.collect.ICollectBrainAndHrDataFunction
import cn.entertech.ble.function.collect.ICollectBrainDataFunction
import cn.entertech.ble.function.collect.ICollectExerciseDegreeDataFunction
import cn.entertech.ble.log.BleLogUtil
import cn.entertech.flowtimeble.R
import cn.entertech.flowtimeble.data.FileListActivity
import cn.entertech.flowtimeble.data.MeditateDataHelper
import cn.entertech.flowtimeble.device.BleFunction.BLE_FUNCTION_FLAG_NOTIFY_BATTERY
import cn.entertech.flowtimeble.device.BleFunction.BLE_FUNCTION_FLAG_NOTIFY_BRAIN_WAVE
import cn.entertech.flowtimeble.device.BleFunction.BLE_FUNCTION_FLAG_NOTIFY_CONTACT
import cn.entertech.flowtimeble.device.BleFunction.BLE_FUNCTION_FLAG_NOTIFY_EXERCISE_LEVEL
import cn.entertech.flowtimeble.device.BleFunction.BLE_FUNCTION_FLAG_NOTIFY_HR
import cn.entertech.flowtimeble.device.BleFunction.BLE_FUNCTION_FLAG_NOTIFY_SLEEP_POSTURE
import cn.entertech.flowtimeble.device.BleFunction.BLE_FUNCTION_FLAG_NOTIFY_TEMPERATURE
import cn.entertech.flowtimeble.device.BleFunction.BLE_FUNCTION_FLAG_READ_BATTERY
import cn.entertech.flowtimeble.device.BleFunction.BLE_FUNCTION_FLAG_READ_FIRMWARE
import cn.entertech.flowtimeble.device.BleFunction.BLE_FUNCTION_FLAG_READ_HARDWARE
import cn.entertech.flowtimeble.device.BleFunction.BLE_FUNCTION_FLAG_READ_MAC
import cn.entertech.flowtimeble.device.BleFunction.BLE_FUNCTION_FLAG_READ_MANUFACTURER
import cn.entertech.flowtimeble.device.BleFunction.BLE_FUNCTION_FLAG_READ_SERIAL
import cn.entertech.flowtimeble.device.BleFunction.BLE_FUNCTION_FLAG_START_COLLECT_BRAIN_HR
import cn.entertech.flowtimeble.device.BleFunction.BLE_FUNCTION_FLAG_START_COLLECT_EXERCISE_DEGREE
import cn.entertech.flowtimeble.device.BleFunction.BLE_FUNCTION_FLAG_STOP_COLLECT_BRAIN_HR
import cn.entertech.flowtimeble.device.BleFunction.BLE_FUNCTION_FLAG_STOP_COLLECT_EXERCISE_DEGREE
import cn.entertech.flowtimeble.device.BleFunction.BLE_FUNCTION_FLAG_STOP_NOTIFY_BATTERY
import cn.entertech.flowtimeble.device.BleFunction.BLE_FUNCTION_FLAG_STOP_NOTIFY_BRAIN_WAVE
import cn.entertech.flowtimeble.device.BleFunction.BLE_FUNCTION_FLAG_STOP_NOTIFY_CONTACT
import cn.entertech.flowtimeble.device.BleFunction.BLE_FUNCTION_FLAG_STOP_NOTIFY_EXERCISE_LEVEL
import cn.entertech.flowtimeble.device.BleFunction.BLE_FUNCTION_FLAG_STOP_NOTIFY_HR
import cn.entertech.flowtimeble.device.BleFunction.BLE_FUNCTION_FLAG_STOP_NOTIFY_SLEEP_POSTURE
import cn.entertech.flowtimeble.device.BleFunction.BLE_FUNCTION_FLAG_STOP_NOTIFY_TEMPERATURE
import cn.entertech.flowtimeble.device.BleFunctionListAdapter.IBleFunctionClick
import cn.entertech.flowtimeble.log.LogAdapter
import cn.entertech.log.local.LogListActivity
import java.text.SimpleDateFormat
import java.util.Date

abstract class BaseDeviceActivity : BaseActivity(), IBleFunctionClick {

    companion object {
        private const val TAG = "BaseDeviceActivity"
        private const val RECONNECT_DELAY_TIME = 2000L
        private val START_TO_STOP_FUNCTION_MAP = mapOf(
            BLE_FUNCTION_FLAG_START_COLLECT_EXERCISE_DEGREE to BLE_FUNCTION_FLAG_STOP_COLLECT_EXERCISE_DEGREE,
            BleFunction.BLE_FUNCTION_FLAG_START_COLLECT_BRAIN to BleFunction.BLE_FUNCTION_FLAG_STOP_COLLECT_BRAIN,
            BLE_FUNCTION_FLAG_START_COLLECT_BRAIN_HR to BLE_FUNCTION_FLAG_STOP_COLLECT_BRAIN_HR,
            BLE_FUNCTION_FLAG_NOTIFY_HR to BLE_FUNCTION_FLAG_STOP_NOTIFY_HR,
            BLE_FUNCTION_FLAG_NOTIFY_BRAIN_WAVE to BLE_FUNCTION_FLAG_STOP_NOTIFY_BRAIN_WAVE,
            BleFunction.BLE_FUNCTION_FLAG_NOTIFY_BRAIN_CONTRACT to BleFunction.BLE_FUNCTION_FLAG_STOP_NOTIFY_BRAIN_CONTRACT,
            BLE_FUNCTION_FLAG_NOTIFY_CONTACT to BLE_FUNCTION_FLAG_STOP_NOTIFY_CONTACT,
            BLE_FUNCTION_FLAG_NOTIFY_SLEEP_POSTURE to BLE_FUNCTION_FLAG_STOP_NOTIFY_SLEEP_POSTURE,
            BLE_FUNCTION_FLAG_NOTIFY_EXERCISE_LEVEL to BLE_FUNCTION_FLAG_STOP_NOTIFY_EXERCISE_LEVEL,
            BLE_FUNCTION_FLAG_NOTIFY_TEMPERATURE to BLE_FUNCTION_FLAG_STOP_NOTIFY_TEMPERATURE,
            BLE_FUNCTION_FLAG_NOTIFY_BATTERY to BLE_FUNCTION_FLAG_STOP_NOTIFY_BATTERY
        )
        private val STOP_TO_START_FUNCTION_MAP = START_TO_STOP_FUNCTION_MAP.entries.associate {
            it.value to it.key
        }
    }

    private var needLog = false
    private var needReconnected = false
    private val adapter by lazy {
        LogAdapter()
    }
    protected var meditateDataHelper: MeditateDataHelper? = null
    private val simple by lazy {
        SimpleDateFormat("yyyy/MM/dd  hh:mm:ss:SSS")
    }
    private var cbShowLog: CheckBox? = null
    private var cbNeedReconnected: CheckBox? = null
    protected var bluetoothDeviceManager: BaseBleConnectManager? = null
    private var scrollViewLogs: RecyclerView? = null
    private var btnClearLog: Button? = null
    private val functionListAdapter by lazy {
        val adapter = BleFunctionListAdapter()
        adapter.bleFunctionClick = this
        adapter
    }
    private var connectionBleStrategy = ConnectionBleStrategy.SCAN_AND_CONNECT_HIGH_SIGNAL
    private var mac: String = ""
    private val activeStartFunctionSet = mutableSetOf<BleFunction>()
    private var connectActionInProgress = false
    private var userDisconnectRequested = false
    private val reconnectRunnable: Runnable by lazy {
        Runnable {
            showMsg("reconnectRunnable needReConnected:   $needReconnected")
            if (needReconnected) {
                showMsg("start reconnect")
                connectDevice()
            }
        }
    }
    protected val connectListener: (String) -> Unit by lazy {
        { mac ->
            deviceConnect(mac)
        }
    }

    protected val disconnectListener: (String) -> Unit by lazy {
        {
            deviceDisconnect()
        }
    }
    private var rvBleFunction: RecyclerView? = null
    private var btnOpenLocalLog: View? = null
    private var btnOpenLocalData: View? = null
    private var rbConnectScan: RadioButton? = null
    private var rbConnectMac: RadioButton? = null
    private var rbConnectName: RadioButton? = null
    private var radioGroup: RadioGroup? = null
    private var tvDeviceTypeName: TextView? = null


    private var btnConnectDevice: Button? = null
    private var btnDisconnectDevice: Button? = null
    private var etConnectInfo: EditText? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_demo)
        rvBleFunction = findViewById(R.id.rvBleFunction)
        tvDeviceTypeName = findViewById(R.id.tvDeviceTypeName)
        tvDeviceTypeName?.text = getDeviceTypeName()
        etConnectInfo = findViewById(R.id.etConnectInfo)
        radioGroup = findViewById(R.id.rgConnectTypes)
        radioGroup?.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbConnectScan -> {
                    etConnectInfo?.visibility = View.GONE
                    connectionBleStrategy = ConnectionBleStrategy.SCAN_AND_CONNECT_HIGH_SIGNAL
                }

                R.id.rbConnectMac -> {
                    etConnectInfo?.visibility = View.VISIBLE
                    etConnectInfo?.hint = getString(R.string.input_mac)
                    connectionBleStrategy = ConnectionBleStrategy.CONNECT_DEVICE_MAC
                }

                R.id.rbConnectName -> {
                    etConnectInfo?.visibility = View.VISIBLE
                    etConnectInfo?.hint = getString(R.string.input_name)
                }
            }
        }
        rbConnectScan = findViewById(R.id.rbConnectScan)
        rbConnectMac = findViewById(R.id.rbConnectMac)
        rbConnectName = findViewById(R.id.rbConnectName)
        btnConnectDevice = findViewById(R.id.btnConnectDevice)
        btnDisconnectDevice = findViewById(R.id.btnDisconnectDevice)
        btnConnectDevice?.setOnClickListener(this)
        scrollViewLogs = findViewById(R.id.scrollView_logs)
        btnClearLog = findViewById(R.id.btnClearLog)
        btnOpenLocalLog = findViewById(R.id.btnOpenLocalLog)
        btnOpenLocalData = findViewById(R.id.btnOpenLocalData)
        btnOpenLocalLog?.setOnClickListener {
            startActivity(LogListActivity::class.java, finishCurrent = false)
        }
        btnOpenLocalData?.setOnClickListener {
            startActivity(FileListActivity::class.java, finishCurrent = false)
        }
        cbShowLog = findViewById(R.id.cbShowLog)
        cbNeedReconnected = findViewById(R.id.cbNeedReconnected)
        scrollViewLogs?.adapter = adapter
        scrollViewLogs?.layoutManager = LinearLayoutManager(this)
        rvBleFunction?.layoutManager = GridLayoutManager(this, 2)
        rvBleFunction?.adapter = functionListAdapter
        bluetoothDeviceManager = EyeHeadManager(this)


        cbShowLog?.isChecked = true
        cbNeedReconnected?.isChecked = true
        needLog = cbShowLog?.isChecked ?: false
        needReconnected = cbNeedReconnected?.isChecked ?: false
        cbShowLog?.setOnCheckedChangeListener { _, isChecked ->
            needLog = isChecked
        }
        cbNeedReconnected?.setOnCheckedChangeListener { _, isChecked ->
            needReconnected = isChecked
        }
        btnClearLog?.setOnClickListener {
            adapter.setData(ArrayList())
        }
        bluetoothDeviceManager = initBleManager()
        bluetoothDeviceManager?.addConnectListener(connectListener)
        bluetoothDeviceManager?.addDisConnectListener(disconnectListener)
        initBleFunctionList()
        updateConnectActionState()
        initPermission()
    }

    open fun initBleManager(): BaseBleConnectManager? {
        return null
    }

    open fun initMeditateDataHelper():MeditateDataHelper?{
        return null
    }

    open fun getDeviceTypeName(): String {
        return ""
    }


    private fun reconnect() {
        mainHandler.removeCallbacks(reconnectRunnable)
        mainHandler.postDelayed(reconnectRunnable, RECONNECT_DELAY_TIME)
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
        if (needRequestPermissions.isNotEmpty()) {
            val permissions = arrayOfNulls<String>(needRequestPermissions.size)
            for (i in needRequestPermissions.indices) {
                permissions[i] = needRequestPermissions[i]
            }
            ActivityCompat.requestPermissions(this, permissions, 1)
        }
    }

    protected fun initBleFunctionList() {
        val bleFunctionList = ArrayList<BleFunctionUiBean>()
        if (BleFunctionPolicy.hrOnly) {
            if (bluetoothDeviceManager is ICollectBrainAndHrDataFunction) {
                bleFunctionList.add(
                    BleFunctionUiBean(
                        BLE_FUNCTION_FLAG_START_COLLECT_BRAIN_HR
                    )
                )
                bleFunctionList.add(
                    BleFunctionUiBean(
                        BLE_FUNCTION_FLAG_STOP_COLLECT_BRAIN_HR
                    )
                )
            }
            if (bluetoothDeviceManager is IHrFunction<*>) {
                bleFunctionList.add(
                    BleFunctionUiBean(
                        BLE_FUNCTION_FLAG_NOTIFY_HR
                    )
                )
                bleFunctionList.add(
                    BleFunctionUiBean(
                        BLE_FUNCTION_FLAG_STOP_NOTIFY_HR
                    )
                )
            }
            functionListAdapter.setNewData(bleFunctionList)
            updateBleFunctionEnableState()
            return
        }
        if (bluetoothDeviceManager is IInfoFunction) {
            bleFunctionList.add(
                BleFunctionUiBean(
                    BLE_FUNCTION_FLAG_READ_FIRMWARE
                )
            )
            bleFunctionList.add(
                BleFunctionUiBean(
                    BLE_FUNCTION_FLAG_READ_HARDWARE
                )
            )
            bleFunctionList.add(
                BleFunctionUiBean(
                    BLE_FUNCTION_FLAG_READ_MAC
                )
            )
            bleFunctionList.add(
                BleFunctionUiBean(
                    BLE_FUNCTION_FLAG_READ_SERIAL
                )
            )
            bleFunctionList.add(
                BleFunctionUiBean(
                    BLE_FUNCTION_FLAG_READ_MANUFACTURER
                )
            )
        }
        if (bluetoothDeviceManager is ICollectBrainAndHrDataFunction) {
            bleFunctionList.add(
                BleFunctionUiBean(
                    BLE_FUNCTION_FLAG_START_COLLECT_BRAIN_HR
                )
            )
            bleFunctionList.add(
                BleFunctionUiBean(
                    BLE_FUNCTION_FLAG_STOP_COLLECT_BRAIN_HR
                )
            )
        }
        if (bluetoothDeviceManager is ICollectBrainDataFunction) {
            bleFunctionList.add(
                BleFunctionUiBean(
                    BleFunction.BLE_FUNCTION_FLAG_START_COLLECT_BRAIN
                )
            )
            bleFunctionList.add(
                BleFunctionUiBean(
                    BleFunction.BLE_FUNCTION_FLAG_STOP_COLLECT_BRAIN
                )
            )
        }
        if (bluetoothDeviceManager is IHrFunction<*>) {
            bleFunctionList.add(
                BleFunctionUiBean(
                    BLE_FUNCTION_FLAG_NOTIFY_HR
                )
            )
            bleFunctionList.add(
                BleFunctionUiBean(
                    BLE_FUNCTION_FLAG_STOP_NOTIFY_HR
                )
            )
        }
        if (bluetoothDeviceManager is IBrainWaveFunction) {
            if (bluetoothDeviceManager is IContactFunction<*>) {
                bleFunctionList.add(BleFunctionUiBean(BleFunction.BLE_FUNCTION_FLAG_NOTIFY_BRAIN_CONTRACT))
                bleFunctionList.add(BleFunctionUiBean(BleFunction.BLE_FUNCTION_FLAG_STOP_NOTIFY_BRAIN_CONTRACT))
            } else {
                bleFunctionList.add(
                    BleFunctionUiBean(
                        BLE_FUNCTION_FLAG_NOTIFY_BRAIN_WAVE
                    )
                )
                bleFunctionList.add(
                    BleFunctionUiBean(
                        BLE_FUNCTION_FLAG_STOP_NOTIFY_BRAIN_WAVE
                    )
                )
            }
        } else {
            if (bluetoothDeviceManager is IContactFunction<*>) {
                bleFunctionList.add(
                    BleFunctionUiBean(
                        BLE_FUNCTION_FLAG_NOTIFY_CONTACT
                    )
                )
                bleFunctionList.add(
                    BleFunctionUiBean(
                        BLE_FUNCTION_FLAG_STOP_NOTIFY_CONTACT
                    )
                )
            }
        }




        if (bluetoothDeviceManager is ITemperatureFunction<*>) {
            bleFunctionList.add(
                BleFunctionUiBean(
                    BLE_FUNCTION_FLAG_NOTIFY_TEMPERATURE
                )
            )
            bleFunctionList.add(
                BleFunctionUiBean(
                    BLE_FUNCTION_FLAG_STOP_NOTIFY_TEMPERATURE
                )
            )
        }
        if (bluetoothDeviceManager is IBatteryFunction<*>) {
            bleFunctionList.add(
                BleFunctionUiBean(
                    BLE_FUNCTION_FLAG_NOTIFY_BATTERY
                )
            )
            bleFunctionList.add(
                BleFunctionUiBean(
                    BLE_FUNCTION_FLAG_STOP_NOTIFY_BATTERY
                )
            )
            bleFunctionList.add(
                BleFunctionUiBean(
                    BLE_FUNCTION_FLAG_READ_BATTERY
                )
            )
        }
        if (bluetoothDeviceManager is ICollectExerciseDegreeDataFunction) {
            bleFunctionList.add(
                BleFunctionUiBean(
                    BLE_FUNCTION_FLAG_START_COLLECT_EXERCISE_DEGREE
                )
            )
            bleFunctionList.add(
                BleFunctionUiBean(
                    BLE_FUNCTION_FLAG_STOP_COLLECT_EXERCISE_DEGREE
                )
            )
        }
        if (bluetoothDeviceManager is ISleepPostureFunction<*>) {
            bleFunctionList.add(
                BleFunctionUiBean(
                    BLE_FUNCTION_FLAG_NOTIFY_SLEEP_POSTURE
                )
            )
            bleFunctionList.add(
                BleFunctionUiBean(
                    BLE_FUNCTION_FLAG_STOP_NOTIFY_SLEEP_POSTURE
                )
            )
        }
        if (bluetoothDeviceManager is IExerciseLevelFunction<*>) {
            bleFunctionList.add(
                BleFunctionUiBean(
                    BLE_FUNCTION_FLAG_NOTIFY_EXERCISE_LEVEL
                )
            )
            bleFunctionList.add(
                BleFunctionUiBean(
                    BLE_FUNCTION_FLAG_STOP_NOTIFY_EXERCISE_LEVEL
                )
            )
        }
        functionListAdapter.setNewData(bleFunctionList)
        updateBleFunctionEnableState()
    }

    private fun updateBleFunctionEnableState() {
        val enabledFunctions = if (bluetoothDeviceManager?.isConnected() == true) {
            BleFunction.values().filter { isBleFunctionEnabled(it) }.toSet()
        } else {
            emptySet()
        }
        runOnUiThread {
            functionListAdapter.setEnabledFunctions(enabledFunctions)
        }
    }

    private fun isBleFunctionEnabled(bleFunctionFlag: BleFunction): Boolean {
        if (bluetoothDeviceManager?.isConnected() != true) {
            return false
        }
        if (START_TO_STOP_FUNCTION_MAP.containsKey(bleFunctionFlag)) {
            return !activeStartFunctionSet.contains(bleFunctionFlag)
        }
        val startFunction = STOP_TO_START_FUNCTION_MAP[bleFunctionFlag]
        if (startFunction != null) {
            return activeStartFunctionSet.contains(startFunction)
        }
        return true
    }

    private fun updateStartFunctionState(startFunction: BleFunction, isActive: Boolean) {
        if (isActive) {
            activeStartFunctionSet.add(startFunction)
        } else {
            activeStartFunctionSet.remove(startFunction)
        }
        updateBleFunctionEnableState()
    }

    private fun updateStopFunctionState(stopFunction: BleFunction, isActive: Boolean) {
        STOP_TO_START_FUNCTION_MAP[stopFunction]?.let { startFunction ->
            updateStartFunctionState(startFunction, isActive)
        }
    }

    private fun restoreStopFunctionStateAfterFailure(stopFunction: BleFunction) {
        updateStopFunctionState(
            stopFunction,
            bluetoothDeviceManager?.isConnected() == true && !userDisconnectRequested
        )
    }

    private fun markNotifyFunctionActive(notifyFunction: BleFunction) {
        if (!activeStartFunctionSet.contains(notifyFunction)) {
            updateStartFunctionState(notifyFunction, true)
        }
    }

    fun onDisconnect(@Suppress("UNUSED_PARAMETER") view: View) {
        userDisconnectRequested = true
        connectActionInProgress = false
        mainHandler.removeCallbacks(reconnectRunnable)
        activeStartFunctionSet.clear()
        updateBleFunctionEnableState()
        updateConnectActionState()
        bluetoothDeviceManager?.disConnect {
            deviceDisconnect()
        }
    }

    protected open fun showMsg(msg: String, needToast: Boolean = false) {
        BleLogUtil.d(TAG, msg)
        if (!needLog) {
            return
        }
        val realMsg = "->: ${simple.format(Date())} $msg\n"
        runOnUiThread {
            adapter.addItem(realMsg)
            scrollViewLogs?.scrollToPosition(adapter.itemCount - 1)
        }

        if (needToast) {
            showToast(msg)
        }
    }

    protected open fun deviceDisconnect() {
        connectActionInProgress = false
        runOnUiThread {
            btnConnectDevice?.text = getString(R.string.go_to_connect_device)
        }
        activeStartFunctionSet.clear()
        updateBleFunctionEnableState()
        updateConnectActionState()
        if (!userDisconnectRequested) {
            reconnect()
        }
    }

    protected open fun deviceConnect(mac: String) {
        connectActionInProgress = false
        runOnUiThread {
            btnConnectDevice?.text = mac
        }
        updateBleFunctionEnableState()
        updateConnectActionState()
    }

    private fun updateConnectActionState() {
        val isConnected = bluetoothDeviceManager?.isConnected() == true
        val isConnecting =
            !userDisconnectRequested && (connectActionInProgress || bluetoothDeviceManager?.isConnecting() == true)
        runOnUiThread {
            btnConnectDevice?.isEnabled = !isConnected && !isConnecting
            btnConnectDevice?.alpha = if (btnConnectDevice?.isEnabled == true) 1f else 0.45f
            btnDisconnectDevice?.isEnabled = isConnected || isConnecting
            btnDisconnectDevice?.alpha = if (btnDisconnectDevice?.isEnabled == true) 1f else 0.45f
            if (!isConnected) {
                btnConnectDevice?.text = if (isConnecting) {
                    getString(R.string.connecting)
                } else {
                    getString(R.string.go_to_connect_device)
                }
            }
        }
    }

    private fun showToast(msg: String) {
        runOnUiThread {
            Toast.makeText(this.applicationContext, msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun connectDevice() {
        if (bluetoothDeviceManager == null) {
            showMsg("设备管理器未初始化", true)
            updateConnectActionState()
            return
        }
        userDisconnectRequested = false
        mainHandler.removeCallbacks(reconnectRunnable)
        if (bluetoothDeviceManager?.isConnected() == true) {
            showMsg("已连接  $bluetoothDeviceManager")
            updateConnectActionState()
            return
        }

        if (bluetoothDeviceManager?.isConnecting() == true) {
            showMsg("正在连接中  $bluetoothDeviceManager")
            updateConnectActionState()
            return
        }
        connectActionInProgress = true
        updateConnectActionState()
        showMsg("开始寻找设备 ，准备连接 $bluetoothDeviceManager")
        bluetoothDeviceManager?.connectDevice(
            { mac ->
                connectActionInProgress = false
                showMsg("connect success $mac", true)
                updateConnectActionState()
            },
            { errorMsg ->
                connectActionInProgress = false
                showMsg("connect failed $errorMsg", true)
                updateConnectActionState()
            },
            connectionBleStrategy, mac = mac,
        )
    }

    override fun onClick(bleFunctionFlag: BleFunction) {
        if (!isBleFunctionEnabled(bleFunctionFlag)) {
            showToast("当前状态下不可执行该功能")
            return
        }
        when (bleFunctionFlag) {
            BLE_FUNCTION_FLAG_START_COLLECT_EXERCISE_DEGREE -> {
                updateStartFunctionState(bleFunctionFlag, true)
                (bluetoothDeviceManager as? ICollectExerciseDegreeDataFunction)?.startCollectExerciseDegreeData(Unit,
                    success = {
                        showToast("开始收集运动数据指令发送成功")
                    },
                    failure = { _, it ->
                        updateStartFunctionState(bleFunctionFlag, false)
                        showToast("开始收集运动数据指令发送失败：$it")
                    })
            }

            BleFunction.BLE_FUNCTION_FLAG_START_COLLECT_BRAIN -> {
                updateStartFunctionState(bleFunctionFlag, true)
                (bluetoothDeviceManager as? ICollectBrainDataFunction)?.startCollectBrainData(Unit,
                    success = {
                        showMsg("发送收集脑波数据成功 指令: ${it.contentToString()}")
                    },
                    failure = { _, it ->
                        updateStartFunctionState(bleFunctionFlag, false)
                        showMsg("发送收集脑波数据失败：$it", true)
                    })
            }

            BleFunction.BLE_FUNCTION_FLAG_STOP_COLLECT_BRAIN -> {
                updateStopFunctionState(bleFunctionFlag, false)
                (bluetoothDeviceManager as? ICollectBrainDataFunction)?.stopCollectBrainData(Unit,
                    success = {
                        showMsg("停止收集脑波数据成功", true)
                    },
                    failure = { _, it ->
                        restoreStopFunctionStateAfterFailure(bleFunctionFlag)
                        showMsg("停止收集脑波数据失败：$it", true)
                    })

            }


            BLE_FUNCTION_FLAG_STOP_COLLECT_EXERCISE_DEGREE -> {
                updateStopFunctionState(bleFunctionFlag, false)
                (bluetoothDeviceManager as? ICollectExerciseDegreeDataFunction)?.stopCollectExerciseDegreeData(Unit,
                    success = {
                        showToast("停止收集运动数据指令发送成功")
                    },
                    failure = { _, it ->
                        restoreStopFunctionStateAfterFailure(bleFunctionFlag)
                        showToast("停止收集运动数据指令发送失败：$it")
                    })
            }

            BLE_FUNCTION_FLAG_START_COLLECT_BRAIN_HR -> {
                updateStartFunctionState(bleFunctionFlag, true)
                (bluetoothDeviceManager as? ICollectBrainAndHrDataFunction)?.startCollectBrainAndHrData(Unit,
                    success = {
                        showMsg("发送收集脑波心率数据指令成功  指令 ${it.contentToString()}")
                    },
                    failure = { _, it ->
                        updateStartFunctionState(bleFunctionFlag, false)
                        showMsg("发送收集脑波心率数据指令失败：$it", true)
                    })
            }

            BLE_FUNCTION_FLAG_STOP_COLLECT_BRAIN_HR -> {
                updateStopFunctionState(bleFunctionFlag, false)
                (bluetoothDeviceManager as? ICollectBrainAndHrDataFunction)?.stopCollectBrainAndHrData(Unit,
                    success = {
                        showToast("发送停止收集脑波心率数据成功")
                    },
                    failure = { _, it ->
                        restoreStopFunctionStateAfterFailure(bleFunctionFlag)
                        showToast("发送停止收集脑波心率数据失败：$it")
                    })
            }

            BLE_FUNCTION_FLAG_READ_BATTERY -> {
                (bluetoothDeviceManager as IBatteryFunction<*>).readBatteryValue(success = {
                    if (it is Int) {
                        showToast("读取电量数据：$it")
                    }
                }, failure = {
                    showToast("读取电量数据失败：$it")
                })
            }

            BLE_FUNCTION_FLAG_NOTIFY_HR -> {
                updateStartFunctionState(bleFunctionFlag, true)
                (bluetoothDeviceManager as? IHrFunction<*>)?.notifyHeartRate(success = {
                    markNotifyFunctionActive(bleFunctionFlag)
                    showMsg("心率数据：${it.contentToString()}")
                    meditateDataHelper?.saveData("hr", it)
                }, failure = {
                    updateStartFunctionState(bleFunctionFlag, false)
                    showToast("订阅心率数据失败：$it")
                })
            }

            BLE_FUNCTION_FLAG_READ_HARDWARE -> {
                (bluetoothDeviceManager as? IInfoFunction)?.apply {
                    readDeviceHardware(success = {
                        showToast("读取Hardware数据：$it")
                    }, failure = {
                        showToast("读取Hardware数据失败：$it")
                    })
                } ?: run {
                    showToast("该设备 不支持 readDeviceHardware")
                }
            }

            BLE_FUNCTION_FLAG_READ_MAC -> {
                (bluetoothDeviceManager as? IInfoFunction)?.readDeviceMac(success = {
                    showToast("读取mac数据：$it")
                }, failure = {
                    showToast("读取mac失败：$it")
                })
            }

            BLE_FUNCTION_FLAG_READ_SERIAL -> {
                (bluetoothDeviceManager as? IInfoFunction)?.readDeviceSerial(success = {
                    showToast("读取Serial数据：$it")
                }, failure = {
                    showToast("读取Serial失败：$it")
                })
            }

            BLE_FUNCTION_FLAG_READ_MANUFACTURER -> {
                (bluetoothDeviceManager as? IInfoFunction)?.readDeviceManufacturer(success = {
                    showToast("读取Manufacturer数据：$it")
                }, failure = {
                    showToast("读取Manufacturer失败：$it")
                })
            }

            BLE_FUNCTION_FLAG_READ_FIRMWARE -> {
                (bluetoothDeviceManager as? IInfoFunction)?.readDeviceFirmware(success = {
                    showToast("读取Firmwar数据：$it")
                }, failure = {
                    showToast("读取Firmwar数据失败：$it")
                })
            }

            BLE_FUNCTION_FLAG_STOP_NOTIFY_HR -> {
                updateStopFunctionState(bleFunctionFlag, false)
                (bluetoothDeviceManager as? IHrFunction<*>)?.stopNotifyHeartRate({ showToast("取消订阅心率数据成功") },
                    { error ->
                        restoreStopFunctionStateAfterFailure(bleFunctionFlag)
                        showToast("取消订阅心率数据失败：$error")
                    })
            }

            BLE_FUNCTION_FLAG_NOTIFY_BRAIN_WAVE -> {
                updateStartFunctionState(bleFunctionFlag, true)
                (bluetoothDeviceManager as? IBrainWaveFunction)?.notifyBrainWave({ data ->
                    markNotifyFunctionActive(bleFunctionFlag)
                    showMsg("脑波数据：${data.contentToString()}")
                    meditateDataHelper?.saveData("brain_wave", data)
                }, { error ->
                    updateStartFunctionState(bleFunctionFlag, false)
                    showToast("脑波数据失败：$error")
                })
            }

            BLE_FUNCTION_FLAG_STOP_NOTIFY_BRAIN_WAVE -> {
                updateStopFunctionState(bleFunctionFlag, false)
                (bluetoothDeviceManager as? IBrainWaveFunction)?.stopNotifyBrainWave({ showToast("取消订阅脑波数据成功") },
                    { error ->
                        restoreStopFunctionStateAfterFailure(bleFunctionFlag)
                        showToast("取消订阅脑波数据失败：$error")
                    })
            }

            BleFunction.BLE_FUNCTION_FLAG_NOTIFY_BRAIN_CONTRACT -> {
                updateStartFunctionState(bleFunctionFlag, true)
                (bluetoothDeviceManager as? IBrainWaveFunction)?.notifyBrainWave({ data ->
                    markNotifyFunctionActive(bleFunctionFlag)
                    showMsg("脑波数据：${data.contentToString()}")
                    meditateDataHelper?.saveData("brain_wave", data)
                }, { error ->
                    updateStartFunctionState(bleFunctionFlag, false)
                    showToast("脑波数据失败：$error")
                })
                (bluetoothDeviceManager as? IContactFunction<*>)?.notifyContact({ data ->
                    markNotifyFunctionActive(bleFunctionFlag)
                    showMsg("佩戴状态数据：${data.contentToString()}")
                    meditateDataHelper?.saveData("contact", data)
                }, { error ->
                    updateStartFunctionState(bleFunctionFlag, false)
                    showToast("佩戴状态数据失败：$error")
                })
            }

            BleFunction.BLE_FUNCTION_FLAG_STOP_NOTIFY_BRAIN_CONTRACT -> {
                updateStopFunctionState(bleFunctionFlag, false)
                (bluetoothDeviceManager as? IContactFunction<*>)?.stopNotifyContact({ showToast("取消订阅佩戴状态数据成功") },
                    { error ->
                        restoreStopFunctionStateAfterFailure(bleFunctionFlag)
                        showToast("取消订阅佩戴状态数据失败：$error")
                    })
                (bluetoothDeviceManager as? IBrainWaveFunction)?.stopNotifyBrainWave({ showToast("取消订阅脑波数据成功") },
                    { error ->
                        restoreStopFunctionStateAfterFailure(bleFunctionFlag)
                        showToast("取消订阅脑波数据失败：$error")
                    })
            }

            BLE_FUNCTION_FLAG_NOTIFY_CONTACT -> {
                updateStartFunctionState(bleFunctionFlag, true)
                (bluetoothDeviceManager as? IContactFunction<*>)?.notifyContact({ data ->
                    markNotifyFunctionActive(bleFunctionFlag)
                    showMsg("佩戴状态数据：${data.contentToString()}")
                    meditateDataHelper?.saveData("contact", data)
                }, { error ->
                    updateStartFunctionState(bleFunctionFlag, false)
                    showToast("佩戴状态数据失败：$error")
                })
            }


            BLE_FUNCTION_FLAG_STOP_NOTIFY_CONTACT -> {
                updateStopFunctionState(bleFunctionFlag, false)
                (bluetoothDeviceManager as? IContactFunction<*>)?.stopNotifyContact({ showToast("取消订阅佩戴状态数据成功") },
                    { error ->
                        restoreStopFunctionStateAfterFailure(bleFunctionFlag)
                        showToast("取消订阅佩戴状态数据失败：$error")
                    })
            }

            BLE_FUNCTION_FLAG_NOTIFY_SLEEP_POSTURE -> {
                updateStartFunctionState(bleFunctionFlag, true)
                (bluetoothDeviceManager as? ISleepPostureFunction<*>)?.notifySleepPosture({ data ->
                    markNotifyFunctionActive(bleFunctionFlag)
                    showMsg("睡眠姿势数据：${data.contentToString()}")
                    meditateDataHelper?.saveData("SleepPosture", data)
                }, { error ->
                    updateStartFunctionState(bleFunctionFlag, false)
                    showToast("睡眠姿势数据失败：$error")
                })
            }

            BLE_FUNCTION_FLAG_STOP_NOTIFY_SLEEP_POSTURE -> {
                updateStopFunctionState(bleFunctionFlag, false)
                (bluetoothDeviceManager as? ISleepPostureFunction<*>)?.stopNotifySleepPosture({
                    showToast(
                        "取消订阅睡眠姿势数据成功"
                    )
                }, { error ->
                    restoreStopFunctionStateAfterFailure(bleFunctionFlag)
                    showToast("取消订阅睡眠姿势数据失败：$error")
                })
            }

            BLE_FUNCTION_FLAG_NOTIFY_EXERCISE_LEVEL -> {
                updateStartFunctionState(bleFunctionFlag, true)
                (bluetoothDeviceManager as? IExerciseLevelFunction<*>)?.notifyExerciseLevel({
                    markNotifyFunctionActive(bleFunctionFlag)
                    showMsg(
                        "运动等级 $it"
                    )
                    meditateDataHelper?.saveData("SleepPosture", it)
                }, { error ->
                    updateStartFunctionState(bleFunctionFlag, false)
                    showToast("订阅运动等级数据失败：$error")
                })
            }

            BLE_FUNCTION_FLAG_STOP_NOTIFY_EXERCISE_LEVEL -> {
                updateStopFunctionState(bleFunctionFlag, false)
                (bluetoothDeviceManager as? IExerciseLevelFunction<*>)?.stopNotifyExerciseLevel({
                    showToast(
                        "取消订阅运动等级数据成功"
                    )
                }, { error ->
                    restoreStopFunctionStateAfterFailure(bleFunctionFlag)
                    showToast("取消订阅运动等级数据失败：$error")
                })
            }

            BLE_FUNCTION_FLAG_NOTIFY_TEMPERATURE -> {
                updateStartFunctionState(bleFunctionFlag, true)
                (bluetoothDeviceManager as? ITemperatureFunction<*>)?.notifyTemperature({
                    markNotifyFunctionActive(bleFunctionFlag)
                    showMsg(
                        "温度数据 $it"
                    )
                    meditateDataHelper?.saveData("Temperature", it)
                }, { error ->
                    updateStartFunctionState(bleFunctionFlag, false)
                    showToast("订阅温度数据失败：$error")
                })
            }

            BLE_FUNCTION_FLAG_STOP_NOTIFY_TEMPERATURE -> {
                updateStopFunctionState(bleFunctionFlag, false)
                (bluetoothDeviceManager as? ITemperatureFunction<*>)?.stopNotifyTemperature({
                    showToast(
                        "取消订阅温度数据成功"
                    )
                }, { error ->
                    restoreStopFunctionStateAfterFailure(bleFunctionFlag)
                    showToast("取消订阅温度数据失败：$error")
                })
            }

            BLE_FUNCTION_FLAG_NOTIFY_BATTERY -> {
                updateStartFunctionState(bleFunctionFlag, true)
                (bluetoothDeviceManager as? IBatteryFunction<*>)?.notifyBattery({
                    markNotifyFunctionActive(bleFunctionFlag)
                    showMsg(
                        "电池数据 $it"
                    )
                    meditateDataHelper?.saveData("Battery", it)
                }, { error ->
                    updateStartFunctionState(bleFunctionFlag, false)
                    showToast("订阅电池数据失败：$error")
                })
            }

            BLE_FUNCTION_FLAG_STOP_NOTIFY_BATTERY -> {
                updateStopFunctionState(bleFunctionFlag, false)
                (bluetoothDeviceManager as? IBatteryFunction<*>)?.stopNotifyBattery({
                    showToast(
                        "取消订阅电池数据成功"
                    )
                }, { error ->
                    restoreStopFunctionStateAfterFailure(bleFunctionFlag)
                    showToast("取消订阅电池数据失败：$error")
                })
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnConnectDevice -> {
                mac = etConnectInfo?.text?.toString() ?: ""
                connectDevice()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothDeviceManager?.removeConnectListener(connectListener)
        bluetoothDeviceManager?.removeDisConnectListener(disconnectListener)
        functionListAdapter.bleFunctionClick = null
    }
}

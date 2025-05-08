package cn.entertech.flowtimeble

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.Spinner
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import cn.entertech.base.util.startActivity
import cn.entertech.ble.BaseBleConnectManager
import cn.entertech.ble.device.cushion.CushionManager
import cn.entertech.ble.device.headband.HeadbandManger
import cn.entertech.ble.device.tag.BrainTagExerciseLevelBean
import cn.entertech.ble.device.tag.BrainTagManager
import cn.entertech.ble.device.tag.BrainTagSleepPostureBean
import cn.entertech.ble.device.tag.BrainTemperatureBean
import cn.entertech.ble.function.IDeviceBatteryFunction
import cn.entertech.ble.function.IDeviceCommandUploadFunction
import cn.entertech.ble.function.IDeviceEegFunction
import cn.entertech.ble.function.IDeviceGyroFunction
import cn.entertech.ble.function.IDeviceHrFunction
import cn.entertech.ble.function.IDeviceTemperatureFunction
import cn.entertech.ble.function.collect.ICollectBrainAndHrDataFunction
import cn.entertech.ble.log.BleLogUtil
import cn.entertech.device.DeviceType
import cn.entertech.flowtimeble.data.FileListActivity
import cn.entertech.flowtimeble.device.BaseDeviceActivity
import cn.entertech.log.local.LogListActivity
import java.util.Date


class MainActivity : BaseDeviceActivity() {

    private var spinnerDeviceTypeList: Spinner? = null
    private var cbNeedReconnected: CheckBox? = null


    private var btnOpenLocalLog: Button? = null
    private var btnOpenLocalData: Button? = null
    private var btnDeviceInfo: Button? = null
    private lateinit var btnScanConnect: Button
    private val meditateDataHelper = MeditateDataHelper("brain_tag")
    private var lastReceiveDataTime = 0L


    private val receiveDataRunnable: Runnable by lazy {
        Runnable {
            lastReceiveDataTime = if (System.currentTimeMillis() - lastReceiveDataTime > 10000) {
                showMsg("超过10s没收到数据")
                0
            } else {
                System.currentTimeMillis()
            }
        }
    }
    private var rawListener = fun(bytes: ByteArray) {
        if (lastReceiveDataTime == 0L) {
            lastReceiveDataTime = System.currentTimeMillis()
            showMsg("收到数据了")
            mainHandler.postDelayed(receiveDataRunnable, 10000)
            return
        }
        mainHandler.removeCallbacks(receiveDataRunnable)
        mainHandler.postDelayed(receiveDataRunnable, 10000)
        meditateDataHelper.saveData(cn.entertech.ble.api.bean.MeditateDataType.SCEEG, bytes)
//        showMsg("braindata: " + HexDump.toHexString(bytes))
    }

    companion object {
        private const val TAG = "MainActivity"

        /**
         * 2s
         * */
        private const val RECONNECT_DELAY_TIME = 2000L
    }

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
        spinnerDeviceTypeList?.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
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
        btnDeviceInfo = findViewById(R.id.btnDeviceInfo)
        cbShowLog = findViewById(R.id.cbStopLog)
        btnOpenLocalLog = findViewById(R.id.btnOpenLocalLog)
        btnOpenLocalData = findViewById(R.id.btnOpenLocalData)
        btnOpenLocalLog?.setOnClickListener {
//            openFolderPicker()
            startActivity(LogListActivity::class.java, finishCurrent = false)
        }
        btnOpenLocalData?.setOnClickListener {
            startActivity(FileListActivity::class.java, finishCurrent = false)
        }
        btnClearLog?.setOnClickListener {
            adapter.setData(ArrayList())
        }
        btnDeviceInfo?.setOnClickListener {
            val bundle = Bundle()
            val file = App.getInstance().getExternalFilesDir("firmwareFile")
            BleLogUtil.d(TAG, "file path : ${file?.path}")
            if (file?.exists() == false) {
                file?.mkdirs()
            }
            val fileList = file?.listFiles()

            val firmwarePath: String = if (fileList.isNullOrEmpty()) {
                ""
            } else {
                fileList[0].path
            }
            BleLogUtil.d(TAG, "firmwarePath : $firmwarePath")
            bundle.putString("deviceType", currentDeviceType.typeName)
            if (firmwarePath.isNotEmpty()) {
                bundle.putString("newFirmware", "3.3.3")
                bundle.putString("firmwarePath", firmwarePath)
            }

//            startActivity(DeviceActivity::class.java, bundle, false)
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

    }

    private var currentDeviceType: DeviceType = DeviceType.DEVICE_TYPE_NONE
    private fun initBleManager(deviceType: DeviceType?) {
        currentDeviceType = deviceType ?: deviceTypes[0]
        bluetoothDeviceManager?.apply {
            removeDisConnectListener(disConnectedListener)
            removeConnectListener(connectedListener)
            removeRawDataListener(rawListener)
            disConnect()
        }
        showMsg("当前选择的设备类型： $currentDeviceType")
        bluetoothDeviceManager = when (deviceType) {
            DeviceType.DEVICE_TYPE_HEADBAND -> HeadbandManger(this)
            DeviceType.DEVICE_TYPE_BRAIN_TAG -> BrainTagManager(this)
            DeviceType.DEVICE_TYPE_CUSHION -> CushionManager(this)
            else -> null
        }
        bluetoothDeviceManager?.addConnectListener(connectedListener)
        bluetoothDeviceManager?.addDisConnectListener(disConnectedListener)
        bluetoothDeviceManager?.addRawDataListener(rawListener)
    }


    override fun showMsg(msg: String) {
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

    override fun deviceConnect(mac: String) {
        super.deviceConnect(mac)
        mainHandler.postDelayed({
            meditateDataHelper.close()
            meditateDataHelper.initHelper()
            startCollection(false)
            startContact()
            (bluetoothDeviceManager as? IDeviceTemperatureFunction<*>)?.apply {
                notifyTemperatureValue({
                    if (it is BrainTemperatureBean) {
                        meditateDataHelper.saveData(
                            cn.entertech.ble.api.bean.MeditateDataType.Temperature, it.raw
                        )
                    }
                }, {
                    showMsg("订阅温度数据失败：$it")
                })
            }
            (bluetoothDeviceManager as? IDeviceGyroFunction<*, *>)?.apply {
                notifySleepPostureValue({
                    if (it is BrainTagSleepPostureBean) {
                        meditateDataHelper.saveData(
                            cn.entertech.ble.api.bean.MeditateDataType.SleepPosture, it.rawData
                        )
                    }
                }, {
                    showMsg("订阅睡眠姿态数据失败：$it")
                })

                notifyExerciseLevelValue({
                    if (it is BrainTagExerciseLevelBean) {
                        meditateDataHelper.saveData(
                            cn.entertech.ble.api.bean.MeditateDataType.ExerciseLevel, it.rawData
                        )
                    }
                }, {
                    showMsg("订阅运动程度数据失败：$it")
                })
            }
        }, 1000)
        runOnUiThread {
            btnScanConnect.text = mac
            Toast.makeText(this@MainActivity, "connect to device success", Toast.LENGTH_SHORT)
                .show()
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

    var connectedListener = fun(string: String) {
        showMsg("connectedListener connect success:   $string")
        showToast("connect success")
    }
    var disConnectedListener = fun(string: String) {
        showMsg("disconnect:   $string")
        mainHandler.removeCallbacks(receiveDataRunnable)
        lastReceiveDataTime = 0L
        reconnect()
        runOnUiThread {
            btnScanConnect.setText(R.string.connect)
            Toast.makeText(this@MainActivity, "disconnect ", Toast.LENGTH_SHORT).show()
        }
    }

    private fun reconnect() {
        mainHandler.removeCallbacks(reconnectRunnable)
        mainHandler.postDelayed(reconnectRunnable, RECONNECT_DELAY_TIME)
    }


    override fun deviceDisconnect() {
        btnScanConnect.setText(R.string.connect)
    }

    private val onShutdown: Byte = 0x45

    fun onShutdown(@Suppress("UNUSED_PARAMETER") view: View) {
        (bluetoothDeviceManager as? IDeviceCommandUploadFunction)?.apply {
            writeCommandUpload(ByteArray(1) { onShutdown }, success = { byteArray ->
                showMsg("发送关机指令： ${byteArray.contentToString()}  成功")
            }, failure = {
                showMsg("发送关机指令失败： $it  ")
            })
        }
    }

    private fun startContact() {
        showMsg("开始佩戴检测 ")
        val currentBleManger = bluetoothDeviceManager
        if (currentBleManger is IDeviceEegFunction) {
            currentBleManger.notifyContact({
                val result = if (it.isEmpty()) {
                    0
                } else {
                    it.contentToString()
                }
                meditateDataHelper.saveData(cn.entertech.ble.api.bean.MeditateDataType.Contact, it)
                showMsg("佩戴检测值： $result")
            }) {
                showMsg("佩戴检测失败： $it")
            }
        } else {
            showMsg("不支持佩戴检测 ")
        }

    }

    private fun stopContact() {
        showMsg("停止佩戴检测")
        val currentBleManger = bluetoothDeviceManager
        if (currentBleManger is IDeviceEegFunction) {
            currentBleManger.stopNotifyContact()
        } else {
            showMsg("不支持佩戴检测 ")
        }
    }

    private fun startCollection(needStop: Boolean = true) {
        if (needStop) {
            stopCollection(success = {
                showMsg("开始收集数据")
                (bluetoothDeviceManager as? ICollectBrainAndHrDataFunction)?.apply {
                    mainHandler.postDelayed({
                        startCollectBrainAndHrData(Unit, success = {
                            showMsg("收集数据指令发送成功 ")
                        }, failure = { _, it ->
                            showMsg("收集数据指令发送失败 $it")
                        })

                    }, 500)

                } ?: kotlin.run {
                    showMsg("收集数据指令发送失败 bluetoothDeviceManager is not ICollectBrainAndHrDataFunction")
                }
            })
        } else {
            showMsg("开始收集数据")

            (bluetoothDeviceManager as? ICollectBrainAndHrDataFunction)?.apply {
                startCollectBrainAndHrData(Unit, success = {
                    showMsg("收集数据指令发送成功 ")
                    if (this is IDeviceHrFunction<*, *>) {
                        notifyHeartRate({
                            showMsg("notifyHeartRate ${it.contentToString()}")
                            meditateDataHelper.saveData(
                                cn.entertech.ble.api.bean.MeditateDataType.HEART_RATE, it
                            )
                        }) {
                            showMsg("订阅HR数据失败 $it")
                        }

                        notifyRr({
                            showMsg("notifyRr ${it.contentToString()}")
                            meditateDataHelper.saveData(
                                cn.entertech.ble.api.bean.MeditateDataType.RR, it
                            )
                        }) {
                            showMsg("订阅rr数据失败 $it")
                        }
                    }
                    if (this is IDeviceEegFunction) {
                        notifyBrainWave({
                            meditateDataHelper.saveData(
                                cn.entertech.ble.api.bean.MeditateDataType.SCEEG, it
                            )
                        }, {
                            showMsg("订阅脑波数据失败$it")
                        })
                    } else {
                        showMsg("设备 不支持 eeg 数据")
                    }
                    if (this is IDeviceTemperatureFunction<*>) {
                        notifyTemperatureValue({
                            if (it is BrainTemperatureBean) {
                                meditateDataHelper.saveData(
                                    cn.entertech.ble.api.bean.MeditateDataType.Temperature, it.raw
                                )
                                showMsg("Temperature: ${it.tem}")
                            } else {
                                showMsg("Temperature is error $it")
                            }
                        }, {
                            showMsg("订阅 Temperature 失败 $it")
                        })
                    }

                    showMsg("订阅睡眠姿势数据")
                    if (this is IDeviceGyroFunction<*, *>) {
                        notifySleepPosture({
                            meditateDataHelper.saveData(
                                cn.entertech.ble.api.bean.MeditateDataType.SleepPosture, it
                            )
                        }, {
                            showMsg("订阅睡眠姿势数据失败")
                        })
                    } else {
                        showMsg("设备 不支持 睡眠姿势 数据")
                    }
                    notifyExerciseLevelData(bluetoothDeviceManager)
                }, failure = { _, it ->
                    showMsg("收集数据指令发送失败 $it")
                })


            } ?: kotlin.run {
                showMsg("收集数据指令发送失败 bluetoothDeviceManager is not ICollectBrainAndHrDataFunction")
            }
        }
    }

    private fun notifyExerciseLevelData(bluetoothDeviceManager: BaseBleConnectManager?) {
        showMsg("订阅运动水平数据")
        (bluetoothDeviceManager as? IDeviceGyroFunction<*, *>)?.notifyExerciseLevel({
            meditateDataHelper.saveData(
                cn.entertech.ble.api.bean.MeditateDataType.ExerciseLevel, it
            )
        }, {
            showMsg("订阅运动水平数据失败")
        }) ?: kotlin.run {
            showMsg("设备 不支持 运动水平 数据")
        }
    }

    private fun stopCollection(success: () -> Unit = {}, failure: (String) -> Unit = {}) {
        showMsg("停止收集数据")
        (bluetoothDeviceManager as? ICollectBrainAndHrDataFunction)?.apply {
            stopCollectBrainAndHrData(Unit, success = {
                showMsg("停止数据指令发送成功 ")
                success()
            }, failure = { _, it ->
                showMsg("停止数据指令发送失败 $it")
                failure("停止数据指令发送失败 $it")
            })
        }
    }


    fun onBattery(@Suppress("UNUSED_PARAMETER") view: View) {
        val currentBleManger = bluetoothDeviceManager
        if (currentBleManger is IDeviceBatteryFunction<*>) {
            currentBleManger.readBatteryLevel({
                showMsg("当前电量： $it")
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "电量:$it", Toast.LENGTH_SHORT).show()
                }
            }, fun(error: String) {
                showMsg("读取电量失败： $error")
            })
        } else {
            showMsg("当前连接设备不支持读取电量")
        }

    }


    fun showLog(view: View) {
//        startActivity(Intent(this, ShowLogActivity::class.java))
    }


    override fun onDestroy() {
        bluetoothDeviceManager?.apply {
            removeDisConnectListener(disConnectedListener)
            removeConnectListener(connectedListener)
            removeRawDataListener(rawListener)
        }
        super.onDestroy()
    }

}

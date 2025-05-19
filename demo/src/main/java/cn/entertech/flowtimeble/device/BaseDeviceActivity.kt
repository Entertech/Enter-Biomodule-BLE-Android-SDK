package cn.entertech.flowtimeble.device

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import cn.entertech.ble.BaseBleConnectManager
import cn.entertech.ble.api.ConnectionBleStrategy
import cn.entertech.ble.function.IBatteryFunction
import cn.entertech.ble.function.IBrainWaveFunction
import cn.entertech.ble.function.IContactFunction
import cn.entertech.ble.function.IExerciseLevelFunction
import cn.entertech.ble.function.IHrFunction
import cn.entertech.ble.function.IInfoFunction
import cn.entertech.ble.function.ISleepPostureFunction
import cn.entertech.ble.function.ITemperatureFunction
import cn.entertech.ble.function.collect.ICollectBrainAndHrDataFunction
import cn.entertech.ble.function.collect.ICollectExerciseDegreeDataFunction
import cn.entertech.ble.log.BleLogUtil
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
import java.text.SimpleDateFormat

abstract class BaseDeviceActivity : AppCompatActivity(), IBleFunctionClick {

    companion object {
        private const val TAG = "BaseDeviceActivity"
    }

    protected var needLog = false
    protected val mainHandler by lazy {
        Handler(Looper.getMainLooper())
    }
    protected val adapter by lazy {
        LogAdapter()
    }
    protected val simple by lazy {
        SimpleDateFormat("yyyy/MM/dd  hh:mm:ss:SSS")
    }
    protected var cbShowLog: CheckBox? = null
    protected var bluetoothDeviceManager: BaseBleConnectManager? = null
    protected var scrollView_logs: RecyclerView? = null
    protected var btnClearLog: Button? = null

    @Volatile
    protected var needReConnected = false
    protected val reconnectRunnable: Runnable by lazy {
        Runnable {
            showMsg("reconnectRunnable needReConnected:   $needReConnected")
            if (needReConnected) {
                showMsg("start reconnect")
                connectDevice()
            }
        }
    }

    protected fun initBleFunction(): List<BleFunctionUiBean> {
        val bleFunctionList = ArrayList<BleFunctionUiBean>()
        if (bluetoothDeviceManager is IInfoFunction) {
            bleFunctionList.add(
                BleFunctionUiBean(
                    "读取固件信息", BLE_FUNCTION_FLAG_READ_FIRMWARE
                )
            )
            bleFunctionList.add(
                BleFunctionUiBean(
                    "读取软件信息", BLE_FUNCTION_FLAG_READ_HARDWARE
                )
            )
            bleFunctionList.add(
                BleFunctionUiBean(
                    "读取Mac地址", BLE_FUNCTION_FLAG_READ_MAC
                )
            )
            bleFunctionList.add(
                BleFunctionUiBean(
                    "读取Serial信息", BLE_FUNCTION_FLAG_READ_SERIAL
                )
            )
            bleFunctionList.add(
                BleFunctionUiBean(
                    "读取MANUFACTURER信息", BLE_FUNCTION_FLAG_READ_MANUFACTURER
                )
            )
        }
        if (bluetoothDeviceManager is ICollectBrainAndHrDataFunction) {
            bleFunctionList.add(
                BleFunctionUiBean(
                    "开始收集脑波心率数据", BLE_FUNCTION_FLAG_START_COLLECT_BRAIN_HR
                )
            )
            bleFunctionList.add(
                BleFunctionUiBean(
                    "停止收集脑波心率数据", BLE_FUNCTION_FLAG_STOP_COLLECT_BRAIN_HR
                )
            )
        }
        if (bluetoothDeviceManager is IHrFunction<*>) {
            bleFunctionList.add(
                BleFunctionUiBean(
                    "订阅心率数据", BLE_FUNCTION_FLAG_NOTIFY_HR
                )
            )
            bleFunctionList.add(
                BleFunctionUiBean(
                    "取消订阅心率数据", BLE_FUNCTION_FLAG_STOP_NOTIFY_HR
                )
            )
        }
        if (bluetoothDeviceManager is IBrainWaveFunction) {
            bleFunctionList.add(
                BleFunctionUiBean(
                    "订阅脑波数据", BLE_FUNCTION_FLAG_NOTIFY_BRAIN_WAVE
                )
            )
            bleFunctionList.add(
                BleFunctionUiBean(
                    "取消订阅脑波数据", BLE_FUNCTION_FLAG_STOP_NOTIFY_BRAIN_WAVE
                )
            )

        }
        if (bluetoothDeviceManager is IContactFunction) {
            bleFunctionList.add(
                BleFunctionUiBean(
                    "订阅佩戴数据", BLE_FUNCTION_FLAG_NOTIFY_CONTACT
                )
            )
            bleFunctionList.add(
                BleFunctionUiBean(
                    "取消订阅佩戴数据", BLE_FUNCTION_FLAG_STOP_NOTIFY_CONTACT
                )
            )
        }
        if (bluetoothDeviceManager is ITemperatureFunction<*>) {
            bleFunctionList.add(
                BleFunctionUiBean(
                    "订阅温度数据", BLE_FUNCTION_FLAG_NOTIFY_TEMPERATURE
                )
            )
            bleFunctionList.add(
                BleFunctionUiBean(
                    "取消订阅温度数据", BLE_FUNCTION_FLAG_STOP_NOTIFY_TEMPERATURE
                )
            )
        }
        if (bluetoothDeviceManager is IBatteryFunction<*>) {
            bleFunctionList.add(
                BleFunctionUiBean(
                    "订阅电量数据", BLE_FUNCTION_FLAG_NOTIFY_BATTERY
                )
            )
            bleFunctionList.add(
                BleFunctionUiBean(
                    "取消订阅电量数据", BLE_FUNCTION_FLAG_STOP_NOTIFY_BATTERY
                )
            )
            bleFunctionList.add(
                BleFunctionUiBean(
                    "读取电量数据", BLE_FUNCTION_FLAG_READ_BATTERY
                )
            )
        }
        if (bluetoothDeviceManager is ICollectExerciseDegreeDataFunction) {
            bleFunctionList.add(
                BleFunctionUiBean(
                    "开始收集运动数据", BLE_FUNCTION_FLAG_START_COLLECT_EXERCISE_DEGREE
                )
            )
            bleFunctionList.add(
                BleFunctionUiBean(
                    "停止收集运动数据", BLE_FUNCTION_FLAG_STOP_COLLECT_EXERCISE_DEGREE
                )
            )
        }
        if (bluetoothDeviceManager is ISleepPostureFunction<*>) {
            bleFunctionList.add(
                BleFunctionUiBean(
                    "订阅睡眠姿势数据", BLE_FUNCTION_FLAG_NOTIFY_SLEEP_POSTURE
                )
            )
            bleFunctionList.add(
                BleFunctionUiBean(
                    "取消订阅睡眠姿势数据", BLE_FUNCTION_FLAG_STOP_NOTIFY_SLEEP_POSTURE
                )
            )
        }
        if (bluetoothDeviceManager is IExerciseLevelFunction<*>) {
            bleFunctionList.add(
                BleFunctionUiBean(
                    "订阅运动水平数据", BLE_FUNCTION_FLAG_NOTIFY_EXERCISE_LEVEL
                )
            )
            bleFunctionList.add(
                BleFunctionUiBean(
                    "取消订阅运动水平数据", BLE_FUNCTION_FLAG_STOP_NOTIFY_EXERCISE_LEVEL
                )
            )
        }
        return bleFunctionList
    }

    fun onDisconnect(@Suppress("UNUSED_PARAMETER") view: View) {
        bluetoothDeviceManager?.disConnect {
            deviceDisconnect()
        }
    }

    protected open fun showMsg(msg: String) {
        Log.d(TAG, "msg: $msg")
    }

    protected open fun deviceDisconnect() {

    }

    protected open fun deviceConnect(mac: String) {

    }

    fun onGetState(@Suppress("UNUSED_PARAMETER") view: View) {
        BleLogUtil.d(
            TAG, "biomoduleBleManager.isConnected()： ${bluetoothDeviceManager?.isConnected()}"
        )
        Toast.makeText(
            this, if (bluetoothDeviceManager?.isConnected() == true) {
                "connected"
            } else {
                "disconnect"
            }, Toast.LENGTH_SHORT
        ).show()
    }

    fun showToast(msg: String) {
        runOnUiThread {
            Toast.makeText(this.applicationContext, msg, Toast.LENGTH_SHORT).show()
        }
    }

    fun onConnect(@Suppress("UNUSED_PARAMETER") view: View) {
        connectDevice()
    }

    protected fun connectDevice() {
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
            deviceConnect(mac)
        }, { msg ->
            showMsg("connect failed $msg")
            runOnUiThread {
                Toast.makeText(
                    this, "failed to connect to device：${msg}", Toast.LENGTH_SHORT
                ).show()
            }
        }, ConnectionBleStrategy.SCAN_AND_CONNECT_HIGH_SIGNAL)
    }

    override fun onClick(bleFunctionFlag: BleFunction) {
        when (bleFunctionFlag) {

            BLE_FUNCTION_FLAG_START_COLLECT_EXERCISE_DEGREE -> {
                (bluetoothDeviceManager as? ICollectExerciseDegreeDataFunction)?.startCollectExerciseDegreeData(Unit,
                    success = {
                        showToast("开始收集运动数据成功")
                    },
                    failure = { _, it ->
                        showToast("开始收集运动数据失败：$it")
                    })
            }

            BLE_FUNCTION_FLAG_STOP_COLLECT_EXERCISE_DEGREE -> {

            }

            BLE_FUNCTION_FLAG_START_COLLECT_BRAIN_HR -> {
                (bluetoothDeviceManager as? ICollectExerciseDegreeDataFunction)?.stopCollectExerciseDegreeData(Unit,
                    success = {
                        showToast("停止收集脑波心率数据成功")
                    },
                    failure = { _, it ->
                        showToast("开停止收集脑波心率数据失败：$it")
                    })
            }

            BLE_FUNCTION_FLAG_STOP_COLLECT_BRAIN_HR -> {
                (bluetoothDeviceManager as? ICollectBrainAndHrDataFunction)?.stopCollectBrainAndHrData(Unit,
                    success = {
                        showToast("停止收集脑波心率数据成功")
                    },
                    failure = { _, it ->
                        showToast("停止收集脑波心率数据失败：$it")
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
                (bluetoothDeviceManager as? IHrFunction<*>)?.notifyHRValue(success = {
                    showMsg("心率数据：$it")
                }, failure = {
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
                (bluetoothDeviceManager as? IHrFunction<*>)?.stopNotifyHeartRate({ showToast("取消订阅心率数据成功") },
                    { error -> showToast("取消订阅心率数据失败：$error") })
            }

            BLE_FUNCTION_FLAG_NOTIFY_BRAIN_WAVE -> {
                (bluetoothDeviceManager as? IBrainWaveFunction)?.notifyBrainWave({ data ->
                    showMsg("脑波数据：${data.contentToString()}")
                }, { error ->
                    showToast("脑波数据失败：$error")
                })
            }

            BLE_FUNCTION_FLAG_STOP_NOTIFY_BRAIN_WAVE -> {
                (bluetoothDeviceManager as? IBrainWaveFunction)?.stopNotifyBrainWave({ showToast("取消订阅脑波数据成功") },
                    { error -> showToast("取消订阅脑波数据失败：$error") })
            }

            BLE_FUNCTION_FLAG_NOTIFY_CONTACT -> {
                (bluetoothDeviceManager as? IContactFunction)?.notifyContact({ data ->
                    showMsg("佩戴状态数据：${data.contentToString()}")
                }, { error ->
                    showToast("佩戴状态数据失败：$error")
                })
            }

            BLE_FUNCTION_FLAG_STOP_NOTIFY_CONTACT -> {
                (bluetoothDeviceManager as? IContactFunction)?.stopNotifyContact({ showToast("取消订阅佩戴状态数据成功") },
                    { error -> showToast("取消订阅佩戴状态数据失败：$error") })
            }

            BLE_FUNCTION_FLAG_NOTIFY_SLEEP_POSTURE -> {
                (bluetoothDeviceManager as? ISleepPostureFunction<*>)?.notifySleepPosture({ data ->
                    showMsg("睡眠姿势数据：${data.contentToString()}")
                }, { error ->
                    showToast("睡眠姿势数据失败：$error")
                })
            }

            BLE_FUNCTION_FLAG_STOP_NOTIFY_SLEEP_POSTURE -> {
                (bluetoothDeviceManager as? ISleepPostureFunction<*>)?.stopNotifySleepPosture({
                    showToast(
                        "取消订阅睡眠姿势数据成功"
                    )
                }, { error -> showToast("取消订阅睡眠姿势数据失败：$error") })
            }

            BLE_FUNCTION_FLAG_NOTIFY_EXERCISE_LEVEL -> {
                (bluetoothDeviceManager as? IExerciseLevelFunction<*>)?.notifyExerciseLevelValue({
                    showToast(
                        "运动等级 $it"
                    )
                }, { error -> showToast("订阅运动等级数据失败：$error") })
            }

            BLE_FUNCTION_FLAG_STOP_NOTIFY_EXERCISE_LEVEL -> {
                (bluetoothDeviceManager as? ISleepPostureFunction<*>)?.stopNotifySleepPosture({
                    showToast(
                        "取消运动等级数据成功"
                    )
                }, { error -> showToast("取消订阅运动等级数据失败：$error") })
            }

            BLE_FUNCTION_FLAG_NOTIFY_TEMPERATURE -> {
                (bluetoothDeviceManager as? ITemperatureFunction<*>)?.notifyTemperatureValue({
                    showToast(
                        "温度数据 $it"
                    )
                }, { error -> showToast("订阅温度数据失败：$error") })
            }

            BLE_FUNCTION_FLAG_STOP_NOTIFY_TEMPERATURE -> {
                (bluetoothDeviceManager as? ITemperatureFunction<*>)?.stopNotifyTemperature({
                    showToast(
                        "取消运动等级数据成功"
                    )
                }, { error -> showToast("取消订阅运动等级数据失败：$error") })
            }

            BLE_FUNCTION_FLAG_NOTIFY_BATTERY -> {
                (bluetoothDeviceManager as? IBatteryFunction<*>)?.notifyBatteryValue({
                    showToast(
                        "电池数据 $it"
                    )
                }, { error -> showToast("订阅电池数据失败：$error") })
            }

            BLE_FUNCTION_FLAG_STOP_NOTIFY_BATTERY -> {
                (bluetoothDeviceManager as? IBatteryFunction<*>)?.stopNotifyBattery({
                    showToast(
                        "取消电池数据成功"
                    )
                }, { error -> showToast("取消订阅电池数据失败：$error") })
            }
        }
    }
}
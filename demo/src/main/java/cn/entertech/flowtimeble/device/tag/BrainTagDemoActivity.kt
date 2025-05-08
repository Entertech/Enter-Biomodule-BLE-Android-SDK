package cn.entertech.flowtimeble.device.tag

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.entertech.ble.device.tag.BrainTagManager
import cn.entertech.ble.device.tag.ITagHrFunction
import cn.entertech.ble.function.IBrainWaveFunction
import cn.entertech.ble.function.IContactFunction
import cn.entertech.ble.function.IDeviceBatteryFunction
import cn.entertech.ble.function.IExerciseLevelFunction
import cn.entertech.ble.function.IInfoFunction
import cn.entertech.ble.function.ISleepPostureFunction
import cn.entertech.ble.function.ITemperatureFunction
import cn.entertech.ble.function.collect.ICollectBrainAndHrDataFunction
import cn.entertech.ble.function.collect.ICollectExerciseDegreeDataFunction
import cn.entertech.flowtimeble.databinding.ActivityBrainTagDemoBinding
import cn.entertech.flowtimeble.device.BaseDeviceActivity
import cn.entertech.flowtimeble.device.tag.BleFunctionListAdapter.IBleFunctionClick
import cn.entertech.flowtimeble.device.tag.BleFunctionUiBean.Companion.BLE_FUNCTION_FLAG_NOTIFY_BATTERY
import cn.entertech.flowtimeble.device.tag.BleFunctionUiBean.Companion.BLE_FUNCTION_FLAG_NOTIFY_BRAIN_WAVE
import cn.entertech.flowtimeble.device.tag.BleFunctionUiBean.Companion.BLE_FUNCTION_FLAG_NOTIFY_CONTACT
import cn.entertech.flowtimeble.device.tag.BleFunctionUiBean.Companion.BLE_FUNCTION_FLAG_NOTIFY_EXERCISE_LEVEL
import cn.entertech.flowtimeble.device.tag.BleFunctionUiBean.Companion.BLE_FUNCTION_FLAG_NOTIFY_HR
import cn.entertech.flowtimeble.device.tag.BleFunctionUiBean.Companion.BLE_FUNCTION_FLAG_NOTIFY_SLEEP_POSTURE
import cn.entertech.flowtimeble.device.tag.BleFunctionUiBean.Companion.BLE_FUNCTION_FLAG_NOTIFY_TEMPERATURE
import cn.entertech.flowtimeble.device.tag.BleFunctionUiBean.Companion.BLE_FUNCTION_FLAG_READ_BATTERY
import cn.entertech.flowtimeble.device.tag.BleFunctionUiBean.Companion.BLE_FUNCTION_FLAG_READ_FIRMWARE
import cn.entertech.flowtimeble.device.tag.BleFunctionUiBean.Companion.BLE_FUNCTION_FLAG_READ_HARDWARE
import cn.entertech.flowtimeble.device.tag.BleFunctionUiBean.Companion.BLE_FUNCTION_FLAG_READ_MAC
import cn.entertech.flowtimeble.device.tag.BleFunctionUiBean.Companion.BLE_FUNCTION_FLAG_READ_MANUFACTURER
import cn.entertech.flowtimeble.device.tag.BleFunctionUiBean.Companion.BLE_FUNCTION_FLAG_READ_SERIAL
import cn.entertech.flowtimeble.device.tag.BleFunctionUiBean.Companion.BLE_FUNCTION_FLAG_START_COLLECT_BRAIN_HR
import cn.entertech.flowtimeble.device.tag.BleFunctionUiBean.Companion.BLE_FUNCTION_FLAG_START_COLLECT_EXERCISE_DEGREE
import cn.entertech.flowtimeble.device.tag.BleFunctionUiBean.Companion.BLE_FUNCTION_FLAG_STOP_COLLECT_BRAIN_HR
import cn.entertech.flowtimeble.device.tag.BleFunctionUiBean.Companion.BLE_FUNCTION_FLAG_STOP_COLLECT_EXERCISE_DEGREE
import cn.entertech.flowtimeble.device.tag.BleFunctionUiBean.Companion.BLE_FUNCTION_FLAG_STOP_NOTIFY_BATTERY
import cn.entertech.flowtimeble.device.tag.BleFunctionUiBean.Companion.BLE_FUNCTION_FLAG_STOP_NOTIFY_BRAIN_WAVE
import cn.entertech.flowtimeble.device.tag.BleFunctionUiBean.Companion.BLE_FUNCTION_FLAG_STOP_NOTIFY_CONTACT
import cn.entertech.flowtimeble.device.tag.BleFunctionUiBean.Companion.BLE_FUNCTION_FLAG_STOP_NOTIFY_EXERCISE_LEVEL
import cn.entertech.flowtimeble.device.tag.BleFunctionUiBean.Companion.BLE_FUNCTION_FLAG_STOP_NOTIFY_HR
import cn.entertech.flowtimeble.device.tag.BleFunctionUiBean.Companion.BLE_FUNCTION_FLAG_STOP_NOTIFY_SLEEP_POSTURE
import cn.entertech.flowtimeble.device.tag.BleFunctionUiBean.Companion.BLE_FUNCTION_FLAG_STOP_NOTIFY_TEMPERATURE

class BrainTagDemoActivity : BaseDeviceActivity(), IBleFunctionClick {

    private lateinit var binding: ActivityBrainTagDemoBinding
    private var rvBleFunction: RecyclerView? = null
    private val adapter by lazy {
        val adapter = BleFunctionListAdapter()
        adapter.bleFunctionClick = this
        adapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBrainTagDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        rvBleFunction = binding.rvBleFunction
        rvBleFunction?.layoutManager = GridLayoutManager(this, 2)
        rvBleFunction?.adapter = adapter
        bluetoothDeviceManager = BrainTagManager(this)
        initBleFunction()
    }


    private fun initBleFunction() {
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
        if (bluetoothDeviceManager is ITagHrFunction<*>) {
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
        if (bluetoothDeviceManager is IDeviceBatteryFunction<*>) {
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


        adapter.setNewData(bleFunctionList)
    }

    override fun onClick(bleFunctionFlag: Int) {
        when (bleFunctionFlag) {
            BLE_FUNCTION_FLAG_READ_BATTERY -> {
                (bluetoothDeviceManager as IDeviceBatteryFunction<*>).readBatteryValue(success = {
                    if (it is Int) {
                        showToast("读取电量数据：$it")
                    }
                }, failure = {
                    showToast("读取电量数据失败：$it")
                })
            }

            BLE_FUNCTION_FLAG_NOTIFY_HR -> {
                (bluetoothDeviceManager as? ITagHrFunction<*>)?.notifyHRValue(success = {

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
                (bluetoothDeviceManager as ITagHrFunction<*>).stopNotifyHeartRate({ showToast("取消订阅心率数据成功") },
                    { error -> showToast("取消订阅心率数据失败：$error") })
            }

            BLE_FUNCTION_FLAG_NOTIFY_BRAIN_WAVE -> {
                (bluetoothDeviceManager as IBrainWaveFunction).notifyBrainWave({ data ->
                    showToast("脑波数据：${data[0]}")
                }, { error ->
                    showToast("脑波数据失败：$error")
                })
            }

            BLE_FUNCTION_FLAG_STOP_NOTIFY_BRAIN_WAVE -> {
                (bluetoothDeviceManager as IBrainWaveFunction).stopNotifyBrainWave({ showToast("取消订阅脑波数据成功") },
                    { error -> showToast("取消订阅脑波数据失败：$error") })
            }

            BLE_FUNCTION_FLAG_NOTIFY_CONTACT -> {
                (bluetoothDeviceManager as IContactFunction).notifyContact({ data ->
                    showToast("佩戴状态数据：${data[0]}")
                }, { error ->
                    showToast("佩戴状态数据失败：$error")
                })
            }

            BLE_FUNCTION_FLAG_STOP_NOTIFY_CONTACT -> {
                (bluetoothDeviceManager as IContactFunction).stopNotifyContact({ showToast("取消订阅佩戴状态数据成功") },
                    { error -> showToast("取消订阅佩戴状态数据失败：$error") })
            }

            BLE_FUNCTION_FLAG_NOTIFY_SLEEP_POSTURE -> {
                (bluetoothDeviceManager as ISleepPostureFunction<*>).notifySleepPosture({ data ->
                    showToast("睡眠姿势数据：${data[0]}")
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
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.bleFunctionClick = null
    }
}
package cn.entertech.qa.hr

import cn.entertech.ble.BaseBleConnectManager
import cn.entertech.ble.function.collect.ICollectBrainAndHrDataFunction
import cn.entertech.flowtimeble.data.MeditateDataHelper
import cn.entertech.flowtimeble.device.BaseDeviceActivity
import cn.entertech.flowtimeble.device.BleFunction
import cn.entertech.flowtimeble.device.BleFunction.BLE_FUNCTION_FLAG_NOTIFY_HR
import cn.entertech.flowtimeble.device.BleFunction.BLE_FUNCTION_FLAG_START_COLLECT_BRAIN_HR
import cn.entertech.flowtimeble.device.BleFunction.BLE_FUNCTION_FLAG_STOP_COLLECT_BRAIN_HR
import cn.entertech.flowtimeble.device.BleFunction.BLE_FUNCTION_FLAG_STOP_NOTIFY_HR
import cn.entertech.flowtimeble.device.BleFunctionUiBean

class QaHrDemoActivity : BaseDeviceActivity() {

    override fun initBleManager(): BaseBleConnectManager {
        return QaHrManager(this)
    }

    override fun getDeviceTypeName(): String {
        return ""
    }

    override fun initMeditateDataHelper(): MeditateDataHelper {
        return MeditateDataHelper("qaHr")
    }

    override fun initBleFunctionList() {
        val bleFunctionList = ArrayList<BleFunctionUiBean>()
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
        if (bluetoothDeviceManager is IQaHrFunction) {
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
            bleFunctionList.add(
                BleFunctionUiBean(
                    BleFunction.BLE_FUNCTION_FLAG_NOTIFY_HR_RAW
                )
            )
            bleFunctionList.add(
                BleFunctionUiBean(
                    BleFunction.BLE_FUNCTION_FLAG_STOP_NOTIFY_HR_RAW
                )
            )
        }
        functionListAdapter.setNewData(bleFunctionList)
        updateBleFunctionEnableState()
    }

    override fun processFunction(bleFunctionFlag: BleFunction) {
        when (bleFunctionFlag) {
            BleFunction.BLE_FUNCTION_FLAG_NOTIFY_HR_RAW -> {
                updateStartFunctionState(bleFunctionFlag, true)
                (bluetoothDeviceManager as? IQaHrFunction)?.notifyHrRawData(success = { data ->
                    markNotifyFunctionActive(bleFunctionFlag)
                    showMsg("心率原始数据：${data.contentToString()}")
                }, failure = { error ->
                    updateStartFunctionState(bleFunctionFlag, false)
                    showMsg("收集心率原始数据失败：$error")
                })
            }

            BleFunction.BLE_FUNCTION_FLAG_STOP_NOTIFY_HR_RAW -> {
                updateStopFunctionState(bleFunctionFlag, false)
                (bluetoothDeviceManager as? IQaHrFunction)?.stopNotifyHrRawData(success = {
                    showMsg("结束收集心率原始数据")
                }, failure = { error ->
                    restoreStopFunctionStateAfterFailure(bleFunctionFlag)
                    showMsg("结束收集心率原始数据失败：$error")
                })
            }

            else -> {}
        }
    }

    override fun notifyHr() {
        updateStartFunctionState(BLE_FUNCTION_FLAG_NOTIFY_HR, true)
        (bluetoothDeviceManager as? IQaHrFunction)?.notifyHeartRate(success = {
            markNotifyFunctionActive(BLE_FUNCTION_FLAG_NOTIFY_HR)
            showMsg("心率数据：${it.contentToString()}")
            meditateDataHelper?.saveData("hr", it)
        }, failure = {
            updateStartFunctionState(BLE_FUNCTION_FLAG_NOTIFY_HR, false)
            showToast("订阅心率数据失败：$it")
        })
    }

    override fun stopNotifyHr() {
        updateStopFunctionState(BLE_FUNCTION_FLAG_STOP_NOTIFY_HR, false)
        (bluetoothDeviceManager as? IQaHrFunction)?.stopNotifyHeartRate(
            { showToast("取消订阅心率数据成功") },
            { error ->
                restoreStopFunctionStateAfterFailure(BLE_FUNCTION_FLAG_STOP_NOTIFY_HR)
                showToast("取消订阅心率数据失败：$error")
            })
    }
}
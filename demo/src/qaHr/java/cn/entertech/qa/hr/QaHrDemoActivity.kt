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

    private val qaHrCsvDataHelper = QaHrCsvDataHelper("qaHr")

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
        functionListAdapter.setNewData(bleFunctionList)
        updateBleFunctionEnableState()
    }

    private fun saveData(dataFileName: String, data: ByteArray) {
        val timestamp = System.currentTimeMillis()
        when (dataFileName) {
            HR_FILE_NAME -> {
                val value = parseUnsignedValue(data)
                saveCsvRecord(HR_FILE_NAME, timestamp, value)
            }

            HR_RAW_DATA_FILE_NAME -> {
                if (data.size == HR_RAW_DATA_PACKET_BYTES) {
                    parseRawDataAsUnsignedInt(data).forEach { value ->
                        saveCsvRecord(HR_RAW_DATA_FILE_NAME, timestamp, value)
                    }
                    return
                }
                saveCsvRecord(HR_RAW_DATA_FILE_NAME, timestamp, getRawDataAsDecimalBytes(data))
            }
        }
    }

    private fun saveCsvRecord(fileName: String, timestamp: Long, value: Long) {
        if (!qaHrCsvDataHelper.isActive()) {
            qaHrCsvDataHelper.startSession(timestamp)
        }
        qaHrCsvDataHelper.saveData(fileName, timestamp, value)
    }

    private fun saveCsvRecord(fileName: String, timestamp: Long, value: String) {
        if (!qaHrCsvDataHelper.isActive()) {
            qaHrCsvDataHelper.startSession(timestamp)
        }
        qaHrCsvDataHelper.saveData(fileName, timestamp, value)
    }

    private fun parseUnsignedValue(data: ByteArray): Long {
        var value = 0L
        for (byte in data) {
            value = (value shl 8) or (byte.toLong() and 0xFFL)
        }
        return value
    }

    private fun parseRawDataAsUnsignedInt(data: ByteArray): List<Long> {
        if (data.size != HR_RAW_DATA_PACKET_BYTES) {
            return emptyList()
        }

        val result = ArrayList<Long>()
        for (index in 0 until HR_RAW_DATA_POINT_COUNT) {
            val start = index * HR_RAW_DATA_POINT_BYTES
            val value =
                ((data[start].toLong() and 0xFFL) shl 24) or ((data[start + 1].toLong() and 0xFFL) shl 16) or ((data[start + 2].toLong() and 0xFFL) shl 8) or (data[start + 3].toLong() and 0xFFL)
            result.add(value)
        }
        return result
    }

    private fun getRawDataAsDecimalBytes(data: ByteArray): String {
        return data.joinToString(" ") {
            (it.toInt() and 0xFF).toString()
        }
    }

    private fun notifyHrRawData() {
        updateStartFunctionState(BleFunction.BLE_FUNCTION_FLAG_NOTIFY_HR_RAW, true)
        (bluetoothDeviceManager as? IQaHrFunction)?.notifyHrRawData(success = { data ->
            markNotifyFunctionActive(BleFunction.BLE_FUNCTION_FLAG_NOTIFY_HR_RAW)
            showMsg("心率原始数据：${data.contentToString()}")
            saveData(HR_RAW_DATA_FILE_NAME, data)
        }, failure = { error ->
            updateStartFunctionState(BleFunction.BLE_FUNCTION_FLAG_NOTIFY_HR_RAW, false)
            showMsg("收集心率原始数据失败：$error")
        })
    }

    private fun stopNotifyHrRawData() {
        updateStopFunctionState(BleFunction.BLE_FUNCTION_FLAG_STOP_NOTIFY_HR_RAW, false)
        (bluetoothDeviceManager as? IQaHrFunction)?.stopNotifyHrRawData(success = {
            showMsg("结束收集心率原始数据")
        }, failure = { error ->
            restoreStopFunctionStateAfterFailure(BleFunction.BLE_FUNCTION_FLAG_STOP_NOTIFY_HR_RAW)
            showMsg("结束收集心率原始数据失败：$error")
        })
    }

    override fun notifyHr(success: (ByteArray) -> Unit, failure: (String) -> Unit) {
        updateStartFunctionState(BLE_FUNCTION_FLAG_NOTIFY_HR, true)
        (bluetoothDeviceManager as? IQaHrFunction)?.notifyHeartRate(success = {
            markNotifyFunctionActive(BLE_FUNCTION_FLAG_NOTIFY_HR)
            showMsg("心率数据：${it.contentToString()}")
            saveData(HR_FILE_NAME, it)
        }, failure = {
            updateStartFunctionState(BLE_FUNCTION_FLAG_NOTIFY_HR, false)
            showMsg("订阅心率数据失败：$it")
        })
    }

    override fun stopNotifyHr(success: () -> Unit, failure: (String) -> Unit) {
        updateStopFunctionState(BLE_FUNCTION_FLAG_STOP_NOTIFY_HR, false)
        (bluetoothDeviceManager as? IQaHrFunction)?.stopNotifyHeartRate(
            { showMsg("取消订阅心率数据成功") },
            { error ->
                restoreStopFunctionStateAfterFailure(BLE_FUNCTION_FLAG_STOP_NOTIFY_HR)
                showMsg("取消订阅心率数据失败：$error")
            })
    }

    override fun stopCollectBrainAndHrData(
        success: (ByteArray) -> Unit, failure: (Int, String) -> Unit
    ) {
        super.stopCollectBrainAndHrData({ data ->
            stopNotifyHrRawData()
            stopNotifyHr()
            qaHrCsvDataHelper.endSession()
            success(data)
        }, failure)
    }


    override fun deviceDisconnect() {
        super.deviceDisconnect()
        qaHrCsvDataHelper.endSession()
    }

    override fun startCollectBrainAndHrData(
        success: (ByteArray) -> Unit, failure: (Int, String) -> Unit
    ) {
        super.startCollectBrainAndHrData({ data ->
            success(data)
            qaHrCsvDataHelper.startSession(System.currentTimeMillis())
            notifyHr()
            notifyHrRawData()
        }, failure)
    }

    override fun onDestroy() {
        qaHrCsvDataHelper.close()
        super.onDestroy()
    }

    companion object {
        private const val HR_FILE_NAME = "hr"
        private const val HR_RAW_DATA_FILE_NAME = "hr_rawdata"
        private const val HR_RAW_DATA_POINT_BYTES = 4
        private const val HR_RAW_DATA_POINT_COUNT = 5
        private const val HR_RAW_DATA_PACKET_BYTES =
            HR_RAW_DATA_POINT_BYTES * HR_RAW_DATA_POINT_COUNT
    }
}

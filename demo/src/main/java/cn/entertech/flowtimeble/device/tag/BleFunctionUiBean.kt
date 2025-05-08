package cn.entertech.flowtimeble.device.tag

data class BleFunctionUiBean(val functionName: String, val functionFlag: Int, val uiType: Int = 0) {
    companion object {
        const val BLE_FUNCTION_FLAG_NOTIFY_HR = 1
        const val BLE_FUNCTION_FLAG_STOP_NOTIFY_HR = 100
        const val BLE_FUNCTION_FLAG_NOTIFY_BRAIN_WAVE = 2
        const val BLE_FUNCTION_FLAG_STOP_NOTIFY_BRAIN_WAVE = 200
        const val BLE_FUNCTION_FLAG_NOTIFY_CONTACT = 3
        const val BLE_FUNCTION_FLAG_STOP_NOTIFY_CONTACT = 300
        const val BLE_FUNCTION_FLAG_NOTIFY_SLEEP_POSTURE = 4
        const val BLE_FUNCTION_FLAG_STOP_NOTIFY_SLEEP_POSTURE = 400
        const val BLE_FUNCTION_FLAG_NOTIFY_EXERCISE_LEVEL = 5
        const val BLE_FUNCTION_FLAG_STOP_NOTIFY_EXERCISE_LEVEL = 500
        const val BLE_FUNCTION_FLAG_NOTIFY_TEMPERATURE = 6
        const val BLE_FUNCTION_FLAG_STOP_NOTIFY_TEMPERATURE = 600
        const val BLE_FUNCTION_FLAG_NOTIFY_BATTERY = 7
        const val BLE_FUNCTION_FLAG_STOP_NOTIFY_BATTERY = 700
        const val BLE_FUNCTION_FLAG_READ_BATTERY = 7000
        const val BLE_FUNCTION_FLAG_READ_FIRMWARE = 8
        const val BLE_FUNCTION_FLAG_READ_HARDWARE = 9
        const val BLE_FUNCTION_FLAG_READ_MAC = 10
        const val BLE_FUNCTION_FLAG_READ_SERIAL = 11
        const val BLE_FUNCTION_FLAG_READ_MANUFACTURER = 12
        const val BLE_FUNCTION_FLAG_START_COLLECT_BRAIN_HR = 13
        const val BLE_FUNCTION_FLAG_STOP_COLLECT_BRAIN_HR = 14
        const val BLE_FUNCTION_FLAG_START_COLLECT_EXERCISE_DEGREE = 15
        const val BLE_FUNCTION_FLAG_STOP_COLLECT_EXERCISE_DEGREE = 16
    }
}
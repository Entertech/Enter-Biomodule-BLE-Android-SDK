package cn.entertech.flowtimeble.device

import cn.entertech.flowtimeble.R


enum class BleFunction(val desStringResId: Int = -1) {
    BLE_FUNCTION_FLAG_NOTIFY_HR(R.string.notify_hr_data), BLE_FUNCTION_FLAG_STOP_NOTIFY_HR(R.string.stop_notify_hr_data), BLE_FUNCTION_FLAG_NOTIFY_BRAIN_WAVE(
        R.string.notify_brain_wave_data
    ),
    BLE_FUNCTION_FLAG_STOP_NOTIFY_BRAIN_WAVE(R.string.stop_notify_brain_wave_data), BLE_FUNCTION_FLAG_NOTIFY_CONTACT(
        R.string.notify_wear_data
    ),
    BLE_FUNCTION_FLAG_STOP_NOTIFY_CONTACT(R.string.stop_notify_wear_data), BLE_FUNCTION_FLAG_NOTIFY_SLEEP_POSTURE(
        R.string.notify_sleep_data
    ),
    BLE_FUNCTION_FLAG_STOP_NOTIFY_SLEEP_POSTURE(R.string.stop_notify_sleep_data), BLE_FUNCTION_FLAG_NOTIFY_EXERCISE_LEVEL(
        R.string.notify_exercise_level_data
    ),
    BLE_FUNCTION_FLAG_STOP_NOTIFY_EXERCISE_LEVEL(R.string.stop_notify_exercise_level_data), BLE_FUNCTION_FLAG_NOTIFY_TEMPERATURE(
        R.string.notify_temperature_data
    ),
    BLE_FUNCTION_FLAG_STOP_NOTIFY_TEMPERATURE(R.string.stop_notify_temperature_data), BLE_FUNCTION_FLAG_NOTIFY_BATTERY(
        R.string.notify_battery_data
    ),
    BLE_FUNCTION_FLAG_STOP_NOTIFY_BATTERY(R.string.stop_notify_battery_data), BLE_FUNCTION_FLAG_READ_BATTERY(
        R.string.read_battery_data
    ),
    BLE_FUNCTION_FLAG_READ_FIRMWARE(R.string.read_firmware_data), BLE_FUNCTION_FLAG_READ_HARDWARE(R.string.read_hardware_data), BLE_FUNCTION_FLAG_READ_MAC(
        R.string.read_mac_data
    ),
    BLE_FUNCTION_FLAG_READ_SERIAL(R.string.read_serial_data), BLE_FUNCTION_FLAG_READ_MANUFACTURER(
        R.string.read_manufacturer_data
    ),
    BLE_FUNCTION_FLAG_START_COLLECT_BRAIN_HR(R.string.start_collect_brain_and_hr_data), BLE_FUNCTION_FLAG_STOP_COLLECT_BRAIN_HR(
        R.string.stop_collect_brain_and_hr_data
    ),
    BLE_FUNCTION_FLAG_START_COLLECT_EXERCISE_DEGREE(R.string.stop_collect_exercise_degree_data), BLE_FUNCTION_FLAG_STOP_COLLECT_EXERCISE_DEGREE(
        R.string.stop_collect_exercise_degree_data
    ),
    BLE_FUNCTION_FLAG_START_COLLECT_BRAIN(R.string.start_collect_brain_data), BLE_FUNCTION_FLAG_STOP_COLLECT_BRAIN(
        R.string.stop_collect_brain_data
    ),
    BLE_FUNCTION_FLAG_NOTIFY_BRAIN_CONTRACT(R.string.notify_brain_contract_data), BLE_FUNCTION_FLAG_STOP_NOTIFY_BRAIN_CONTRACT(
        R.string.stop_notify_brain_contract_data
    )
}
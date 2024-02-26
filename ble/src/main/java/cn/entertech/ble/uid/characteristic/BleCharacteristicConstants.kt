package cn.entertech.ble.uid.characteristic

object BleCharacteristicConstants {

    /**
     * 设备名称（不可改变）
     * */
    const val BLE_CHARACTERISTIC_UUID_SERIAL_NUMBER_STRING =
        "BLE_UUID_SERIAL_NUMBER_STRING_CHARACTERISTIC"

    /**
     * MAC 6字节从机设备MAC 地址
     * */
    const val BLE_UUID_MODEL_NUMBER_STRING_CHAR = "BLE_UUID_MODEL_NUMBER_STRING_CHAR"

    /**
     * 固件版本
     * */
    const val BLE_CHARACTERISTIC_UUID_FIRMWARE_REVISION_STRING =
        "BLE_UUID_FIRMWARE_REVISION_STRING_CHARACTERISTIC"

    /**
     * 硬件版本
     * */
    const val BLE_CHARACTERISTIC_UUID_HARDWARE_REVISION_STRING =
        "BLE_UUID_HARDWARE_REVISION_STRING_CHARACTERISTIC"

    /**
     * 制造商
     * */
    const val BLE_CHARACTERISTIC_UUID_MANUFACTURER_NAME_STRING =
        "BLE_UUID_MANUFACTURER_NAME_STRING_CHARACTERISTIC"

    /**
     * 心率数据
     * */
    const val BLE_CHARACTERISTIC_UUID_HRS_DATA = "BLE_UUID_HRS_DATA_CHARACTERISTIC"


    /**
     * dfu 控制指令
     * */
    const val BLE_CHARACTERISTIC_UUID_DFU_CTRL_PT = "BLE_DFU_CTRL_PT_UUID"

    /**
     * dfu数据包
     * */
    const val BLE_CHARACTERISTIC_UUID_DFU_PKT_CHAR = "BLE_DFU_PKT_CHAR_UUID"

    /**
     * 电池电量
     * */
    const val BLE_CHARACTERISTIC_UUID_BATTERY_LEVEL = "BLE_UUID_BATTERY_LEVEL_CHARACTERISTIC"

    /**
     * 指令下行
     * */
    const val BLE_CHARACTERISTIC_UUID_COMMAND_UPLOAD = "BLE_UUID_COMMAND_UPLOAD_CHARACTERISTIC"

    /**
     * 指令上行
     * */
    const val BLE_CHARACTERISTIC_UUID_COMMAND_DOWNLOAD = "BLE_UUID_COMMAND_DOWNLOAD_CHARACTERISTIC"

    /**
     * 脑电数据
     * */
    const val BLE_CHARACTERISTIC_UUID_EEG_DATA = "BLE_UUID_EEG_DATA_CHARACTERISTIC"

    /**
     * 电极佩戴脱落检测
     * */
    const val BLE_CHARACTERISTIC_UUID_CONTACT_DATA = "BLE_UUID_EEG_DATA_CHARACTERISTIC"

}
package cn.entertech.ble.uid.device

import cn.entertech.ble.uid.service.BleServiceConstants
import cn.entertech.ble.uid.service.BluetoothService

/**
 * @param uuid 广播uuid 用于扫描搜索
 * */
class DeviceUuidBean(val uuid: String) {
    private val map: MutableMap<String, BluetoothService> = HashMap()

    fun addService(serviceName: String, service: BluetoothService) {
        map[serviceName] = service
    }

    fun getDeviceInfoService(): BluetoothService? =
        map[BleServiceConstants.BLE_SERVICE_UUID_DEVICE_INFORMATION]

}

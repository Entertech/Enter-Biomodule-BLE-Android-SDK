package cn.entertech.ble.uid.device.headband

import cn.entertech.ble.uid.BleUUIDConstants
import cn.entertech.ble.uid.characteristic.BleCharacteristicConstants
import cn.entertech.ble.uid.characteristic.BluetoothCharacteristic
import cn.entertech.ble.uid.device.BaseBleDeviceUidManage
import cn.entertech.ble.uid.device.DeviceUuidBean
import cn.entertech.ble.uid.property.BluetoothProperty
import cn.entertech.ble.uid.service.BleServiceConstants
import cn.entertech.ble.uid.service.BluetoothService

/**
 * 头环
 * */
object HeadbandUidManage : BaseBleDeviceUidManage() {


    override fun initDeviceUuidBean() {
        deviceUuidBean = DeviceUuidBean(BleUUIDConstants.UUID_0000FF10_1212_ABCD_1523_785FEABCD123)
        val deviceInfoService =
            BluetoothService(BleUUIDConstants.UUID_0000180A_0000_1000_8000_00805F9B34FB)
        deviceInfoService.apply {
            addCharacteristic(
                BleCharacteristicConstants.BLE_CHARACTERISTIC_UUID_SERIAL_NUMBER_STRING,
                BluetoothCharacteristic(
                    BleUUIDConstants.UUID_00002A24_0000_1000_8000_00805F9B34FB, listOf(
                        BluetoothProperty.BLUETOOTH_PROPERTY_READ
                    )
                )
            )

            addCharacteristic(
                BleCharacteristicConstants.BLE_UUID_MODEL_NUMBER_STRING_CHAR,
                BluetoothCharacteristic(
                    BleUUIDConstants.UUID_00002A25_0000_1000_8000_00805F9B34FB,
                    listOf(
                        BluetoothProperty.BLUETOOTH_PROPERTY_READ
                    )
                )
            )
            addCharacteristic(
                BleCharacteristicConstants.BLE_CHARACTERISTIC_UUID_FIRMWARE_REVISION_STRING,
                BleUUIDConstants.UUID_00002A26_0000_1000_8000_00805F9B34FB,
                listOf(
                    BluetoothProperty.BLUETOOTH_PROPERTY_READ
                )
            )

            addCharacteristic(
                BleCharacteristicConstants.BLE_CHARACTERISTIC_UUID_HARDWARE_REVISION_STRING,
                BleUUIDConstants.UUID_00002A27_0000_1000_8000_00805F9B34FB,
                listOf(
                    BluetoothProperty.BLUETOOTH_PROPERTY_READ
                )
            )

            addCharacteristic(
                BleCharacteristicConstants.BLE_CHARACTERISTIC_UUID_MANUFACTURER_NAME_STRING,
                BleUUIDConstants.UUID_00002A29_0000_1000_8000_00805F9B34FB,
                listOf(
                    BluetoothProperty.BLUETOOTH_PROPERTY_READ
                )
            )
        }
        val hrsService =
            BluetoothService(BleUUIDConstants.UUID_0000FF50_1212_ABCD_1523_785FEABCD123)
        hrsService.addCharacteristic(
            BleCharacteristicConstants.BLE_CHARACTERISTIC_UUID_HRS_DATA,
            BleUUIDConstants.UUID_0000FF51_1212_ABCD_1523_785FEABCD123,
            listOf(
                BluetoothProperty.BLUETOOTH_PROPERTY_NOTIFY
            )
        )

        val dufService =
            BluetoothService(BleUUIDConstants.UUID_0000FF40_1212_ABCD_1523_785FEABCD123)
        dufService.apply {
            addCharacteristic(
                BleCharacteristicConstants.BLE_CHARACTERISTIC_UUID_DFU_CTRL_PT,
                BleUUIDConstants.UUID_0000FF41_1212_ABCD_1523_785FEABCD123,
                listOf(
                    BluetoothProperty.BLUETOOTH_PROPERTY_NOTIFY,
                    BluetoothProperty.BLUETOOTH_PROPERTY_WRITE
                )
            )

            addCharacteristic(
                BleCharacteristicConstants.BLE_CHARACTERISTIC_UUID_DFU_PKT_CHAR,
                BleUUIDConstants.UUID_0000FF42_1212_ABCD_1523_785FEABCD123,
                listOf(
                    BluetoothProperty.BLUETOOTH_PROPERTY_WRITE_WITHOUT_RESPONSE,
                )
            )
        }

        val batteryService =
            BluetoothService(BleUUIDConstants.UUID_0000180F_0000_1000_8000_00805F9B34FB)
        batteryService.addCharacteristic(
            BleCharacteristicConstants.BLE_CHARACTERISTIC_UUID_BATTERY_LEVEL,
            BleUUIDConstants.UUID_00002A19_0000_1000_8000_00805F9B34FB,
            listOf(
                BluetoothProperty.BLUETOOTH_PROPERTY_NOTIFY,
                BluetoothProperty.BLUETOOTH_PROPERTY_READ
            )
        )
        val commandService =
            BluetoothService(BleUUIDConstants.UUID_0000FF20_1212_ABCD_1523_785FEABCD123)
        commandService.addCharacteristic(
            BleCharacteristicConstants.BLE_CHARACTERISTIC_UUID_COMMAND_UPLOAD,
            BleUUIDConstants.UUID_0000FF21_1212_ABCD_1523_785FEABCD123,
            listOf(BluetoothProperty.BLUETOOTH_PROPERTY_WRITE)

        )
        commandService.addCharacteristic(
            BleCharacteristicConstants.BLE_CHARACTERISTIC_UUID_COMMAND_DOWNLOAD,
            BleUUIDConstants.UUID_0000FF22_1212_ABCD_1523_785FEABCD123,
            listOf(BluetoothProperty.BLUETOOTH_PROPERTY_NOTIFY)
        )
        val eegService =
            BluetoothService(BleUUIDConstants.UUID_0000FF30_1212_ABCD_1523_785FEABCD123)
        eegService.addCharacteristic(
            BleCharacteristicConstants.BLE_CHARACTERISTIC_UUID_EEG_DATA,
            BleUUIDConstants.UUID_0000FF31_1212_ABCD_1523_785FEABCD123,
            listOf(BluetoothProperty.BLUETOOTH_PROPERTY_NOTIFY)
        )

        eegService.addCharacteristic(
            BleCharacteristicConstants.BLE_CHARACTERISTIC_UUID_CONTACT_DATA,
            BleUUIDConstants.UUID_0000FF32_1212_ABCD_1523_785FEABCD123,
            listOf(
                BluetoothProperty.BLUETOOTH_PROPERTY_NOTIFY,
                BluetoothProperty.BLUETOOTH_PROPERTY_READ,
            )
        )

        deviceUuidBean?.addService(
            BleServiceConstants.BLE_SERVICE_UUID_DEVICE_INFORMATION,
            deviceInfoService
        )
        deviceUuidBean?.addService(BleServiceConstants.BLE_SERVICE_UUID_HRS, hrsService)
        deviceUuidBean?.addService(BleServiceConstants.BLE_SERVICE_UUID_DFU, dufService)
        deviceUuidBean?.addService(BleServiceConstants.BLE_SERVICE_UUID_BATTERY, batteryService)
        deviceUuidBean?.addService(BleServiceConstants.BLE_SERVICE_UUID_COMMAND, commandService)
        deviceUuidBean?.addService(BleServiceConstants.BLE_SERVICE_UUID_EEG, eegService)
    }

    override fun getBroadcastUUid(): String? {
        return deviceUuidBean?.uuid
    }

    override fun getDeviceManufacturerUuid(): String {
        return "00002A29-0000-1000-8000-00805F9B34FB"
//        deviceUuidBean?.
    }

    override fun getCharacteristicCommandDownload(): String {
        return "0000FF21-1212-abcd-1523-785feabcd123"
    }

    override fun getCharacteristicBatteryLevelUUid(): String {
        return "00002A19-0000-1000-8000-00805F9B34FB"
    }

    override fun getCharacteristicHrUUid(): String {
        return "0000FF51-1212-abcd-1523-785feabcd123"
    }

    override fun getCharacteristicEEGUUid(): String {
        return "0000FF31-1212-abcd-1523-785feabcd123"
    }

    override fun getCharacteristicDeviceSerialUUid(): String {
        return "00002A25-0000-1000-8000-00805F9B34FB"
    }

    override fun getCharacteristicDeviceFirmwareUUid(): String {
        return "00002A26-0000-1000-8000-00805F9B34FB"
    }

    override fun getCharacteristicDeviceHardwareUUid(): String {
        return "00002A27-0000-1000-8000-00805F9B34FB"
    }

    override fun getCharacteristicDeviceMacUUid(): String {
        return "00002A24-0000-1000-8000-00805F9B34FB"
    }

    override fun getCharacteristicContactDateMacUUid(): String {
        return "0000FF32-1212-abcd-1523-785feabcd123"
    }
}
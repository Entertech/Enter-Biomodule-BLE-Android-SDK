package cn.entertech.flowtimeble.skin

import cn.entertech.ble.base.IBluetoothDeviceFunction
import cn.entertech.ble.utils.notify
import cn.entertech.ble.utils.stopNotify

interface ISkinFunction : IBluetoothDeviceFunction {

    fun getSkinService(): ISkinService? =
        getBleConnectManager()?.getBaseBluetoothDeviceUuidFactory() as? ISkinService

    fun notifySkinRate(
        success: ((ByteArray) -> Unit) = {},
        failure: ((String) -> Unit)? = null
    ) {
        notify(getSkinService()?.getSkinCharacteristicOrNull(), success, failure)
    }

    fun stopNotifySkinRate() {
        stopNotify(getSkinService()?.getSkinCharacteristicOrNull())
    }
}
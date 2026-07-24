package cn.entertech.qa.hr

import cn.entertech.ble.function.IBluetoothDeviceFunction
import cn.entertech.ble.function.IHrFunction
import cn.entertech.ble.utils.notify
import cn.entertech.ble.utils.stopNotify


interface IQaHrFunction : IHrFunction<QaHrBean>, IHrRawFunction, IBluetoothDeviceFunction {

    fun getHrService(): IQaHrService? =
        getBleConnectManager()?.getBaseBluetoothDeviceUuidFactory() as? IQaHrService

    override fun notifyHeartRate(
        success: ((ByteArray) -> Unit), failure: (String) -> Unit
    ) {
        notify(getHrService()?.getHrCharacteristicOrNull(), success, failure)
    }

    override fun stopNotifyHeartRate(success: () -> Unit, failure: (String) -> Unit) {
        stopNotify(getHrService()?.getHrCharacteristicOrNull(), success, failure)
    }


    override fun notifyHrRawData(
        success: (ByteArray) -> Unit,
        failure: (String) -> Unit
    ) {
        notify(getHrService()?.getHrRawCharacteristicOrNull(), success, failure)
    }

    override fun stopNotifyHrRawData(
        success: () -> Unit,
        failure: (String) -> Unit
    ) {
        stopNotify(getHrService()?.getHrRawCharacteristicOrNull(), success, failure)
    }

}
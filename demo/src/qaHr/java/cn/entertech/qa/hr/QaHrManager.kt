package cn.entertech.qa.hr

import android.content.Context
import cn.entertech.ble.BaseBleConnectManager
import cn.entertech.ble.BaseBluetoothDeviceUuidFactory
import cn.entertech.ble.device.headband.Command
import cn.entertech.ble.function.IDeviceCommandUploadFunction
import cn.entertech.ble.function.collect.ICollectBrainAndHrDataFunction
import cn.entertech.ble.utils.CharUtil

class QaHrManager(context: Context) : BaseBleConnectManager(context),
    IQaHrFunction, ICollectBrainAndHrDataFunction, IDeviceCommandUploadFunction {


    override fun getBaseBluetoothDeviceUuidFactory(): BaseBluetoothDeviceUuidFactory {
        return QaHrUuidFactory
    }
    override fun <MeditateType> getStartCollectBrainAndHrDataCommand(type: MeditateType): ByteArray {
        return Command.START_HEART_AND_BRAIN_COLLECT.value
    }

    override fun <MeditateType> getStopCollectBrainAndHrDataCommand(type: MeditateType): ByteArray {
        return Command.STOP_HEART_AND_BRAIN_COLLECT.value
    }

    @Deprecated("")
    override fun getBleConnectManager(): BaseBleConnectManager {
        return this
    }

    override fun getDefaultCastHrValue(): (ByteArray) -> QaHrBean = { bytes ->
        val hrByte = if (bytes.isNotEmpty()) {
            bytes[0]
        } else {
            0
        }
        QaHrBean(bytes, CharUtil.converUnchart(hrByte))
    }


}
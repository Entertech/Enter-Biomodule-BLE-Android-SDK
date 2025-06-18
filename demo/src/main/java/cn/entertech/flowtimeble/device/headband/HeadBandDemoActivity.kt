package cn.entertech.flowtimeble.device.headband

import cn.entertech.ble.BaseBleConnectManager
import cn.entertech.ble.device.headband.HeadbandManger
import cn.entertech.flowtimeble.R
import cn.entertech.flowtimeble.data.MeditateDataHelper
import cn.entertech.flowtimeble.device.BaseDeviceActivity

class HeadBandDemoActivity : BaseDeviceActivity() {

    override fun initBleManager(): BaseBleConnectManager {
        return HeadbandManger(this)
    }

    override fun getDeviceTypeName(): String {
        return getString(R.string.device_type_headband)
    }

    override fun initMeditateDataHelper(): MeditateDataHelper {
        return MeditateDataHelper("headband")
    }
}
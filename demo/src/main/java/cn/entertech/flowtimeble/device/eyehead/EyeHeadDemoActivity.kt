package cn.entertech.flowtimeble.device.eyehead

import cn.entertech.ble.BaseBleConnectManager
import cn.entertech.ble.device.eyehead.EyeHeadManager
import cn.entertech.flowtimeble.R
import cn.entertech.flowtimeble.data.MeditateDataHelper
import cn.entertech.flowtimeble.device.BaseDeviceActivity

class EyeHeadDemoActivity : BaseDeviceActivity() {

    override fun initBleManager(): BaseBleConnectManager {
        return EyeHeadManager(this)
    }

    override fun getDeviceTypeName(): String {
        return getString(R.string.device_type_eyehead)
    }

    override fun initMeditateDataHelper(): MeditateDataHelper {
        return MeditateDataHelper("eye_head")
    }
}
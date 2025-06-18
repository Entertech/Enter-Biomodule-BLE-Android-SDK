package cn.entertech.flowtimeble.device.tag

import cn.entertech.ble.BaseBleConnectManager
import cn.entertech.ble.device.tag.BrainTagManager
import cn.entertech.flowtimeble.R
import cn.entertech.flowtimeble.data.MeditateDataHelper
import cn.entertech.flowtimeble.device.BaseDeviceActivity

class BrainTagDemoActivity : BaseDeviceActivity() {

    companion object {
        private const val TAG = "BrainTagDemoActivity"
    }

    override fun initBleManager(): BaseBleConnectManager {
        return BrainTagManager(this)
    }

    override fun getDeviceTypeName(): String {
        return getString(R.string.device_type_tag)
    }


    override fun initMeditateDataHelper(): MeditateDataHelper {
        return MeditateDataHelper("brain_tag")
    }
}
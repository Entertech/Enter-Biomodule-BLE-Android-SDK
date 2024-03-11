package cn.entertech.ble.fix

/**
 * [128,0,0,128,0,0,128,0,0,128,0,0,128,0,0,128,0,0]
 * [255, 255, 254, 255, 255, 254, 255, 255, 254, 255, 255, 254, 255, 255, 254, 255, 255, 254]
 * */
object Firmware128FixHelper:BaseFirmwareFixStrategy() {

    private const val TAG = "Firmware128FixHelper"

    private val targetSub by lazy {
        listOf(128, 0, 0, 128, 0, 0, 128, 0, 0, 128, 0, 0, 128, 0, 0, 128, 0, 0)
    }

    override fun getTargetSubInt()=targetSub



}
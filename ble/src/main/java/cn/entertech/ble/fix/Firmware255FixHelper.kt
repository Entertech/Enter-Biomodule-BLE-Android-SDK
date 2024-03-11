package cn.entertech.ble.fix

/**
 * [128,0,0,128,0,0,128,0,0,128,0,0,128,0,0,128,0,0]
 * [255, 255, 254, 255, 255, 254, 255, 255, 254, 255, 255, 254, 255, 255, 254, 255, 255, 254]
 * */
object Firmware255FixHelper:BaseFirmwareFixStrategy() {

    private const val TAG = "Firmware255FixHelper"

    private val targetSubInt by lazy {
        listOf(255, 255, 254, 255, 255, 254, 255, 255, 254, 255, 255, 254, 255, 255, 254, 255, 255, 254)
    }

    override fun getTargetSubInt()=targetSubInt



}
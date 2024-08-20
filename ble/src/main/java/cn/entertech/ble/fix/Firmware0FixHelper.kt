package cn.entertech.ble.fix

/**
 * [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]
 * */
object Firmware0FixHelper:BaseFirmwareFixStrategy() {

    private const val TAG = "Firmware255FixHelper"

    private val targetSub by lazy {
        listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
    }

    override fun getTargetSubInt()=targetSub



}
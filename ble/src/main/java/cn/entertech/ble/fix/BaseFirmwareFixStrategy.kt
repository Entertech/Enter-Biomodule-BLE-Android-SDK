package cn.entertech.ble.fix

abstract class BaseFirmwareFixStrategy {

    private var fixTriggerCallback: IFixTriggerCallback? = null

    companion object {
        @JvmStatic
        protected val RECORD_COUNT_TARGET = 500
    }

    private var recordCount = 0

    private var index = 0

    private val compareArray by lazy {
        getTargetSubInt().map {
            it.toByte()
        }
    }
    private val compareArraySize by lazy {
        compareArray.size
    }

    @Synchronized
    fun fixFirmware(byte: Byte) {
        if (byte == compareArray[index]) {
            if (index == compareArraySize - 1) {
                //说明已经是相同的数据
                ++recordCount
                ++index
                if (recordCount == RECORD_COUNT_TARGET) {
                    //发送指令，修复脑波与心率
                    resetFlag()
                    fixTriggerCallback?.fixTrigger()
                    return
                }
            } else {
                ++index
            }
            index %= compareArraySize
        } else {
            //不相同，重置
            resetFlag()
        }
    }

    private fun resetFlag() {
        index = 0
        recordCount = 0
    }

    fun startFix(callback: IFixTriggerCallback?) {
        resetFlag()
        fixTriggerCallback = callback
    }

    protected abstract fun getTargetSubInt(): List<Int>


    fun stopFix() {
        fixTriggerCallback = null
    }
}
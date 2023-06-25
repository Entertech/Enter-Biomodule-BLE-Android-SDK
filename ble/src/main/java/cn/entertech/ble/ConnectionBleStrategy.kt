package cn.entertech.ble

/**
 * 连接蓝牙设备的策略:
 *
 * 先扫描后连接设备方式 [SCAN_AND_CONNECT_HIGH_SIGNAL]
 *
 * 连接已配对设备方式 [CONNECT_BONDED]
 * */
enum class ConnectionBleStrategy(val flag:Int) {
    /**连接设备方式-扫描 高信号 设备*/
    SCAN_AND_CONNECT_HIGH_SIGNAL(0),
    /**
     * 连接已配对的设备
     * */
    CONNECT_BONDED(1);


    private val strategyMap: HashMap<Int, ConnectionBleStrategy> by lazy {
        val map = HashMap<Int, ConnectionBleStrategy>()
        values().forEach {
            map[it.flag] = it
        }
        map
    }

    fun getConnectionBleStrategy(flag: Int): ConnectionBleStrategy = strategyMap[flag]?:this


}
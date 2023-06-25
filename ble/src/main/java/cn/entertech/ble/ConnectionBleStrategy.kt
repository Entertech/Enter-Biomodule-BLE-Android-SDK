package cn.entertech.ble

/**
 * 连接蓝牙设备的策略:
 *
 * 先扫描后连接设备方式 [SCAN_AND_CONNECT_HIGH_SIGNAL]
 *
 * 连接已配对设备方式 [CONNECT_BONDED]
 * */
enum class ConnectionBleStrategy {
    /**连接设备方式-扫描 高信号 设备*/
    SCAN_AND_CONNECT_HIGH_SIGNAL,
    /**
     * 连接已配对的设备
     * */
    CONNECT_BONDED
}
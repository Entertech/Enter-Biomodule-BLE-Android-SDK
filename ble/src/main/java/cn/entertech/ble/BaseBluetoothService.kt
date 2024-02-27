package cn.entertech.ble

/**
 * 蓝牙服务
 * */
abstract class BaseBluetoothService {
    private val disConnectListeners = mutableListOf<(String) -> Unit>()
    private val connectListeners = mutableListOf<(String) -> Unit>()


    /**
     * add device disconnect listener
     */
    fun addDisConnectListener(listener: (String) -> Unit) {
        disConnectListeners.add(listener)
    }

    /**
     * remove device disconnect listener
     */
    fun removeDisConnectListener(listener: (String) -> Unit) {
        disConnectListeners.remove(listener)
    }

    /**
     * add device connect listener
     */
    fun addConnectListener(listener: (String) -> Unit) {
        connectListeners.add(listener)
    }

    /**
     * remove device connect listener
     */
    fun removeConnectListener(listener: (String) -> Unit) {
        connectListeners.remove(listener)
    }


    /**
     * 连接蓝牙设备
     * @param strategy 连接策略
     * @param mac 只有当CONNECT_DEVICE_MAC mac才有用
     * */
    abstract fun connectBleDevice(
        strategy: ConnectionBleStrategy,
        mac: String,
        connectSuccess: (String) -> Unit,
        connectFailure: () -> Unit
    )

    /**
     * 停止连接设备
     * */
    abstract fun stopConnectBleDevice()

    abstract fun disConnect(disConnectSuccess: () -> Unit, disConnectFailure: () -> Unit)
    abstract fun isConnecting(): Boolean
    abstract fun isConnected(): Boolean

    abstract fun command(
        bytes: ByteArray, success: ((ByteArray) -> Unit),
        failure: ((String) -> Unit)
    )

    abstract fun command(
        string: String, success: ((String) -> Unit),
        failure: ((String) -> Unit)
    )
}
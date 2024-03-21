package cn.entertech.ble

class BluetoothConnectBuilder {
    var connectSuccess: ((String) -> Unit)? = null
        private set
    var connectFailure: ((String) -> Unit)? = null
        private set
    var connectionBleStrategy: ConnectionBleStrategy =
        ConnectionBleStrategy.SCAN_AND_CONNECT_HIGH_SIGNAL
        private set
    var filter: (String?, String?) -> Boolean = { _, _ -> true }
        private set

    var mac: String = ""
        private set
    var scanTimeout: Long = 0

    fun setConnectSuccessListener(connectSuccess: ((String) -> Unit)?): BluetoothConnectBuilder {
        this.connectSuccess = connectSuccess
        return this
    }

    fun setConnectFailureListener(connectFailure: ((String) -> Unit)?): BluetoothConnectBuilder {
        this.connectFailure = connectFailure
        return this
    }

    fun setConnectionBleStrategy(connectionBleStrategy: ConnectionBleStrategy): BluetoothConnectBuilder {
        this.connectionBleStrategy = connectionBleStrategy
        return this
    }

    fun setDeviceMac(mac: String): BluetoothConnectBuilder {
        this.mac = mac
        return this
    }

    fun setConnectionBleStrategy(connectionBleStrategyFlag: Int): BluetoothConnectBuilder {
        this.connectionBleStrategy =
            ConnectionBleStrategy.getConnectionBleStrategy(connectionBleStrategyFlag)
        return this
    }

    fun setFilterLogic(filter: (String?, String?) -> Boolean): BluetoothConnectBuilder {
        this.filter = filter
        return this
    }

    fun setScanTimeOut(time: Long): BluetoothConnectBuilder {
        this.scanTimeout = time
        return this
    }
}
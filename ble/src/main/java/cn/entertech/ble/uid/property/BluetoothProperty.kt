package cn.entertech.ble.uid.property

/**
 * 蓝牙特征属性
 * */
enum class BluetoothProperty {
    /**
     * 读属性，具有这个属性的特性是可读的，也就是说这个属性允许手机来读取一些信息。手机可以发送指令来读取某个具有读属性UUID的信息。
     * */
    BLUETOOTH_PROPERTY_READ,

    /**
     * 通知属性， 具有这个属性的特性是可以发送通知的，也就是说具有这个属性的特性（Characteristic）可以主动发送信息给手机。
     * */
    BLUETOOTH_PROPERTY_NOTIFY,

    /**
     * 写属性， 具有这个属性的特性是可以接收写入数据的。通常手机发送数据给蓝模块就是通过这个属性完成的。这个属性在Write 完成后，
     * 会发送写入完成结果的反馈给手机，然后手机再可以写入下一包或处理后续业务，这个属性在写入一包数据后，需要等待应用层返回写入结果，速度比较慢。
     * */
    BLUETOOTH_PROPERTY_WRITE,

    /**
     * 写属性，从字面意思上看，只是写，不需要返回写的结果，这个属性的特点是不需要应用层返回，完全依靠协议层完成，速度快，但是写入速度超过协议处理速度的时候，会丢包。
     * */
    BLUETOOTH_PROPERTY_WRITE_WITHOUT_RESPONSE,
}
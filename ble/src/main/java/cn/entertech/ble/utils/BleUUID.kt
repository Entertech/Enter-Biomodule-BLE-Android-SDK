package cn.entertech.ble.utils

/**
 * Created by EnterTech on 2017/10/27.
 */
/**
 * 广播
 * */
enum class NapBleDevice(val uuid: String) {
    NAPTIME("0000ff15-1212-abcd-1523-785feabcd123")
}


enum class NapBleCharacter(val uuid: String) {

    CMD_DOWNLOAD       ("0000FF21-1212-abcd-1523-785feabcd123"),
    BATTERY_LEVEL      ("00002A19-0000-1000-8000-00805F9B34FB"),
    SLEEP_POSTURE      ("0000ff41-1212-abcd-1523-785feabcd123"),
    EXERCISE_LEVEL      ("0000ff42-1212-abcd-1523-785feabcd123"),
    TEMPERATURE      ("0000ff61-1212-abcd-1523-785feabcd123"),
    EEG_DATA           ("0000FF31-1212-abcd-1523-785feabcd123"),
    CONTACT_DATE       ("0000FF32-1212-abcd-1523-785feabcd123"),
    DEVICE_MAC         ("00002A24-0000-1000-8000-00805F9B34FB"),
    DEVICE_SERIAL      ("00002A25-0000-1000-8000-00805F9B34FB"),
    DEVICE_FIRMWARE    ("00002A26-0000-1000-8000-00805F9B34FB"),
    DEVICE_HARDWARE    ("00002A27-0000-1000-8000-00805F9B34FB"),
    DEVICE_MANUFACTURER("00002A29-0000-1000-8000-00805F9B34FB"),
    HEART_RATE         ("0000FF51-1212-abcd-1523-785feabcd123"),
}
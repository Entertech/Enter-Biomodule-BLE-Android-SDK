package cn.entertech.ble.util

import java.util.*

/**
 * Created by EnterTech on 2017/10/27.
 */

enum class NapBleDevice(val uuid: String) {
    NAPTIME("0000FF10-1212-abcd-1523-785feabcd123")
}

enum class NapBleService(val uuid: String) {
    CONNECT    ("0000FF10-1212-abcd-1523-785feabcd123"),
    COMMAND    ("0000FF20-1212-abcd-1523-785feabcd123"),
    BATTERY    ("0000180F-0000-1000-8000-00805F9B34FB"),
    EEG        ("0000FF30-1212-abcd-1523-785feabcd123"),
    DFU        ("0000FF40-1212-abcd-1523-785feabcd123"),
    DEVICE_INFO("0000180A-0000-1000-8000-00805F9B34FB"),
}

enum class NapBleCharacter(val uuid: String) {
    CNT_SHAKE_ID       ("0000FF11-1212-abcd-1523-785feabcd123"),
    CNT_SHAKE_CMD      ("0000FF12-1212-abcd-1523-785feabcd123"),
    CNT_SHAKE_STATE    ("0000FF13-1212-abcd-1523-785feabcd123"),
    CMD_DOWNLOAD       ("0000FF21-1212-abcd-1523-785feabcd123"),
    CMD_UPLOAD         ("0000FF22-1212-abcd-1523-785feabcd123"),
    BATTERY_LEVEL      ("00002A19-0000-1000-8000-00805F9B34FB"),
    EEG_DATA           ("0000FF31-1212-abcd-1523-785feabcd123"),
    CONTACT_DATE       ("0000FF32-1212-abcd-1523-785feabcd123"),
    DFU_CTRL           ("0000FF41-1212-abcd-1523-785feabcd123"),
    DFU_PKT            ("0000FF42-1212-abcd-1523-785feabcd123"),
    DEVICE_MAC         ("00002A24-0000-1000-8000-00805F9B34FB"),
    DEVICE_SERIAL      ("00002A25-0000-1000-8000-00805F9B34FB"),
    DEVICE_FIRMWARE    ("00002A26-0000-1000-8000-00805F9B34FB"),
    DEVICE_HARDWARE    ("00002A27-0000-1000-8000-00805F9B34FB"),
    DEVICE_MANUFACTURER("00002A29-0000-1000-8000-00805F9B34FB"),
    HEART_RATE         ("0000FF51-1212-abcd-1523-785feabcd123"),
}
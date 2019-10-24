package cn.entertech.bleuisdk.ui

import android.content.Context
import android.graphics.Color
import cn.entertech.ble.multiple.MultipleBiomoduleBleManager
import java.util.concurrent.CopyOnWriteArrayList

class DeviceUIConfig(var context: Context) {
    var mainColor: Int = Color.parseColor("#0064ff")
    var firmwareUpdatePath: String? = null
    var firmwareNewVersion: String? = null
    var firmwareOldVersion: String? = null
    var isFirmwareUpdate: Boolean = false
    var managers = CopyOnWriteArrayList<MultipleBiomoduleBleManager>()
    var isDeviceBind: Boolean = false
    var isMultipleDevice: Boolean = false
    var deviceCount: Int = 1
    var isInited: Boolean = false

    companion object {
        @Volatile
        var mInstance: DeviceUIConfig? = null

        fun getInstance(context: Context): DeviceUIConfig {
            if (mInstance == null) {
                synchronized(DeviceUIConfig::class.java) {
                    if (mInstance == null) {
                        mInstance = DeviceUIConfig(context)
                    }
                }
            }
            return mInstance!!
        }
    }

    fun init(isDeviceBind: Boolean, isMultipleDevice: Boolean, deviceCount: Int = 1) {
        this.isDeviceBind = isDeviceBind
        this.isMultipleDevice = isMultipleDevice
        if (isMultipleDevice) {
            this.deviceCount = deviceCount
        } else {
            this.deviceCount = 1
        }
        this.isInited = true
        initDeviceManager()
    }

    fun initDeviceManager() {
        managers.clear()
        for (i in 0 until deviceCount) {
            managers.add(MultipleBiomoduleBleManager(context))
        }
    }

    fun updateFirmware(isUpdate: Boolean, oldVersion: String, newVersion: String, path: String) {
        this.isFirmwareUpdate = isUpdate
        this.firmwareOldVersion = oldVersion
        this.firmwareNewVersion = newVersion
        this.firmwareUpdatePath = path
    }
}
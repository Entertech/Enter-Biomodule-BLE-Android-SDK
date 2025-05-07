package cn.entertech.flowtimeble

import android.app.Application
import cn.entertech.ble.log.BleLogUtil
import cn.entertech.log.api.EnterTechLogUtils
import cn.entertech.log.local.StorageLogPrinter
import java.io.File
import java.text.SimpleDateFormat


class App : Application() {

    companion object {
        var application: Application? = null
        fun getInstance(): Application {
            return application!!
        }

        private const val TAG = "App"
    }

    /**
     *
     */
    val logFile by lazy {
        val current = System.currentTimeMillis()
        val currentTime = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss")
        val formatTime = currentTime.format(current)
        val logFile = File(filesDir.absolutePath + "/log/" + formatTime + "log")
        if (!logFile.exists()) {
            logFile.mkdirs()
        }
        logFile
    }

    override fun onCreate() {
        super.onCreate()
        application = this
        val maxBytesSize = 50 * 1024 * 1024
        CrashHandlerOld.init(this)
        EnterTechLogUtils.printer = StorageLogPrinter(this)
        BleLogUtil.print = EnterTechLogUtils
    }
}
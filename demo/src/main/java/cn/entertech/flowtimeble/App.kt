package cn.entertech.flowtimeble

import android.app.Application
import java.io.File
import java.text.SimpleDateFormat


class App:Application() {

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
        val logFile=File(filesDir.absolutePath +"/log/"+formatTime+"log")
        if(!logFile.exists()){
            logFile.mkdirs()
        }
        logFile
    }

    override fun onCreate() {
        super.onCreate()
        application = this
        val maxBytesSize = 50 *1024*1024
        CrashHandlerOld.init(this)
    }
}
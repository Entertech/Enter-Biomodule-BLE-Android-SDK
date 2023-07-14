package cn.entertech.flowtimeble

import android.app.Application
import com.orhanobut.logger.DiskLogAdapter
import com.orhanobut.logger.Logger


class App:Application() {
    override fun onCreate() {
        super.onCreate()
        val maxBytesSize = 50 *1024*1024
        Logger.addLogAdapter(DiskLogAdapter(cacheDir, maxBytesSize))
        CrashHandlerOld.init(this)
    }
}
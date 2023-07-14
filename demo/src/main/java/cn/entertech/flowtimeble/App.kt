package cn.entertech.flowtimeble

import android.app.Application
import com.orhanobut.logger.DiskLogAdapter
import com.orhanobut.logger.Logger
import java.io.File

class App:Application() {
    override fun onCreate() {
        super.onCreate()
        val maxBytesSize = 5000
        Logger.addLogAdapter(DiskLogAdapter(cacheDir, maxBytesSize))
    }
}
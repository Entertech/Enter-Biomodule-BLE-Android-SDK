package cn.entertech.flowtimeble

import android.content.Context
import cn.entertech.ble.utils.BleLogUtil
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.util.*


/**
 * 崩溃日志处理类
 * @author JPlus
 * @date 2019/3/14.
 */

object CrashHandlerOld : Thread.UncaughtExceptionHandler {

    private var mMaxNum = 0
    private var mSysInfo = "暂无信息"
    private var mListener: CrashListener? = null
    private var mDirPath: String? = null
    private var mDefaultCrashHandler: Thread.UncaughtExceptionHandler? = null
    /**
     * 初始化
     * @param context 上下文
     * @param maxNum 最大保存文件数量，默认为1
     * @param dir 存储文件的目录，默认为应用私有文件夹下crash目录
     * @param listener 发生崩溃时走的回调
     */
    fun init(context: Context, maxNum: Int = 1, dir: String = context.writePrivateDir("crash").absolutePath, listener: CrashListener?=null) {
        mDirPath = dir
        BleLogUtil.d("","dir: $dir")
        mMaxNum = maxNum
        mListener = listener
        mSysInfo = getSysInfo(context)
        mDefaultCrashHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    /**
     * 获取所有崩溃日志
     * @return 日志文件列表
     */
    fun getAllFiles(): Array<File>? {
        return File(mDirPath).listFiles()
    }

    /**
     * 获取最新崩溃日志
     * @return 最新文件
     */
    fun getNewFile(): File? {
        //筛选出最近最新的一次崩溃日志
        return getAllFiles()?.let {
            if (it.size > 0) it.reversed()[0] else null
        }
    }

    private fun writeNewFile(path: String, name: String, body: String) {
        getAllFiles()?.let {
            if (it.size >= mMaxNum) {
                //大于设置的数量则删除最旧文件
                it.sorted()[0].delete()
            }
            //继续存崩溃日志，新线程写入文件
            File(path, name).writeFile(body, false)
        }
    }

    /**
     * 当系统中有未被捕获的异常，系统将会自动调用 uncaughtException 方法
     * @param thread
     * @param exception
     */
    override fun uncaughtException(thread: Thread?, exception: Throwable?) {
        val defaultName = "crash_" + Date(System.currentTimeMillis()).getDateTimeByMillis(false).replace(":", "-") + ".log"
        val exceptionInfo = StringBuilder(
            mListener?.backFileRule() ?: (defaultName + "\n\n" + mSysInfo + "\n\n"
                    + exception?.message)
        )
        exceptionInfo.append("\n" + getExceptionInfo(exception))
        mDirPath?.let {
            if (mMaxNum > 0) {
                writeNewFile(it, mListener?.backFileRule() ?:defaultName, exceptionInfo.toString())
            }
        }
        // 系统默认处理
        mDefaultCrashHandler?.uncaughtException(thread, exception)
    }

    private fun getSysInfo(context: Context): String {
        val map = linkedMapOf<String, String>()
        map["phone_type"] = getDeviceInfo()
        map["app_version"] = context.getAppVersionName()
        map["os"] = "android"
        map["os_level"] = "Android ${getOsLevel()}"
        map["api_level"] = "Level ${getApiLevel()}"
        map["device_id"] = context.getDeviceIMEI()
        map["product"] = getDeviceProduct()
        map["cpu"] = getCpuABI()
        val str = StringBuilder("=".repeat(10) + "PhoneInfo" + "=".repeat(10) + "\n")
        for (item in map) {
            str.append(item.key).append(" : ").append(item.value).append("\n")
        }
        str.append("=".repeat(10) + "=".repeat(10) + "\n")
        return str.toString()
    }

    private fun getExceptionInfo(exception: Throwable?): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        exception?.printStackTrace(pw)
        return sw.toString()
    }

    interface CrashListener {
        /**
         * 返回每次发生崩溃时的日志文件名
         * @return 文件名
         */
        fun backFileRule(): String
    }


}

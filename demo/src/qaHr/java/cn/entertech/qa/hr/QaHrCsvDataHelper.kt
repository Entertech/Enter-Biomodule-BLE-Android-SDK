package cn.entertech.qa.hr

import cn.android.file.FileHelper
import cn.entertech.flowtimeble.App
import java.text.SimpleDateFormat
import java.util.Date

class QaHrCsvDataHelper(private val parentFileName: String) {

    private val dataHelperMap by lazy {
        HashMap<String, FileHelper>()
    }
    private val simple by lazy {
        SimpleDateFormat("yyyy/MM/dd  HH:mm:ss:SSS")
    }
    private var sessionStartTimestamp: Long = 0L

    fun startSession(startTimestamp: Long) {
        endSession()
        sessionStartTimestamp = startTimestamp
        initSessionFile("hr")
        initSessionFile("hr_rawdata")
    }

    fun isActive() = sessionStartTimestamp > 0L

    fun endSession() {
        sessionStartTimestamp = 0L
        dataHelperMap.forEach {
            it.value.close()
        }
        dataHelperMap.clear()
    }

    fun saveData(dataFileName: String, timestamp: Long, data: Long) {
        if (sessionStartTimestamp <= 0L) {
            return
        }
        val helper = dataHelperMap[dataFileName] ?: return
        helper.writeData("$timestamp,$data\n")
    }

    fun saveData(dataFileName: String, timestamp: Long, data: String) {
        if (sessionStartTimestamp <= 0L) {
            return
        }
        val helper = dataHelperMap[dataFileName] ?: return
        helper.writeData("$timestamp,$data\n")
    }

    private fun initSessionFile(fileSuffix: String) {
        val helper = FileHelper()
        val targetDir = App.getInstance().getExternalFilesDir(parentFileName)
        helper.setFilePath(
            "$targetDir/$fileSuffix",
            "${simple.format(Date(sessionStartTimestamp))}-$fileSuffix.csv"
        )
        dataHelperMap[fileSuffix] = helper
        helper.writeData("timestamp,value\n")
    }

    fun close() {
        endSession()
    }
}

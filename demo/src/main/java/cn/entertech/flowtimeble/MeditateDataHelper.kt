package cn.entertech.flowtimeble


import android.os.Looper
import cn.android.file.FileHelper
import java.text.SimpleDateFormat
import java.util.Locale

class MeditateDataHelper(val parentFileName: String) {

    //    private const val PARENT_FILE_NAME = "rawData"
    private val dataHelperMap by lazy {
        HashMap<String, FileHelper>()
    }
    private val mainHandler by lazy {
        android.os.Handler(Looper.getMainLooper())
    }

    fun getRawDataFile() = App.getInstance()
        .getExternalFilesDir(parentFileName)

    fun initHelper(session: String = "") {
        val current = System.currentTimeMillis()
        mainHandler.post {
            dataHelperMap.clear()
            cn.entertech.ble.api.bean.MeditateDataType.values().forEach {
                val helper = FileHelper()
                val targetFile = App.getInstance()
                    .getExternalFilesDir("${parentFileName}/${getFileDirName(session)}")
                helper.setFilePath(
                    "$targetFile",
                    "${it.name.lowercase()}-${
                        getFormatTime(
                            current,
                            "yyyy-MM-dd-HH-mm-ss"
                        )
                    }-$session"
                )
                dataHelperMap[it.name] = helper
            }
        }
    }


    fun saveData(type: cn.entertech.ble.api.bean.MeditateDataType, data: ByteArray) {
        dataHelperMap[type.name]?.writeData(data)
    }

    private fun saveStringData(type: cn.entertech.ble.api.bean.MeditateDataType, data: String) {
        dataHelperMap[type.name]?.writeData(data)
    }

    fun saveData(dataType: cn.entertech.ble.api.bean.MeditateDataType, data: String) {
        saveStringData(
            dataType, getFormatTime(
                System.currentTimeMillis(),
                "HH-mm-ss.SSSXXX"
            ) + ":  $data"
        )
    }

    fun close() {
        dataHelperMap.forEach {
            it.value.close()
        }
        dataHelperMap.clear()
    }

    private fun getFileDirName(fileSuffixName: String): String {
        return "${
            getFormatTime(
                System.currentTimeMillis(),
                "yyyy-MM-dd-HH-mm-ss"
            )
        }-$fileSuffixName"
    }

    private fun getFileName(filePrefixName: String, fileSuffixName: String): String {
        return "$filePrefixName-${
            getFormatTime(
                System.currentTimeMillis(),
                "yyyy-MM-dd-HH-mm-ss"
            )
        }-$fileSuffixName.txt"
    }

    fun getFormatTime(time: Long, pattern: String?): String? {
        val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())
        return dateFormat.format(time)
    }

}
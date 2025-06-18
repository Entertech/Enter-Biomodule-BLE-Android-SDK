package cn.entertech.flowtimeble.data


import android.os.Looper
import cn.android.file.FileHelper
import cn.entertech.base.util.getValueWithInit
import cn.entertech.flowtimeble.App
import java.text.SimpleDateFormat
import java.util.Locale

class MeditateDataHelper(private val parentFileName: String) {

    private val dataHelperMap by lazy {
        HashMap<String, FileHelper?>()
    }

    fun getRawDataFile() = App.getInstance().getExternalFilesDir(parentFileName)

    private fun getFileHelper(dataFileName: String) = dataHelperMap.getValueWithInit(dataFileName, {
        val current = System.currentTimeMillis()
        val helper = FileHelper()
        val targetFile = App.getInstance()
            .getExternalFilesDir("${parentFileName}/${getFileDirName(dataFileName)}")
        helper.setFilePath(
            "$targetFile", "${dataFileName.lowercase()}-${
                getFormatTime(
                    current, "yyyy-MM-dd-HH-mm-ss"
                )
            }"
        )
        helper
    })


    fun saveData(dataFileName: String, data: ByteArray) {
        getFileHelper(dataFileName)?.writeData(data)
    }

    private fun saveStringData(dataFileName: String, data: String) {
        getFileHelper(dataFileName)?.writeData(data)
    }

    fun saveStringDataWithTime(dataFileName: String, data: String) {
        saveStringData(
            dataFileName, getFormatTime(
                System.currentTimeMillis(), "HH-mm-ss.SSSXXX"
            ) + ":  $data"
        )
    }

    fun close() {
        dataHelperMap.forEach {
            it.value?.close()
        }
        dataHelperMap.clear()
    }

    private fun getFileDirName(fileSuffixName: String): String {
        return "${
            getFormatTime(
                System.currentTimeMillis(), "yyyy-MM-dd-HH-mm-ss"
            )
        }-$fileSuffixName"
    }

    private fun getFileName(filePrefixName: String, fileSuffixName: String): String {
        return "$filePrefixName-${
            getFormatTime(
                System.currentTimeMillis(), "yyyy-MM-dd-HH-mm-ss"
            )
        }-$fileSuffixName.txt"
    }

    private fun getFormatTime(time: Long, pattern: String?): String? {
        val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())
        return dateFormat.format(time)
    }

}
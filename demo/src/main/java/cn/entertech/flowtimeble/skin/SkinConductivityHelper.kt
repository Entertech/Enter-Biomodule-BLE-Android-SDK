package cn.entertech.flowtimeble.skin

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import cn.entertech.ble.utils.BleLogUtil
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException


object SkinConductivityHelper {
    const val TAG = "SkinConductivityHelper"
    private const val MAX_NUM_SKIN_DATA = 1000
    private val skinConductivityData = ArrayList<Int>()
    private var fileName: String? = null
    const val FILE_SUFFIX = ".txt"

    fun getSaveFileDirectory(context: Context): File {
        return File(context.filesDir, "skinData")
    }

    fun addSkinConductivityData(data: Int, context: Context) {
        skinConductivityData.add(data)
        if (skinConductivityData.size > MAX_NUM_SKIN_DATA) {
            saveDataIntoFile(context, null, true)
        }
    }

    @Throws
    private fun saveDataIntoFile(data: List<Int>, file: File) {
        BufferedWriter(FileWriter(file)).use { writer ->
            for (value in data) {
                writer.write(value.toString())
                writer.newLine() // 换行
            }
        }
    }

    fun saveDataIntoFile(
        context: Context, listener: ISaveDataListener? = null,
        isSavePart: Boolean = false
    ) {
        if (skinConductivityData.isEmpty()) {
            fileName = null
            return
        }
        listener?.start()
        val name = System.currentTimeMillis().toString() + FILE_SUFFIX
        if (fileName == null) {
            fileName = name
        }
        val file = File(getSaveFileDirectory(context), name)
        if (!file.exists()) {
            file.parentFile?.mkdirs()
        }
        val dataCache = ArrayList(skinConductivityData)
        skinConductivityData.clear()
        BleLogUtil.d(
            TAG, "dataCache size: ${dataCache.size}  " +
                    "dataSize: ${skinConductivityData.size}"
        )
        val handlerThread = HandlerThread("save_skin")
        handlerThread.start()
        val handler = Handler(handlerThread.looper)
        handler.post {
            try {
                saveDataIntoFile(dataCache, file)
                if (!isSavePart) {
                    fileName = null
                }
                listener?.complete()

            } catch (e: IOException) {
                BleLogUtil.e(TAG, "saveDataIntoFile error ${e.message}")
                if (!isSavePart) {
                    fileName = null
                }
                listener?.error(e.message ?: "")
            } finally {
                handlerThread.quit()
            }
        }
    }
}

interface ISaveDataListener {
    fun start()
    fun complete()
    fun error(errorMsg: String)

}

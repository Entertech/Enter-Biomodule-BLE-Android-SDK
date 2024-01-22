package cn.entertech.flowtimeble.skin

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.text.method.ScrollingMovementMethod
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import cn.entertech.flowtimeble.R
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

class SkinConductivityInfoActivity : AppCompatActivity() {
    private var tvSkinDataInfo: TextView? = null

    companion object{
        const val FILE_PATH="file_path"
    }
    private val handlerThread by lazy {
        HandlerThread("readSkinData")
    }
    private val handler by lazy {
        Handler(handlerThread.looper)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.skin_conductivity_info_activity)
        tvSkinDataInfo=findViewById(R.id.tvSkinDataInfo)
        tvSkinDataInfo?.movementMethod = ScrollingMovementMethod.getInstance();
        val path=intent.getStringExtra(FILE_PATH)
        if(path.isNullOrEmpty()){
            Toast.makeText(this.applicationContext,"文件路径无效",Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        handlerThread.start()
        handler.post {
            val sb=StringBuilder()
            val rb = BufferedReader(InputStreamReader(FileInputStream(File(path))))
            var str: String? = rb.readLine()?.trim()
            while (str != null) {
                sb.appendLine(str)
                str = rb.readLine()?.trim()
            }
            val logString = sb.toString()
            tvSkinDataInfo?.text = logString
            handlerThread.quit()
        }
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        handlerThread.quit()
        super.onDestroy()
    }
}
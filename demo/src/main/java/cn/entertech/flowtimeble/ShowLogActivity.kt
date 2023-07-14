package cn.entertech.flowtimeble

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import kotlin.concurrent.thread

class ShowLogActivity : AppCompatActivity() {
    private lateinit var tvShowLogs:TextView
    private val mainHandler by lazy {
        Handler(Looper.getMainLooper())
    }
    private var thread:Thread?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.show_log_activity)
        tvShowLogs=findViewById(R.id.tvShowLogs)
        showLogs()
    }

    private fun showLogs(){
        thread=thread {
            val sb = java.lang.StringBuilder()
            cacheDir.listFiles()?.forEach {
                val rb = BufferedReader(InputStreamReader(FileInputStream(it)))
                var str: String? = rb.readLine()?.trim()
                while (str != null) {
                    sb.append(str)
                    str=rb.readLine()?.trim()
                }
            }
            mainHandler.post {
                tvShowLogs.text=sb.toString()
            }
        }
    }

    override fun onDestroy() {
        thread?.interrupt()
        mainHandler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
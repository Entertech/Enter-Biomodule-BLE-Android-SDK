package cn.entertech.flowtimeble

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.Button
import android.widget.TextView
import cn.entertech.ble.log.BleLogUtil
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import kotlin.concurrent.thread

class ShowLogActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var tvShowLogs: TextView
    private lateinit var btnNextLog: Button
    private lateinit var btnPreviousLog: Button
    private lateinit var tvIndex: TextView
    private val mainHandler by lazy {
        Handler(Looper.getMainLooper())
    }

    private val runTime by lazy {
        Runtime.getRuntime()
    }

    private val logCacheList: ArrayList<String> by lazy {
        val logCacheList = ArrayList<String>()
        logCacheList
    }
    private var currentIndex = -1
    private var thread: Thread? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.show_log_activity)
        tvShowLogs = findViewById(R.id.tvShowLogs)
        tvIndex = findViewById(R.id.tvIndex)
        btnNextLog = findViewById(R.id.btnNextLog)
        btnPreviousLog = findViewById(R.id.btnPreviousLog)
        btnPreviousLog.setOnClickListener(this)
        btnNextLog.setOnClickListener(this)
        tvShowLogs.setMovementMethod(ScrollingMovementMethod.getInstance());
        showLogs()
    }



    private fun showLogs() {
        thread = thread {
            val sb = java.lang.StringBuilder()
            (application as App).logFile.parentFile?.listFiles()?.forEach {
                val rb = BufferedReader(InputStreamReader(FileInputStream(it)))
                var str: String? = rb.readLine()?.trim()
                while (str != null) {
                    sb.append(str)
                    if (sb.length > 400000) {
                        val firstLog = sb.toString()
                        logCacheList.add(firstLog)
                        sb.clear()
                        if (currentIndex < 0) {
                            currentIndex++
                            tvIndex.text = currentIndex.toString()+"/"+logCacheList.size
                            mainHandler.post {
                                tvShowLogs.text = firstLog
                            }
                        }
                        if (logCacheList.size > 100) {
                            BleLogUtil.d("wk", "大于100了 ${logCacheList.size}")
                            sb.clear()
                            return@thread
                        }
                    }
                    str = rb.readLine()?.trim()
                }
                val logString = sb.toString()
                if (currentIndex > -1) {
                    logCacheList.add(logString)
                } else {
                    currentIndex++
                    mainHandler.post {
                        tvShowLogs.text = logString
                    }
                }
                tvIndex.text = currentIndex.toString()+"/"+logCacheList.size
            }

        }
    }

    override fun onDestroy() {
        thread?.interrupt()
        mainHandler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnNextLog -> {
                currentIndex++
                if (currentIndex > logCacheList.size) {
                    currentIndex = logCacheList.size - 1
                }
                tvIndex.text = currentIndex.toString()+"/"+logCacheList.size
                if (currentIndex < logCacheList.size && currentIndex >= 0) {
                    tvShowLogs.text = logCacheList[currentIndex]
                }
            }
            R.id.btnPreviousLog -> {
                currentIndex--
                if (currentIndex < 0) {
                    currentIndex = 0
                }
                tvIndex.text = currentIndex.toString()+"/"+logCacheList.size
                if (currentIndex < logCacheList.size && currentIndex >= 0) {
                    tvShowLogs.text = logCacheList[currentIndex]
                }
            }
        }

    }
}
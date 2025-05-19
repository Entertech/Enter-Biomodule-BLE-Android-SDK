package cn.entertech.flowtimeble.device

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cn.entertech.base.util.startActivity
import cn.entertech.flowtimeble.R
import cn.entertech.flowtimeble.device.headband.HeadBandDemoActivity
import cn.entertech.flowtimeble.device.tag.BrainTagDemoActivity

class DeviceSelectActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_device_select)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        findViewById<View>(R.id.tvTag)?.setOnClickListener {
            startActivity(BrainTagDemoActivity::class.java, finishCurrent = false)
        }
        findViewById<View>(R.id.tvHeadband)?.setOnClickListener {
            startActivity(HeadBandDemoActivity::class.java, finishCurrent = false)
        }
    }
}
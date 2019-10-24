package cn.entertech.bleuisdk.ui.activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import cn.entertech.bleuisdk.R
import cn.entertech.bleuisdk.ui.DeviceUIConfig
import cn.entertech.bleuisdk.utils.Constant.Companion.INTENT_BLE_MANAGER_INDEX
import kotlinx.android.synthetic.main.activity_device_turn_on.*


class DeviceTurnOnActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initFullScreenDisplay()
        setStatusBarLight()
        setContentView(R.layout.activity_device_turn_on)
        initView()
    }


    private fun initView() {
        var mainColor = DeviceUIConfig.getInstance(this).mainColor
        btn_next.setBackgroundColor(mainColor)
        findViewById<TextView>(R.id.tv_title).text = getString(R.string.turn_deveice_on)
        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            finish()
        }
    }

    fun toDeviceConnect(view: View) {
        var index = intent.getIntExtra(INTENT_BLE_MANAGER_INDEX, 0)
        startActivity(Intent(this, DeviceActivity::class.java).putExtra(INTENT_BLE_MANAGER_INDEX, index))
    }

}

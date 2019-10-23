package cn.entertech.ble.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import cn.entertech.ble.R
import cn.entertech.ble.ui.BaseActivity
import cn.entertech.ble.ui.DeviceUIConfig
import cn.entertech.ble.utils.Constant.Companion.INTENT_BLE_MANAGER_INDEX
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
        var index = intent.getIntExtra(INTENT_BLE_MANAGER_INDEX, -1)
        startActivity(Intent(this, DeviceActivity::class.java).putExtra(INTENT_BLE_MANAGER_INDEX, index))
    }

}

package cn.entertech.bleuisdk.ui.activity

import android.os.Bundle
import android.view.View
import cn.entertech.bleuisdk.R
import kotlinx.android.synthetic.main.layout_common_title.*

class DeviceCanNotConnectActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_can_not_connect)
        initFullScreenDisplay()
        setStatusBarLight()
        tv_title.visibility = View.GONE
        iv_back.setOnClickListener {
            finish()
        }
    }
}

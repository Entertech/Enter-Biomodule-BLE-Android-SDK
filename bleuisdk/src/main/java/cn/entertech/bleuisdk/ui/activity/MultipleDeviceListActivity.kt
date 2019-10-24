package cn.entertech.bleuisdk.ui.activity

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.widget.Toast
import cn.entertech.ble.multiple.MultipleBiomoduleBleManager
import cn.entertech.bleuisdk.R
import cn.entertech.bleuisdk.ui.DeviceUIConfig
import cn.entertech.bleuisdk.ui.adapter.MultipleDeviceListAdapter
import com.polidea.rxandroidble2.RxBleDevice
import kotlinx.android.synthetic.main.activity_multiple_device_list.*
import kotlinx.android.synthetic.main.layout_common_title.*

class MultipleDeviceListActivity : BaseActivity() {
    private var mListAdapter: MultipleDeviceListAdapter? = null
    private var mDeviceUIConfig: DeviceUIConfig? = null
    private var mDevices: List<RxBleDevice?> = ArrayList()
    private var deviceManagers: List<MultipleBiomoduleBleManager> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multiple_device_list)
        initFullScreenDisplay()
        setStatusBarLight()
        mDeviceUIConfig = DeviceUIConfig.getInstance(this)
        if (!mDeviceUIConfig!!.isInited) {
            Toast.makeText(this, "请先初始化UI配置", Toast.LENGTH_SHORT).show()
            finish()
        }
        initDeviceList()
    }

    fun initDeviceList() {
        tv_title.text = "设备连接"
        deviceManagers = mDeviceUIConfig!!.managers
        mDevices = deviceManagers.map { it.getDevice() }
        mListAdapter = MultipleDeviceListAdapter(this,mDevices)
        rv_device_list.layoutManager = LinearLayoutManager(this)
        rv_device_list.adapter = mListAdapter
    }

    override fun onResume() {
        super.onResume()
        mDevices = deviceManagers.map { it.getDevice() }
        mListAdapter = MultipleDeviceListAdapter(this,mDevices)
        rv_device_list.adapter = mListAdapter
    }
}

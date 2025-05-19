package cn.entertech.flowtimeble.device.tag

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.entertech.ble.device.tag.BrainTagManager
import cn.entertech.ble.device.tag.function.ITagHrFunction
import cn.entertech.ble.function.IBrainWaveFunction
import cn.entertech.ble.function.IContactFunction
import cn.entertech.ble.function.IDeviceBatteryFunction
import cn.entertech.ble.function.IHrFunction
import cn.entertech.ble.function.IInfoFunction
import cn.entertech.ble.function.ISleepPostureFunction
import cn.entertech.ble.function.collect.ICollectBrainAndHrDataFunction
import cn.entertech.ble.function.collect.ICollectExerciseDegreeDataFunction
import cn.entertech.ble.log.BleLogUtil
import cn.entertech.flowtimeble.databinding.ActivityDeviceDemoBinding
import cn.entertech.flowtimeble.device.BaseDeviceActivity
import cn.entertech.flowtimeble.device.BleFunctionListAdapter

import java.util.Date

class BrainTagDemoActivity : BaseDeviceActivity() {

    companion object {
        private const val TAG = "BrainTagDemoActivity"
    }

    private lateinit var binding: ActivityDeviceDemoBinding
    private var rvBleFunction: RecyclerView? = null
    private val functionListAdapter by lazy {
        val adapter = BleFunctionListAdapter()
        adapter.bleFunctionClick = this
        adapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeviceDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        rvBleFunction = binding.rvBleFunction
        scrollView_logs = binding.scrollViewLogs
        btnClearLog = binding.btnClearLog
        cbShowLog = binding.cbStopLog
        scrollView_logs?.adapter = adapter
        scrollView_logs?.layoutManager = LinearLayoutManager(this)
        rvBleFunction?.layoutManager = GridLayoutManager(this, 2)
        rvBleFunction?.adapter = functionListAdapter
        bluetoothDeviceManager = BrainTagManager(this)
        cbShowLog?.isChecked = true
        needLog = cbShowLog?.isChecked ?: false
        cbShowLog?.setOnCheckedChangeListener { _, isChecked ->
            needLog = isChecked
        }
        btnClearLog?.setOnClickListener {
            adapter.setData(ArrayList())
        }
        functionListAdapter.setNewData(initBleFunction())

    }

    override fun showMsg(msg: String) {
        BleLogUtil.d(TAG, msg)
        if (!needLog) {
            return
        }
        val realMsg = "->: ${simple.format(Date())} $msg\n"
        runOnUiThread {
            adapter.addItem(realMsg)
            scrollView_logs?.scrollToPosition(adapter.itemCount - 1)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        functionListAdapter.bleFunctionClick = null
    }
}
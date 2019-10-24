package cn.entertech.bleuisdk.ui.adapter

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import cn.entertech.bleuisdk.R
import cn.entertech.bleuisdk.ui.activity.DeviceActivity
import cn.entertech.bleuisdk.ui.activity.DeviceTurnOnActivity
import cn.entertech.bleuisdk.utils.Constant.Companion.INTENT_BLE_MANAGER_INDEX
import cn.entertech.bleuisdk.utils.SettingManager
import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.RxBleDevice

/**
 * Created by EnterTech on 2017/3/16.
 */

class MultipleDeviceListAdapter(var context: Context, devices: List<RxBleDevice?>) : RecyclerView.Adapter<MultipleDeviceListAdapter.DeviceHolder>() {
    private var devices: List<RxBleDevice?> = ArrayList()

    init {
        this.devices = devices
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_multiple_device_list, parent, false)
        return DeviceHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceHolder, position: Int) {
        val device = devices[position]
        holder.deviceName?.text = "设备${position + 1}"
        if (device == null) {
            holder.connectStatus?.text = "未连接"
        } else {
            if (device.connectionState == RxBleConnection.RxBleConnectionState.CONNECTED) {
                holder.connectStatus?.text = "已连接"
            } else {
                holder.connectStatus?.text = "未连接"
            }
        }
        holder.itemView.setOnClickListener {
            if (!SettingManager.getInstance(context).isConnectBefore) {
                context.startActivity(Intent(context, DeviceTurnOnActivity::class.java).putExtra(INTENT_BLE_MANAGER_INDEX,position))
            } else {
                context.startActivity(Intent(context, DeviceActivity::class.java).putExtra(INTENT_BLE_MANAGER_INDEX,position))
            }
        }
    }

    override fun getItemCount(): Int {
        return devices.size
    }


    inner class DeviceHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var deviceName: TextView? = null
        var connectStatus: TextView? = null

        init {
            deviceName = itemView.findViewById(R.id.tv_device_name)
            connectStatus = itemView.findViewById(R.id.tv_device_connect_status)
        }
    }
}
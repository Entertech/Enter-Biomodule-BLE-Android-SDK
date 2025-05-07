package cn.entertech.flowtimeble.log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cn.entertech.base.list.adapter.BaseRecyclerViewAdapter

class LogAdapter(data: MutableList<String> = ArrayList()) :
    BaseRecyclerViewAdapter<String, LogAdapter.LogAdapterVH>(data) {

    class LogAdapterVH(val logView: TextView, rootView: View) : RecyclerView.ViewHolder(rootView)

    override fun onBindViewHolder(holder: LogAdapterVH, position: Int) {
        holder.logView.text = getItem(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogAdapterVH {
        val rootView = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        val logView = rootView.findViewById<TextView>(android.R.id.text1)
        return LogAdapterVH(logView, rootView)
    }

    fun addItem(log: String) {
        originData.add(log)
        if (originData.size > 1000) {
            originData.removeAt(0)
        }
        notifyDataSetChanged()
    }


}
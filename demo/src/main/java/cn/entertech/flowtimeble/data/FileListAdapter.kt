package cn.entertech.flowtimeble.data

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cn.entertech.base.list.adapter.BaseRecyclerViewAdapter
import cn.entertech.base.list.adapter.IRecycleViewClickListener
import cn.entertech.flowtimeble.R
import java.io.File

class FileListAdapter(
    data: List<File> = emptyList(),
    clickListener: IRecycleViewClickListener<File>
) :
    BaseRecyclerViewAdapter<File, FileListAdapter.LogListVH>(
        data,
        itemLayoutResId = R.layout.log_list_item_layout,
        listener = clickListener
    ) {
    class LogListVH(rootView: View) : RecyclerView.ViewHolder(rootView) {
        val text = rootView.findViewById<TextView>(R.id.tvLogName)
    }

    override fun onBindViewHolder(holder: LogListVH, position: Int) {
        val file = getItem(position)
        holder.itemView.setOnClickListener {
            clickListener?.itemClick(this, it, position, file)
        }
        holder.itemView.setOnLongClickListener {
            clickListener?.itemLongClick(this, it, position, file)
            true
        }
        holder.text?.apply {
            text = file.name
        }
    }

    override fun getViewHolder(rootView: View): LogListVH {
        return LogListVH(rootView)
    }
}
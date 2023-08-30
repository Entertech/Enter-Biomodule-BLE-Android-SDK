package cn.entertech.flowtimeble.skin

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cn.entertech.flowtimeble.skin.SkinConductivityHelper.FILE_SUFFIX
import cn.entertech.flowtimeble.skin.SkinConductivityInfoActivity.Companion.FILE_PATH
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

class SkinConductivityRecordAdapter(val data: Array<out File>, val context: Context) :
    RecyclerView.Adapter<SkinConductivityRecordAdapter.SkinDataListVH>() {
    private val sim by lazy {
        SimpleDateFormat("yyyy年MM月dd日HH:mm:ss.SSS")
    }

    class SkinDataListVH(private val rootView: View) : RecyclerView.ViewHolder(rootView) {
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkinDataListVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(
                android.R.layout.simple_list_item_1, parent, false
            )
        return SkinDataListVH(view)
    }

    override fun onBindViewHolder(holder: SkinDataListVH, position: Int) {
        val rootView = holder.itemView
        if (rootView is TextView) {
            val file = data[position]
            val timeString = file.name.removeSuffix(FILE_SUFFIX)
            val data = Date(timeString.toLong())
            if (data != null) {
                rootView.text = sim.format(data)
            } else {
                rootView.text = timeString
            }
            rootView.setOnClickListener {
                val intent = Intent(context, SkinConductivityInfoActivity::class.java)
                intent.putExtra(FILE_PATH, file.path)
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount() = data.size
}
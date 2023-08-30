package cn.entertech.flowtimeble.skin

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import cn.entertech.flowtimeble.BuildConfig
import cn.entertech.flowtimeble.skin.SkinConductivityHelper.FILE_SUFFIX
import cn.entertech.flowtimeble.skin.SkinConductivityInfoActivity.Companion.FILE_PATH
import java.io.File
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

    @SuppressLint("QueryPermissionsNeeded")
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
            rootView.setOnLongClickListener {
                // 指定要打开的文件的URI
                val intent = Intent(Intent.ACTION_VIEW)
                val uriForFile:Uri
                if (Build.VERSION.SDK_INT > 23){
                    //Android 7.0之后
                    uriForFile = FileProvider.getUriForFile(context,
                        BuildConfig.APPLICATION_ID + ".fileprovider", file);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//给目标文件临时授权
                }else {
                    uriForFile = Uri.fromFile(file);
                }
                // 创建隐式意图，设置动作为 ACTION_VIEW，数据为文本文件的URI

                intent.addCategory("android.intent.category.DEFAULT");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setDataAndType(uriForFile, context.contentResolver.getType(uriForFile))
                context.startActivity(intent)

              /*  // 检查是否有可以处理此意图的应用程序
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                } else {
                    Toast.makeText(
                        context.applicationContext,
                        "没有找到打开该文件的应用程序",
                        Toast.LENGTH_SHORT
                    ).show()
                }*/
                true
            }
        }
    }

    override fun getItemCount() = data.size
}
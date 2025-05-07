package cn.entertech.flowtimeble.data

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.android.base.view.ToastUtil
import cn.entertech.base.BaseActivity
import cn.entertech.base.list.adapter.BaseRecyclerViewAdapter
import cn.entertech.base.list.adapter.IRecycleViewClickListener
import cn.entertech.base.util.startActivity
import cn.entertech.ble.log.BleLogUtil
import cn.entertech.flowtimeble.R
import java.io.File

class FileListActivity : BaseActivity(), IRecycleViewClickListener<File> {

    companion object {
        private const val TAG = "FileListActivity"
        const val FILE_PATH = "filePath"
    }

    private val mLogListAdapter by lazy {
        FileListAdapter(clickListener = this)
    }
    private var rvLogFileList: RecyclerView? = null

    override fun getActivityLayoutResId(): Int {
        return R.layout.log_list_activity
    }

    override fun initActivityData() {
        super.initActivityData()
        val filePath = intent.getStringExtra(FILE_PATH)
        BleLogUtil.d(TAG, "filePath $filePath")
        val rootFile = if (filePath.isNullOrEmpty()) {
            application.getExternalFilesDir(null)
        } else {
            File((filePath))
        }
        val fileList = rootFile?.listFiles()?.toList() ?: emptyList()
        if (fileList.isEmpty()) {
            finish()
            return
        }
        mLogListAdapter.setData(fileList)
    }

    override fun initActivityView() {
        super.initActivityView()
        rvLogFileList = findViewById(R.id.rvLogFileList)
        rvLogFileList?.adapter = mLogListAdapter
        rvLogFileList?.layoutManager = LinearLayoutManager(this)

    }

    override fun itemClick(
        adapter: BaseRecyclerViewAdapter<File, *>,
        view: View?,
        position: Int,
        target: File
    ) {
        if (target.isDirectory) {
            val bundle = Bundle()
            bundle.putString(FILE_PATH, target.path)
            startActivity(FileListActivity::class.java, bundle, finishCurrent = false)
        } else {
            openTextFile(target)
        }
    }

    override fun itemLongClick(
        adapter: BaseRecyclerViewAdapter<File, *>,
        view: View?,
        position: Int,
        target: File
    ) {
        if (!target.isDirectory) {
            shareFile(target)
        }
    }

    private fun openTextFile(file: File) {
        if (file.exists()) {
            try {
                val fileUri: Uri = FileProvider.getUriForFile(
                    this, "${packageName}.fileprovider", file
                )

                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(fileUri, "text/plain")
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent)
                } else {
                    // 处理没有应用可以打开文件的情况
                    ToastUtil.toastShort(this, "没有应用可以打开此文件")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            ToastUtil.toastShort(this, "$file 文件不存在")
        }
    }

    private fun shareFile(file: File) {
        if (file.exists()) {
            val fileUri: Uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain" // 根据文件类型设置 MIME 类型
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            // 启动分享文件的选择器
            startActivity(Intent.createChooser(intent, "Share file using"))
        } else {
            ToastUtil.toastShort(this, "文件不存在")
        }
    }
}
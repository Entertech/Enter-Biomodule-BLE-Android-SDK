package cn.entertech.flowtimeble.data

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.android.base.view.ToastUtil
import cn.entertech.base.BaseActivity
import cn.entertech.base.list.adapter.BaseRecyclerViewAdapter
import cn.entertech.base.list.adapter.IRecycleViewClickListener
import cn.entertech.base.util.startActivity

import cn.entertech.ble.log.BleLogUtil
import cn.entertech.flowtimeble.R
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.Executors
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class FileListActivity : BaseActivity(), IRecycleViewClickListener<File> {

    companion object {
        private const val TAG = "FileListActivity"
        const val FILE_PATH = "filePath"
    }

    private val mLogListAdapter by lazy {
        FileListAdapter(clickListener = this)
    }
    private val uiHandler by lazy {
        Handler(Looper.getMainLooper())
    }
    private val zipExecutor by lazy {
        Executors.newSingleThreadExecutor()
    }
    private var rvLogFileList: RecyclerView? = null
    @Volatile
    private var isSharingZip = false

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
        rvLogFileList?.clipToPadding = false
        applySystemBarsPadding()

    }

    private fun applySystemBarsPadding() {
        val recyclerView = rvLogFileList ?: return
        val initialLeft = recyclerView.paddingLeft
        val initialTop = recyclerView.paddingTop
        val initialRight = recyclerView.paddingRight
        val initialBottom = recyclerView.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(recyclerView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                initialLeft + systemBars.left,
                initialTop + systemBars.top,
                initialRight + systemBars.right,
                initialBottom + systemBars.bottom
            )
            insets
        }
    }

    override fun itemClick(
        adapter: BaseRecyclerViewAdapter<File, *>, view: View?, position: Int, target: File
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
        adapter: BaseRecyclerViewAdapter<File, *>, view: View?, position: Int, target: File
    ) {
        if (!target.isDirectory) {
            shareFile(target)
        } else {
            shareDirectoryAsZip(target)
        }
    }

    private fun shareDirectoryAsZip(target: File) {
        if (isSharingZip) {
            ToastUtil.toastShort(this, "正在压缩，请稍候")
            return
        }

        val zipCacheDir = externalCacheDir ?: cacheDir
        if (!zipCacheDir.exists() && !zipCacheDir.mkdirs()) {
            ToastUtil.toastShort(this, "创建压缩缓存目录失败")
            return
        }
        val zipFile = File(zipCacheDir, "${target.name}.zip")
        isSharingZip = true
        ToastUtil.toastShort(this, "正在压缩，请稍候")
        zipExecutor.execute {
            try {
                deleteOldZipFile(zipFile)
                zipFolder(target, zipFile)
                uiHandler.post {
                    if (!isFinishing && !isDestroyed) {
                        shareZipFile(zipFile)
                    }
                }
            } catch (e: Exception) {
                BleLogUtil.i("压缩分享文件失败: ${e.message}")
                uiHandler.post {
                    if (!isFinishing && !isDestroyed) {
                        ToastUtil.toastShort(this, "压缩分享文件失败")
                    }
                }
            } finally {
                uiHandler.post {
                    isSharingZip = false
                }
            }
        }
    }

    private fun deleteOldZipFile(zipFile: File) {
        if (!zipFile.exists()) {
            return
        }
        if (zipFile.delete()) {
            BleLogUtil.i("成功删除旧的分享文件")
        } else {
            BleLogUtil.i("删除旧的分享文件失败")
        }
    }

    private fun shareZipFile(zipFile: File) {
        if (!zipFile.exists()) {
            BleLogUtil.i("需要分享文件不存在")
            ToastUtil.toastShort(this, "需要分享文件不存在")
            return
        }

        val zipUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", zipFile)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_STREAM, zipUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(Intent.createChooser(shareIntent, "Share ZIP File"))
    }


    private fun zipFolderContents(
        folder: File,
        parentPath: String,
        zipFile: File,
        zipOut: ZipOutputStream
    ) {
        if (Thread.currentThread().isInterrupted) {
            return
        }
        folder.listFiles()?.forEach { file ->
            if (file.canonicalFile == zipFile.canonicalFile) {
                return@forEach
            }
            val zipEntryName = if (parentPath.isEmpty()) file.name else "$parentPath/${file.name}"
            if (file.isDirectory) {
                zipFolderContents(file, zipEntryName, zipFile, zipOut)
            } else {
                FileInputStream(file).use { fis ->
                    val zipEntry = ZipEntry(zipEntryName)
                    zipOut.putNextEntry(zipEntry)
                    fis.copyTo(zipOut)
                    zipOut.closeEntry()
                }
            }
        }
    }

    private fun zipFolder(folder: File, zipFile: File) {
        if (!folder.exists() || !folder.isDirectory) {
            throw IllegalArgumentException("The folder path is invalid or not a directory.")
        }

        ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zipOut ->
            zipFolderContents(folder, folder.name, zipFile, zipOut)
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

    override fun onDestroy() {
        zipExecutor.shutdownNow()
        super.onDestroy()
    }
}

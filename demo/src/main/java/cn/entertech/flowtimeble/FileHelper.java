package cn.entertech.flowtimeble;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class FileHelper {
    private final static String TAG = "FileHelper";

    private final Handler mHandler;
    /**
     * 获取文件所在的文件夹名
     */
    protected String folderName;
    private String filePath;

    public FileHelper() {
        HandlerThread handlerThread = new HandlerThread("write_file_thread");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());
    }

    private FileOutputStream currentDataPw;
    private PrintWriter printWriter;

    public void setFilePath(String filePath, String fileName) {
        this.folderName = filePath;
        this.filePath = filePath + "/" + fileName;
        Log.i(TAG, "setFilePath filePath " + filePath + "fileName " + fileName);
    }

    private boolean init() {
        boolean isFirst = false;
        try {
            if (currentDataPw == null) {
                currentDataPw = new FileOutputStream(filePath);
                isFirst = true;
            }
            if (printWriter == null) {
                printWriter = new PrintWriter(filePath);
                isFirst = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "init error" + e.getMessage());
        }
        return isFirst;
    }


    public void writeData(final byte[] data) {
        mHandler.post(() -> {
            try {
                init();
                if (currentDataPw != null) {
                    currentDataPw.write(data);
                    currentDataPw.flush();
                }
            } catch (IOException ignored) {
            }
        });

    }

    public void writeData(String data) {
        mHandler.post(() -> {
            try {
                init();
                if (printWriter != null) {
                    printWriter.println(data);
                    printWriter.flush();
                }
                if (currentDataPw != null) {
                    currentDataPw.flush();
                }
            } catch (IOException ignored) {
            }
        });
    }


    public void close() {
        mHandler.post(() -> {
            if (printWriter != null) {
                printWriter.close();
                printWriter = null;
            }

            if (currentDataPw != null) {
                try {
                    currentDataPw.close();
                } catch (IOException e) {
                } finally {
                    currentDataPw = null;
                }
            }
        });

    }

    public String getFolderName() {
        return folderName;
    }

}

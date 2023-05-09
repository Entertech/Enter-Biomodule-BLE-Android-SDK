package cn.entertech.ble.utils;

import android.media.MediaMetadataRetriever;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;

import com.orhanobut.logger.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;


public class FileUtils {
    /**
     * 判断字符串是否为null或空字符串或空格
     */
    public static boolean isEmptyOrBlank(String str) {
        if (str == null || str.trim().length() == 0)
            return true;
        else
            return false;
    }

    /**
     * 文件重命名
     */
    public static boolean rename(String filePath, String newFileName, boolean overwriteIfExists) {
        if (isEmptyOrBlank(filePath) || isEmptyOrBlank(newFileName)) return false;
        File file = new File(filePath);
        if (!file.exists()) return false;
        File renameFile = new File(splicing(getFileDirectory(filePath), newFileName));
        if (overwriteIfExists) {
            renameFile.deleteOnExit();
        }
        if (renameFile.exists()) {
            return false;
        }
        return file.renameTo(renameFile);
    }

    /**
     * 拼接文件路径与文件名，自动补全连接符号/
     */
    public static String splicing(String dirPath, String fileName) {
        return getSupplementaryDirPath(dirPath) + fileName;
    }

    /**
     * 补全文件夹路径（确保最后字符是/）
     */
    public static String getSupplementaryDirPath(String dirPath) {
        if (!dirPath.endsWith(File.separator))
            return dirPath += File.separator;
        else
            return dirPath;
    }


    /**
     * 获取文件的目录部分（结尾带/）
     */
    public static String getFileDirectory(String filePath) {
        if (filePath.contains(File.separator))
            return filePath.substring(0, filePath.lastIndexOf(File.separator));
        else
            return "";
    }


    /**
     * 获取指定路径内存
     */
    public static long getPathMemory(String Path) {
        StatFs statFs = new StatFs(Path);
        long blockSize = statFs.getBlockSizeLong();
        long availableBlocks = statFs.getAvailableBlocksLong();
        return blockSize * availableBlocks;
    }


    //删除单个文件
    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            return file.delete();
        }
        return false;
    }

    /**
     * 删除文件或目录（包含目录下文件）
     */
    public static void removeFile(File file) {
        //如果是文件直接删除
        if (file.isFile()) {
            file.delete();
            return;
        }
        //如果是目录，递归判断，如果是空目录，直接删除，如果是文件，遍历删除
        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                file.delete();
                return;
            }
            for (File childFile : childFiles) {
                removeFile(childFile);
            }
            file.delete();
        }
    }

    /**
     * 创建目录，如果不存在的话
     */
    public static void mkdirIfNotExists(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    //读取文本文件中的内容
    public static String readTxtFile(String strFilePath) {
        String path = strFilePath;
        String content = ""; //文件内容字符串
        //打开文件
        File file = new File(path);
        //如果path是传递过来的参数，可以做一个非目录的判断
        if (file.isDirectory()) {
            Logger.d("The File doesn't not exist.");
        } else {
            try {
                InputStream instream = new FileInputStream(file);
                if (instream != null) {
                    InputStreamReader inputreader = new InputStreamReader(instream);
                    BufferedReader buffreader = new BufferedReader(inputreader);
                    String line;
                    //分行读取
                    while ((line = buffreader.readLine()) != null) {
                        content += line + "@";
                    }
                    instream.close();
                }
            } catch (FileNotFoundException e) {
                Logger.e("The File doesn't not exist.");
            } catch (IOException e) {
                Logger.e(e.getMessage());
            }
        }
        return content;
    }

    //获取本地文件大小
    public static long getVideoSize(File f) {
        FileChannel fc = null;
        try {
            if (f.exists() && f.isFile()) {
                FileInputStream fis = new FileInputStream(f);
                fc = fis.getChannel();
                long size = fc.size();
                return size;
            } else {
                Logger.e(f.getName() + " doesn't exist or is not a file");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != fc) {
                try {
                    fc.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return 0;
    }

    /**
     * 遍历文件夹
     */
    public static ArrayList<String> getAllDataFileName(String path) {
        ArrayList<String> fileList = new ArrayList<>();
        File file = new File(path);
        File[] tempList = file.listFiles();
        if (tempList != null && tempList.length > 0) {
            for (int i = 0; i < tempList.length; i++) {
                if (tempList[i].isDirectory()) {
                    //文件名
                    String fileName = tempList[i].getName();
                    fileList.add(fileName);
                }
            }
        }
        return fileList;
    }

    //遍历文件
    public static ArrayList<String> getFileName(String fileAbsolutePath) {
        ArrayList<String> fileList = new ArrayList<>();
        File file = new File(fileAbsolutePath);
        File[] subFile = file.listFiles();
        for (int iFileLength = 0; iFileLength < subFile.length; iFileLength++) {
            // 判断是否为文件夹
            if (!subFile[iFileLength].isDirectory()) {
                String filename = subFile[iFileLength].getName();
                fileList.add(filename);
            }
        }
        return fileList;
    }

    //判断本地配置文件是否存在
    public static void checkOptionFile() {
        File file = new File(FilePath.systemPath);
        try {
            if (!file.exists()) {
                file.createNewFile();
                Logger.i("创建文件" + file.getPath() + "成功");
                FileWriter fw = new FileWriter(file, true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter printWriter = new PrintWriter(bw);
                printWriter.print("name@虚拟现实心理健康训练系统\r\n");
                printWriter.print("version@VR_PHT_M1\r\n");
                printWriter.print("mac@xx:xx:xx:xx:xxx\r\n");
                printWriter.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //读取本地文件
    public static ArrayList<HashMap<String, String>> readLocalSysText() {
        ArrayList<HashMap<String, String>> hashMaps = new ArrayList<>();
        //获取内存路径
        File file = new File(FilePath.systemPath);
        if (file.exists()) {
            try {
                InputStream instream = new FileInputStream(file);
                if (instream != null) {
                    InputStreamReader inputreader = new InputStreamReader(instream);
                    BufferedReader buffreader = new BufferedReader(inputreader);
                    String line;
                    //分行读取
                    while ((line = buffreader.readLine()) != null) {
                        String[] lineSplit = line.split("@");
                        HashMap<String, String> map = new HashMap<>();
                        map.put(lineSplit[0], lineSplit[1]);
                        hashMaps.add(map);
                    }
                    instream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return hashMaps;
    }


    //修改本地文件
    public static void writeLocalFile(String mac) {
        File file = new File(FilePath.systemPath);
        try {
            if (file.exists()) {
                RandomAccessFile raf = new RandomAccessFile(file, "rwd");
                char[] chars = mac.toCharArray();
                for (int i = 0; i < chars.length; i++) {
                    raf.seek(file.length() - (chars.length - i));
                    raf.write(chars[i]);
                }
                raf.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //修改视频文件
    public static void modifyVideoFile(File file, String value) {
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rws");
            for (int i = 0; i < 100; ++i) {
                randomAccessFile.seek(i);
                if (value.contains(",")) {
                    randomAccessFile.write(StringtoInt(value)[i]);
                } else {
                    randomAccessFile.write(1);
                }
            }
            Logger.i("文件修改成功");
        } catch (Exception e) {
            Logger.i("文件修改失败");
            e.printStackTrace();
        }
    }

    public static int[] StringtoInt(String password) {
        int[] intPassword = new int[password.length()];
        StringTokenizer stringTokenizer = new StringTokenizer(password, ",");
        for (int i = 0; stringTokenizer.hasMoreElements(); intPassword[i++] = Integer.valueOf(stringTokenizer.nextToken())) {
        }
        return intPassword;
    }


    private long getAllFileLength(String url) {
        try {
            URL u = null;
            long fileLength = 0;
            u = new URL(url);
            HttpURLConnection urlcon = (HttpURLConnection) u.openConnection();
            fileLength = fileLength + handleFileLen(urlcon.getHeaderFields());
            return fileLength;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return 0;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    //获取线上文件大小
    public long handleFileLen(Map<String, List<String>> headers) {
        if (headers == null || headers.isEmpty()) {

            return -1;
        }
        List<String> sLength = headers.get("Content-Length");
        if (sLength == null || sLength.isEmpty()) {
            return -1;
        }
        Log.e("handleFileLen", sLength.get(0));
        String temp = sLength.get(0);
        long len = TextUtils.isEmpty(temp) ? -1 : Long.parseLong(temp);
        // 某些服务，如果设置了conn.setRequestProperty("Range", "bytes=" + 0 + "-");
        // 会返回 Content-Range: bytes 0-225427911/225427913
        if (len < 0) {
            List<String> sRange = headers.get("Content-Range");
            if (sRange == null || sRange.isEmpty()) {
                len = -1;
            } else {
                int start = temp.indexOf("/");
                len = Long.parseLong(temp.substring(start + 1));
            }
        }
        return len;
    }

    //获取视频时长
    public static int getLocalVideoDuration(String videoPath) {
        int duration;
        try {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(videoPath);
            duration = Integer.parseInt(mmr.extractMetadata
                    (MediaMetadataRetriever.METADATA_KEY_DURATION));
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        return duration;
    }





    /**
     * 遍历文件夹
     */
    public static ArrayList<String> getAllDataFileName(String path,boolean flag) {
        ArrayList<String> fileList = new ArrayList<>();
        File file = new File(path);
        File[] tempList = file.listFiles();
        if (tempList != null && tempList.length > 0) {
            for (int i = 0; i < tempList.length; i++) {
                if (!tempList[i].isDirectory()) {
                    // 文件名
                    String fileName = tempList[i].getName();
                    if (flag){
                        fileList.add(fileName);
                    }else{
                        fileList.add(fileName.substring(0, fileName.indexOf(".")));
                    }
                }
            }
        }
        return fileList;
    }

    public static void waitForWirtenCompleted(File file) {
        if (!file.exists())
            return;
        long old_length;
        do {
            old_length = file.length();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Logger.e("waitForWirtenCompleted: ", old_length + " " + file.length());
        } while (old_length != file.length());
    }
}

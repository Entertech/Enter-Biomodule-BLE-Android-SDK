package cn.entertech.ble.utils;

public class CharUtil {

    public static int converUnchart(byte data) {
        return (data & 0xff);
    }

    public static int bytesToInt(byte[] b){
        String s = new String(b);
        return Integer.parseInt(s);
    }
}

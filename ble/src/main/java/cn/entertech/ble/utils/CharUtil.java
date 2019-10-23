package cn.entertech.ble.utils;

public class CharUtil {

    public static int converUnchart(byte data) {
        return (data & 0xff);
    }
}

package cn.entertech.ble.util;

public class CharUtil {

    public static int converUnchart(byte data) {
        return (data & 0xff);
    }
}

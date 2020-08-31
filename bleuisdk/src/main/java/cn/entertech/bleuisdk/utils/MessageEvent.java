package cn.entertech.bleuisdk.utils;

public class MessageEvent {
    public static final int MESSAGE_CODE_DEVICE_CONNECT = 0;
    public static final int MESSAGE_CODE_DATA_EDIT = 1;
    public static final int MESSAGE_CODE_DATA_EDIT_DONE = 2;
    public static final int MESSAGE_CODE_TO_DEVICE_CONNECT = 3;
    public static final int MESSAGE_CODE_TO_NET_RESTORE = 4;
    public static final int MESSAGE_CODE_TO_REFRESH_RECORD = 5;
    public static final int MESSAGE_CODE_STATISTICS_EDIT_DONE = 6;
    public static final int MESSAGE_CODE_PURCHASE_DONE = 7;
    public static final int MESSAGE_CODE_REFRESH_JOURNEY = 8;
    public static final int MESSAGE_CODE_FINISH_PREMIUM_DONE_PAGE = 9;
    public static final int MESSAGE_CODE_SHARE_REPORT = 10;
    private String message;
    private int messageCode;
    private String data;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getMessageCode() {
        return messageCode;
    }

    public void setMessageCode(int messageCode) {
        this.messageCode = messageCode;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}

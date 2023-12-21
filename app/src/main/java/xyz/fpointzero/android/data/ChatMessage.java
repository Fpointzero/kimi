package xyz.fpointzero.android.data;

import org.litepal.crud.LitePalSupport;

public class ChatMessage extends LitePalSupport {
    private String userid;
    private boolean isSend;
    private String message;
    private long timestamp;

    public ChatMessage() {
        // 默认构造方法
    }

    public ChatMessage(String userid, boolean isSend, String message, long timestamp) {
        this.userid = userid;
        this.isSend = isSend;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getUserid() {
        return userid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSend() {
        return isSend;
    }

    public void setSend(boolean send) {
        isSend = send;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }
}

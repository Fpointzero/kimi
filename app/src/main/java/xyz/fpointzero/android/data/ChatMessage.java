package xyz.fpointzero.android.data;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

/**
 * 聊天消息类
 */
public class ChatMessage extends LitePalSupport {
    @Column(unique = true)
    int id;
    private String userid;
    private boolean isSend;
    private boolean isImg;
    private String message;
    private long timestamp;

    public ChatMessage() {
        // 默认构造方法
    }

    public ChatMessage(String userid, boolean isSend, boolean isImg, String message, long timestamp) {
        this.userid = userid;
        this.isSend = isSend;
        this.isImg = isImg;
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

    public boolean isImg() {
        return isImg;
    }

    public void setSend(boolean send) {
        isSend = send;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
}

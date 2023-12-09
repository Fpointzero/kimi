package xyz.fpointzero.android.data;

import org.litepal.crud.LitePalSupport;

public class ChatMessage extends LitePalSupport {
    private String sender;
    private String receiver;
    private String message;
    private long timestamp;

    public ChatMessage() {
        // 默认构造方法
    }

    public ChatMessage(String sender, String receiver, String message, long timestamp) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
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
}

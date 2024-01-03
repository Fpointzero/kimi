package xyz.fpointzero.android.data;

import android.annotation.SuppressLint;
import android.database.Cursor;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

/**
 * 聊天记录
 */
public class MessageRecord {
    private int id = -1;
    private String userID;
    private String username;

    private boolean isSend;
    private boolean isImg;
    //    private String publicKey;
    private String msg;

    public MessageRecord(int id, String userID, String username, boolean isSend, boolean isImg, String msg) {
        this.id = id;
        this.userID = userID;
        this.username = username;
        this.isSend = isSend;
        this.isImg = isImg;
        this.msg = msg;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isSend() {
        return isSend;
    }

    public void setSend(boolean send) {
        isSend = send;
    }

    public void setImg(boolean img) {
        isImg = img;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public boolean isImg() {
        return isImg;
    }

    /**
     * 获取类似QQ那种消息列表
     * @return
     */
    @SuppressLint("Range")
    public static List<MessageRecord> getMsgRecordList() {
        List<MessageRecord> messageList;
//        Cursor cursor = LitePal.findBySQL("SELECT * from (SELECT * FROM `chatmessage` a LEFT JOIN user b ON a.userid = b.userid ORDER BY timestamp DESC) as t GROUP BY userid;");
        Cursor cursor = LitePal.findBySQL("SELECT * from (SELECT * from `chatmessage` AS a LEFT JOIN user AS b ON a.userid = b.userid ORDER BY timestamp ASC) as t GROUP BY userid ORDER BY timestamp DESC");
        // 为什么ASC反而变成了DESC ？ SQL语句要用DESC，但是这里用ASC;
//        Cursor cursor = LitePal.findBySQL("select * from (SELECT * FROM `chatmessage` as a LEFT JOIN user as b on a.userid = b.userid order by timestamp ASC) as t GROUP BY userid;");
        messageList = handlerCursor(cursor);
        cursor.close();
        return messageList;
    }

    /**
     * 根据搜索返回消息列表
     * @param search
     * @return
     */
    @SuppressLint("Range")
    public static List<MessageRecord> getAllMsgRecordListBy(String search) {
        List<MessageRecord> messageList;
        Cursor cursor = LitePal.findBySQL("SELECT * from (SELECT * from `chatmessage` AS a LEFT JOIN user AS b ON a.userid = b.userid) as t WHERE username LIKE ? OR message LIKE ? ORDER BY timestamp DESC", "%" + search + "%", "%" + search + "%");
        messageList = handlerCursor(cursor);
        cursor.close();
        return messageList;
    }

    @SuppressLint("Range")
    private static List<MessageRecord> handlerCursor(Cursor cursor) {
        List<MessageRecord> messageList = new ArrayList<MessageRecord>();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String userID = cursor.getString(cursor.getColumnIndex("userid"));
            String username = cursor.getString(cursor.getColumnIndex("username"));
            boolean isSend = cursor.getInt(cursor.getColumnIndex("issend")) == 1;
            boolean isImg = cursor.getInt(cursor.getColumnIndex("isimg")) == 1;
            String message = cursor.getString(cursor.getColumnIndex("message"));
            MessageRecord msg = new MessageRecord(id, userID, username, isSend, isImg,message);
            messageList.add(msg);
        }
        return messageList;
    }
}

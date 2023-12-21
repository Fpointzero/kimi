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
    //    private String publicKey;
    private String msg;

    public MessageRecord(int id, String userID, String username, boolean isSend, String msg) {
        this.id = id;
        this.userID = userID;
        this.username = username;
        this.isSend = isSend;
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

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @SuppressLint("Range")
    public static List<MessageRecord> getMsgRecordList() {
        List<MessageRecord> messageList = new ArrayList<MessageRecord>();
//        Cursor cursor = LitePal.findBySQL("SELECT * from (SELECT * FROM `chatmessage` a LEFT JOIN user b ON a.userid = b.userid ORDER BY timestamp DESC) as t GROUP BY userid;");
        Cursor cursor = LitePal.findBySQL("SELECT * from (SELECT * from `chatmessage` AS a LEFT JOIN user AS b ON a.userid = b.userid ORDER BY timestamp ASC) as t GROUP BY userid ORDER BY timestamp DESC");
        // 为什么ASC反而变成了DESC ？ SQL语句要用DESC，但是这里用ASC;
//        Cursor cursor = LitePal.findBySQL("select * from (SELECT * FROM `chatmessage` as a LEFT JOIN user as b on a.userid = b.userid order by timestamp ASC) as t GROUP BY userid;");
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String userID = cursor.getString(cursor.getColumnIndex("userid"));
            String username = cursor.getString(cursor.getColumnIndex("username"));
            boolean isSend = cursor.getInt(cursor.getColumnIndex("issend")) == 1;
            String message = cursor.getString(cursor.getColumnIndex("message"));
            MessageRecord msg = new MessageRecord(id, userID, username, isSend, message);
            messageList.add(msg);
        }
        cursor.close();
        return messageList;
    }

    @SuppressLint("Range")
    public static List<MessageRecord> getAllMsgRecordListBy(String search) {
        List<MessageRecord> messageList = new ArrayList<MessageRecord>();
        Cursor cursor = LitePal.findBySQL("SELECT * from (SELECT * from `chatmessage` AS a LEFT JOIN user AS b ON a.userid = b.userid) as t WHERE username LIKE ? OR message LIKE ? ORDER BY timestamp DESC", "%" + search + "%", "%" + search + "%");
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String userID = cursor.getString(cursor.getColumnIndex("userid"));
            String username = cursor.getString(cursor.getColumnIndex("username"));
            boolean isSend = cursor.getInt(cursor.getColumnIndex("issend")) == 1;
            String message = cursor.getString(cursor.getColumnIndex("message"));
            MessageRecord msg = new MessageRecord(id, userID, username, isSend, message);
            messageList.add(msg);
        }
        cursor.close();
        return messageList;
    }
}

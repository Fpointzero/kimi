package xyz.fpointzero.android.utils.data;

import android.annotation.SuppressLint;
import android.database.Cursor;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import xyz.fpointzero.android.data.Message;

public class MessageUtil {
    private static final String TAG = "MessageUtil";

    public static void sendTextMsg() {

    }

    @SuppressLint("Range")
    public static List<Message> getMsgList() {
        List<Message> messageList = new ArrayList<Message>();
//        Cursor cursor = LitePal.findBySQL("SELECT * from (SELECT * FROM `chatmessage` a LEFT JOIN user b ON a.userid = b.userid ORDER BY timestamp DESC) as t GROUP BY userid;");
        Cursor cursor = LitePal.findBySQL("SELECT * from (SELECT * from `chatmessage` AS a LEFT JOIN user AS b ON a.userid = b.userid ORDER BY timestamp ASC) as t GROUP BY userid ORDER BY timestamp DESC");
        // 为什么ASC反而变成了DESC ？ SQL语句要用DESC，但是这里用ASC;
//        Cursor cursor = LitePal.findBySQL("select * from (SELECT * FROM `chatmessage` as a LEFT JOIN user as b on a.userid = b.userid order by timestamp ASC) as t GROUP BY userid;");
        while (cursor.moveToNext()) {
            String userID = cursor.getString(cursor.getColumnIndex("userid"));
            String username = cursor.getString(cursor.getColumnIndex("username"));
            String message = cursor.getString(cursor.getColumnIndex("message"));
            String ip = cursor.getString(cursor.getColumnIndex("ip"));
            Message msg = new Message(userID, username, message, ip);
            messageList.add(msg);
        }
        cursor.close();
        return messageList;
    }

    @SuppressLint("Range")
    public static List<Message> getAllMsgListBy(String search) {
        List<Message> messageList = new ArrayList<Message>();
//        Cursor cursor = LitePal.findBySQL("SELECT * from (SELECT * FROM `chatmessage` a LEFT JOIN user b ON a.userid = b.userid ORDER BY timestamp DESC) as t GROUP BY userid;");
        Cursor cursor = LitePal.findBySQL("SELECT * from (SELECT * from `chatmessage` AS a LEFT JOIN user AS b ON a.userid = b.userid) as t WHERE username LIKE ? OR message LIKE ? ORDER BY timestamp DESC", "%" + search + "%", "%" + search + "%");

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String userID = cursor.getString(cursor.getColumnIndex("userid"));
            String username = cursor.getString(cursor.getColumnIndex("username"));
            String message = cursor.getString(cursor.getColumnIndex("message"));
            String ip = cursor.getString(cursor.getColumnIndex("ip"));
            Message msg = new Message(userID, username, message, ip);
            msg.setAction(id);
            messageList.add(msg);
        }
        cursor.close();
        return messageList;
    }

    //    public static String sendMessage(String url, Message msg) {
//        OkHttpClient client = new OkHttpClient();
//
//        MediaType mediaType = MediaType.parse("application/json");
//        RequestBody requestBody = RequestBody.create(msg.toString(), mediaType);
//        Request request = new Request.Builder()
//                .url(url)
//                .post(requestBody)
//                .build();
//
////        client.newCall(request).enqueue(new okhttp3.Callback() {
////            @Override
////            public void onResponse(Call call, Response response) throws IOException {
////                if (response.isSuccessful()) {
////                    String responseBody = response.body().string();
////                    callback.onSuccess(responseBody);
////                } else {
////                    callback.onError();
////                }
////            }
////
////            @Override
////            public void onFailure(Call call, IOException e) {
////                e.printStackTrace();
////                callback.onError();
////            }
////        });
//        try {
//            Response response = client.newCall(request).execute();
//            if (response.isSuccessful()) {
//                String responseBody = response.body().string();
//            } else {
//                return null;
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
    /*
    public static void sendMessage(String ipAndPort, Message msg, final Callback callback) {
        String url = ipAndPort.contains(":") ? ipAndPort : ipAndPort + ":10808";

        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody requestBody = RequestBody.create(msg.toString(), mediaType);
        Request request = new Request.Builder()
                .url("http://" + url + "/")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.e(TAG, "Callback: 回调");
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    new Handler(Looper.getMainLooper()).post(() -> {
                        callback.onSuccess(responseBody);
                    });
                } else {
                    callback.onError();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                callback.onError();
            }
        });
    }
*/
}

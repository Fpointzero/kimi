package xyz.fpointzero.android.utils.network;

import xyz.fpointzero.android.network.Message;
import xyz.fpointzero.android.network.MyWebSocket;
import xyz.fpointzero.android.network.ClientWebSocketManager;

public class MessageUtil {
    private static final String TAG = "MessageUtil";
    
    public static void sendTextMsg() {
        
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

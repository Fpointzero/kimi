package xyz.fpointzero.android.network;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSON;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.litepal.LitePal;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.ByteString;
import xyz.fpointzero.android.constants.ConnectType;
import xyz.fpointzero.android.constants.DataType;
import xyz.fpointzero.android.constants.Role;
import xyz.fpointzero.android.data.ChatMessage;
import xyz.fpointzero.android.data.Message;
import xyz.fpointzero.android.data.User;
import xyz.fpointzero.android.utils.data.FileUtil;
import xyz.fpointzero.android.utils.data.MessageUtil;
import xyz.fpointzero.android.utils.data.SerializationUtil;
import xyz.fpointzero.android.utils.data.UserUtil;
import xyz.fpointzero.android.utils.data.SettingUtil;
import xyz.fpointzero.android.utils.crypto.RSAUtil;

public class MockWebServerManager {
    private static final String TAG = "MockWebServer";
    private static MockWebServerManager sInstance;
    private static ArrayList<WeakReference<WebSocketDataListener>> sWeakRefListeners;
    private static HashMap<String, MyWebSocket> connectWebSocketMap;
    private int port; // 端口
    private MockWebServer mMockWebServer;

    private MockWebServerManager() {
    }

    public MyWebSocket getServerWS(String userID) {
        return connectWebSocketMap.get(userID);
    }

    public static synchronized MockWebServerManager getInstance() {
        if (sInstance == null) {
            connectWebSocketMap = new HashMap<String, MyWebSocket>();
            sInstance = new MockWebServerManager();
            sWeakRefListeners = new ArrayList<>();
        }
        return sInstance;
    }

    /**
     * 服务器运行
     */
    public void start() {
        port = SettingUtil.getInstance().getSetting().getServerPort();
        initMockServer();
    }

    public void start(int port) {
        this.port = port;
        initMockServer();
    }

    public void close() throws IOException {
        mMockWebServer.shutdown();
        mMockWebServer.close();
    }

    /**
     * 初始化服务器
     */
    private void initMockServer() {
        try {
            // 初始化
//            connectWebSocketList = new ArrayList<MyWebSocket>();

            mMockWebServer = new MockWebServer();
            InetAddress ipAddress = InetAddress.getByName("0.0.0.0");
            while (true) {
                if (SettingUtil.getInstance().getSetting() != null)
                    break;
                Log.d(TAG, "initMockServer: sleep 1");
                Thread.sleep(1000);
            }

//            mMockWebServer.enqueue(response);
            mMockWebServer.setDispatcher(dispatcher);
            mMockWebServer.start(ipAddress, port);
            //获取连接url，初始化websocket客户端
            String websocketUrl = "Listen: " + mMockWebServer.getHostName() + ":" + mMockWebServer.getPort() + "/";


            Log.d(TAG, "initMockServer :" + websocketUrl);
        } catch (Exception e) {
            Log.e(TAG, "initMockServer: ", e);
        }


    }

//    public void sendToOthers(WebSocket mWebSocket, final String msg) {
//        for (MyWebSocket webSocket : connectWebSocketList) {
//            if (webSocket != mWebSocket) {
//                webSocket.send(msg);
//            }
//        }
//    }

    private Dispatcher dispatcher = new Dispatcher() {
        @Override
        public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
            String queryPath = request.getPath();
            if (queryPath.equals("/webSocket")) {
                return new MockResponse().withWebSocketUpgrade(serverWebSocketListener);
            }
            // 返回默认的响应;
            return new MockResponse().setResponseCode(404);
        }

    };
    private WebSocketListener serverWebSocketListener = new WebSocketListener() {
        @Override
        public void onFailure(@NotNull okhttp3.WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
            super.onFailure(webSocket, t, response);
            Set<String> keys = connectWebSocketMap.keySet();
            for (String key : keys) {
                if (connectWebSocketMap.get(key).getmWebSocket() == webSocket) {
                    connectWebSocketMap.remove(key);
                    return;
                }
            }
            Log.e(TAG, "onFailure！" + t.getMessage());
        }

        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
            super.onMessage(webSocket, text);
            // 明文传输
            try {
                Log.d(TAG, "onMessage: " + text);
                Message data = JSON.parseObject(text, Message.class);
//                User user = new User(data.getUserID(), data.getUsername(), data.getIp());
                List<User> tmpList = LitePal.where("userid = ?", data.getUserID()).find(User.class); // 临时存放用户
                User user = null;
                // 不存在则创建，存在则更新用户名
                if (tmpList.isEmpty()) {
                    user = new User(data.getUserID(), data.getUsername(), data.getIp());
                    user.save();
                } else {
                    user = tmpList.get(0);
                    user.setUsername(data.getUsername());
                    user.updateAll("userid = ?", user.getUserID());
                }
                // 黑名单处理
                if (user.isBlack()) {
                    webSocket.close(ConnectType.CONNECT_REFUSE, "连接已拒绝");
                    return;
                }

                // 存放对方公钥
                if (DataType.DATA_CONNECT == data.getAction()) {
                    // 每次连接都要更新对方状态（包括名字，IP这两个）
                    try {
                        if (!user.save()) {
                            
                        }
                        
                    } catch (Exception e) {
                        Log.e(TAG, "onMessage: ", e);
                    }
                    // 将连接加入到管理中
                    MyWebSocket tmp = new MyWebSocket(webSocket, RSAUtil.publicKeyFromString(data.getMsg()));
                    // 如果不存在则插入，存在则不变
                    connectWebSocketMap.putIfAbsent(user.getUserID(), tmp);
                    webSocket.send(Message.getConnectMessage().toString());
                }

                // 加好友处理
                if (data.getAction() == DataType.DATA_ADD) {
                    if (!user.isWhite())
                        ClientWebSocketManager.getInstance().onWSDataChanged(Role.SERVER, data);
                    else
                        webSocket.send(new Message(DataType.DATA_ADD, "success").toString());
                }

                // 接受图片
                if (user.isWhite() && data.getAction() == DataType.PRIVATE.IMAGE) {
                    Bitmap bitmap = SerializationUtil.deserializeBitmapFromBase64String(data.getMsg());
                    ChatMessage chatMessage = new ChatMessage(user.getUserID(), true, true, null, System.currentTimeMillis());
                    chatMessage.setMessage(data.getUserID() + "/" + chatMessage.getTimestamp() + ".png");
                    chatMessage.save();
                    FileUtil.createNewImg(chatMessage.getMessage(), bitmap);
                    onWSDataChanged(Role.SERVER, data);
                }

            } catch (Exception e) {
                Log.e(TAG, "onMessage", e);
            }
        }

        @Override
        public void onMessage(@NonNull WebSocket webSocket, @NonNull ByteString bytes) {
            super.onMessage(webSocket, bytes);
            // 密文传输
            try {
                // 私钥解密
                String text = MessageUtil.msgDecrypt(bytes);
                Log.d(TAG, "onMessage(Byte): " + text);
                Message data = JSON.parseObject(text, Message.class);
                
                // 用户
                List<User> tmpList = LitePal.where("userid = ?", data.getUserID()).find(User.class);
                if (tmpList.isEmpty())
                    return;
                User user = tmpList.get(0);
                
                
                if (DataType.DATA_PING == data.getAction()) {
//                    final String message = JSON.toJSONString(new Message(Type.DATA_PING, "pong response"));
                    sendEncryptMsg(data.getUserID(), DataType.DATA_PING, "ping");
                    return;
                }

                if (user.isWhite()) {
                    if (DataType.DATA_PRIVATE == data.getAction()) {
                        new ChatMessage(user.getUserID(), true, false, data.getMsg(), System.currentTimeMillis()).save();
                    }
                    onWSDataChanged(Role.SERVER, data);
                }
            } catch (Exception e) {
                Log.e(TAG, "onMessage(Byte): " + e.getMessage(), e);
            }

        }

        @Override
        public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
            super.onClosed(webSocket, code, reason);

            Set<String> keys = connectWebSocketMap.keySet();
            for (String key : keys) {
                if (connectWebSocketMap.get(key).getmWebSocket() == webSocket) {
                    connectWebSocketMap.remove(key);
                    return;
                }
            }
            Log.d(TAG, "服务器：onClosed：");
        }
    };

    public void sendEncryptMsg(String userid, int dataType, String msg) throws Exception {
        connectWebSocketMap.get(userid).sendByEncrypt(new Message(dataType, msg).toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 发送加密后的信息给所有连接服务器的
     *
     * @param bytes 明文
     * @throws Exception
     */
    public void sendAll(byte[] bytes) throws Exception {
        Set<String> keys = connectWebSocketMap.keySet();
        for (String key : keys) {
            connectWebSocketMap.get(key).sendByEncrypt(bytes);
        }
    }

    public int getPort() {
        return port;
    }

    public static HashMap<String, MyWebSocket> getConnectWebSocketMap() {
        return connectWebSocketMap;
    }

    public static void onWSDataChanged(int dataType, Message data) {
        ClientWebSocketManager.getInstance().onWSDataChanged(dataType, data);
    }
}

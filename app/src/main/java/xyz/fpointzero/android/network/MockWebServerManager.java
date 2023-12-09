package xyz.fpointzero.android.network;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSON;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.ByteString;
import xyz.fpointzero.android.constants.DataType;
import xyz.fpointzero.android.data.User;
import xyz.fpointzero.android.utils.data.UserUtil;
import xyz.fpointzero.android.utils.data.SettingUtil;
import xyz.fpointzero.android.utils.crypto.RSAUtil;

public class MockWebServerManager {
    private static final String TAG = "MockWebServer";
    private static MockWebServerManager sInstance;
    private static ArrayList<WeakReference<MyWebSocketManager.WebSocketDataListener>> sWeakRefListeners;
    private static Handler sDelivery; // 返回主线程入口
    private int port; // 端口
    private MockWebServer mMockWebServer;
    private ArrayList<MyWebSocket> connectWebSocketList;

    private MockWebServerManager() {
    }

    public static synchronized MockWebServerManager getInstance() {
        if (sInstance == null) {
            sInstance = new MockWebServerManager();
            sWeakRefListeners = new ArrayList<>();
            sDelivery = new Handler(Looper.getMainLooper());
        }
        return sInstance;
    }

    public void start() {
        initMockServer();
    }

    public void start(int port) {
        this.port = port;
        initMockServer();
    }

    public void stop() throws IOException {
        mMockWebServer.shutdown();
        mMockWebServer.close();
    }

    private void initMockServer() {
        try {
            mMockWebServer = new MockWebServer();
            InetAddress ipAddress = InetAddress.getByName("0.0.0.0");
            while (true) {
                Log.d(TAG, "initMockServer: sleep 1");
                Thread.sleep(1000);
                if (SettingUtil.getInstance().getSetting() != null)
                    break;
            }
            port = SettingUtil.getInstance().getSetting().getServerPort();
            connectWebSocketList = new ArrayList<MyWebSocket>();
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

    /**
     * @param msg 数据
     */
    public void send(final String msg) {
        for (MyWebSocket webSocket : connectWebSocketList) {
            webSocket.send(msg);
        }
    }

    public void sendToOthers(WebSocket mWebSocket, final String msg) {
        for (MyWebSocket webSocket : connectWebSocketList) {
            if (webSocket != mWebSocket) {
                webSocket.send(msg);
            }
        }
    }

    private Dispatcher dispatcher = new Dispatcher() {
        @Override
        public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
            String queryPath = request.getPath();
            if (queryPath.equals("/")) {
                try {

                    Message data = JSON.parseObject(request.getBody().readUtf8(), Message.class);
                    User user = new User(data.getUserID(), data.getIp());

                    Log.d(TAG, "dispatch: data :" + data.toString());

                    if (UserUtil.isInBlackList(user)) {
                        return new MockResponse().setResponseCode(400);
                    } else {
                        if (UserUtil.isInWhiteList(user)) {
                            // 已经是好友
//                                onWSDataChanged(DataType.DATA_RECEIVE, data);
//                                MyWebSocketManager.getInstance().onWSDataChanged(DataType.DATA_RECEIVE, data);
                            if (DataType.DATA_CONNECT == data.getAction()) {
//                                    onWSDataChanged(DataType.DATA_CONNECT, data);
                                MyWebSocketManager.getInstance().onWSDataChanged(DataType.DATA_CONNECT, data);
                                return new MockResponse().setBody(new Message(DataType.DATA_INFO, "已经是好友了请勿重复申请").toString()).setResponseCode(200);
                            }
                        } else {
                            // 不是好友
                            if (DataType.DATA_CONNECT == data.getAction()) {
//                                    onWSDataChanged(DataType.DATA_CONNECT, data);
                                MyWebSocketManager.getInstance().onWSDataChanged(DataType.DATA_CONNECT, data);
                                return new MockResponse().setBody(new Message(DataType.DATA_INFO, "申请成功").toString()).setResponseCode(200);
                            }
                        }

                    }
                } catch (Exception e) {
                    Log.e(TAG, "dispatch: " + e.getMessage() + "\n", e);
                }
            }
            if (queryPath.equals("/webSocket")) {
                return new MockResponse().withWebSocketUpgrade(serverWebSocketListener);
            } else if (queryPath.equals("/check")) {
                return new MockResponse().setResponseCode(200);
            }
            // 返回默认的响应;
            return new MockResponse().setResponseCode(404);
        }

    };
    private WebSocketListener serverWebSocketListener = new WebSocketListener() {
        @Override
        public void onClosing(@NotNull okhttp3.WebSocket webSocket, int code, @NotNull String reason) {
            super.onClosing(webSocket, code, reason);
        }

        @Override
        public void onFailure(@NotNull okhttp3.WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
            super.onFailure(webSocket, t, response);
            Log.e(TAG, "onFailure！" + t.getMessage());
        }

        @Override
        public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
            super.onOpen(webSocket, response);
            //有客户端连接时回调
            Log.e(TAG, "Server: Client Connect!");
//            webSocket.send("我是服务器，你好呀");
//            final String msg = Message.getConnectMessage().toString();
//            send(msg);

        }

        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
            super.onMessage(webSocket, text);
            Log.d(TAG + ":Debug", "服务器收到消息：" + text);


            try {
                Message data = JSON.parseObject(text, Message.class);
                User user = new User(data.getUserID(), data.getIp());
                if (DataType.DATA_CONNECT == data.getAction()) {
                    if (!UserUtil.isInWhiteList(user)) {
                        MyWebSocketManager.getInstance().onWSDataChanged(DataType.SERVER, data);
//                        webSocket.close(ErrorType.CONNECT_REFUSE, "连接已拒绝");
                        // 可能有bug
                    }
                    // 将连接加入到管理中
                    MyWebSocket tmp = new MyWebSocket(webSocket, RSAUtil.publicKeyFromString(data.getMsg()));
                    if (!connectWebSocketList.contains(tmp)) {
                        connectWebSocketList.add(tmp);
                    }
                    webSocket.send(Message.getConnectMessage().toString());
                }

            } catch (Exception e) {
                Log.e(TAG, "onMessage", e);
            }
        }

        @Override
        public void onMessage(@NonNull WebSocket webSocket, @NonNull ByteString bytes) {
            super.onMessage(webSocket, bytes);
            try {
                String text;
                text = new String(RSAUtil.decrypt(bytes.toByteArray(), SettingUtil.getInstance().getSetting().getPrivateKey()));

                Log.d(TAG, "onMessage(Byte): " + text);

                Message data = JSON.parseObject(text, Message.class);
                User user = new User(data.getUserID(), data.getIp());

                if (DataType.DATA_PING == data.getAction()) {
                    final String message = JSON.toJSONString(new Message(DataType.DATA_PING, "pong response"));
//                    webSocket.send(message);
                    for (int i = 0; i < connectWebSocketList.size(); i++) {
                        if (connectWebSocketList.get(i).getmWebSocket() == webSocket) {
                            connectWebSocketList.get(i).sendByEncrypt(new Message(DataType.DATA_PING, "ping").toString().getBytes(StandardCharsets.UTF_8));
                        }
                    }

                    return;
                }

                if (UserUtil.isInWhiteList(user)) {
                    if (DataType.DATA_PRIVATE == data.getAction()) {
                        
                        return;
                    }
                    if (DataType.DATA_GROUP == data.getAction()) {
                        // 群聊
                        /** 群里其他人同步信息 */
                        sendToOthers(webSocket, text);
                    }
                    MyWebSocketManager.getInstance().onWSDataChanged(DataType.SERVER, data);
//                        onWSDataChanged(DataType.DATA_RECEIVE, data);
                } else {

                }
            } catch (Exception e) {
                Log.e(TAG, "onMessage(Byte): " + e.getMessage(), e);
            }

        }

        @Override
        public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
            super.onClosed(webSocket, code, reason);
            connectWebSocketList.clear();
            Log.d(TAG, "服务器：onClosed：");
        }
    };
}

package xyz.fpointzero.android.network;

import android.content.ContentValues;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSON;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.litepal.LitePal;

import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import xyz.fpointzero.android.constants.DataType;
import xyz.fpointzero.android.constants.ConnectType;
import xyz.fpointzero.android.constants.Role;
import xyz.fpointzero.android.data.ChatMessage;
import xyz.fpointzero.android.data.Message;
import xyz.fpointzero.android.data.User;
import xyz.fpointzero.android.utils.crypto.MD5Util;
import xyz.fpointzero.android.utils.data.SettingUtil;
import xyz.fpointzero.android.utils.crypto.RSAUtil;
import xyz.fpointzero.android.utils.data.UserUtil;

public class MyWebSocket {
    public static final String TAG = "MyWebSocket";
    private OkHttpClient mClient;
    private WebSocket mWebSocket;
    private String wsURL;
    private PublicKey publicKey; // 服务器加密通信需要
    private boolean isReceivePong;
    Handler heartHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull android.os.Message msg) {
            if (msg.what != 10) return false;
            final String message = new Message(DataType.DATA_PING, "ping").toString();
            if (isReceivePong) {
                try {
                    sendByEncrypt(message.getBytes());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                isReceivePong = false;
                heartHandler.sendEmptyMessageDelayed(10, 20000);
            } else {
                //没有收到pong命令，进行重连
                disconnect(ConnectType.CONNECT_CLOSE, "断线重连");
            }
            return false;
        }
    });
    ;

    /**
     * 客户端创建webSocket
     *
     * @param url
     */
    public MyWebSocket(String url) {
        this.wsURL = url;
        Log.d(TAG, "mWbSocketUrl=" + this.wsURL);
        mClient = new OkHttpClient.Builder()
                .pingInterval(10, TimeUnit.SECONDS)
                .build();
        connect();
    }

    /**
     * 服务端的 webSocket
     *
     * @param webSocket
     * @param publicKey
     */
    public MyWebSocket(WebSocket webSocket, PublicKey publicKey) {
        mWebSocket = webSocket;
        isReceivePong = false;
        this.publicKey = publicKey;

    }

    public void connect() {
        Request request = new Request.Builder()
                .url(this.wsURL)
                .build();
        mWebSocket = mClient.newWebSocket(request, new WsListener());
//        MyWebSocketManager.getInstance().add(this);
    }

    /**
     * 发送消息
     *
     * @param message
     */
    public void send(final String message) {
        if (mWebSocket != null) {
            mWebSocket.send(message);
        }
    }

    /**
     * 发送消息
     *
     * @param message
     */
    public void send(final ByteString message) {
        if (mWebSocket != null) {
            mWebSocket.send(message);
        }
    }

    public void sendByEncrypt(final byte[] msg) throws Exception {
        if (publicKey != null) {
            ByteString message = new ByteString(RSAUtil.encrypt(msg, publicKey));
            send(message);
        } else
            throw new Exception("MyWebSocket's publicKey is null!!!!");
    }

    public void sendByEncrypt(int dataType, String msg) throws Exception {
        sendByEncrypt(new Message(DataType.DATA_PRIVATE, msg).toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 主动断开连接
     *
     * @param code
     * @param reason
     */
    public void disconnect(int code, String reason) {
        if (mWebSocket != null)
            mWebSocket.close(code, reason);
    }

    class WsListener extends WebSocketListener {
        @Override
        public void onClosed(@NotNull okhttp3.WebSocket webSocket, int code, @NotNull String reason) {
            super.onClosed(webSocket, code, reason);
            Log.e(TAG, "onClosed！");

            heartHandler.sendEmptyMessage(~10);
            Set<String> keys = ClientWebSocketManager.getClientWebSocketMap().keySet();
            for (String key : keys) {
                if (ClientWebSocketManager.getClientWebSocketMap().get(key).getmWebSocket() == webSocket) {
                    ClientWebSocketManager.getClientWebSocketMap().remove(key);
                    return;
                }
            }

        }

        @Override
        public void onClosing(@NotNull okhttp3.WebSocket webSocket, int code, @NotNull String reason) {
            super.onClosing(webSocket, code, reason);
        }

        @Override
        public void onFailure(@NotNull okhttp3.WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
            super.onFailure(webSocket, t, response);
            Log.e(TAG, "onFailure！" + t.getMessage());
            Set<String> keys = ClientWebSocketManager.getClientWebSocketMap().keySet();
            for (String key : keys) {
                if (ClientWebSocketManager.getClientWebSocketMap().get(key).getmWebSocket() == webSocket) {
                    ClientWebSocketManager.getClientWebSocketMap().remove(key);
                    return;
                }
            }
        }

        @Override
        public void onMessage(@NotNull okhttp3.WebSocket webSocket, @NotNull String text) {
            super.onMessage(webSocket, text);
            Log.e(TAG + ":Debug", "客户端收到消息:" + text);
            try {
                Message data = JSON.parseObject(text, Message.class);
                User user = new User(data.getUserID(), data.getUsername(), data.getIp());

                if (DataType.DATA_ERROR == data.getAction()) {
                    disconnect(ConnectType.CONNECT_REFUSE, "error");
                    return;
                }

                // 更新数据
                try {
                    user.save();
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("username", data.getUsername());
                    contentValues.put("ip", data.getIp());
                    LitePal.updateAll(User.class, contentValues, "userid = ?", data.getUserID());
                } catch (Exception e) {
                    Log.e(TAG, "onMessage: ", e);
                }

                // 存取对方公钥，发送心跳包保持连接存活
                if (DataType.DATA_CONNECT == data.getAction()) {
                    //主动发送心跳包
                    isReceivePong = true;
                    heartHandler.sendEmptyMessage(10);
                    publicKey = RSAUtil.publicKeyFromString(data.getMsg());
                    if (ClientWebSocketManager.getClientWebSocketMap().get(data.getUserID()) == null)
                        ClientWebSocketManager.getInstance().putIfAbsent(data.getUserID(), MyWebSocket.this);
                    else
                        webSocket.close(ConnectType.CONNECT_CLOSE, "重复的连接");
                    return;
                }

                // 加好友处理
                if (data.getAction() == DataType.DATA_ADD) {
                    onWSDataChanged(Role.CLIENT, data);
                }
            } catch (Exception e) {
                Log.e(TAG, "onMessage:", e);

            }
        }

        @Override
        public void onMessage(@NotNull okhttp3.WebSocket webSocket, @NotNull ByteString bytes) {
            super.onMessage(webSocket, bytes);
            try {
                String text = new String(RSAUtil.decrypt(bytes.toByteArray(), SettingUtil.getInstance().getSetting().getPrivateKey()));
                Log.d(TAG, "onMessage(Byte): " + text);

                Message data = JSON.parseObject(text, Message.class);
                User user = new User(data.getUserID(), data.getUsername(), data.getIp());
                if (DataType.DATA_PING == data.getAction()) {
                    isReceivePong = true;
                    return;
                }
                if (UserUtil.isInWhiteList(user)) {
                    if (DataType.DATA_PRIVATE == data.getAction()) {
                        new ChatMessage(user.getUserID(), true, data.getMsg(), System.currentTimeMillis()).save();
                    }
                    onWSDataChanged(Role.CLIENT, data);
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }

        @Override
        public void onOpen(@NotNull okhttp3.WebSocket webSocket, @NotNull Response response) {
            super.onOpen(webSocket, response);
            Log.e(TAG, "连接成功！");

            mWebSocket = webSocket;

            // 交换公钥
            final String msg = JSON.toJSONString(new Message(DataType.DATA_CONNECT, RSAUtil.publicKeyToString(SettingUtil.getInstance().getSetting().getPublicKey())));
            send(msg);
            Log.d(TAG, "onOpen: " + msg);
        }

    }

    @Override
    public boolean equals(@androidx.annotation.Nullable Object obj) {
        if (obj == null)
            return false;
        if (this == obj)
            return true;
        if (obj instanceof MyWebSocket) {
            MyWebSocket m = (MyWebSocket) obj;
            return m.mWebSocket == this.mWebSocket;
        }
        return false;
    }

    public WebSocket getmWebSocket() {
        return mWebSocket;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public String getWsURL() {
        return wsURL;
    }

    public String getUserID() {
        if (publicKey != null)
            return MD5Util.stringToMD5(RSAUtil.publicKeyToString(publicKey));
        return null;
    }
    
    private static void onWSDataChanged(int type, Message data) {
        ClientWebSocketManager.getInstance().onWSDataChanged(type, data);
    }
}

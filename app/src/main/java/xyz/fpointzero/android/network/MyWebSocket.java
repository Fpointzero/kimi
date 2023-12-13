package xyz.fpointzero.android.network;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSON;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.PublicKey;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import xyz.fpointzero.android.constants.Type;
import xyz.fpointzero.android.constants.ErrorType;
import xyz.fpointzero.android.utils.crypto.MD5Util;
import xyz.fpointzero.android.utils.data.SettingUtil;
import xyz.fpointzero.android.utils.crypto.RSAUtil;

public class MyWebSocket {
    public static final String TAG = "MyWebSocket";
    private OkHttpClient mClient;
    private WebSocket mWebSocket;
    private String wsURL;
    private PublicKey publicKey; // 服务器加密通信需要
    private boolean isReceivePong;

    /**
     * 客户端创建webSocket
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
        }
        else
            throw new Exception("MyWebSocket's publicKey is null!!!!");
    }

    Handler heartHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull android.os.Message msg) {
            if (msg.what != 10) return false;
            final String message = new xyz.fpointzero.android.network.Message(Type.DATA_PING, "ping").toString();
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
                disconnect(ErrorType.CONNECT_CLOSE, "断线重连");
            }
            return false;
        }
    });

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
            //断线重连
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
                xyz.fpointzero.android.network.Message data = JSON.parseObject(text, xyz.fpointzero.android.network.Message.class);
                if (Type.DATA_CONNECT == data.getAction()) {
                    //主动发送心跳包
                    isReceivePong = true;
                    heartHandler.sendEmptyMessage(10);
                    publicKey = RSAUtil.publicKeyFromString(data.getMsg());
                    if (ClientWebSocketManager.getClientWebSocketMap().get(data.getUserID()) == null)
                        ClientWebSocketManager.getInstance().putIfAbsent(data.getUserID(), MyWebSocket.this);
                    else
                        webSocket.close(ErrorType.CONNECT_CLOSE, "重复的连接");
                    return;
                }
                if (data.getAction() == Type.DATA_ADD) {
                    ClientWebSocketManager.getInstance().onWSDataChanged(Type.CLIENT, data);
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
                
                if (Type.DATA_PING == data.getAction()) {
                    isReceivePong = true;
                    return;
                }
                
                if (Type.DATA_ERROR == data.getAction()) {
                    webSocket.close(ErrorType.CONNECT_REFUSE, "error");
                    return;
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
            final String msg = JSON.toJSONString(new xyz.fpointzero.android.network.Message(Type.DATA_CONNECT, RSAUtil.publicKeyToString(SettingUtil.getInstance().getSetting().getPublicKey())));
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
}

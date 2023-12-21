package xyz.fpointzero.android.network;

import android.os.Handler;
import android.os.Looper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import okio.ByteString;
import xyz.fpointzero.android.constants.ConnectType;
import xyz.fpointzero.android.data.Message;


public class ClientWebSocketManager {
    private static final String TAG = "MyWebSocket";
    private static ClientWebSocketManager sInstance;
    private static Handler sDelivery;
    private static ArrayList<WeakReference<WebSocketDataListener>> sWeakRefListeners;
    //连接的websocket地址
    private static HashMap<String, MyWebSocket> clientWebSocketMap;
    private static List<MyWebSocket> tmpList;

    private ClientWebSocketManager() {
    }

    public synchronized static ClientWebSocketManager getInstance() {
        if (sInstance == null) {
            clientWebSocketMap = new HashMap<String, MyWebSocket>();
            sDelivery = new Handler(Looper.getMainLooper());
            sWeakRefListeners = new ArrayList<WeakReference<WebSocketDataListener>>();
            sInstance = new ClientWebSocketManager();
        }
        return sInstance;
    }

    public MyWebSocket getClientWS(String userID) {
        return clientWebSocketMap.get(userID);
    }

    /**
     * 创建webSocket
     *
     * @param url
     * @return
     */
    public MyWebSocket createClientWS(String url) {
        Set<String> keys = clientWebSocketMap.keySet();
        for (String key : keys) {
            if (clientWebSocketMap.get(key).getWsURL().equals(url)) {
                return clientWebSocketMap.get(key);
            }
        }
        MyWebSocket myWebSocket = new MyWebSocket(url);
        return myWebSocket;
    }

    public void closeAll() {
        Set<String> keys = clientWebSocketMap.keySet();
        for (String key : keys) {
            clientWebSocketMap.get(key).disconnect(ConnectType.CONNECT_CLOSE, "客户端已关闭");
        }
    }

    void put(String userID, MyWebSocket myWebSocket) {
        clientWebSocketMap.put(userID, myWebSocket);
    }

    void putIfAbsent(String userID, MyWebSocket myWebSocket) {
        clientWebSocketMap.putIfAbsent(userID, myWebSocket);
    }

    public void sendAll(final byte[] msg) {
        Set<String> keys = clientWebSocketMap.keySet();
        for (String key : keys) {
            clientWebSocketMap.get(key).send(new ByteString(msg));
        }
    }
    public void sendAll(final ByteString msg) {
        Set<String> keys = clientWebSocketMap.keySet();
        for (String key : keys) {
            clientWebSocketMap.get(key).send(msg);
        }
    }

    public void sendAllByEncrypt(final byte[] msg) throws Exception {
        Set<String> keys = clientWebSocketMap.keySet();
        for (String key : keys) {
            clientWebSocketMap.get(key).sendByEncrypt(msg);
        }
    }

    public void sendByEncrypt(String userID, final byte[] msg) throws Exception {
        clientWebSocketMap.get(userID).sendByEncrypt(msg);
    }

    /**
     * 遍历监听者，发送消息
     *
     * @param type
     * @param info
     */
    public void onWSDataChanged(int type, final Message info) {
        Iterator<WeakReference<WebSocketDataListener>> iterator = sWeakRefListeners.iterator();
        while (iterator.hasNext()) {
            WeakReference<WebSocketDataListener> ref = iterator.next();
            if (ref == null) {
                break;
            }
            final WebSocketDataListener listener = ref.get();
            if (listener == null) {
                iterator.remove();
            } else {
                // To fresh UI
                sDelivery.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onWebSocketData(type, info);
                    }
                });
            }
        }
    }


    /**
     * 注册监听者
     *
     * @param listener
     */
    public void registerWSDataListener(WebSocketDataListener listener) {
        if (!sWeakRefListeners.contains(listener)) {
            sWeakRefListeners.add(new WeakReference<>(listener));
        }
    }

    /**
     * 解绑监听
     *
     * @param listener
     */
    public void unregisterWSDataListener(WebSocketDataListener listener) {
        Iterator<WeakReference<WebSocketDataListener>> iterator = sWeakRefListeners.iterator();
        while (iterator.hasNext()) {
            WeakReference<WebSocketDataListener> ref = iterator.next();
            if (ref == null) {
                break;
            }
            if (ref.get() == null) {
                iterator.remove();
            }
            if (ref.get() == listener) {
                iterator.remove();
                break;
            }
        }
    }


    public interface WebSocketDataListener {
        void onWebSocketData(int type, Message info);
    }
    public static HashMap<String, MyWebSocket> getClientWebSocketMap() {
        return clientWebSocketMap;
    }
    
    public static Handler getsDelivery() {
        return sDelivery;
    }
}

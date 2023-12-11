package xyz.fpointzero.android.network;

import android.os.Handler;
import android.os.Looper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

import okio.ByteString;
import xyz.fpointzero.android.utils.data.UserUtil;


public class MyWebSocketManager {
    private static final String TAG = "MyWebSocket";
    private static MyWebSocketManager sInstance;
    private static Handler sDelivery;
    private static ArrayList<WeakReference<WebSocketDataListener>> sWeakRefListeners;
    //连接的websocket地址
    private static ArrayList<MyWebSocket> mWebSockets;
    
    private MyWebSocketManager(){}
    
    public MyWebSocket getClientWS(String url) {
        for (int i = 0; i < mWebSockets.size(); i++) {
            if (mWebSockets.get(i).getWsURL().equals(url)) {
                return mWebSockets.get(i);
            }
        }
        MyWebSocket myWebSocket = new MyWebSocket(url);
        return myWebSocket;
    }
    public void closeAll() {
        for (int i = 0; i < mWebSockets.size(); i++) {
            mWebSockets.get(i).disconnect(1000, "客户端已关闭");
        }
    }

    public synchronized static MyWebSocketManager getInstance() {
        if (sInstance == null) {
            mWebSockets = new ArrayList<MyWebSocket>();
            sDelivery = new Handler(Looper.getMainLooper());
            sWeakRefListeners = new ArrayList<WeakReference<WebSocketDataListener>>();
            sInstance = new MyWebSocketManager();
        }
        return sInstance;
    }
    
    void add(MyWebSocket myWebSocket) {
        if (!mWebSockets.contains(myWebSocket))
            mWebSockets.add(myWebSocket);
    }
    
    public void sendAll(final String msg) {
        for (int i = 0; i < mWebSockets.size(); i++) {
            mWebSockets.get(i).send(msg);
        }
    }

    public void sendAll(final ByteString msg) {
        for (int i = 0; i < mWebSockets.size(); i++) {
            mWebSockets.get(i).send(msg);
        }
    }
    
    public void sendAllByEncrypt(final byte[] msg) throws Exception {
        for (int i = 0; i < mWebSockets.size(); i++) {
            mWebSockets.get(i).sendByEncrypt(msg);
        }
    }

    public void sendByEncrypt(final byte[] msg, String userID) throws Exception {
        for (int i = 0; i < mWebSockets.size(); i++) {
            if(UserUtil.getUserID(mWebSockets.get(i).getPublicKey()).equals(userID)){
                mWebSockets.get(i).sendByEncrypt(msg);
                return;
            }
        }
    }

    /**
     * 遍历监听者，发送消息
     *
     * @param type
     * @param info
     */
    public void onWSDataChanged(int type, final xyz.fpointzero.android.network.Message info) {
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
        void onWebSocketData(int type, xyz.fpointzero.android.network.Message info);
    }

    public ArrayList<MyWebSocket> getmWebSockets() {
        return mWebSockets;
    }
}

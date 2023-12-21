package xyz.fpointzero.android.network;

import xyz.fpointzero.android.data.Message;

public interface WebSocketDataListener {
    void onWebSocketData(int type, Message info);
}
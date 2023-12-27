package xyz.fpointzero.android.network;

import android.app.IntentService;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import xyz.fpointzero.android.R;

public class MockServerService extends Service {
    public static final String TAG = "MockServerService";
    private static final Notification CHANNEL_ID = new Notification();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(() -> {
            // 创建前台通知
            MockWebServerManager.getInstance().start();
            if (MockWebServerManager.getInstance() == null)
                stopSelf();
        }).start();
        return super.onStartCommand(intent, flags, startId);
    }
}

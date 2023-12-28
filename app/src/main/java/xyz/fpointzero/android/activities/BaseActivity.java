package xyz.fpointzero.android.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import xyz.fpointzero.android.MainActivity;
import xyz.fpointzero.android.constants.DataType;
import xyz.fpointzero.android.data.Message;
import xyz.fpointzero.android.network.ClientWebSocketManager;
import xyz.fpointzero.android.network.WebSocketDataListener;
import xyz.fpointzero.android.utils.activity.ActivityUtil;
import xyz.fpointzero.android.utils.activity.NoticeUtil;

public abstract class BaseActivity extends AppCompatActivity {
    private static WebSocketDataListener backgroundListener;

    @Override
    protected void onRestart() {
        super.onRestart();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        ActivityUtil.getInstance().setNowActivity(this);
        if (backgroundListener == null) {
            backgroundListener = new WebSocketDataListener() {
                @Override
                public void onWebSocketData(int type, Message data) {
                    if (data.getAction() == DataType.DATA_PRIVATE) {
                        NoticeUtil.newMessageNotice(ActivityUtil.getInstance().getActivity(MainActivity.TAG), data.getUsername(), data.getMsg(), data.getUserID());
                    }
                }
            };
        }
        ClientWebSocketManager.getInstance().unregisterWSDataListener(backgroundListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (backgroundListener == null) {
            backgroundListener = new WebSocketDataListener() {
                @Override
                public void onWebSocketData(int type, Message data) {
                    if (data.getAction() == DataType.DATA_PRIVATE) {
                        NoticeUtil.newMessageNotice(ActivityUtil.getInstance().getActivity(MainActivity.TAG), data.getUsername(), data.getMsg(), data.getUserID());
                    }
                }
            };
        }
        ClientWebSocketManager.getInstance().registerWSDataListener(backgroundListener);
    }


}

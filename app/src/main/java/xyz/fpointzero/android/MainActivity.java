package xyz.fpointzero.android;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.fragment.app.FragmentTransaction;

import java.io.IOException;

import xyz.fpointzero.android.utils.activity.ActivityUtil;
import xyz.fpointzero.android.activities.BaseActivity;
import xyz.fpointzero.android.constants.Type;
import xyz.fpointzero.android.fragments.MessageFragment;
import xyz.fpointzero.android.network.Message;
import xyz.fpointzero.android.network.MockWebServerManager;
import xyz.fpointzero.android.network.MyWebSocketManager;
import xyz.fpointzero.android.utils.activity.DialogUtil;
import xyz.fpointzero.android.utils.data.SettingUtil;

public class MainActivity extends BaseActivity implements MyWebSocketManager.WebSocketDataListener {
    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityUtil.getInstance().getMap().put(TAG, this);
        // 初始化变量
        init();
    }

    private void init() {
        // 应用初始化
        SettingUtil.getInstance().initSetting(MainActivity.this);

        // 服务运行
        new Thread(() -> {
            MockWebServerManager.getInstance().start();
//            MockWebServerManager.getInstance().registerWSDataListener(serverSocketListener);
        }).start();

        // socket事件注册
        MyWebSocketManager.getInstance().registerWSDataListener(this);

        // 界面加载
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        ((TextView) findViewById(R.id.textview_title)).setText(R.string.navigation_message);
        transaction.replace(R.id.fragment_main_info, new MessageFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onWebSocketData(int type, Message info) {
        Log.d(TAG, "onWebSocketData: Type: " + type + " Receive data: " + info.toString());
        if (type == Type.SERVER) {
            if (info.getAction() == Type.DATA_CONNECT)
                DialogUtil.showConnectDialog(MainActivity.this, info);
        } else if (type == Type.CLIENT) {
            if (info.getAction() == Type.DATA_CONNECT)
                DialogUtil.showConnectDialog(MainActivity.this, info);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SettingUtil.getInstance().saveSetting(MainActivity.this);
        try {
            MyWebSocketManager.getInstance().closeAll();
            MockWebServerManager.getInstance().close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
package xyz.fpointzero.android;

import android.content.ContentValues;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.fragment.app.FragmentTransaction;

import org.litepal.LitePal;

import java.io.IOException;

import xyz.fpointzero.android.data.User;
import xyz.fpointzero.android.utils.activity.ActivityUtil;
import xyz.fpointzero.android.activities.BaseActivity;
import xyz.fpointzero.android.constants.DataType;
import xyz.fpointzero.android.fragments.MessageFragment;
import xyz.fpointzero.android.data.Message;
import xyz.fpointzero.android.network.MockWebServerManager;
import xyz.fpointzero.android.network.ClientWebSocketManager;
import xyz.fpointzero.android.utils.activity.DialogUtil;
import xyz.fpointzero.android.utils.data.SettingUtil;

public class MainActivity extends BaseActivity implements ClientWebSocketManager.WebSocketDataListener {
    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityUtil.getInstance().getMap().put(TAG, this);
        setSupportActionBar(findViewById(R.id.title_bar));
        
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
        ClientWebSocketManager.getInstance().registerWSDataListener(this);

        // 界面加载
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        ((TextView) findViewById(R.id.textview_title)).setText(R.string.navigation_message);
        transaction.replace(R.id.fragment_main_info, MessageFragment.getInstance());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onWebSocketData(int type, Message data) {
        Log.d(TAG, "onWebSocketData: Type: " + type + " Receive data: " + data.toString());
        if (type == DataType.SERVER) {
            if (data.getAction() == DataType.DATA_ADD && data.getMsg().equals("request"))
                DialogUtil.showConnectDialog(MainActivity.this, data);
        } else if (type == DataType.CLIENT) {
            if (data.getAction() == DataType.DATA_ADD && data.getMsg().equals("success")) {
                DialogUtil.showSuccessDialog(MainActivity.this, data.getUsername() + " (" + data.getUserID() +") 已同意");
                ContentValues contentValues = new ContentValues();
                contentValues.put("isWhite", "1");
                LitePal.updateAll(User.class, contentValues, "userid = ?", data.getUserID());
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SettingUtil.getInstance().saveSetting(MainActivity.this);
        try {
            ClientWebSocketManager.getInstance().closeAll();
            MockWebServerManager.getInstance().close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
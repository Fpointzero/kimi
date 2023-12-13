package xyz.fpointzero.android.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import java.nio.charset.StandardCharsets;

import xyz.fpointzero.android.R;
import xyz.fpointzero.android.constants.Type;
import xyz.fpointzero.android.layout.TitleChildBar;
import xyz.fpointzero.android.network.Message;
import xyz.fpointzero.android.network.MyWebSocket;
import xyz.fpointzero.android.network.ClientWebSocketManager;
import xyz.fpointzero.android.utils.activity.ActivityUtil;

public class AddFriendActivity extends BaseActivity implements View.OnClickListener {
    public final static String TAG = "AddFriendActivity";
    private Button btnAddFriend;
    private EditText edtextAddIp;
    private TitleChildBar titleBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        ActivityUtil.getInstance().getMap().put(TAG, this);
        
        Toolbar toolbar = findViewById(R.id.title_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        edtextAddIp = findViewById(R.id.edtext_add_ip);
        btnAddFriend = (Button) findViewById(R.id.btn_add_friend);
        btnAddFriend.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        try {
            String ipAndPort = edtextAddIp.getText().toString();
            String url = "ws://";
            url += ipAndPort.contains(":") ? ipAndPort : ipAndPort + ":10808";
            url += "/webSocket";

            String finalUrl = url;
            MyWebSocket myWebSocket = ClientWebSocketManager.getInstance().createClientWS(finalUrl);
            myWebSocket.send(new Message(Type.DATA_ADD, "request").toString());
            
        } catch (Exception e) {
            Log.e(TAG, "onClick: " + e.getMessage(), e);
        }
    }
    
}
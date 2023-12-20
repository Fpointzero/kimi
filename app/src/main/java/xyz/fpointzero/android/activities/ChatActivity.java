package xyz.fpointzero.android.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.litepal.LitePal;

import java.util.List;

import xyz.fpointzero.android.R;
import xyz.fpointzero.android.adapters.ChatMessageAdapter;
import xyz.fpointzero.android.data.ChatMessage;
import xyz.fpointzero.android.fragments.ContactFragment;
import xyz.fpointzero.android.layout.TitleChildBar;
import xyz.fpointzero.android.network.ClientWebSocketManager;
import xyz.fpointzero.android.network.MockWebServerManager;
import xyz.fpointzero.android.network.MyWebSocket;
import xyz.fpointzero.android.utils.activity.ActivityUtil;
import xyz.fpointzero.android.utils.data.UserUtil;

public class ChatActivity extends BaseActivity {
    public static final String TAG = "ChatActivity";
    private List<ChatMessage> chatMessageList;
    private ChatMessageAdapter chatMessageAdapter;
    String userID;
    String username;
    String ip;
    TextView tvUsername;
    TextView tvStatus;
    MyWebSocket socket;

    @SuppressLint("UseSupportActionBar")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ActivityUtil.getInstance().getMap().put(TAG, this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        Toolbar toolbar = findViewById(R.id.title_bar);
//        setActionBar(toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        init();
    }

    @SuppressLint("ResourceAsColor")
    private void init() {
        Intent intent = getIntent();
        try {
            // init data
            Bundle bundle = intent.getExtras();
            userID = bundle.getString("userID");
            username = bundle.getString("username");
            ip = bundle.getString("ip");
            chatMessageList = LitePal.where("sender = ? or receiver = ?", userID, userID).find(ChatMessage.class);
            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.activity_chat_recyclerview);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(linearLayoutManager);
            chatMessageAdapter = new ChatMessageAdapter(chatMessageList);
            recyclerView.setAdapter(chatMessageAdapter);
            // init view
//            TitleChildBar titleBar = findViewById(R.id.activity_chat_title);
//            titleBar.setTitle(username);
            tvUsername = findViewById(R.id.tv_username);
            tvStatus = findViewById(R.id.tv_status);
            tvUsername.setText(username);

            // 判断是否在线 TODO:动态刷新
            MyWebSocket socket1 = MockWebServerManager.getInstance().getServerWS(userID);
            MyWebSocket socket2 = ClientWebSocketManager.getInstance().getClientWS(userID);
            if (socket1 != null)
                socket = socket1;
            else if (socket2 != null)
                socket = socket2;
            else
                socket = null;

            if (socket == null) {
                tvStatus.setTextColor(R.color.red);
                tvStatus.setText("offline");
            } else {
                tvStatus.setTextColor(R.color.green);
                tvStatus.setText("online");
            }
        } catch (Exception e) {
            Log.e(TAG, "initData: " + e.getMessage(), e);
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.title_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemID = item.getItemId();
        if (itemID == android.R.id.home) {
            onBackPressed();
        } else if (itemID == R.id.option_remove) {
            UserUtil.removeWhiteList(userID);
            finish();
            ContactFragment.flushContactList();
        } else if (itemID == R.id.option_connect) {
            if (socket == null) {
                // TODO:修改内容
                new Thread(() -> {
                    ClientWebSocketManager.getInstance().createClientWS(String.format("ws://%s/webSocket", ip));
                    runOnUiThread(() -> {
                        Toast.makeText(ChatActivity.this, "连接", Toast.LENGTH_SHORT).show();
                    }); 
                }).start();
            }
        }
        return true;
    }

}
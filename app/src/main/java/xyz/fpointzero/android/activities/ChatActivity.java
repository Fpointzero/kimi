package xyz.fpointzero.android.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.litepal.LitePal;

import java.nio.charset.StandardCharsets;
import java.util.List;

import xyz.fpointzero.android.R;
import xyz.fpointzero.android.adapters.ChatMessageAdapter;
import xyz.fpointzero.android.constants.ConnectType;
import xyz.fpointzero.android.constants.DataType;
import xyz.fpointzero.android.data.ChatMessage;
import xyz.fpointzero.android.fragments.ContactFragment;
import xyz.fpointzero.android.network.ClientWebSocketManager;
import xyz.fpointzero.android.network.Message;
import xyz.fpointzero.android.network.MockWebServerManager;
import xyz.fpointzero.android.network.MyWebSocket;
import xyz.fpointzero.android.utils.activity.ActivityUtil;
import xyz.fpointzero.android.utils.data.SettingUtil;
import xyz.fpointzero.android.utils.data.UserUtil;

public class ChatActivity extends BaseActivity implements View.OnClickListener, View.OnFocusChangeListener {
    public static final String TAG = "ChatActivity";
    private List<ChatMessage> chatMessageList;
    private ChatMessageAdapter chatMessageAdapter;

    Thread flushThread;
    boolean isStop = false;
    String userID;
    String username;
    String ip;
    TextView tvUsername;
    TextView tvStatus;
    MyWebSocket socket;
    RecyclerView recyclerView;
    ImageView uploadImg;
    ImageView btnSend;
    EditText input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ActivityUtil.getInstance().getMap().put(TAG, this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = findViewById(R.id.title_bar);
//        setActionBar(toolbar);
        // 初始化标题栏
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // 初始化数据
        init();

        // 注册事件
        input = findViewById(R.id.input);
        input.setOnFocusChangeListener(this);

        uploadImg = findViewById(R.id.upload_image);
        btnSend = findViewById(R.id.send);
        uploadImg.setOnClickListener(this);
        btnSend.setOnClickListener(this);

        // 动态更新线程
        flushThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(2000);
                    chatMessageList = LitePal.where("userid = ?", userID).find(ChatMessage.class);
                    chatMessageAdapter.setChatMsgList(chatMessageList);
                    runOnUiThread(() -> {
                        flushStatus();
                        chatMessageAdapter.notifyDataSetChanged();
                    });
                    if (isStop) 
                        return;
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        flushThread.start();
        
        // TODO:来新消息提示
    }

    private void init() {
        Intent intent = getIntent();
        try {
            // init data
            Bundle bundle = intent.getExtras();
            userID = bundle.getString("userID");
            username = bundle.getString("username");
            ip = bundle.getString("ip");
            recyclerView = (RecyclerView) findViewById(R.id.activity_chat_recyclerview);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(linearLayoutManager);

            chatMessageList = LitePal.where("userid = ?", userID).order("timestamp").find(ChatMessage.class);
            chatMessageAdapter = new ChatMessageAdapter(chatMessageList);
            recyclerView.setAdapter(chatMessageAdapter);
            // init view
//            TitleChildBar titleBar = findViewById(R.id.activity_chat_title);
//            titleBar.setTitle(username);
            tvUsername = findViewById(R.id.tv_username);
            tvStatus = findViewById(R.id.tv_status);
            tvUsername.setText(username);

            flushStatus();
            recyclerView.scrollToPosition(chatMessageList.size() - 1); // 滚动到最底部
        } catch (Exception e) {
            Log.e(TAG, "initData: " + e.getMessage(), e);
            finish();
        }
    }

    @SuppressLint("ResourceAsColor")
    private void flushStatus() {
        // 判断是否在线 
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
            tvStatus.setText("离线");
        } else {
            tvStatus.setTextColor(R.color.green);
            tvStatus.setText("在线");
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
            flushStatus();
            if (socket == null) {
                new Thread(() -> {
                    ClientWebSocketManager.getInstance().createClientWS(String.format("ws://%s/webSocket", ip));
                    runOnUiThread(() -> {
                        Toast.makeText(ChatActivity.this, "连接", Toast.LENGTH_SHORT).show();
                    });
                }).start();
            }
        } else if (itemID == R.id.option_disconnect) {
            flushStatus();
            if (socket != null) {
                socket.disconnect(ConnectType.CONNECT_CLOSE, "主动断开");
            }
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        flushStatus();
        if (socket == null)
            return;
        if (id == R.id.upload_image) {

        } else if (id == R.id.send) {
            Toast.makeText(this, "发送成功", Toast.LENGTH_SHORT).show();
            try {
                String msg = input.getText().toString();
                if (!"".equals(msg)) {
                    new ChatMessage(userID, false, msg, System.currentTimeMillis()).save();
                    socket.sendByEncrypt(new Message(DataType.DATA_PRIVATE, msg).toString().getBytes(StandardCharsets.UTF_8));
                    chatMessageList = LitePal.where("userid = ?", userID).find(ChatMessage.class);
                    chatMessageAdapter.setChatMsgList(chatMessageList);
                    chatMessageAdapter.notifyDataSetChanged();
                    input.setText("");
                    recyclerView.scrollToPosition(chatMessageList.size() - 1);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        int id = v.getId();
        if (id == R.id.input) {
            if (hasFocus) {
                uploadImg.setVisibility(View.GONE);
                btnSend.setVisibility(View.VISIBLE);
            } else {
                btnSend.setVisibility(View.GONE);
                uploadImg.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isStop = true;
    }
}
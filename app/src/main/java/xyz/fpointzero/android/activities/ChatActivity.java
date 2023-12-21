package xyz.fpointzero.android.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import xyz.fpointzero.android.data.User;
import xyz.fpointzero.android.network.ClientWebSocketManager;
import xyz.fpointzero.android.data.Message;
import xyz.fpointzero.android.network.MockWebServerManager;
import xyz.fpointzero.android.network.MyWebSocket;
import xyz.fpointzero.android.utils.activity.ActivityUtil;
import xyz.fpointzero.android.utils.data.UserUtil;

public class ChatActivity extends BaseActivity implements View.OnClickListener, View.OnFocusChangeListener {
    public static final String TAG = "ChatActivity";
    // core
    Thread flushThread;
    boolean isStop = false;
    MyWebSocket socket;
    long timestamp;
    boolean isUserScroll = false;

    // user data
    User user;
    String userID;
    String username;
    String ip;
    int msgId = -1;

    // title
    TextView tvUsername;
    TextView tvStatus;
    
    // recyclerView
    List<ChatMessage> chatMessageList;
    ChatMessageAdapter chatMessageAdapter;
    RecyclerView recyclerView;

    // bottom
    ImageView uploadImg;
    ImageView btnSend;
    EditText input;
    ViewGroup root;
    LinearLayout newMsgNotice;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ActivityUtil.getInstance().getMap().put(TAG, this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = findViewById(R.id.title_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // 初始化数据
        init();

        // 初始化标题
        tvUsername = findViewById(R.id.tv_username);
        tvStatus = findViewById(R.id.tv_status);
        tvUsername.setText(username);
        flushStatus();
        if (socket == null) {
            tvStatus.setTextColor(R.color.red);
            tvStatus.setText("离线");
        } else {
            tvStatus.setTextColor(R.color.green);
            tvStatus.setText("在线");
        }

        // 初始化listview
        recyclerView = (RecyclerView) findViewById(R.id.activity_chat_recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        
        chatMessageAdapter = new ChatMessageAdapter();
        initRecyclerViewData();
        
        recyclerView.setAdapter(chatMessageAdapter);
//        初始化滚动
        locateId();

        // 初始化底栏
        input = findViewById(R.id.input);
        newMsgNotice = findViewById(R.id.new_msg_notice);
        try {
            timestamp = chatMessageList.get(0).getTimestamp();
        } catch (Exception e) {
            timestamp = 0L;
        }
        

        // 注册事件
        /* 切换发送消息和发送图片功能 */
        input.setOnFocusChangeListener(this);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
//                Log.d(TAG, "onScrollStateChanged: " + newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    // RecyclerView 停止滚动
                } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    // 用户手指拖动 RecyclerView
                    isUserScroll = true;
                    if (input.isFocused()) {
                        input.clearFocus();
                        hideSoftKeyboard(input);
                    }
                } else if (newState == RecyclerView.SCROLL_STATE_SETTLING) {
                    // RecyclerView 正在自动滚动
                }


            }
        });

        uploadImg = findViewById(R.id.upload_image);
        btnSend = findViewById(R.id.send);
        uploadImg.setOnClickListener(this);
        btnSend.setOnClickListener(this);
        newMsgNotice.setOnClickListener(this);

        // 动态更新线程
        flushThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(2000);
                    runOnUiThread(() -> {
                        // 状态更新
                        flushStatus();
                        if (socket == null) {
                            tvStatus.setTextColor(R.color.red);
                            tvStatus.setText("离线");
                        } else {
                            tvStatus.setTextColor(R.color.green);
                            tvStatus.setText("在线");
                        }
                        
                        // 消息更新
                        initRecyclerViewData();
                        chatMessageAdapter.notifyDataSetChanged();
                        
                        // 行为处理
                        long now;
                        try {
                            now = chatMessageList.get(0).getTimestamp();
                        } catch (Exception e) {
                            now = 0L;
                        }
                        
                        if (now > timestamp) {
                            timestamp = now;
                            if (isUserScroll)
                                newMsgNotice.setVisibility(View.VISIBLE);
                            else {
                                recyclerView.smoothScrollToPosition(0);
                            }
                        }
                    });
                    if (isStop)
                        return;
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        flushThread.start();
    }
    
    private void initRecyclerViewData() {
        chatMessageList = LitePal.where("userid = ?", userID).order("timestamp DESC").find(ChatMessage.class);
        chatMessageAdapter.setChatMsgList(chatMessageList);
//        chatMessageAdapter = new ChatMessageAdapter(chatMessageList);
//        recyclerView.setAdapter();
    }

    private void hideSoftKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void init() {
        Intent intent = getIntent();
        try {
            // 初始化数据
            Bundle bundle = intent.getExtras();
            userID = bundle.getString("userID");
            msgId = bundle.getInt("id");
            user = LitePal.where("userid = ?", userID).find(User.class).get(0);
            username = user.getUsername();
            ip = user.getIp();
        } catch (Exception e) {
            Log.e(TAG, "init: " + e.getMessage(), e);
            finish();
        }
    }
    
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
            if (socket != null) {
                socket.disconnect(ConnectType.CONNECT_CLOSE, "主动断开");
            }
            finish();
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
        if (socket != null) {
            if (id == R.id.upload_image) {

            } else if (id == R.id.send) {
                Toast.makeText(this, "发送成功", Toast.LENGTH_SHORT).show();
                try {
                    String msg = input.getText().toString();
                    if (!"".equals(msg)) {
                        new ChatMessage(userID, false, msg, System.currentTimeMillis()).save();
                        socket.sendByEncrypt(DataType.DATA_PRIVATE, msg);
                        initRecyclerViewData();
                        chatMessageAdapter.notifyDataSetChanged();
                        input.setText("");

                        // 移动到最底部
                        recyclerView.scrollToPosition(0);
                        timestamp = chatMessageList.get(0).getTimestamp();
                        isUserScroll = false;
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        if (id == R.id.new_msg_notice) {
            newMsgNotice.setVisibility(View.GONE);
            recyclerView.scrollToPosition(0);
            isUserScroll = false;
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
    
    private void locateId() {
        if (msgId == -1)
            return;
        for (int i = 0; i < chatMessageList.size(); i++) {
            if(chatMessageList.get(i).getId() == msgId) {
                recyclerView.scrollToPosition(i);
                return;
            }
        }
    }
}
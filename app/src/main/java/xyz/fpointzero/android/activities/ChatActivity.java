package xyz.fpointzero.android.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Picture;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import xyz.fpointzero.android.network.WebSocketDataListener;
import xyz.fpointzero.android.utils.activity.ActivityUtil;
import xyz.fpointzero.android.utils.activity.DialogUtil;
import xyz.fpointzero.android.utils.data.FileUtil;
import xyz.fpointzero.android.utils.data.SerializationUtil;
import xyz.fpointzero.android.utils.data.UserUtil;

public class ChatActivity extends BaseActivity implements View.OnClickListener, View.OnFocusChangeListener, WebSocketDataListener {
    public static final String TAG = "ChatActivity";
    public static final int CHOOSE_PHOTO = 3;
    // core
    Thread flushThread;
    boolean isStop = false;
    MyWebSocket socket;
    long timestamp;
    boolean isUserScroll = false;
    Bitmap bitmap; // 图片的bitmap

    // user data
    User user;
    String userID;
    int msgId = -1;

    // title
    TextView tvUsername;
    TextView tvStatus;
    ImageView imageStatus;
    Menu menu;

    // recyclerView
    List<ChatMessage> chatMessageList;
    ChatMessageAdapter chatMessageAdapter;
    RecyclerView recyclerView;

    // bottom
    ImageView btnImageUpload;
    ImageView btnSend;
    EditText input;
    ViewGroup root;
    LinearLayout newMsgNotice;

    // ======================================初始化函数======================================
    private void init() {
        Intent intent = getIntent();
        try {
            // 初始化数据
            Bundle bundle = intent.getExtras();
            userID = bundle.getString("userID");
            msgId = bundle.getInt("id");
            user = LitePal.where("userid = ?", userID).find(User.class).get(0);
        } catch (Exception e) {
            Log.e(TAG, "init: " + e.getMessage(), e);
            finish();
        }
    }

    @SuppressLint("ResourceAsColor")
    private void initView() {
// 初始化标题
        tvUsername = findViewById(R.id.tv_username);
        tvStatus = findViewById(R.id.tv_status);
        imageStatus = findViewById(R.id.img_status);
        tvUsername.setText(user.getUsername());

        flushStatusView();

        // 初始化listview
        recyclerView = (RecyclerView) findViewById(R.id.activity_chat_recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        chatMessageAdapter = new ChatMessageAdapter();
        initRecyclerViewData();

        recyclerView.setAdapter(chatMessageAdapter);
//        初始化滚动
        initRecyclerViewLocationByMsgId();

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

                    // 取消editText聚焦并且关闭手机虚拟键盘
                    if (input.isFocused()) {
                        input.clearFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                    }
                } else if (newState == RecyclerView.SCROLL_STATE_SETTLING) {
                    // RecyclerView 正在自动滚动
                }


            }
        });

        btnImageUpload = findViewById(R.id.upload_image);
        btnSend = findViewById(R.id.send);
        btnImageUpload.setOnClickListener(this);
        btnSend.setOnClickListener(this);
        newMsgNotice.setOnClickListener(this);
    }

    private void initRecyclerViewData() {
        chatMessageList = LitePal.where("userid = ?", userID).order("timestamp DESC").find(ChatMessage.class);
        chatMessageAdapter.setChatMsgList(chatMessageList);
//        chatMessageAdapter = new ChatMessageAdapter(chatMessageList);
//        recyclerView.setAdapter();
    }

    private void initRecyclerViewLocationByMsgId() {
        if (msgId == -1)
            return;
        for (int i = 0; i < chatMessageList.size(); i++) {
            if (chatMessageList.get(i).getId() == msgId) {
                recyclerView.scrollToPosition(i);
                return;
            }
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

    private void flushStatusView() {
        flushStatus();
        user = LitePal.where("userid = ?", userID).find(User.class).get(0);
        if (socket == null) {
            int color = ContextCompat.getColor(this, R.color.grey);
            tvStatus.setTextColor(color);
            tvStatus.setText("离线");
            imageStatus.setImageResource(R.drawable.offline);
//            menu.findItem(R.id.option_disconnect).setVisible(false);
        } else {
            int color = ContextCompat.getColor(this, R.color.green);
            tvStatus.setTextColor(color);
            tvStatus.setText("在线");
            imageStatus.setImageResource(R.drawable.online);
//            menu.findItem(R.id.option_connect).setVisible(false);
        }

        if (menu != null) {
            menu.findItem(R.id.option_connect).setVisible(socket == null);
            menu.findItem(R.id.option_disconnect).setVisible(socket != null);
            menu.findItem(R.id.option_remove).setVisible(user.isWhite());
            menu.findItem(R.id.option_add_friend).setVisible(!user.isWhite());
        }
    }

    // ======================================功能型方法======================================

    private void openAlbum() {
        if (socket != null) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, CHOOSE_PHOTO);
        } else
            Toast.makeText(this, "请先连接", Toast.LENGTH_SHORT).show();

    }

    @TargetApi(19)
    private void handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        Log.d("TAG", "handleImageOnKitKat: uri is " + uri);
        if (DocumentsContract.isDocumentUri(this, uri)) {
            // 如果是document类型的Uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1]; // 解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            imagePath = getImagePath(uri, null); // 如果是content类型的Uri，则使用普通方式处理
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            imagePath = uri.getPath(); // 如果是file类型的Uri，直接获取图片路径即可
        }
        sendImage(imagePath); // 根据图片路径显示图片
    }

    private void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        sendImage(imagePath);
    }

    @SuppressLint("Range")
    private String getImagePath(Uri uri, String selection) {
        String path = null;
        // 通过Uri和selection来获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private void sendImage(String imagePath) {
        if (imagePath != null) {
            try {
                bitmap = BitmapFactory.decodeFile(imagePath);
                String msg = SerializationUtil.serializeBitmapToBase64String(bitmap);
                Log.e(TAG, "sendImage: " + msg);
                ChatMessage chatMessage = new ChatMessage(user.getUserID(), false, true, null, System.currentTimeMillis());
                chatMessage.setMessage(userID + "/" + chatMessage.getTimestamp() + ".png");
                chatMessage.save();
                FileUtil.createNewImg(chatMessage.getMessage(), bitmap);
                new Thread(() -> {
                    socket.send(new Message(DataType.PRIVATE.IMAGE, msg).toString());
                    runOnUiThread(() -> {
                        Toast.makeText(this, "发送成功", Toast.LENGTH_SHORT).show();
                    });
                }).start();
                // 更新
                initRecyclerViewData();
                chatMessageAdapter.notifyDataSetChanged();
            } catch (Exception e) {
                Log.e(TAG, "sendImage: ", e);
                Toast.makeText(this, "failed to get image", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "failed to get image", Toast.LENGTH_SHORT).show();
        }
    }

    // ======================================重写的方法======================================
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    // 判断手机系统版本号
                    if (Build.VERSION.SDK_INT >= 19) {
                        // 4.4及以上系统使用这个方法处理图片
                        handleImageOnKitKat(data);
                    } else {
                        // 4.4以下系统使用这个方法处理图片
                        handleImageBeforeKitKat(data);
                    }
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.title_chat, menu);
        this.menu = menu;
        // 放这里继续更新一次ui是更新菜单栏，在onCreate里面调用会报错
        flushStatusView();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemID = item.getItemId();
        if (itemID == android.R.id.home) {
            onBackPressed();
        } else if (itemID == R.id.option_remove) {
            DialogUtil.showWarningDialog(this, "您确定要删除好友吗？", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    UserUtil.removeWhiteList(userID);
                    if (socket != null) {
                        socket.disconnect(ConnectType.CONNECT_CLOSE, "主动断开");
                    }
                    finish();
                }
            });
        } else if (itemID == R.id.option_connect) {
            flushStatus();
            if (socket == null) {
                new Thread(() -> {
                    String ipAndPort = user.getIp();
                    String url = "ws://";
                    url += ipAndPort.contains(":") ? ipAndPort : ipAndPort + ":10808";
                    url += "/webSocket";
                    ClientWebSocketManager.getInstance().createClientWS(url);
                    runOnUiThread(() -> {
                        Toast.makeText(ChatActivity.this, "连接" + user.getIp(), Toast.LENGTH_SHORT).show();
                    });
                }).start();
            }
        } else if (itemID == R.id.option_disconnect) {
            flushStatus();
            if (socket != null) {
                socket.disconnect(ConnectType.CONNECT_CLOSE, "主动断开");
                Toast.makeText(ChatActivity.this, "断开连接中", Toast.LENGTH_LONG).show();
            }
        } else if (itemID == R.id.option_change_ip) {
            DialogUtil.showEditIPDialog(this, user);
        } else if (itemID == R.id.option_add_friend) {
            try {
                String ipAndPort = user.getIp();
                String url = "ws://";
                url += ipAndPort.contains(":") ? ipAndPort : ipAndPort + ":10808";
                url += "/webSocket";

                String finalUrl = url;
                new Thread(() -> {
                    MyWebSocket myWebSocket = ClientWebSocketManager.getInstance().createClientWS(finalUrl);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    myWebSocket.send(new Message(DataType.DATA_ADD, "request").toString());
                }).start();


            } catch (Exception e) {
                Log.e(TAG, "onClick: " + e.getMessage(), e);
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                } else {
                    Toast.makeText(this, "You denied the permission.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        flushStatus();
        if (id == R.id.send) {
            if (socket == null) {
                Toast.makeText(this, "请先连接", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "发送成功", Toast.LENGTH_SHORT).show();
                try {
                    String msg = input.getText().toString();
                    if (!"".equals(msg)) {
                        new ChatMessage(userID, false, false, msg, System.currentTimeMillis()).save();
                        socket.sendByEncrypt(DataType.PRIVATE.NORMAL, msg);
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
        } else if (id == R.id.upload_image) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            } else {
                openAlbum();
            }
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        int id = v.getId();
        if (id == R.id.input) {
            if (hasFocus) {
                btnImageUpload.setVisibility(View.GONE);
                btnSend.setVisibility(View.VISIBLE);
            } else {
                btnSend.setVisibility(View.GONE);
                btnImageUpload.setVisibility(View.VISIBLE);
            }
        }
    }

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

        // 初始化视图
        initView();

        // 注册网络监听事件
        ClientWebSocketManager.getInstance().registerWSDataListener(this);

        // 动态更新线程
        flushThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(2000);
                    runOnUiThread(() -> {
                        // 状态更新
                        flushStatusView();

                        // 消息更新
//                        initRecyclerViewData();
//                        chatMessageAdapter.notifyDataSetChanged();

                        // 行为处理
//                        long now;
//                        try {
//                            now = chatMessageList.get(0).getTimestamp();
//                        } catch (Exception e) {
//                            now = 0L;
//                        }
//
//                        if (now > timestamp) {
//                            timestamp = now;
//                            if (isUserScroll)
//                                newMsgNotice.setVisibility(View.VISIBLE);
//                            else {
//                                recyclerView.smoothScrollToPosition(0);
//                            }
//                        }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isStop = true;
        ClientWebSocketManager.getInstance().unregisterWSDataListener(this);
    }

    @Override
    public void onWebSocketData(int type, Message data) {
        if (data.getUserID().equals(userID)) {
            if (data.getAction() == DataType.PRIVATE.CHECK && data.getMsg().equals(DataType.ERROR)) {
                Toast.makeText(this, "您不是对方的好友", Toast.LENGTH_SHORT).show();
                return;
            }
            initRecyclerViewData();
            chatMessageAdapter.notifyDataSetChanged();
            if (data.getAction() == DataType.PRIVATE.NORMAL || data.getAction() == DataType.PRIVATE.IMAGE) {
                if (isUserScroll)
                    newMsgNotice.setVisibility(View.VISIBLE);
                else {
                    recyclerView.smoothScrollToPosition(0);
                }
            }
        }
    }
}
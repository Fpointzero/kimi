package xyz.fpointzero.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.litepal.LitePal;

import java.util.List;

import xyz.fpointzero.android.R;
import xyz.fpointzero.android.adapters.ChatMessageAdapter;
import xyz.fpointzero.android.data.ChatMessage;
import xyz.fpointzero.android.layout.TitleChildBar;
import xyz.fpointzero.android.utils.activity.ActivityUtil;

public class ChatActivity extends BaseActivity {
    public static final String TAG = "ChatActivity";
    private List<ChatMessage> chatMessageList;
    private ChatMessageAdapter chatMessageAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ActivityUtil.getInstance().getMap().put(TAG, this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        init();
        
        // TODO: 检测聊天过程中的数据更新
    }
    
    private void init() {
        Intent intent = getIntent();
        try {
            // init data
            Bundle bundle = intent.getExtras();
            String userID = bundle.getString("userID");
            String username = bundle.getString("username");
            chatMessageList = LitePal.where("sender = ? or receiver = ?", userID, userID).find(ChatMessage.class);
            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.activity_chat_recyclerview);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(linearLayoutManager);
            chatMessageAdapter = new ChatMessageAdapter(chatMessageList);
            recyclerView.setAdapter(chatMessageAdapter);
            // init view
            TitleChildBar titleBar = findViewById(R.id.activity_chat_title);
            titleBar.setTitle(username);
        } catch (Exception e) {
            Log.e(TAG, "initData: " + e.getMessage(), e);
            finish();
        }
    }
    
}
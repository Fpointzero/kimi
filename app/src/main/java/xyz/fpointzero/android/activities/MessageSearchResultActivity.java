package xyz.fpointzero.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import xyz.fpointzero.android.R;
import xyz.fpointzero.android.adapters.MessageAdapter;
import xyz.fpointzero.android.data.Message;
import xyz.fpointzero.android.utils.activity.ActivityUtil;
import xyz.fpointzero.android.utils.data.MessageUtil;

public class MessageSearchResultActivity extends BaseActivity{
    public static final String TAG = "MessageSearchResultActivity";
    String search;
    // recyclerView
    List<Message> messageList;
    MessageAdapter messageAdapter;
    RecyclerView recyclerView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityUtil.getInstance().getMap().put(TAG, this);
        setContentView(R.layout.activity_msg_search_result);
        Toolbar toolbar = findViewById(R.id.title_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        
        // 处理intent数据
        init();

        // 初始化listview
        recyclerView = findViewById(R.id.recyclerview);
        messageList = MessageUtil.getAllMsgListBy(search);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        messageAdapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(messageAdapter);
    }
    
    private void init() {
        Intent intent = getIntent();
        try {
            // 初始化数据
            Bundle bundle = intent.getExtras();
            search = bundle.getString("search");
        } catch (Exception e) {
            Log.e(TAG, "init: " + e.getMessage(), e);
            finish();
        }
    }
}

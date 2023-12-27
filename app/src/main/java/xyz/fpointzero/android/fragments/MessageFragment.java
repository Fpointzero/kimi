package xyz.fpointzero.android.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import xyz.fpointzero.android.R;
import xyz.fpointzero.android.activities.MessageSearchResultActivity;
import xyz.fpointzero.android.adapters.MessageRecordAdapter;
import xyz.fpointzero.android.data.Message;
import xyz.fpointzero.android.data.MessageRecord;
import xyz.fpointzero.android.network.ClientWebSocketManager;
import xyz.fpointzero.android.network.WebSocketDataListener;

public class MessageFragment extends Fragment implements View.OnClickListener, WebSocketDataListener {
    private static MessageFragment instance;
    EditText etSearch;
    Button btnSearch;
    MessageRecordAdapter messageAdapter;
    List<MessageRecord> messageList;
    Thread flushThread;
    RecyclerView recyclerView;
    boolean isPause = false; // 管理线程是否暂停
    boolean isStop = false; // 管理线程是否停止
    int mode = 1; // 模式：1默认显示的聊天记录，2搜索聊天记录

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        flushThread = new Thread(() -> {
//            while (true) {
//                if (!isPause) {
//                    try {
//                        Thread.sleep(2000);
//                        requireActivity().runOnUiThread(() -> {
//                            messageList = MessageRecord.getMsgRecordList();
//                            if (messageAdapter != null) {
//                                messageAdapter.setMessageList(messageList);
//                                messageAdapter.notifyDataSetChanged();
//                            }
//                        });
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }
//                }
//                if (isStop)
//                    return;
//            }
//        });
//        flushThread.start();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, container, false);
        etSearch = view.findViewById(R.id.et_search);
        btnSearch = view.findViewById(R.id.btn_search);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview_message);

        // 列表生成
        messageList = MessageRecord.getMsgRecordList();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        messageAdapter = new MessageRecordAdapter(messageList);
        recyclerView.setAdapter(messageAdapter);

        // 事件注册
        btnSearch.setOnClickListener(this);

        return view;
    }

    public static MessageFragment getInstance() {
        if (instance == null) {
            instance = new MessageFragment();
        }
        return instance;
    }

    @Override
    public void onResume() {
        super.onResume();
        // 动态刷新
        isPause = false;
        ClientWebSocketManager.getInstance().registerWSDataListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_search) {
            search();
        }
    }

    private void search() {
        String input = etSearch.getText().toString();
        Intent intent = new Intent(getContext(), MessageSearchResultActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("search", input);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void onPause() {
        super.onPause();
        isPause = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isStop = true;
        ClientWebSocketManager.getInstance().unregisterWSDataListener(this);
    }

    @Override
    public void onWebSocketData(int type, Message info) {
        messageList = MessageRecord.getMsgRecordList();
        messageAdapter.setMessageList(messageList);
        messageAdapter.notifyDataSetChanged();
    }
}

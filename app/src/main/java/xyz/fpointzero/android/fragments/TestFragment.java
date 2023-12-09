package xyz.fpointzero.android.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alibaba.fastjson.JSON;

import java.nio.charset.StandardCharsets;

import xyz.fpointzero.android.R;
import xyz.fpointzero.android.constants.DataType;
import xyz.fpointzero.android.network.Message;
import xyz.fpointzero.android.network.MockWebServerManager;
import xyz.fpointzero.android.network.MyWebSocket;
import xyz.fpointzero.android.network.MyWebSocketManager;

public class TestFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_test, container, false);
        EditText editText = (EditText) view.findViewById(R.id.edtext_test_send);
        Button btnServerSend = (Button) view.findViewById(R.id.btn_test_server_send);
        Button btnClientSend = (Button) view.findViewById(R.id.btn_test_client_send);
        Button btnConnect = (Button) view.findViewById(R.id.btn_connect);


        btnConnect.setOnClickListener(v -> {
            new Thread(() -> {
                String input = editText.getText().toString();
                MyWebSocket myWebSocket = new MyWebSocket(input);
            }).start();
        });
        btnServerSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String msg;
                msg = JSON.toJSONString(new Message(DataType.DATA_PING, editText.getText().toString()));
                MockWebServerManager.getInstance().send(msg);
            }
        });
        btnClientSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String msg;
                msg = JSON.toJSONString(new Message(DataType.DATA_PING, editText.getText().toString()));
                try {
                    MyWebSocketManager.getInstance().sendAllByEncrypt(msg.getBytes(StandardCharsets.UTF_8));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }
        });

        return view;
    }
}

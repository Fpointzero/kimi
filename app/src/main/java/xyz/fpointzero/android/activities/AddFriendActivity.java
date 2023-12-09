package xyz.fpointzero.android.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import xyz.fpointzero.android.R;
import xyz.fpointzero.android.constants.DataType;
import xyz.fpointzero.android.network.Callback;
import xyz.fpointzero.android.network.Message;
import xyz.fpointzero.android.utils.activity.ActivityUtil;
import xyz.fpointzero.android.utils.activity.DialogUtil;
import xyz.fpointzero.android.utils.network.MessageUtil;

public class AddFriendActivity extends BaseActivity implements View.OnClickListener{
    private final static String TAG = "AddFriendActivity";
    private Button btnAddFriend;
    private EditText edtextAddIp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        ActivityUtil.getInstance().getMap().put(TAG, this);
        
        edtextAddIp = findViewById(R.id.edtext_add_ip);
        btnAddFriend = (Button)findViewById(R.id.btn_add_friend);
        btnAddFriend.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        try {
//            MessageUtil.sendMessage("http://" + edtextAddIp.getText().toString() + ":10808/", new Message("connect", "加好友"));
            MessageUtil.sendMessage(edtextAddIp.getText().toString(), new Message(DataType.DATA_CONNECT, "加好友"), new Callback() {
                @Override
                public void onSuccess(String response) {
                    DialogUtil.showSuccessDialog(AddFriendActivity.this, "发送请求成功");
                }

                @Override
                public void onError() {

                }
            });
        } catch (Exception e) {
            Log.e(TAG, "onClick: " + e.getMessage(), e);
            Log.e(TAG, "onClick: " + new Message(DataType.DATA_CONNECT, "加好友").toString());
        }
        Toast.makeText(this, "打完加好友哦", Toast.LENGTH_SHORT).show();
    }
}
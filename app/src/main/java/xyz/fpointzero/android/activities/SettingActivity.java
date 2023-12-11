package xyz.fpointzero.android.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;

import xyz.fpointzero.android.R;
import xyz.fpointzero.android.utils.activity.ActivityUtil;
import xyz.fpointzero.android.utils.data.SettingUtil;

public class SettingActivity extends BaseActivity implements View.OnClickListener {
    public static final String TAG = "SettingActivity";
    private EditText username;
    private EditText port;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ActivityUtil.getInstance().getMap().put(TAG, this);

        username = findViewById(R.id.edText_username);
        port = findViewById(R.id.edText_port);
        Button btnSave = findViewById(R.id.btn_save);

        username.setText(SettingUtil.getInstance().getSetting().getUsername());
        port.setText(String.valueOf(SettingUtil.getInstance().getSetting().getServerPort()));
        btnSave.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        try {
            SettingUtil.getInstance().getSetting().setUsername(username.getText().toString());
            SettingUtil.getInstance().getSetting().setServerPort(Integer.parseInt(port.getText().toString()));
            SettingUtil.getInstance().saveSetting(SettingActivity.this);
            finish();
            Toast.makeText(this, "保存成功，端口修改请重启应用后生效", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "onClick: ", e);
        }
    }
}

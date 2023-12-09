package xyz.fpointzero.android.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import xyz.fpointzero.android.utils.activity.ActivityUtil;

public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onRestart() {
        super.onRestart();
        ActivityUtil.getInstance().setNowActivity(this);
    }
}

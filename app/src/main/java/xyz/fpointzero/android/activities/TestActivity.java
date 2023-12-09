package xyz.fpointzero.android.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import xyz.fpointzero.android.R;
import xyz.fpointzero.android.fragments.MessageFragment;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_main_info, new MessageFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
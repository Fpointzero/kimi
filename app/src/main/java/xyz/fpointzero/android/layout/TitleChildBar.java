package xyz.fpointzero.android.layout;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import xyz.fpointzero.android.R;

public class TitleChildBar extends Toolbar {
    private ImageButton backButton;
    private TextView title;

    public TitleChildBar(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.title_child_bar, this, true);
        backButton = (ImageButton) findViewById(R.id.btn_back);
        backButton.setOnClickListener(v -> {
            ((Activity)(context)).onBackPressed();
        });
    }
    
    public TitleChildBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.title_child_bar, this, true);
        backButton = (ImageButton) findViewById(R.id.btn_back);
        title = (TextView) findViewById(R.id.textview_child_title); 
        
        backButton.setOnClickListener(v -> {
            ((Activity)(context)).onBackPressed();
        });

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TitleChildBar);
            String textValue = a.getString(R.styleable.TitleChildBar_titleValue);
            a.recycle();

            // 设置 TextView 的内容
            if (textValue != null) {
                title.setText(textValue);
            }
        }
    }
    
    public void setTitle(String text) {
        title.setText(text);
    }
}

package xyz.fpointzero.android.layout;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;

import xyz.fpointzero.android.R;
import xyz.fpointzero.android.activities.AddFriendActivity;
import xyz.fpointzero.android.activities.SettingActivity;

/**
 * MainActivity TitleBar
 */
public class TitleBar extends LinearLayout implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

    private ImageButton menuButton;
    private PopupMenu popupMenu;
    private TextView title;

    public TitleBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.title_bar, this, true);
        
        title = findViewById(R.id.textview_title);
        // 获取菜单按钮的引用
        menuButton = findViewById(R.id.menu_btn);

        // 设置菜单按钮的点击事件
        menuButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        String titleValue = title.getText().toString();
        // 显示菜单
        popupMenu = new PopupMenu(getContext(), menuButton);
        if (titleValue.equals(getResources().getString(R.string.navigation_message)))
            popupMenu.getMenuInflater().inflate(R.menu.title_menu_contact, popupMenu.getMenu());
        if (titleValue.equals(getResources().getString(R.string.navigation_contact)))
            popupMenu.getMenuInflater().inflate(R.menu.title_menu_contact, popupMenu.getMenu());
        if (titleValue.equals(getResources().getString(R.string.navigation_nearby)))
            popupMenu.getMenuInflater().inflate(R.menu.title_menu_contact, popupMenu.getMenu());
        if (titleValue.equals(getResources().getString(R.string.navigation_mine)))
            popupMenu.getMenuInflater().inflate(R.menu.title_menu_mine, popupMenu.getMenu());
        // 处理菜单的点击事件
        popupMenu.setOnMenuItemClickListener(TitleBar.this);

        // 显示菜单
        popupMenu.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int itemID = item.getItemId();
        if (itemID == R.id.option_add_friend) {
            getContext().startActivity(new Intent(getContext(), AddFriendActivity.class));
            return true;
        }
        if (itemID == R.id.option_setting) {
            getContext().startActivity(new Intent(getContext(), SettingActivity.class));
            return true;
        }
        return false;
    }

    public void setTitle(String text) {
        title.setText(text);
    }
    public void setTitle(int id) {
        title.setText(id);
    }
}

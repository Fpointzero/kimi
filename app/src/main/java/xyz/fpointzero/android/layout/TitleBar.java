package xyz.fpointzero.android.layout;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;

import xyz.fpointzero.android.R;
import xyz.fpointzero.android.activities.AddFriendActivity;

public class TitleBar extends LinearLayout {

    private ImageButton menuButton;

    public TitleBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.title_bar, this, true);

        // 获取菜单按钮的引用
        menuButton = findViewById(R.id.menu_btn);

        // 设置菜单按钮的点击事件
        menuButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 显示菜单
                PopupMenu popupMenu = new PopupMenu(getContext(), menuButton);
                popupMenu.getMenuInflater().inflate(R.menu.title_menu, popupMenu.getMenu());

                // 处理菜单的点击事件
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        // 根据菜单项的 ID 进行处理
                        int itemID = item.getItemId();
                        if (itemID == R.id.add_friend) {
//                            Toast.makeText(context, "", Toast.LENGTH_SHORT).show();
                            context.startActivity(new Intent(context, AddFriendActivity.class));
                            return true;
                        }
                        return false;
                    }
                });

                // 显示菜单
                popupMenu.show();
            }
        });
    }
}

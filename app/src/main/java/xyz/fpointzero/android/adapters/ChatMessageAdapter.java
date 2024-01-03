package xyz.fpointzero.android.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

import xyz.fpointzero.android.R;
import xyz.fpointzero.android.data.ChatMessage;
import xyz.fpointzero.android.utils.DateUtil;
import xyz.fpointzero.android.utils.data.FileUtil;
import xyz.fpointzero.android.utils.data.SerializationUtil;
import xyz.fpointzero.android.utils.data.SettingUtil;

/**
 * 聊天信息适配器
 */
public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.ViewHolder> {
    private List<ChatMessage> chatMsgList;

    public ChatMessageAdapter() {
    }

    public ChatMessageAdapter(List<ChatMessage> list) {
        chatMsgList = list;
    }

    public void setChatMsgList(List<ChatMessage> chatMsgList) {
        this.chatMsgList = chatMsgList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chatmessage, parent, false);
        final ChatMessageAdapter.ViewHolder holder = new ChatMessageAdapter.ViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatMessage chatMsg = chatMsgList.get(position);
        String pathname = "";
        Bitmap bitmap = null;
        if (chatMsg.isImg()) {
            // 加载图片
            pathname = FileUtil.getInternalStorageDir() + File.separator + chatMsg.getMessage();
            bitmap = BitmapFactory.decodeFile(pathname);
        }
        
        if (!chatMsg.isSend()) {
            holder.rightTime.setText(DateUtil.toYMD(chatMsg.getTimestamp()));
            holder.leftMsg.setVisibility(View.GONE);
            
            if (chatMsg.isImg()) {
                // 加载图片
                holder.rightImg.setImageBitmap(bitmap);
                holder.rightImg.setVisibility(View.VISIBLE);
                holder.rightContent.setVisibility(View.GONE);
                
            } else {
                holder.rightContent.setText(chatMsg.getMessage());
                holder.rightContent.setVisibility(View.VISIBLE);
                holder.rightImg.setVisibility(View.GONE);
            }
            holder.rightMsg.setVisibility(View.VISIBLE);
            
        } else {
            holder.leftTime.setText(DateUtil.toYMD(chatMsg.getTimestamp()));
            holder.rightMsg.setVisibility(View.GONE);
            
            if (chatMsg.isImg()) {
                holder.leftImg.setImageBitmap(bitmap);
                holder.leftImg.setVisibility(View.VISIBLE);
                holder.leftContent.setVisibility(View.GONE);
                
            } else {
                holder.leftContent.setText(chatMsg.getMessage());
                holder.leftContent.setVisibility(View.VISIBLE);
                holder.leftImg.setVisibility(View.GONE);
            }
            
            holder.leftMsg.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return chatMsgList.size();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        View chatMsgView;
        // receiver show
        LinearLayout leftMsg;
        TextView leftTime;
        TextView leftContent;
        ImageView leftImg;
        // sender show
        LinearLayout rightMsg;
        TextView rightTime;
        TextView rightContent;
        ImageView rightImg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            chatMsgView = itemView;

            leftMsg = itemView.findViewById(R.id.left_message);
            rightMsg = itemView.findViewById(R.id.right_message);

            leftTime = itemView.findViewById(R.id.left_time);
            rightTime = itemView.findViewById(R.id.right_time);

            leftContent = itemView.findViewById(R.id.left_content);
            rightContent = itemView.findViewById(R.id.right_content);

            leftImg = itemView.findViewById(R.id.left_image);
            rightImg = itemView.findViewById(R.id.right_image);
        }
    }

    /**
     * 解决复用导致长度不一致，因为消息是反着的recyclerView所以是getItemCount() - position - 1否则是position即可
     *
     * @param position position to query
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        return getItemCount() - position - 1;
    }
}

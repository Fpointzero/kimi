package xyz.fpointzero.android.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import xyz.fpointzero.android.R;
import xyz.fpointzero.android.data.ChatMessage;
import xyz.fpointzero.android.utils.DateUtil;
import xyz.fpointzero.android.utils.data.SettingUtil;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.ViewHolder> {
    private List<ChatMessage> chatMsgList;
    
    public ChatMessageAdapter(List<ChatMessage> list) {
        chatMsgList = list;
    }

    public void setChatMsgList(List<ChatMessage> chatMsgList) {
        this.chatMsgList = chatMsgList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_list, parent, false);
        final ChatMessageAdapter.ViewHolder holder = new ChatMessageAdapter.ViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatMessage chatMsg = chatMsgList.get(position);
        // TODO: 处理图像内容 还有处理
        if (chatMsg.getSender() != null && chatMsg.getSender().equals(SettingUtil.getInstance().getSetting().getUserID())) {
            holder.rightTime.setText(DateUtil.toYMD(chatMsg.getTimestamp()));
            holder.rightContent.setText(chatMsg.getMessage());
            
            holder.rightImg.setVisibility(View.GONE);
            holder.leftMsg.setVisibility(View.GONE);
            holder.rightMsg.setVisibility(View.VISIBLE);
        } else {
            holder.leftTime.setText(DateUtil.toYMD(chatMsg.getTimestamp()));
            holder.leftContent.setText(chatMsg.getMessage());

            holder.leftImg.setVisibility(View.GONE);
            holder.rightMsg.setVisibility(View.GONE);
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

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}

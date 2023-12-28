package xyz.fpointzero.android.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import xyz.fpointzero.android.R;
import xyz.fpointzero.android.activities.ChatActivity;
import xyz.fpointzero.android.data.MessageRecord;
import xyz.fpointzero.android.utils.data.SettingUtil;

public class MessageRecordAdapter extends RecyclerView.Adapter<MessageRecordAdapter.ViewHolder> {
    private List<MessageRecord> messageList;

    public MessageRecordAdapter(List<MessageRecord> messageList) {
        this.messageList = messageList;
    }

    public void setMessageList(List<MessageRecord> messageList) {
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_messagerecord, parent, false);
        final MessageRecordAdapter.ViewHolder holder = new MessageRecordAdapter.ViewHolder(view);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getBindingAdapterPosition();
                MessageRecord msg = messageList.get(position);
                Context context = holder.itemView.getContext();
                Intent intent = new Intent(context, ChatActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("id", msg.getId());
                bundle.putString("userID", msg.getUserID());
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MessageRecord msg = messageList.get(position);
        holder.username.setText(msg.getUsername());
        if (msg.isSend()) {
            if (!msg.isImg())
                holder.msg.setText(msg.getUsername() + ":" + msg.getMsg());
            else 
                holder.msg.setText(msg.getUsername() + ": [图片]");
        } else {
            if (!msg.isImg())
                holder.msg.setText(SettingUtil.getInstance().getSetting().getUsername() + ":" + msg.getMsg());
            else
                holder.msg.setText(SettingUtil.getInstance().getSetting().getUsername() + ": [图片]");
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView username;
        TextView msg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.tv_username);
            msg = itemView.findViewById(R.id.tv_msg);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}

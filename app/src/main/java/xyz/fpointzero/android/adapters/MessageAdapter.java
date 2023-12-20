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
import xyz.fpointzero.android.data.ChatMessage;
import xyz.fpointzero.android.data.User;
import xyz.fpointzero.android.network.Message;
import xyz.fpointzero.android.utils.data.UserUtil;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder>{
    private List<Message> messageList;
    public MessageAdapter(List<Message> messageList) {
        this.messageList = messageList;
    }

    public void setMessageList(List<Message> messageList) {
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        final MessageAdapter.ViewHolder holder = new MessageAdapter.ViewHolder(view);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getBindingAdapterPosition();
                Message msg = messageList.get(position);
                Context context = holder.itemView.getContext();
                Intent intent = new Intent(context, ChatActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("userID", msg.getUserID());
                bundle.putString("username", msg.getUsername());
                bundle.putString("ip", msg.getIp());
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message msg = messageList.get(position);
        holder.username.setText(msg.getUsername());
        holder.msg.setText(msg.getMsg());
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView username;
        TextView msg;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.tv_username);
            msg = itemView.findViewById(R.id.tv_msg);
        }
    }
}

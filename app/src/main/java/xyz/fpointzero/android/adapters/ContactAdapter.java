package xyz.fpointzero.android.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import xyz.fpointzero.android.R;
import xyz.fpointzero.android.activities.ChatActivity;
import xyz.fpointzero.android.data.User;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {
    private List<User> contactList;
    
    public ContactAdapter(List<User> contactList){
        this.contactList = contactList;
    }

    public void setContactList(List<User> contactList) {
        this.contactList = contactList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        
        
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getBindingAdapterPosition();
                User user = contactList.get(position);
                Context context = holder.itemView.getContext();
                Intent intent = new Intent(context, ChatActivity.class);
//                Intent intent = new Intent(context, TestActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("userID", user.getUserID());
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });
        return holder;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = contactList.get(position);
        holder.username.setText(user.getUsername());
        holder.userid.setText("(" + user.getUserID() + ")");
        holder.ip.setText(user.getIp());
        
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView avatar;
        TextView username;
        TextView userid;
        TextView ip;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            username = itemView.findViewById(R.id.tv_username);
            userid = itemView.findViewById(R.id.tv_id);
            ip = itemView.findViewById(R.id.tv_ip);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import xyz.fpointzero.android.MainActivity;
import xyz.fpointzero.android.R;
import xyz.fpointzero.android.activities.ChatActivity;
import xyz.fpointzero.android.activities.TestActivity;
import xyz.fpointzero.android.data.User;
import xyz.fpointzero.android.utils.activity.ActivityUtil;

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
                Context context = holder.contactID.getContext();
                Intent intent = new Intent(context, ChatActivity.class);
//                Intent intent = new Intent(context, TestActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("userID", user.getUserID());
                bundle.putString("username", user.getUsername());
                bundle.putString("ip", user.getIp());
                intent.putExtras(bundle);
                context.startActivity(intent);
                Toast.makeText(context, "click " + user.getUserID(), Toast.LENGTH_SHORT).show();
            }
        });
        return holder;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = contactList.get(position);
        holder.contactID.setText(user.getUsername() + " (" + user.getUserID() + ")");
        
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView contactImage;
        TextView contactID;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            contactImage = itemView.findViewById(R.id.image_contact);
            contactID = itemView.findViewById(R.id.textview_contact);
        }
    }
    
}

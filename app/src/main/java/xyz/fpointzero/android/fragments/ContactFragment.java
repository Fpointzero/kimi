package xyz.fpointzero.android.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import xyz.fpointzero.android.R;
import xyz.fpointzero.android.adapters.ContactAdapter;
import xyz.fpointzero.android.data.User;
import xyz.fpointzero.android.utils.data.UserUtil;

public class ContactFragment extends Fragment implements View.OnClickListener{
    public static final String TAG = "ContactFragment";
    private static List<User> contactList;
    private static ContactAdapter contactAdapter;
    private EditText etSearch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact, container, false);

        etSearch = view.findViewById(R.id.et_search);
        Button btnSearch = view.findViewById(R.id.btn_search);
        btnSearch.setOnClickListener(this);
        // 列表
        contactList = UserUtil.getWhiteList();
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_contact);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        contactAdapter = new ContactAdapter(contactList);
        recyclerView.setAdapter(contactAdapter);
        return view;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        flushContactList();
    }
    
    @SuppressLint("NotifyDataSetChanged")
    public static void flushContactList() {
        Log.d(TAG, "flushContactList: ");
        contactList = UserUtil.getWhiteList();
        contactAdapter.setContactList(contactList);
        contactAdapter.notifyDataSetChanged();
    }
    
    public void search() {
        contactList = UserUtil.getWhiteList(etSearch.getText().toString());
        contactAdapter.setContactList(contactList);
        contactAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        if (vid == R.id.btn_search) {
            search();
        }
    }
}

package xyz.fpointzero.android.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

public class ContactFragment extends Fragment {
    private List<User> contactList;
    private ContactAdapter contactAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact, container, false);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
//        contactList = ListUtil.getWhiteList();
        contactList = UserUtil.getWhiteList();
        if (contactAdapter == null) {
            RecyclerView recyclerView = (RecyclerView) requireActivity().findViewById(R.id.recycler_contact);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireActivity());
            recyclerView.setLayoutManager(linearLayoutManager);
            contactAdapter = new ContactAdapter(contactList);
            recyclerView.setAdapter(contactAdapter);
        }
    }
}

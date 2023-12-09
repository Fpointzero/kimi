package xyz.fpointzero.android.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import xyz.fpointzero.android.R;
import xyz.fpointzero.android.utils.data.SettingUtil;
import xyz.fpointzero.android.utils.network.NetworkUtil;

public class MineFragment extends Fragment {
    TextView ip;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine, container, false);
        TextView userID = view.findViewById(R.id.textview_userid);
        ip = view.findViewById(R.id.textview_ip);
        
        userID.setText(SettingUtil.getInstance().getSetting().getUserID());
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ip.setText(NetworkUtil.getDeviceIPv4Address());
//        Toast.makeText(requireActivity(), "Mine re", Toast.LENGTH_SHORT).show();
    }
}

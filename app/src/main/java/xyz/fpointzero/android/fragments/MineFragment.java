package xyz.fpointzero.android.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.w3c.dom.Text;

import xyz.fpointzero.android.R;
import xyz.fpointzero.android.utils.data.SettingUtil;
import xyz.fpointzero.android.utils.network.NetworkUtil;

public class MineFragment extends Fragment {
    private static MineFragment instance;
    TextView username;
    TextView ip;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine, container, false);
        TextView userID = view.findViewById(R.id.textview_userid);
        ip = view.findViewById(R.id.textview_ip);
        username = view.findViewById(R.id.textview_username);
        
        userID.setText(SettingUtil.getInstance().getSetting().getUserID());
        return view;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onResume() {
        super.onResume();
        ip.setText(NetworkUtil.getDeviceIPv4Address() + ":" + SettingUtil.getInstance().getSetting().getServerPort());
        username.setText(SettingUtil.getInstance().getSetting().getUsername());
//        Toast.makeText(requireActivity(), "Mine re", Toast.LENGTH_SHORT).show();
    }

    public static MineFragment getInstance() {
        if (instance == null)
            instance = new MineFragment();
        return instance;
    }
}

package xyz.fpointzero.android.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import xyz.fpointzero.android.R;
import xyz.fpointzero.android.layout.TitleBar;

public class BottomNavFragment extends Fragment implements NavigationBarView.OnItemSelectedListener {
    TitleBar title;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.nav_bottom, container, false);

        BottomNavigationView bottomNavigationView = view.findViewById(R.id.navigation_bottom);
        bottomNavigationView.setOnItemSelectedListener(this);
        return view;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        title = requireActivity().findViewById(R.id.title_bar);
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        if (itemId == R.id.navigation_message) {
            title.setTitle(R.string.navigation_message);
            transaction.replace(R.id.fragment_main_info, MessageFragment.getInstance());

        } else if (itemId == R.id.navigation_contact) {
            title.setTitle(R.string.navigation_contact);
            transaction.replace(R.id.fragment_main_info, ContactFragment.getInstance());

        } else if (itemId == R.id.navigation_nearby) {
            title.setTitle(R.string.navigation_nearby);
            transaction.replace(R.id.fragment_main_info, NearbyFragment.getInstance());

        } else if (itemId == R.id.navigation_mine) {
            title.setTitle(R.string.navigation_mine);
            transaction.replace(R.id.fragment_main_info, MineFragment.getInstance());

        }
        transaction.addToBackStack(null);
        transaction.commit();
        item.setChecked(true);
        return true;
    }
}

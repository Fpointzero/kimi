package xyz.fpointzero.android.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import xyz.fpointzero.android.R;

public class BottomNavFragment extends Fragment {
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.nav_bottom, container, false);

        BottomNavigationView bottomNavigationView = view.findViewById(R.id.navigation_bottom);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                TextView title = requireActivity().findViewById(R.id.textview_title);
                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                if (itemId == R.id.navigation_message) {
                    title.setText(R.string.navigation_message);
                    transaction.replace(R.id.fragment_main_info, new MessageFragment());
                    
                } else if (itemId == R.id.navigation_contact) {
                    title.setText(R.string.navigation_contact);
                    transaction.replace(R.id.fragment_main_info, new ContactFragment());
                    
                } else if (itemId == R.id.navigation_nearby) {
                    title.setText(R.string.navigation_nearby);
                    transaction.replace(R.id.fragment_main_info, new TestFragment());
                    
                } else if (itemId == R.id.navigation_mine) {
                    title.setText(R.string.navigation_mine);
                    transaction.replace(R.id.fragment_main_info, new MineFragment());
                    
                }
                transaction.addToBackStack(null);
                transaction.commit();
                item.setChecked(true);
                return true;
            }
        });
        return view;
    }
}

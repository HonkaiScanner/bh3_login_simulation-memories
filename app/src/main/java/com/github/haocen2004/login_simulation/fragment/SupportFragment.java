package com.github.haocen2004.login_simulation.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.github.haocen2004.login_simulation.R;
import com.github.haocen2004.login_simulation.databinding.FragmentSupportBinding;
import com.github.haocen2004.login_simulation.fragment.sponsor.DisplayFragment;
import com.github.haocen2004.login_simulation.fragment.sponsor.UserFragment;
import com.google.android.material.tabs.TabLayoutMediator;

import org.jetbrains.annotations.NotNull;

public class SupportFragment extends Fragment {
    private FragmentSupportBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSupportBinding.inflate(inflater, container, false);
//        Logger.setView(binding.getRoot());
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.viewPagerSp.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return position == 0 ? new DisplayFragment() : new UserFragment();
            }

            @Override
            public int getItemCount() {
                return 2;
            }
        });
        new TabLayoutMediator(binding.tabLayout, binding.viewPagerSp, (tab, position) -> {
            if (position == 0) {
                tab.setText(R.string.sponsors_list);
            } else {
                tab.setText(R.string.become_sp);
            }
        }).attach();
    }
}
package com.github.haocen2004.login_simulation.Fragment.Sponsor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.haocen2004.login_simulation.databinding.FragmentSpUserBinding;

public class UserFragment extends Fragment {
    private FragmentSpUserBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSpUserBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.buttonRegStep1.setOnClickListener(view1 -> {
            binding.buttonLogin.setVisibility(View.GONE);
            binding.buttonRegStep1.setVisibility(View.GONE);
            binding.buttonRegisterStep2.setVisibility(View.VISIBLE);
        });
        binding.checkBox.setOnCheckedChangeListener((compoundButton, b) -> binding.buttonLogin.setEnabled(b));
    }
}
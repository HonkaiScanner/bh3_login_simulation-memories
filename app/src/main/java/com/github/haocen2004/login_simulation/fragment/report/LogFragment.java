package com.github.haocen2004.login_simulation.fragment.report;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.haocen2004.login_simulation.adapter.LoggerAdapter;
import com.github.haocen2004.login_simulation.data.LogLiveData;
import com.github.haocen2004.login_simulation.databinding.FragmentLogBinding;
import com.github.haocen2004.login_simulation.utils.Logger;

import org.jetbrains.annotations.NotNull;


public class LogFragment extends Fragment {


    private LoggerAdapter loggerAdapter;
    private boolean loadLogs = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        com.github.haocen2004.login_simulation.databinding.FragmentLogBinding binding = FragmentLogBinding.inflate(inflater, container, false);
        RecyclerView logRecyclerView = binding.logRecycleView;
        loggerAdapter = new LoggerAdapter(getActivity());
        logRecyclerView.setFocusableInTouchMode(false);
        logRecyclerView.setHasFixedSize(true);
        logRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        logRecyclerView.setAdapter(loggerAdapter);
        LogLiveData.getINSTANCE().observe(getViewLifecycleOwner(), logData -> {
//
//            loggerAdapter.ins
            if (!loadLogs) {
                Logger.d("LogLiveData", "load log data");
                loggerAdapter.setAllLogs(logData);
                loadLogs = true;
            }
            loggerAdapter.notifyItemInserted(loggerAdapter.getItemCount());
        });
        return binding.getRoot();
    }


}
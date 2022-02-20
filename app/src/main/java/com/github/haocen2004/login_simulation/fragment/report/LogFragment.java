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


public class LogFragment extends Fragment {


    private FragmentLogBinding binding;
    private RecyclerView logRecyclerView;
    private LoggerAdapter loggerAdapter;
    private LogLiveData logLiveData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLogBinding.inflate(inflater, container, false);
        logRecyclerView = binding.logRecycleView;
        loggerAdapter = new LoggerAdapter(getActivity());
        logRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        logRecyclerView.setAdapter(loggerAdapter);
//        Logger.setView(binding.getRoot());
        LogLiveData.getINSTANCE(getContext()).observe(getViewLifecycleOwner(), logData -> {
            loggerAdapter.setAllLogs(logData);
            loggerAdapter.notifyItemChanged(logData.size() - 1);
        });
//        adapter.setAllSponsors(sponsorRepo.getAllSponsors());
//        // 刷新操作
//        new Handler(Looper.getMainLooper()).post(adapter::notifyDataSetChanged);
        return binding.getRoot();
    }


}
package com.github.haocen2004.login_simulation.fragment.sponsor;

import static com.github.haocen2004.login_simulation.util.Constant.CHECK_VER;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.haocen2004.login_simulation.adapter.SponsorAdapter;
import com.github.haocen2004.login_simulation.data.database.sponsor.SponsorData;
import com.github.haocen2004.login_simulation.data.database.sponsor.SponsorRepo;
import com.github.haocen2004.login_simulation.databinding.FragmentSpDisplayBinding;
import com.github.haocen2004.login_simulation.util.Logger;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DisplayFragment extends Fragment {
    private FragmentSpDisplayBinding binding;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSpDisplayBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerViewSp = binding.recyclerViewSp;
        SponsorAdapter sponsorAdapter = new SponsorAdapter(getActivity());
        initAdapter(sponsorAdapter);
        recyclerViewSp.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewSp.setAdapter(sponsorAdapter);
    }

    private void initAdapter(SponsorAdapter adapter) {
        List<SponsorData> sponsorDataOld = new ArrayList<>();
        sponsorDataOld.add(new SponsorData("Loading...", "", "a", "b", "c", "d"));
        adapter.setAllSponsors(sponsorDataOld);
        if (CHECK_VER) {
            new Thread(() -> {
                SponsorRepo sponsorRepo = new SponsorRepo(requireContext());
                if (sponsorRepo.getAllSponsors().size() > 0) {
                    adapter.setAllSponsors(sponsorRepo.getAllSponsors());
                    // 刷新操作
                    try {
                        Looper.prepare();
                    } catch (Exception ignore) {
                    }
                    new Handler(Looper.getMainLooper()).post(adapter::notifyDataSetChanged);
                } else {
                    Logger.d("sponsor Adapter", "Sponsors get failed.");
                }
            }).start();
        }
    }
}
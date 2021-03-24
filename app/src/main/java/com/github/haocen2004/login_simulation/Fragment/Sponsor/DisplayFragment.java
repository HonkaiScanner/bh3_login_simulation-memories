package com.github.haocen2004.login_simulation.Fragment.Sponsor;

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

import com.github.haocen2004.login_simulation.Adapter.SponsorAdapter;
import com.github.haocen2004.login_simulation.Database.SponsorData;
import com.github.haocen2004.login_simulation.Database.SponsorRepo;
import com.github.haocen2004.login_simulation.databinding.FragmentSpDisplayBinding;

import java.util.ArrayList;
import java.util.List;

import static com.github.haocen2004.login_simulation.util.Constant.CHECK_VER;

public class DisplayFragment extends Fragment {
    private RecyclerView recyclerViewSp;
    private SponsorAdapter sponsorAdapter;
    private FragmentSpDisplayBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSpDisplayBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerViewSp = binding.recyclerViewSp;
        sponsorAdapter = new SponsorAdapter(getActivity());
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
                SponsorRepo sponsorRepo = new SponsorRepo(getContext());
                adapter.setAllSponsors(sponsorRepo.getAllSponsors());
                // 刷新操作
                Looper.prepare();
                new Handler(Looper.getMainLooper()).post(adapter::notifyDataSetChanged);

            }).start();
        }
    }
}
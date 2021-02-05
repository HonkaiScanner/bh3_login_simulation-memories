package com.github.haocen2004.login_simulation.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.haocen2004.login_simulation.Adapter.SupportTabLayoutAdapter;
import com.github.haocen2004.login_simulation.R;
import com.google.android.material.appbar.CollapsingToolbarLayout;

public class SupportFragment extends Fragment {
    private RecyclerView recyclerView;
    private SupportTabLayoutAdapter supportTabLayoutAdapter;

    public SupportFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recyclerView = getActivity().findViewById(R.id.recyclerView);
        supportTabLayoutAdapter = new SupportTabLayoutAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(supportTabLayoutAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((CollapsingToolbarLayout) requireActivity().findViewById(R.id.collapsingToolbarLayout))
                .setTitle(getString(R.string.list_pay));
        return inflater.inflate(R.layout.fragment_support, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        openUrl("https://afdian.net/@Haocen20004", requireActivity());
        super.onViewCreated(view, savedInstanceState);
    }
}
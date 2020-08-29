package com.github.haocen2004.login_simulation.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.github.haocen2004.login_simulation.R;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import static com.github.haocen2004.login_simulation.util.Tools.openUrl;

public class ReportFragment extends Fragment {

    public ReportFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((CollapsingToolbarLayout) requireActivity().findViewById(R.id.collapsingToolbarLayout))
                .setTitle(getString(R.string.list_report));
        return inflater.inflate(R.layout.fragment_report, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        openUrl("https://github.com/Haocen2004/bh3_login_simulation/issues", (AppCompatActivity) getActivity());
        super.onViewCreated(view, savedInstanceState);
    }
}
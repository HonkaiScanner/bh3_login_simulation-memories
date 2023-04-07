package com.github.haocen2004.login_simulation.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.paging.LoadState;
import androidx.recyclerview.widget.RecyclerView;

import com.github.haocen2004.login_simulation.R;

public class LoadStateAdapter extends androidx.paging.LoadStateAdapter<LoadStateAdapter.LoadStateViewHolder> {

    @Override
    public void onBindViewHolder(@NonNull final LoadStateViewHolder holder, LoadState loadState) {
        if (loadState instanceof LoadState.Loading) {
            holder.progressBar.setVisibility(View.VISIBLE);
        }
    }


    @NonNull
    @Override
    public LoadStateViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, @NonNull LoadState loadState) {
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        View itemView = layoutInflater.inflate(R.layout.load_state_view, viewGroup, false);
        return new LoadStateViewHolder(itemView);
    }

    static class LoadStateViewHolder extends RecyclerView.ViewHolder {
        ProgressBar progressBar;

        LoadStateViewHolder(@NonNull View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.loadStateProgress);
        }
    }
}
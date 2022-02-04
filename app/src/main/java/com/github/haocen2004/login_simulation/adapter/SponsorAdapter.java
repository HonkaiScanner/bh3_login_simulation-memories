package com.github.haocen2004.login_simulation.adapter;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.haocen2004.login_simulation.R;
import com.github.haocen2004.login_simulation.data.database.sponsor.SponsorData;

import java.util.List;

public class SponsorAdapter extends RecyclerView.Adapter<SponsorAdapter.SponsorViewHolder> {
    private List<SponsorData> allSponsors;
    private final Activity activity;

    public SponsorAdapter(Activity activity) {
        this.activity = activity;
    }

    @NonNull
    @Override
    public SponsorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View itemView = layoutInflater.inflate(R.layout.cell_card, parent, false);
        return new SponsorViewHolder(itemView);
    }

    public void setAllSponsors(List<SponsorData> allSponsors) {
        this.allSponsors = allSponsors;
    }

    @Override
    public void onBindViewHolder(@NonNull final SponsorViewHolder holder, final int position) {
        SponsorData sponsorData = allSponsors.get(position);
        holder.textViewName.setText(sponsorData.getName());
        holder.textViewDesc.setText(sponsorData.getDesc());
        holder.imageViewAvatar.setImageURI(Uri.parse(sponsorData.getAvatarImgUrl()));
        Glide.with(activity).load(sponsorData.getAvatarImgUrl()).circleCrop().into(holder.imageViewAvatar);
        holder.itemView.setOnClickListener(v -> {
            try {
                Uri uri = Uri.parse(sponsorData.getPersonalPageUrl());
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(uri);
                holder.itemView.getContext().startActivity(intent);
            } catch (Exception ignore) {
            }
        });
    }

    @Override
    public int getItemCount() {
        return allSponsors.size();
    }

    static class SponsorViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName, textViewDesc;
        ImageView imageViewAvatar;

        SponsorViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewMessage);
            textViewDesc = itemView.findViewById(R.id.textViewLevel);
            imageViewAvatar = itemView.findViewById(R.id.imageViewAvatar);

        }
    }
}
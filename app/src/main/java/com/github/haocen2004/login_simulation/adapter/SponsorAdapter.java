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
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.haocen2004.login_simulation.R;
import com.github.haocen2004.login_simulation.data.sponsor.database.SponsorData;

import java.net.URL;

public class SponsorAdapter extends PagingDataAdapter<SponsorData, SponsorAdapter.SponsorViewHolder> {
    private final Activity activity;

    public SponsorAdapter(Activity activity) {
        super(new DiffUtil.ItemCallback<>() {
            @Override
            public boolean areItemsTheSame(@NonNull SponsorData oldItem, @NonNull SponsorData newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull SponsorData oldItem, @NonNull SponsorData newItem) {
                return oldItem.getScannerKey().equals(newItem.getScannerKey());
            }
        });
        this.activity = activity;
    }

    @NonNull
    @Override
    public SponsorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View itemView = layoutInflater.inflate(R.layout.cell_card, parent, false);
        return new SponsorViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(@NonNull final SponsorViewHolder holder, final int position) {
        SponsorData sponsorData = getItem(position);
        if (sponsorData == null) {
            holder.textViewName.setText(R.string.bsgamesdk_loadingTips);
        } else {
            holder.textViewName.setText(sponsorData.getName());
            holder.textViewDesc.setText(sponsorData.getDesc());
//            holder.imageViewAvatar.setImageURI(Uri.parse(sponsorData.getAvatarImgUrl()));
            try {
                URL url = new URL(sponsorData.getAvatarImgUrl());
                if (url.getProtocol().contains("http")) {
                    Glide.with(activity.getApplicationContext()).load(sponsorData.getAvatarImgUrl()).circleCrop().into(holder.imageViewAvatar);
                } else {
                    Glide.with(activity.getApplicationContext()).load("https://i0.hdslb.com/bfs/face/member/noface.jpg").circleCrop().into(holder.imageViewAvatar);
                }
            } catch (Exception e) {
                Glide.with(activity.getApplicationContext()).load("https://i0.hdslb.com/bfs/face/member/noface.jpg").circleCrop().into(holder.imageViewAvatar);
            }
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
    }

    static class SponsorViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName, textViewDesc;
        ImageView imageViewAvatar;

        SponsorViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewDesc = itemView.findViewById(R.id.textViewDesc);
            imageViewAvatar = itemView.findViewById(R.id.imageViewAvatar);

        }
    }
}
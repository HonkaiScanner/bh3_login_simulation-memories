package com.github.haocen2004.login_simulation.Adapter;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.haocen2004.login_simulation.Data.SponsorData;
import com.github.haocen2004.login_simulation.R;

import java.util.List;

public class SupportTabLayoutAdapter extends RecyclerView.Adapter<SupportTabLayoutAdapter.MyViewHolder> {
    private List<SponsorData> allSponsors;

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View itemView = layoutInflater.inflate(R.layout.cell_card, parent, false);
        return new MyViewHolder(itemView);
    }

    public void setAllSponsors(List<SponsorData> allSponsors) {
        this.allSponsors = allSponsors;
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        SponsorData sponsorData = allSponsors.get(position);
        holder.textViewName.setText(sponsorData.getName());
        holder.textViewDesc.setText(sponsorData.getDesc());
        holder.imageViewAvatar.setImageURI(Uri.parse(""));
        holder.itemView.setOnClickListener(v -> {
            Uri uri = Uri.parse(sponsorData.getPersonalPageUrl());
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return allSponsors.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName, textViewDesc;
        ImageView imageViewAvatar;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewDesc = itemView.findViewById(R.id.textViewDesc);
            imageViewAvatar = itemView.findViewById(R.id.imageViewAvatar);

        }
    }
}

//proxy sps -S ss -H rc4-md5 -J oYbxV6 -T tcp -P sz-ap03.hkix.xyz:57932 -t tcp -p :30067 -h chacha20 -j fszyytz --disable-http --disable-socks --disable-proxy-host

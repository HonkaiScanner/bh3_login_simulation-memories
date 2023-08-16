package com.github.haocen2004.login_simulation.adapter.sponsor;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.drakeet.about.AbsAboutActivity;
import com.drakeet.multitype.ItemViewBinder;
import com.github.haocen2004.login_simulation.data.ICallback;

public class CardSettingBinder extends ItemViewBinder<CardSetting, CardSettingBinder.ViewHolder> {

    private @NonNull
    final AbsAboutActivity activity;

    public CardSettingBinder(@NonNull AbsAboutActivity activity) {
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull LayoutInflater layoutInflater, @NonNull ViewGroup viewGroup) {
        return new CardSettingBinder.ViewHolder(layoutInflater.inflate(com.drakeet.about.R.layout.about_page_item_card, viewGroup, false), activity);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, CardSetting card) {
        viewHolder.content.setText(card.content);
        viewHolder.onClickCallback = card.onClick;
        viewHolder.onLongClickCallback = card.onLongClick;
    }

    @Override
    public long getItemId(CardSetting item) {
        return item.hashCode();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        public TextView content;
        public ICallback onClickCallback = null;
        public ICallback onLongClickCallback = null;
        public AbsAboutActivity activity;

        public ViewHolder(View itemView, AbsAboutActivity activity) {
            super(itemView);
            this.activity = activity;
            content = itemView.findViewById(com.drakeet.about.R.id.content);
            itemView.getRootView().setOnClickListener(this);
            itemView.getRootView().setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (onClickCallback != null) {
                onClickCallback.run(content);
            } else {
                Log.d("CardSetting", "empty callback!");
            }
        }


        @Override
        public boolean onLongClick(View v) {
            if (onLongClickCallback != null) {
                onLongClickCallback.run(content);
                return true;
            } else {
                return false;
            }
        }
    }
}

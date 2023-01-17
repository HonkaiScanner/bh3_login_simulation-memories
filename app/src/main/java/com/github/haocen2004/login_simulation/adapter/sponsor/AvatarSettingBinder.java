package com.github.haocen2004.login_simulation.adapter.sponsor;

import static com.github.haocen2004.login_simulation.util.Tools.showSecondConfirmDialog;

import android.annotation.SuppressLint;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.drakeet.about.AbsAboutActivity;
import com.drakeet.multitype.ItemViewBinder;
import com.github.haocen2004.login_simulation.R;
import com.github.haocen2004.login_simulation.util.Logger;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import cn.leancloud.LCObject;
import cn.leancloud.LCUser;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class AvatarSettingBinder extends ItemViewBinder<AvatarSetting, AvatarSettingBinder.ViewHolder> {

    private @NonNull
    final AbsAboutActivity activity;

    public AvatarSettingBinder(@NonNull AbsAboutActivity activity) {
        this.activity = activity;
    }

    @Override
    public long getItemId(@NonNull AvatarSetting item) {
        return item.hashCode();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull LayoutInflater layoutInflater, @NonNull ViewGroup viewGroup) {
        return new AvatarSettingBinder.ViewHolder(layoutInflater.inflate(com.drakeet.about.R.layout.about_page_item_contributor, viewGroup, false), activity);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, AvatarSetting avatarSetting) {
        Glide.with(activity).load(avatarSetting.avatarUrl).into(viewHolder.avatar);
        viewHolder.name.setText(avatarSetting.name);
        viewHolder.desc.setText(avatarSetting.desc);
        viewHolder.data = avatarSetting;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        protected @NonNull
        final AbsAboutActivity activity;
        public ImageView avatar;
        public TextView name;
        public TextView desc;
        public AvatarSetting data;

        public ViewHolder(@NonNull View itemView, @NonNull AbsAboutActivity activity) {
            super(itemView);
            this.activity = activity;
            avatar = itemView.findViewById(com.drakeet.about.R.id.avatar);
            name = itemView.findViewById(com.drakeet.about.R.id.name);
            desc = itemView.findViewById(com.drakeet.about.R.id.desc);
            itemView.getRootView().setOnClickListener(this);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onClick(View v) {

            LCUser user = LCUser.currentUser();
            MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(activity);
            final View dialogView = LayoutInflater.from(activity)
                    .inflate(R.layout.card_setting_dialog, null);
            EditText edit_text = dialogView.findViewById(R.id.cardSettingEditText);
            edit_text.setHint("头像URL");
            edit_text.setText(user.getString("avatarImgUrl"));
            TextView textView = dialogView.findViewById(R.id.cardSettingTextView);
            textView.setText("修改头像url");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                edit_text.setAutofillHints(user.getString("avatarImgUrl"));
            }
            dialog.setView(dialogView)
                    .setCancelable(false)
                    .setPositiveButton("确定", (dialog1, which) -> {
                        String newValue = edit_text.getText().toString();
                        showSecondConfirmDialog("头像URL", newValue, (o) -> {
                            if (newValue.equals(user.getString("avatarImgUrl")) || newValue.equals(""))
                                return;
                            user.put("avatarImgUrl", newValue);
                            user.saveInBackground().subscribe(new Observer<LCObject>() {
                                public void onSubscribe(@NonNull Disposable disposable) {
                                }

                                public void onNext(@NonNull LCObject todo) {

                                    Glide.with(activity).load(newValue).into(avatar);
                                    Logger.getLogger(null).makeToast("更新成功！");
                                    Logger.d("SponsorInfoActivity", "save avatarImgUrl succ");
                                }

                                public void onError(@NonNull Throwable throwable) {
                                    Logger.getLogger(null).makeToast("更新失败\n" + throwable.getMessage());
                                    throwable.printStackTrace();
                                }

                                public void onComplete() {
                                }
                            });
                        });
                        dialog1.dismiss();
                    })
                    .setNegativeButton("取消", ((dialog1, which) -> dialog1.dismiss()))
                    .show();

        }
    }
}

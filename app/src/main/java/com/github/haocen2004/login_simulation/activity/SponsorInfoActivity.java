package com.github.haocen2004.login_simulation.activity;

import static com.github.haocen2004.login_simulation.util.Tools.showSecondConfirmDialog;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.drakeet.about.Category;
import com.drakeet.multitype.MultiTypeAdapter;
import com.github.haocen2004.login_simulation.R;
import com.github.haocen2004.login_simulation.adapter.sponsor.AvatarSetting;
import com.github.haocen2004.login_simulation.adapter.sponsor.AvatarSettingBinder;
import com.github.haocen2004.login_simulation.adapter.sponsor.CardSetting;
import com.github.haocen2004.login_simulation.adapter.sponsor.CardSettingBinder;
import com.github.haocen2004.login_simulation.data.dialog.ButtonData;
import com.github.haocen2004.login_simulation.data.dialog.DialogData;
import com.github.haocen2004.login_simulation.data.dialog.DialogLiveData;
import com.github.haocen2004.login_simulation.util.DialogHelper;
import com.github.haocen2004.login_simulation.util.Logger;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

import cn.leancloud.LCObject;
import cn.leancloud.LCUser;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class SponsorInfoActivity extends BaseAbsActivity {

    private final LCUser user = LCUser.currentUser();
    private final String TAG = "SponsorInfoActivity";
    private MultiTypeAdapter adapter;

    @Override
    protected void onCreateHeader(@NonNull ImageView icon, @NonNull TextView slogan, @NonNull TextView version) {
        if (user == null) finish();
        Glide.with(this).load(user.getString("avatarImgUrl")).into(icon);
        slogan.setText(user.getUsername());
        version.setText("这是船新的个人设置界面喵");
    }

    @Override
    protected void onItemsCreated(@NonNull List<Object> items) {
        if (user == null) finish();
        items.add(new Category("头像"));
        items.add(new AvatarSetting(user.getString("avatarImgUrl"), "点击修改当前头像", "目前仅支持渲染直链"));

        items.add(new Category("用户昵称"));
        items.add(new CardSetting(user.getUsername(), (object) -> {

            MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(this);
            final View dialogView = LayoutInflater.from(this)
                    .inflate(R.layout.card_setting_dialog, null);
            EditText edit_text = dialogView.findViewById(R.id.cardSettingEditText);
            edit_text.setHint("用户昵称");
            edit_text.setText(user.getUsername());
            TextView textView = dialogView.findViewById(R.id.cardSettingTextView);
            textView.setText("修改用户昵称");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                edit_text.setAutofillHints(user.getUsername());
            }
            dialog.setView(dialogView)
                    .setCancelable(false)
                    .setPositiveButton("确定", (dialog1, which) -> {
                        String newUserName = edit_text.getText().toString();
                        showSecondConfirmDialog("用户昵称", newUserName, (context) -> {
                            if (newUserName.equals(user.getUsername()) || newUserName.equals(""))
                                return;
                            user.setUsername(newUserName);
                            user.saveInBackground().subscribe(new Observer<LCObject>() {
                                public void onSubscribe(@NonNull Disposable disposable) {
                                }

                                public void onNext(@NonNull LCObject todo) {

                                    ((TextView) object).setText(newUserName);
                                    Logger.getLogger(null).makeToast("更新成功！");
                                    Logger.d(TAG, "save username succ");
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

        }));

        items.add(new Category("简介"));
        items.add(new CardSetting(user.getString("desc"), (object) -> {

            MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(this);
            final View dialogView = LayoutInflater.from(this)
                    .inflate(R.layout.card_setting_dialog, null);
            EditText edit_text = dialogView.findViewById(R.id.cardSettingEditText);
            edit_text.setHint("简介");
            edit_text.setText(user.getString("desc"));
            TextView textView = dialogView.findViewById(R.id.cardSettingTextView);
            textView.setText("修改简介");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                edit_text.setAutofillHints(user.getString("desc"));
            }
            dialog.setView(dialogView)
                    .setCancelable(false)
                    .setPositiveButton("确定", (dialog1, which) -> {
                        String newValue = edit_text.getText().toString();
                        showSecondConfirmDialog("个人简介", newValue, (context) -> {
                            if (newValue.equals(user.getString("desc")) || newValue.equals(""))
                                return;
                            user.put("desc", newValue);
                            user.saveInBackground().subscribe(new Observer<LCObject>() {
                                public void onSubscribe(@NonNull Disposable disposable) {
                                }

                                public void onNext(@NonNull LCObject todo) {

                                    ((TextView) object).setText(newValue);
                                    Logger.getLogger(null).makeToast("更新成功！");
                                    Logger.d(TAG, "save desc succ");
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


        }));


        items.add(new Category("自定义扫码名称"));
        items.add(new CardSetting(user.getString("custom_username"), (object) -> {
            DialogData dialogData = new DialogData("特殊修改提示", "该项目涉及扫码信息显示\n修改需要等待作者审核！\n\n自定义扫码名称将在第二行保留扫码器相关信息\n默认为 [扫码器赞助者] ");
            dialogData.setPositiveButtonData(new ButtonData("我已知晓") {
                @Override
                public void callback(DialogHelper dialogHelper) {
                    super.callback(dialogHelper);
                    if (user.getBoolean("has_update_data")) {
                        Logger.getLogger(null).makeToast("你已经有一个正在审核的修改了！");
                        return;
                    }

                    MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(activity);
                    final View dialogView = LayoutInflater.from(activity)
                            .inflate(R.layout.card_setting_dialog, null);
                    EditText edit_text = dialogView.findViewById(R.id.cardSettingEditText);
                    edit_text.setHint("简介");
                    edit_text.setText(user.getString("custom_username"));
                    TextView textView = dialogView.findViewById(R.id.cardSettingTextView);
                    textView.setText("修改自定义扫码名称");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        edit_text.setAutofillHints(user.getString("custom_username"));
                    }
                    dialog.setView(dialogView)
                            .setCancelable(false)
                            .setPositiveButton("确定", (dialog1, which) -> {
                                String newValue = edit_text.getText().toString();
                                showSecondConfirmDialog("自定义扫码名称", newValue, (context) -> {
                                    if (newValue.equals(user.getString("custom_username")) || newValue.equals(""))
                                        return;
                                    user.put("new_custom_username", newValue);
                                    user.put("has_update_data", true);
                                    user.saveInBackground().subscribe(new Observer<LCObject>() {
                                        public void onSubscribe(@NonNull Disposable disposable) {
                                        }

                                        public void onNext(@NonNull LCObject todo) {

//                                            ((TextView)object).setText(newValue);
                                            DialogData dialogData1 = new DialogData("提交成功", "修改已提交\n请耐心等待作者审核\n也可带上下方赞助者身份码私聊催审", "好的");
                                            DialogLiveData.getINSTANCE(null).addNewDialog(dialogData1);
                                            Logger.d(TAG, "send update custom_username request succ");
                                        }

                                        public void onError(@NonNull Throwable throwable) {
                                            Logger.getLogger(null).makeToast("提交更新失败\n" + throwable.getMessage());
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
            });
            dialogData.setNegativeButtonData("取消修改");
            DialogLiveData.getINSTANCE(this).addNewDialog(dialogData);
        }));

        items.add(new Category("赞助者身份码"));
        items.add(new CardSetting(user.getString("scanner_key"), (object) -> Logger.getLogger(this).makeToast("该项目仅供查看"), (object) -> {
            ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("scanner_key", user.getString("scanner_key"));
            clipboard.setPrimaryClip(clip);
            Logger.getLogger(null).makeToast("已复制到剪贴板");
        }));
    }


    @Override
    protected void onPostCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (user == null) finish();
        adapter = getAdapter();
        adapter.register(AvatarSetting.class, new AvatarSettingBinder(this));
        adapter.register(CardSetting.class, new CardSettingBinder(this));
    }

}
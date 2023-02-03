package com.github.haocen2004.login_simulation.utils;

import static com.github.haocen2004.login_simulation.utils.Constant.HAS_TIPS;
import static com.github.haocen2004.login_simulation.utils.Constant.TIPS;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;

import com.github.haocen2004.login_simulation.R;
import com.github.haocen2004.login_simulation.databinding.TipsCardBinding;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class ChipsHelper implements ChipGroup.OnCheckedStateChangeListener, View.OnClickListener, DialogInterface.OnDismissListener, CompoundButton.OnCheckedChangeListener {

    private final Context mContext;
    private final TipsCardBinding mBinding;
    private final LayoutInflater mLayoutInflater;
    private final List<String> chipTypes = List.of(new String[]{"深渊", "战场", "乐土"});
    private final List<String> chipLevelA = List.of(new String[]{"苦痛", "红莲", "寂灭", "无限", "原罪", "打死就行"});
    private final List<String> chipLevelB = List.of(new String[]{"3档", "2档", "1档", "4档", "打死就行"});
    private final List<String> chipLevelC = List.of(new String[]{"1.75x", "2.25x", "2.5x", "2.75x", "1x", "1.5x", "2x"});
    private final List<List<String>> chipLevels = List.of(chipLevelA, chipLevelB, chipLevelC);
    private final List<ChipGroup> chipGroups;
    private final List<Chip> headChipList;
    private final Map<String, Chip> chipMap = new HashMap<>();
    private final List<Chip> chipList = new ArrayList<>();
    private final String TAG = "ChipsHelper";
    private MaterialAlertDialogBuilder dialog;
    private final ReentrantLock lock = new ReentrantLock();


    public ChipsHelper(Context context, LayoutInflater layoutInflater) {
        mContext = context;
        mLayoutInflater = layoutInflater;
        mBinding = TipsCardBinding.inflate(mLayoutInflater);
        mBinding.chipGroupResult.removeAllViews();
        mBinding.chipGroupVoid.removeAllViews();
        mBinding.chipGroupBattle.removeAllViews();
        mBinding.chipGroupRealms.removeAllViews();
        mBinding.chipVoid.setText("深渊");
        mBinding.chipBattle.setText("战场");
        mBinding.chipRealms.setText("乐土");
        mBinding.chipVoid.setOnCheckedChangeListener(this);
        mBinding.chipBattle.setOnCheckedChangeListener(this);
        mBinding.chipRealms.setOnCheckedChangeListener(this);
        mBinding.chipGroupVoid.setOnCheckedStateChangeListener(this);
        mBinding.chipGroupBattle.setOnCheckedStateChangeListener(this);
        mBinding.chipGroupRealms.setOnCheckedStateChangeListener(this);
        chipGroups = List.of(mBinding.chipGroupVoid, mBinding.chipGroupBattle, mBinding.chipGroupRealms, mBinding.chipGroupResult);
        headChipList = List.of(mBinding.chipVoid, mBinding.chipBattle, mBinding.chipRealms);
    }

    @Override
    public void onCheckedChanged(@NonNull ChipGroup group, @NonNull List<Integer> checkedIds) {

        int i = chipGroups.indexOf(group);

        String prefix = headChipList.get(i).getText().toString();

        for (int j = 0; j < mBinding.chipGroupResult.getChildCount(); j++) {
            Chip tempChip = (Chip) mBinding.chipGroupResult.getChildAt(j);
            if (tempChip.getText().toString().startsWith(prefix)) {
                mBinding.chipGroupResult.removeView(tempChip);
            }
        }

        for (Integer checkedId : checkedIds) {
            lock.lock();

            String key;
            try {
                key = prefix + chipList.get(checkedId - 1).getText().toString();
            } catch (IndexOutOfBoundsException e) {
                Logger.d(TAG, "IOB error, please retry");
                return;
            }
            Logger.d(TAG, key);

            if (chipMap.containsKey(key)) {
                mBinding.chipGroupResult.addView(chipMap.get(key));
            } else {
                Chip tempChip = (Chip) mLayoutInflater.inflate(R.layout.chip_result, null, false);
                tempChip.setText(key);
                mBinding.chipGroupResult.addView(tempChip);
                chipList.add(tempChip);
                chipMap.put(key, tempChip);
            }

            lock.unlock();

        }
    }

    @Override
    public void onClick(View v) {
        if (dialog == null) {
            dialog = new MaterialAlertDialogBuilder(mContext);
            dialog.setTitle(R.string.btn_tip);
            dialog.setView(mBinding.getRoot());
            dialog.setCancelable(true);
            dialog.setOnDismissListener(this);
            dialog.setPositiveButton("确认", null);
        }
        dialog.show();

    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        ((ViewGroup) mBinding.getRoot().getParent()).removeView(mBinding.getRoot());
        Logger.d(TAG, "try save tag");
        StringBuilder stringBuilder = new StringBuilder();
        for (int j = 0; j < mBinding.chipGroupResult.getChildCount(); j++) {
            Chip tempChip = (Chip) mBinding.chipGroupResult.getChildAt(j);
            stringBuilder.append(tempChip.getText().toString())
                    .append("|");
        }
        if (stringBuilder.length() > 1) {
            TIPS = stringBuilder.substring(0, stringBuilder.length() - 1);
            HAS_TIPS = true;
            Logger.d(TAG, TIPS);
        } else {
            TIPS = "";
            HAS_TIPS = false;
        }

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        int i = chipTypes.indexOf(buttonView.getText().toString());
        ChipGroup tempGroup = chipGroups.get(i);
        tempGroup.removeAllViews();
        if (isChecked) {
            lock.lock();
            for (String s : chipLevels.get(i)) {
                if (chipMap.containsKey(s)) {
                    tempGroup.addView(chipMap.get(s));
                } else {
                    Chip tempChip = (Chip) mLayoutInflater.inflate(R.layout.chip_select, null, false);
                    tempChip.setText(s);
                    tempGroup.addView(tempChip);
                    chipList.add(tempChip);
                    chipMap.put(s, tempChip);
                }
            }
            lock.unlock();
            onCheckedChanged(tempGroup, tempGroup.getCheckedChipIds());
        } else {
            clearUnSelectChips(i);
        }

    }

    private void clearUnSelectChips(int index) {
        String prefix = headChipList.get(index).getText().toString();
        for (int j = 0; j < mBinding.chipGroupResult.getChildCount(); j++) {
            Chip tempChip = (Chip) mBinding.chipGroupResult.getChildAt(j);
            if (tempChip.getText().toString().startsWith(prefix)) {
                mBinding.chipGroupResult.removeView(tempChip);
            }
        }
    }

}
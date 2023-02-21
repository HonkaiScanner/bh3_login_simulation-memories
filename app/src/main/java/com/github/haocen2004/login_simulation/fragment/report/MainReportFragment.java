package com.github.haocen2004.login_simulation.fragment.report;

import static com.github.haocen2004.login_simulation.data.Constant.QQ_GROUP_URL;
import static com.github.haocen2004.login_simulation.utils.Logger.getLogger;
import static com.github.haocen2004.login_simulation.utils.Tools.openUrl;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.haocen2004.login_simulation.data.dialog.ButtonData;
import com.github.haocen2004.login_simulation.data.dialog.DialogData;
import com.github.haocen2004.login_simulation.data.dialog.DialogLiveData;
import com.github.haocen2004.login_simulation.databinding.FragmentReportMainBinding;
import com.github.haocen2004.login_simulation.utils.DialogHelper;
import com.github.haocen2004.login_simulation.utils.Logger;
import com.tencent.bugly.crashreport.CrashReport;

public class MainReportFragment extends Fragment implements View.OnClickListener {
    private FragmentReportMainBinding binding;
    private Logger Log;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentReportMainBinding.inflate(inflater, container, false);
//        Logger.setView(binding.getRoot());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        openUrl("https://github.com/Haocen2004/bh3_login_simulation/issues", requireActivity());
        binding.reportGithub.setOnClickListener(this);
        binding.reportBili.setOnClickListener(this);
        binding.reportQq.setOnClickListener(this);
        binding.reportHand.setOnClickListener(this);
        Log = getLogger(getContext());
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onClick(View view) {
        if (binding.reportGithub.equals(view)) {
            openUrl("https://github.com/HonkaiScanner/bh3_login_simulation/issues", requireActivity());
        } else if (binding.reportBili.equals(view)) {
            openUrl("https://space.bilibili.com/269140934", requireActivity());
        } else if (binding.reportQq.equals(view)) {
            DialogData dialogData = new DialogData("加群暗号", "Hao_cen");
            dialogData.setPositiveButtonData(new ButtonData("打开加群界面") {
                @Override
                public void callback(DialogHelper dialogHelper) {
                    openUrl(QQ_GROUP_URL, requireActivity());
                    super.callback(dialogHelper);
                }
            });
            DialogLiveData.getINSTANCE(null).addNewDialog(dialogData);
        } else if (binding.reportHand.equals(view)) {
            DialogData dialogData = new DialogData("反馈须知", "该按钮为遇到错误时未发生崩溃使用\n请完整执行完会产生错误的操作后再点击\n请优先切换至日志窗口查看日志后再判断是否需要上报完整日志\n\n请再次确认是否上报");
            dialogData.setPositiveButtonData(new ButtonData("确认开始上报") {
                @Override
                public void callback(DialogHelper dialogHelper) {
                    CrashReport.testJavaCrash();
                    CrashReport.testANRCrash();
                    super.callback(dialogHelper);
                }
            });
            DialogLiveData.getINSTANCE(null).addNewDialog(dialogData);
        } else {
            Log.makeToast("Wrong Button");
        }
    }
}
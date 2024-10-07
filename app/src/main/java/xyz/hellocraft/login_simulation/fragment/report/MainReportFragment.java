package xyz.hellocraft.login_simulation.fragment.report;

import static xyz.hellocraft.login_simulation.data.Constant.DEBUG_MODE;
import static xyz.hellocraft.login_simulation.data.Constant.QQ_GROUP_URL;
import static xyz.hellocraft.login_simulation.utils.Logger.getLogger;
import static xyz.hellocraft.login_simulation.utils.Tools.openUrl;
import static xyz.hellocraft.login_simulation.utils.Tools.traverseFilesAndDelete;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.github.haocen2004.login_simulation.databinding.FragmentReportMainBinding;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.File;
import java.util.List;
import java.util.Objects;

import xyz.hellocraft.login_simulation.activity.ActivityManager;
import xyz.hellocraft.login_simulation.data.dialog.ButtonData;
import xyz.hellocraft.login_simulation.data.dialog.DialogData;
import xyz.hellocraft.login_simulation.data.dialog.DialogLiveData;
import xyz.hellocraft.login_simulation.login.LoginInstanceManager;
import xyz.hellocraft.login_simulation.utils.DialogHelper;
import xyz.hellocraft.login_simulation.utils.Logger;

public class MainReportFragment extends Fragment implements View.OnClickListener {
    private FragmentReportMainBinding binding;
    private Logger Log;
    private int clickCounter = 0;

    private final List<String> WHITELIST_FILES = List.of("sponsors_db", "shared_prefs", "VMh6lRyyinstallation");
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
        binding.reportClear.setOnClickListener(this);
        if (DEBUG_MODE) binding.reportHand.setVisibility(View.VISIBLE);
        binding.reportHand.setOnClickListener(this);
        Log = getLogger(getContext());
        super.onViewCreated(view, savedInstanceState);
    }

    private final Handler crashHandle = new Handler(Looper.getMainLooper());

    @Override
    public void onClick(View view) {
        if (binding.reportGithub.equals(view)) {
            openUrl("https://github.com/HonkaiScanner/bh3_login_simulation-memories/issues", requireActivity());
        } else if (binding.reportBili.equals(view)) {
            openUrl("https://space.bilibili.com/269140934", requireActivity());
        } else if (binding.reportClear.equals(view)) {
            DialogData dialogData = new DialogData("清理缓存", "点击确认将 重启扫码器\n并清理 所有 缓存\n\n可能会导致部分渠道服账号登陆状态丢失");
            dialogData.setPositiveButtonData(new ButtonData("开始清理") {
                @Override
                public void callback(DialogHelper dialogHelper) {
                    try {
                        File internalFiles = new File(Objects.requireNonNull(requireActivity().getFilesDir().getParent()));
                        traverseFilesAndDelete(internalFiles);
                        ActivityManager.getInstance().clearActivity();
                    } catch (Exception ignore) {
                    }
                    super.callback(dialogHelper);
                }
            });
            dialogData.setNegativeButtonData("我再想想");
            DialogLiveData.getINSTANCE().addNewDialog(dialogData);

        } else if (binding.reportQq.equals(view)) {
            DialogData dialogData = new DialogData("加群暗号", "Hao_cen");
            dialogData.setPositiveButtonData(new ButtonData("打开加群界面") {
                @Override
                public void callback(DialogHelper dialogHelper) {
                    openUrl(QQ_GROUP_URL, requireActivity());
                    super.callback(dialogHelper);
                }
            });
            DialogLiveData.getINSTANCE().addNewDialog(dialogData);
        } else if (binding.reportHand.equals(view)) {
            if (LoginInstanceManager.getINSTANCE((AppCompatActivity) getActivity()).isInit() || clickCounter > 4) {
                DialogData dialogData = new DialogData("反馈须知", "该按钮为遇到错误时未发生崩溃使用\n请完整执行完会产生错误的操作后再点击 立即上报\n或者点击 延迟3分钟上报 并在期间内复现会出现问题的操作\n\n请优先使用本页面的清除缓存按钮来排除一些错误\n\n执行上报会导致扫码器退出并在下次启动时进入崩溃提交界面\n请再次确认是否上报");
                dialogData.setPositiveButtonData(new ButtonData("立即上报") {
                    @Override
                    public void callback(DialogHelper dialogHelper) {
                        CrashReport.testJavaCrash();
                        CrashReport.testANRCrash();
                        super.callback(dialogHelper);
                    }
                });
                dialogData.setNeutralButtonData(new ButtonData("延迟3分钟上报") {
                    @Override
                    public void callback(DialogHelper dialogHelper) {
                        super.callback(dialogHelper);
                        crashHandle.postDelayed(CrashReport::testJavaCrash, 3 * 60 * 1000);
                        Log.makeToast("你选择了3分钟后上报\n现在请开始复现会出现错误的操作");
                    }
                });
                dialogData.setNegativeButtonData("取消");
                dialogData.setCancelable(true);
                DialogLiveData.getINSTANCE().addNewDialog(dialogData);
            } else {
                clickCounter++;
                Log.makeToast("请先执行会产生问题的步骤后再点此提交！");
            }
        } else {
            Log.makeToast("Wrong Button");
        }
    }
}
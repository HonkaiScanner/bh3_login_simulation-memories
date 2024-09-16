package xyz.hellocraft.login_simulation.activity;

import static xyz.hellocraft.login_simulation.data.Constant.DEBUG_MODE;
import static xyz.hellocraft.login_simulation.utils.Tools.traverseFilesAndDelete;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.core.content.FileProvider;

import com.github.haocen2004.login_simulation.R;
import com.github.haocen2004.login_simulation.databinding.ActivityCrashBinding;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import xyz.hellocraft.login_simulation.utils.Logger;
import xyz.hellocraft.login_simulation.utils.Network;
import xyz.hellocraft.login_simulation.utils.Tools;

public class CrashActivity extends BaseActivity {
    ActivityCrashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCrashBinding.inflate(getLayoutInflater());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(binding.getRoot());
        String crashReportName = Tools.getString(this, "crash-report-name");
        String debugLogName = Tools.getString(this, "recent_debug_log");
        binding.textView5.setText(getString(R.string.crash_hint2).replace("{packageName}", getPackageName()));
        binding.textView6.setText(crashReportName);
        binding.textView7.setText(Tools.getUUID(this));
        binding.textView8.setText(Tools.getString(this, "installationId"));
        binding.button.setOnClickListener(view -> openAssignFolder(getExternalFilesDir(null) + "/crash-report/" + crashReportName));

        if (DEBUG_MODE && !debugLogName.equals("NONE")) {
            runOnUiThread(() -> {
                try {
                    binding.buttonDebugLog.setVisibility(View.VISIBLE);
                } catch (Exception ignore) {
                }
            });
            binding.buttonDebugLog.setOnClickListener(v -> openAssignFolder(getExternalFilesDir(null) + "/logs/" + debugLogName));
        }


        try {
            File internalFiles = new File(Objects.requireNonNull(this.getFilesDir().getParent()));
            traverseFilesAndDelete(internalFiles);
        } catch (Exception ignore) {
        }

        if (Tools.getString(this, crashReportName + "-url").contains("https://pastebin.com/")) {
            binding.buttonUrl.setOnClickListener(v -> {
                ClipboardManager clipboard = (ClipboardManager) getApplication().getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("ScannerLog", Tools.getString(this, crashReportName + "-url"));
                clipboard.setPrimaryClip(clip);
            });

            runOnUiThread(() -> {
                try {
                    binding.buttonUrl.setVisibility(View.VISIBLE);
                } catch (Exception ignore) {
                }
            });
        } else {
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    try {
                        StringBuilder stringBuilder = new StringBuilder("api_dev_key=UkAIUoDQRmtW5SI3hGr6Vuf-F5EALgPx&api_paste_name=");
                        stringBuilder.append(Tools.getString(getApplicationContext(), "crash-report-name"));
                        stringBuilder.append("&api_option=paste&api_paste_expire_date=1M&api_paste_code=");
                        String path = getExternalFilesDir(null) + "/crash-report/" + Tools.getString(getApplicationContext(), "crash-report-name");
                        File file = new File(path);
                        FileReader fileReader = new FileReader(file);
                        BufferedReader bufferedReader = new BufferedReader(fileReader);
                        String nextLine;
                        StringBuilder crashInfo = new StringBuilder();
                        while ((nextLine = bufferedReader.readLine()) != null) {
                            crashInfo.append(nextLine).append("\n");
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            stringBuilder.append(URLEncoder.encode(crashInfo.toString(), StandardCharsets.UTF_8));
                        } else {
                            stringBuilder.append(URLEncoder.encode(crashInfo.toString(), "UTF-8"));
                        }
                        String ret = Network.sendPost("https://pastebin.com/api/api_post.php", stringBuilder.toString(), null, true, false);
                        Logger.d("pastebin", ret);
                        if (ret.contains("https://pastebin.com/")) {
                            Tools.saveString(getApplicationContext(), crashReportName + "-url", ret);
                            try {
                                binding.buttonUrl.setOnClickListener(v -> {
                                    ClipboardManager clipboard = (ClipboardManager) getApplication().getSystemService(CLIPBOARD_SERVICE);
                                    ClipData clip = ClipData.newPlainText("ScannerLog", ret);
                                    clipboard.setPrimaryClip(clip);
                                });
                            } catch (Exception ignore) {
                            }

                            runOnUiThread(() -> {
                                try {
                                    binding.buttonUrl.setVisibility(View.VISIBLE);
                                } catch (Exception ignore) {
                                }
                            });
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

            }.start();
        }
    }


    private void openAssignFolder(String path) {
        File file = new File(path);
        if (!file.exists()) return;
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file));

        intent.setType("text/plain");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);


        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Tools.saveBoolean(this, "has_crash", false);
    }
}

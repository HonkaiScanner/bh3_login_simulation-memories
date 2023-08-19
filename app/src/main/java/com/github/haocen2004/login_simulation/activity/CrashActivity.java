package com.github.haocen2004.login_simulation.activity;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.core.content.FileProvider;

import com.github.haocen2004.login_simulation.R;
import com.github.haocen2004.login_simulation.databinding.ActivityCrashBinding;
import com.github.haocen2004.login_simulation.utils.Logger;
import com.github.haocen2004.login_simulation.utils.Network;
import com.github.haocen2004.login_simulation.utils.Tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class CrashActivity extends BaseActivity {
    ActivityCrashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCrashBinding.inflate(getLayoutInflater());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(binding.getRoot());
        binding.textView5.setText(getString(R.string.crash_hint2).replace("{packageName}", getPackageName()));
        binding.textView6.setText(Tools.getString(this, "crash-report-name"));
        binding.textView7.setText(Tools.getUUID(this));
        binding.textView8.setText(Tools.getString(this, "installationId"));
        binding.button.setOnClickListener(view -> openAssignFolder(getExternalFilesDir(null) + "/crash-report/" + Tools.getString(this, "crash-report-name")));
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
                    while ((nextLine = bufferedReader.readLine()) != null) {
                        stringBuilder.append(nextLine).append("\n");
                    }
                    String ret = Network.sendPost("https://pastebin.com/api/api_post.php", stringBuilder.toString(), null, true, false);
                    Logger.d("pastebin", ret);
                    if (ret.contains("https://pastebin.com/")) {
                        binding.buttonUrl.setOnClickListener(v -> {
                            ClipboardManager clipboard = (ClipboardManager) getApplication().getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("ScannerLog", ret);
                            clipboard.setPrimaryClip(clip);
                        });

                        runOnUiThread(() -> binding.buttonUrl.setVisibility(View.VISIBLE));
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

        }.start();
    }

    private void openAssignFolder(String path) {
        File file = new File(path);
        if (!file.exists()) return;
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file));

        intent.setType("text/plain");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);


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

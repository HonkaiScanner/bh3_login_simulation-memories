package com.github.haocen2004.login_simulation.activity;

import static com.github.haocen2004.login_simulation.utils.FileSizeUtil.getAutoFileOrFilesSize;

import android.content.Intent;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.github.haocen2004.login_simulation.adapter.LogInfoAdapter;
import com.github.haocen2004.login_simulation.adapter.LoggerAdapter;
import com.github.haocen2004.login_simulation.data.LogData;
import com.github.haocen2004.login_simulation.databinding.ActivityHistoryLogsBinding;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HistoryLogsActivity extends BaseActivity {

    ActivityHistoryLogsBinding binding;

    private String dirPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoryLogsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        dirPath = getExternalFilesDir(null) + "/logs/";
        LogInfoAdapter logInfoAdapter = new LogInfoAdapter(this);
        Intent intent = getIntent();
        if (intent.hasExtra("scanner:logPath")) {
            String time = intent.getStringExtra("scanner:logPath");
            if (time != null) {
                if (time.contains(":")) {
                    String logFileDate = time.split(" ")[0];
                    File logFile = new File(dirPath + "logs-" + logFileDate + ".log");
                    List<LogData> logData = new ArrayList<>();
                    try {
                        FileReader fileReader = new FileReader(logFile);
                        BufferedReader bufferedReader = new BufferedReader(fileReader);
                        String nextLine;
                        boolean readLog = false;
                        while ((nextLine = bufferedReader.readLine()) != null) {
                            if (nextLine.startsWith("===")) readLog = false;
                            if (nextLine.contains(time)) {
                                readLog = true;
                                continue;
                            }
                            if (readLog) {
                                nextLine = nextLine.substring(20);
                                String level = nextLine.substring(0, nextLine.indexOf("/"));
                                String tag = nextLine.substring(nextLine.indexOf("/") + 1, nextLine.indexOf(":"));
                                String message = nextLine.substring(nextLine.indexOf(":") + 2).replace("{%&n%}", "\\n").replace("{%&t%}", "\\t").replace("{%&r%}", "\\r");
                                logData.add(new LogData(level, tag, message));
                            }
                        }
                    } catch (IOException e) {
                        logData.add(new LogData("未找到日志！", "", ""));
                    }
                    LoggerAdapter loggerAdapter = new LoggerAdapter(this);
                    loggerAdapter.setAllLogs(logData);
                    binding.selectLogRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                    binding.selectLogRecyclerView.setAdapter(loggerAdapter);
                } else {
                    File logFile = new File(dirPath + "logs-" + time + ".log");
                    logInfoAdapter.setAllLogs(processLogFile(logFile));
                    binding.selectLogRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                    binding.selectLogRecyclerView.setAdapter(logInfoAdapter);
                }
            }
        } else {
            logInfoAdapter.setAllLogs(loadLogs());
            binding.selectLogRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            binding.selectLogRecyclerView.setAdapter(logInfoAdapter);
        }
    }

    private List<LogData> processLogFile(File logFile) {
        List<LogData> logData = new ArrayList<>();
        try {
            FileReader fileReader = new FileReader(logFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String nextLine, cacheName = null;
            int lineCount = 0;
            while ((nextLine = bufferedReader.readLine()) != null) {
                if (nextLine.startsWith("===")) {
                    if (lineCount != 0) {
                        logData.add(new LogData(cacheName, "", String.valueOf(lineCount)));
                    }
                    cacheName = nextLine.replace("=", "").strip();
                    lineCount = 0;
                } else {
                    lineCount++;
                }
            }
            if (lineCount != 0) {
                logData.add(new LogData(cacheName, "", String.valueOf(lineCount)));
            } else if (logData.size() == 0) {
                logData.add(new LogData("未找到日志！", "", ""));
            }
            return logData;
        } catch (IOException e) {
            e.printStackTrace();
            return logData;
        }
    }

    private List<LogData> loadLogs() {
        File dir = new File(dirPath);
        List<LogData> logData = new ArrayList<>();
        if (dir.exists()) {
            File[] logs = dir.listFiles();
            if (logs != null) {
                for (File log : logs) {
                    String logName = log.getName().replace("logs-", "").replace(".log", "");
                    logData.add(new LogData(logName, String.valueOf(processLogFile(log).size()), getAutoFileOrFilesSize(log)));
                }
            }
        }
        if (logData.size() == 0) {
            logData.add(new LogData("未找到日志！", "", ""));
        }
        return logData;
    }
}
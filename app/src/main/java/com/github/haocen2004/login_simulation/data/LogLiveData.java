package com.github.haocen2004.login_simulation.data;

import static com.github.haocen2004.login_simulation.data.Constant.DEBUG_MODE;

import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;

public class LogLiveData extends LiveData<List<LogData>> {
    private final List<LogData> logDataList;
    private final List<LogData> fullLogDataList;
    private volatile static LogLiveData INSTANCE;

    public LogLiveData() {
        logDataList = new ArrayList<>();
        fullLogDataList = new ArrayList<>();
        logDataList.add(new LogData("长按", "复制日志", "长按该条复制所有"));
        fullLogDataList.add(new LogData("长按", "复制日志", "长按该条复制所有"));
        postValue(logDataList);
    }

    public static LogLiveData getINSTANCE() {
        if (INSTANCE == null) {
            synchronized (LogLiveData.class) {
                if (INSTANCE == null) {
                    INSTANCE = new LogLiveData();
                }
            }
        }
        return INSTANCE;
    }

    public List<LogData> getDebugLogList() {
        return fullLogDataList;
    }

    public void addNewLog(String level, String TAG, String msg) {
        LogData newLogData = new LogData(level, TAG, msg);
        fullLogDataList.add(newLogData);
        if (!level.equalsIgnoreCase("debug")) {
            logDataList.add(newLogData);
            postValue(logDataList);
        }
        if (DEBUG_MODE) {
            postValue(fullLogDataList);
        } else {
            postValue(logDataList);
        }
    }

}

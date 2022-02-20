package com.github.haocen2004.login_simulation.data;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

public class LogLiveData extends LiveData<List<LogData>> {
    private final Context context;
    private MutableLiveData<List<LogData>> logLiveData;
    private final List<LogData> logDataList;
    private static LogLiveData INSTANCE;

    public LogLiveData(Context context) {
        this.context = context.getApplicationContext();
        logDataList = new ArrayList<>();
        postValue(logDataList);
    }

    public static LogLiveData getINSTANCE(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new LogLiveData(context);
        }
        return INSTANCE;
    }

    public void addNewLog(String level, String TAG, String msg) {
        LogData newLogData = new LogData(level, TAG, msg);
        logDataList.add(newLogData);
        postValue(logDataList);
    }

}

package com.github.haocen2004.login_simulation.data.dialog;

import androidx.lifecycle.LiveData;

import com.github.haocen2004.login_simulation.utils.Logger;

import java.util.ArrayList;
import java.util.List;

public class DialogLiveData extends LiveData<List<DialogData>> {
    private static DialogLiveData INSTANCE;
    private final List<DialogData> logDataList;
    private boolean hasDataCache = false;

    public DialogLiveData() {
        logDataList = new ArrayList<>();
    }

    public static DialogLiveData getINSTANCE() {
        if (INSTANCE == null) {
            INSTANCE = new DialogLiveData();
            Logger.d("DialogLiveData", "created new INSTANCE.");
        }
        return INSTANCE;
    }

    public void addNewDialog(DialogData dialog) {
        Logger.d("DialogLiveData", "addNewDialog:" + dialog.getTitle());
        logDataList.add(dialog);
        postValue(logDataList);
    }

    public void addDelayDialog(DialogData dialog) {
        Logger.d("DialogLiveData", "addNewDialog:" + dialog.getTitle());
        logDataList.add(dialog);
        hasDataCache = true;
    }

    public void notifyDialog() {
        if (hasDataCache) {
            postValue(logDataList);
            hasDataCache = false;
        }
    }

    public void insertEulaDialog(DialogData dialogData) {
        logDataList.add(0, dialogData);
        postValue(logDataList);
    }
}

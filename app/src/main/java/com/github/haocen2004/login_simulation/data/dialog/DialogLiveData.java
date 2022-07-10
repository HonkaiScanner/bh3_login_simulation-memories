package com.github.haocen2004.login_simulation.data.dialog;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.github.haocen2004.login_simulation.util.Logger;

import java.util.ArrayList;
import java.util.List;

public class DialogLiveData extends LiveData<List<DialogData>> {
    private final Context context;
    private final List<DialogData> logDataList;
    private static DialogLiveData INSTANCE;

    public DialogLiveData(Context context) {
        this.context = context.getApplicationContext();
        logDataList = new ArrayList<>();
    }

    public static DialogLiveData getINSTANCE(Context context) {
        if (INSTANCE == null) {
            Logger.d("DialogLiveData", "create new INSTANCE.");
            INSTANCE = new DialogLiveData(context);
        }
        return INSTANCE;
    }

    public void addNewDialog(DialogData dialog) {
        logDataList.add(dialog);
        postValue(logDataList);
    }

    public void insertNewDialog(DialogData oldDialog, DialogData newDialog) {
        int currPos = logDataList.indexOf(oldDialog);
        logDataList.add(currPos + 1, newDialog);
        postValue(logDataList);

    }
}

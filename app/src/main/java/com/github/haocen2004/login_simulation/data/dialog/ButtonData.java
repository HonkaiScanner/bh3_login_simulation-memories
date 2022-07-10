package com.github.haocen2004.login_simulation.data.dialog;

import com.github.haocen2004.login_simulation.util.DialogHelper;
import com.github.haocen2004.login_simulation.util.Logger;

public class ButtonData {
    private final String text;

    public ButtonData(String text) {
        this.text = text;
    }

    public void callback(DialogHelper dialogHelper) {
        dialogHelper.getCurrDialog().dismiss();
        dialogHelper.setCurrPos(dialogHelper.getCurrPos() + 1);
        dialogHelper.setCurrClose(true);
        Logger.d("dialog", "default close.");
    }

    public String getText() {
        return text;
    }
}

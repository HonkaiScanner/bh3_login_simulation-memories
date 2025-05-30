package xyz.hellocraft.login_simulation.data.dialog;

import android.content.Context;

import xyz.hellocraft.login_simulation.utils.DialogHelper;
import xyz.hellocraft.login_simulation.utils.Logger;

public class ButtonData {
    private final String text;

    public ButtonData(String text) {
        this.text = text;
    }

    public void callback(DialogHelper dialogHelper) {
        dialogHelper.getCurrDialog().dismiss();
        Logger.d("dialog", "default close.");
        dialogHelper.next();
    }

    public void callback(DialogHelper dialogHelper, Context context) {
        callback(dialogHelper);
    }

    public String getText() {
        return text;
    }
}

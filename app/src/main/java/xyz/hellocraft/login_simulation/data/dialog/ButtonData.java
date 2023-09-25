package xyz.hellocraft.login_simulation.data.dialog;

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
        dialogHelper.setCurrClose(true);
        dialogHelper.next();
    }

    public String getText() {
        return text;
    }
}

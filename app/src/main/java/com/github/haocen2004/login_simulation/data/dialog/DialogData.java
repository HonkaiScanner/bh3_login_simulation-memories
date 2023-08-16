package com.github.haocen2004.login_simulation.data.dialog;

public class DialogData {
    private final String title;
    private final String message;
    private ButtonData NegativeButtonData = null;
    private ButtonData NeutralButtonData = null;
    private ButtonData PositiveButtonData = null;
    private boolean cancelable = false;

    public DialogData(String title, String message) {
        this.title = title;
        this.message = message;
    }

    public DialogData(String title, String message, String PositiveButtonText) {
        this.title = title;
        this.message = message;
        this.PositiveButtonData = new ButtonData(PositiveButtonText);
    }

    public boolean isCancelable() {
        return cancelable;
    }

    public void setCancelable(boolean cancelable) {
        this.cancelable = cancelable;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public ButtonData getNegativeButtonData() {
        return NegativeButtonData;
    }

    public void setNegativeButtonData(ButtonData negativeButtonData) {
        NegativeButtonData = negativeButtonData;
    }

    public void setNegativeButtonData(String negativeButtonText) {
        NegativeButtonData = new ButtonData(negativeButtonText);
    }

    public ButtonData getNeutralButtonData() {
        return NeutralButtonData;
    }

    public void setNeutralButtonData(ButtonData neutralButtonData) {
        NeutralButtonData = neutralButtonData;
    }

    public void setNeutralButtonData(String neutralButtonText) {
        NeutralButtonData = new ButtonData(neutralButtonText);
    }

    public ButtonData getPositiveButtonData() {
        return PositiveButtonData;
    }

    public void setPositiveButtonData(ButtonData positiveButtonData) {
        PositiveButtonData = positiveButtonData;

    }

    public void setPositiveButtonData(String positiveButtonText) {
        PositiveButtonData = new ButtonData(positiveButtonText);
    }
}

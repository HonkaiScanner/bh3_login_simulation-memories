package xyz.hellocraft.login_simulation.data;

import org.jetbrains.annotations.NotNull;

public class LogData {
    private final String level;
    private final String TAG;
    private final String message;
    private final String time;

    public LogData(String level, String TAG, String message, String time) {
        this.level = level;
        this.TAG = TAG;
        this.message = message;
        this.time = time;
    }

    public String getLevel() {
        return level;
    }

    public String getTAG() {
        return TAG;
    }

    public String getMessage() {
        return message;
    }

    public String getTime() {
        return time;
    }

    @NotNull
    @Override
    public String toString() {
        return "{" +
                "time='" + time + '\'' +
                "level='" + level + '\'' +
                ", TAG='" + TAG + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}

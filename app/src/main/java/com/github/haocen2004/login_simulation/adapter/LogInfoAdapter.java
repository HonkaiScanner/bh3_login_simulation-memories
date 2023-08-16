package com.github.haocen2004.login_simulation.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.haocen2004.login_simulation.R;
import com.github.haocen2004.login_simulation.activity.HistoryLogsActivity;
import com.github.haocen2004.login_simulation.data.LogData;
import com.github.haocen2004.login_simulation.data.dialog.ButtonData;
import com.github.haocen2004.login_simulation.data.dialog.DialogData;
import com.github.haocen2004.login_simulation.data.dialog.DialogLiveData;
import com.github.haocen2004.login_simulation.utils.DialogHelper;
import com.github.haocen2004.login_simulation.utils.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LogInfoAdapter extends RecyclerView.Adapter<LogInfoAdapter.LogInfoViewHolder> {
    private final Activity activity;
    private List<LogData> allLogs = new ArrayList<>();

    public LogInfoAdapter(Activity activity) {
        this.activity = activity;
    }

    public void setAllLogs(List<LogData> allLogs) {
        this.allLogs = allLogs;
    }

    @NonNull
    @Override
    public LogInfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View itemView = layoutInflater.inflate(R.layout.log_view, parent, false);
        return new LogInfoViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final LogInfoViewHolder holder, final int position) {
        LogData LoggerData = allLogs.get(position);
//        holder.textViewNumber.setText(String.valueOf(position + 1 ));
        holder.textViewMessage.setText(LoggerData.getMessage());
        holder.textViewLevel.setText(LoggerData.getLevel());
        holder.textViewHint.setText(LoggerData.getTAG());
        if (LoggerData.getTAG().length() > 2) {
            holder.textViewHint.setVisibility(View.VISIBLE);
        }
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(activity, HistoryLogsActivity.class);
            intent.putExtra("scanner:logPath", LoggerData.getLevel());
            activity.startActivity(intent);
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (LoggerData.getLevel().contains(":")) {
                Logger.getLogger(null).makeToast("你只能按天删除日志文件！");
                return true;
            }
            String dirPath = activity.getExternalFilesDir(null) + "/logs/";
            DialogData dialogData = new DialogData("删除日志", "确认删除 " + LoggerData.getLevel() + " 的日志吗？");
            dialogData.setPositiveButtonData(new ButtonData("确认") {
                @Override
                public void callback(DialogHelper dialogHelper) {
                    File logFile = new File(dirPath + "logs-" + LoggerData.getLevel() + ".log");
                    logFile.delete();
                    allLogs.remove(holder.getBindingAdapterPosition());
                    notifyItemRemoved(holder.getBindingAdapterPosition());
                    super.callback(dialogHelper);
                }
            });
            dialogData.setNegativeButtonData("取消");
            DialogLiveData.getINSTANCE().addNewDialog(dialogData);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return allLogs.size();
    }

    static class LogInfoViewHolder extends RecyclerView.ViewHolder {
        TextView textViewNumber, textViewMessage, textViewLevel, textViewHint;

        LogInfoViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNumber = itemView.findViewById(R.id.textViewNumber);
            textViewMessage = itemView.findViewById(R.id.textViewMessage);
            textViewLevel = itemView.findViewById(R.id.textViewLevel);
            textViewHint = itemView.findViewById(R.id.textViewHint);

        }
    }
}
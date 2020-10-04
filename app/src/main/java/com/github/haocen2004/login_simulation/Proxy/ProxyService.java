package com.github.haocen2004.login_simulation.Proxy;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.github.haocen2004.login_simulation.R;
import com.github.megatronking.netbare.NetBareService;

public class ProxyService extends NetBareService {
    private String CHANNEL_ID = "bh3_login_simulation_proxy_service_channel";

    public ProxyService() {
    }

    @Override
    protected int notificationId() {
        return 1;

    }

    @NonNull
    @Override
    protected Notification createNotification() {

        createNotificationChannel();
        return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("textTitle")
                .setContentText("textContent")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}

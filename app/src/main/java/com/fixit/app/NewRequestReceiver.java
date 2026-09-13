package com.fixit.app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

// Owner: Person 5 (Request Data Layer + Broadcast)
// Registered in AndroidManifest.xml for action "com.fixit.app.NEW_REQUEST".
public class NewRequestReceiver extends BroadcastReceiver {

    public static final String CHANNEL_ID = "new_request_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: Person 5 - read request details from intent extras

        createNotificationChannel(context);

        // TODO: Person 5 - build and show a NotificationCompat notification to the landlord
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "New Maintenance Requests",
                    NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}

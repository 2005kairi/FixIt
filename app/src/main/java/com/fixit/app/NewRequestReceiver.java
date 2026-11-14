package com.fixit.app;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class NewRequestReceiver extends BroadcastReceiver {
    public static final String CHANNEL_ID = "new_request_channel";
    public static final String ACTION_NEW_REQUEST =
            "com.fixit.app.NEW_REQUEST";
    public static final String ACTION_NEW_REQUEST_REFRESH =
            "com.fixit.app.NEW_REQUEST_REFRESH";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null ||
                !ACTION_NEW_REQUEST.equals(intent.getAction())) return;
        createNotificationChannel(context);

        Intent refresh = new Intent(ACTION_NEW_REQUEST_REFRESH);
        refresh.setPackage(context.getPackageName());
        context.sendBroadcast(refresh);

        Intent open = new Intent(context,
                LandlordHomeActivity.class);
        open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pending = PendingIntent.getActivity(context,
                0, open, PendingIntent.FLAG_UPDATE_CURRENT |
                PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("New Maintenance Request")
                .setContentText("A tenant submitted a new request.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pending)
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(context,
                Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) return;
        NotificationManager manager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) manager.notify(1, builder.build());
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "New Maintenance Requests",
                    NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager =
                    context.getSystemService(NotificationManager.class);
            if (manager != null)
                manager.createNotificationChannel(channel);
        }
    }
}

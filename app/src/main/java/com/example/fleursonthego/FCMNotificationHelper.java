package com.example.fleursonthego;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.content.Context;
import androidx.core.app.NotificationCompat;

public class FCMNotificationHelper {

    private static final String CHANNEL_ID = "default_channel";
    private static final int NOTIFICATION_ID = 1;

    // Method to send notification
    public static void sendNotification(Context context, String title, String body) {
        // Create the notification channel (required for Android 8.0 and higher)
        createNotificationChannel(context);

        // Build the notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.ic_notification) // Use your app's notification icon
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(true);

        // Get the NotificationManager system service and display the notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    // Method to create a notification channel (required for Android 8.0 and higher)
    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create a notification channel for Android 8.0 (API level 26) and above
            CharSequence name = "Default Channel";
            String description = "This is the default notification channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            // Register the channel with the system
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}

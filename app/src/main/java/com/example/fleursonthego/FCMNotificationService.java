package com.example.fleursonthego;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FCMNotificationService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "default_channel";
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Check if the message contains notification payload
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            // Handle the notification
            FCMNotificationHelper.sendNotification(this, title, body);
        }

        // Check if the message contains data payload
        if (remoteMessage.getData().size() > 0) {
            String title = remoteMessage.getData().get("title");
            String body = remoteMessage.getData().get("body");
            // Handle the data payload
            FCMNotificationHelper.sendNotification(this, title, body);
        }
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        // Log or send the token to your server to associate with the user/device
        // Example: Log.d("FCM", "New Token: " + token);
    }
}

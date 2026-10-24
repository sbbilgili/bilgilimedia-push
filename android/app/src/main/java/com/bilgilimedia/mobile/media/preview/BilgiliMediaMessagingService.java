package com.bilgilimedia.mobile.media.preview;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class BilgiliMediaMessagingService extends FirebaseMessagingService {
    private static final String CHANNEL_ID = "bilgilimedia_content";

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        MainActivity.syncTopicsFromCachedConfig(this);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Map<String, String> data = remoteMessage.getData();
        String title = data.get("title");
        String body = data.get("body");
        String articleId = data.get("article_id");

        if (title == null || title.trim().isEmpty()) title = "BilgiliMedia";
        if (body == null || body.trim().isEmpty()) body = "Yeni içerik yayımlandı.";

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "BilgiliMedia içerikleri",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Seçtiğiniz kategorilerde yeni içerik bildirimleri.");
            manager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (articleId != null) intent.putExtra("article_id", articleId);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                articleId == null ? 1408 : articleId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        android.app.Notification.Builder builder =
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                        ? new android.app.Notification.Builder(this, CHANNEL_ID)
                        : new android.app.Notification.Builder(this);

        builder.setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new android.app.Notification.BigTextStyle().bigText(body))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        manager.notify(
                articleId == null ? (int) System.currentTimeMillis() : articleId.hashCode(),
                builder.build());
    }
}

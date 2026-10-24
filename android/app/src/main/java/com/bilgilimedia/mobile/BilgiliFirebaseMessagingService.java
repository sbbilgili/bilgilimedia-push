package com.bilgilimedia.mobile;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class BilgiliFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        new LocalStore(this).setFcmToken(token);
        BilgiliMediaApp.syncTopicsFromCache(this);
    }

    @Override
    public void onMessageReceived(RemoteMessage message) {
        super.onMessageReceived(message);
        LocalStore store = new LocalStore(this);
        if (!store.notificationMaster()) return;
        Map<String, String> data = message.getData();
        String articleId = data.get("article_id");
        String title = data.get("title");
        String body = data.get("body");
        NotificationUtil.show(this, articleId, title, body);
    }
}

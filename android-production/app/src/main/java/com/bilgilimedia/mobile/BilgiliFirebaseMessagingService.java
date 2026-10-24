package com.bilgilimedia.mobile;

import android.content.SharedPreferences;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class BilgiliFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        SharedPreferences prefs =
                getSharedPreferences(DataRepository.PREFS, MODE_PRIVATE);
        prefs.edit()
                .putString("fcm_token", token == null ? "" : token)
                .putBoolean("firebase_ready", token != null && !token.isEmpty())
                .apply();

        try {
            String raw = prefs.getString("app_config_cache", "");
            if (raw != null && !raw.isEmpty()) {
                NotificationSync.sync(this, AppConfig.parse(raw));
            }
        } catch (Exception ignored) {
            // A later foreground/background sync will retry with a valid config.
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage message) {
        super.onMessageReceived(message);

        SharedPreferences prefs =
                getSharedPreferences(DataRepository.PREFS, MODE_PRIVATE);
        if (!prefs.getBoolean("notify_master", true)) return;
        if (!NotificationHelper.canNotify(this)) return;

        Map<String, String> data = message.getData();
        String id = value(data.get("article_id"));
        String categoryId = value(data.get("category_id"));
        String category = value(data.get("category"));
        String title = value(data.get("title"));
        String body = value(data.get("body"));

        if (!categoryAllowed(prefs, categoryId, category)) return;

        Set<String> notified = new HashSet<>(
                prefs.getStringSet("notified_content_ids", new HashSet<>()));
        if (!id.isEmpty() && notified.contains(id)) return;

        if (title.isEmpty()) title = "Yeni BilgiliMedia içeriği";
        if (body.isEmpty()) body = "Yeni içerik yayımlandı.";

        if (!NotificationHelper.show(this, id, category, title, body)) return;

        if (!id.isEmpty()) {
            notified.add(id);
            Set<String> known = new HashSet<>(
                    prefs.getStringSet("known_content_ids", new HashSet<>()));
            known.add(id);
            prefs.edit()
                    .putStringSet("notified_content_ids", notified)
                    .putStringSet("known_content_ids", known)
                    .apply();
        }
    }

    private boolean categoryAllowed(
            SharedPreferences prefs, String categoryId, String categoryName) {
        try {
            String raw = prefs.getString("app_config_cache", "");
            if (raw != null && !raw.isEmpty()) {
                AppConfig config = AppConfig.parse(raw);
                return NotificationSync.isCategorySelected(
                        this, config, categoryId, categoryName);
            }
        } catch (Exception ignored) {
            // Use the legacy name preference only if cached config cannot be parsed.
        }

        Set<String> selectedNames = new HashSet<>(
                prefs.getStringSet("notify_categories", new HashSet<>()));
        boolean legacyInitialized =
                prefs.getBoolean("notify_categories_initialized", false);
        if (legacyInitialized && selectedNames.isEmpty()) return false;
        return categoryName.isEmpty()
                || selectedNames.isEmpty()
                || selectedNames.contains(categoryName);
    }

    private static String value(String value) {
        return value == null ? "" : value;
    }
}

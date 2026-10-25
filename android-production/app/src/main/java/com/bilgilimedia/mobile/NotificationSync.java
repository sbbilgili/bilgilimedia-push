package com.bilgilimedia.mobile;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Firebase topic synchronization compatible with the golden 1.4.8 client.
 *
 * Golden behavior:
 * - notify_master defaults to false.
 * - notify_categories stores visible category names.
 * - an empty category selection means no category notifications.
 * - remote category name -> topic mapping comes from app-config.
 */
final class NotificationSync {
    private static final Object LOCK = new Object();
    private static final Set<String> IN_FLIGHT = new HashSet<>();

    private NotificationSync() {}

    static void sync(Context context, AppConfig config) {
        if (context == null) return;

        SharedPreferences prefs =
                context.getSharedPreferences(DataRepository.PREFS, Context.MODE_PRIVATE);
        boolean master = prefs.getBoolean("notify_master", false);
        Set<String> selectedNames = new LinkedHashSet<>(
                prefs.getStringSet("notify_categories", new HashSet<>()));

        JSONObject topicMap;
        try {
            topicMap = new JSONObject(prefs.getString("category_topic_map", "{}"));
        } catch (Exception ignored) {
            topicMap = new JSONObject();
        }

        Set<String> desired = new LinkedHashSet<>();
        if (master) {
            for (String name : selectedNames) {
                if (name == null || name.trim().isEmpty()) continue;
                String cleanName = name.trim();
                String topic = topicMap.optString(cleanName, "").trim();
                if (topic.isEmpty()) topic = AppConfig.slug(cleanName);
                if (!topic.isEmpty()) desired.add(topic);
            }
        }

        prefs.edit()
                .putStringSet("desired_fcm_topics", new HashSet<>(desired))
                .apply();

        Set<String> known = new HashSet<>(
                prefs.getStringSet("known_fcm_topics", new HashSet<>()));

        final FirebaseMessaging messaging;
        try {
            messaging = FirebaseMessaging.getInstance();
        } catch (RuntimeException e) {
            prefs.edit().putBoolean("firebase_ready", false).apply();
            return;
        }

        for (String oldTopic : known) {
            if (desired.contains(oldTopic)) {
                setState(prefs, oldTopic, "bağlı");
                continue;
            }
            unsubscribe(messaging, prefs, oldTopic);
        }

        for (String topic : desired) {
            if (known.contains(topic)) continue;
            subscribe(messaging, prefs, topic);
        }

        messaging.getToken().addOnCompleteListener(task -> {
            SharedPreferences.Editor editor = prefs.edit()
                    .putBoolean("firebase_ready", task.isSuccessful());
            if (task.isSuccessful() && task.getResult() != null) {
                editor.putString("fcm_token", task.getResult());
            }
            editor.apply();
        });
    }

    static boolean isCategorySelected(
            Context context,
            AppConfig config,
            String categoryId,
            String categoryName) {
        if (context == null || categoryName == null) return false;
        SharedPreferences prefs =
                context.getSharedPreferences(DataRepository.PREFS, Context.MODE_PRIVATE);
        Set<String> selected = new HashSet<>(
                prefs.getStringSet("notify_categories", new HashSet<>()));
        return selected.contains(categoryName);
    }

    private static void subscribe(
            FirebaseMessaging messaging, SharedPreferences prefs, String topic) {
        String operation = "sub:" + topic;
        if (!begin(operation)) return;

        setState(prefs, topic, "bağlanıyor");
        messaging.subscribeToTopic(topic).addOnCompleteListener(task -> {
            boolean reverse = false;
            try {
                if (task.isSuccessful()) {
                    updateKnown(prefs, topic, true, "bağlı");
                    reverse = !isCurrentlyDesired(prefs, topic);
                } else {
                    setState(prefs, topic, "hata");
                }
            } finally {
                end(operation);
            }
            if (reverse) unsubscribe(messaging, prefs, topic);
        });
    }

    private static void unsubscribe(
            FirebaseMessaging messaging, SharedPreferences prefs, String topic) {
        String operation = "unsub:" + topic;
        if (!begin(operation)) return;

        setState(prefs, topic, "ayrılıyor");
        messaging.unsubscribeFromTopic(topic).addOnCompleteListener(task -> {
            boolean reverse = false;
            try {
                if (task.isSuccessful()) {
                    updateKnown(prefs, topic, false, "kapalı");
                    reverse = isCurrentlyDesired(prefs, topic);
                } else {
                    setState(prefs, topic, "hata");
                }
            } finally {
                end(operation);
            }
            if (reverse) subscribe(messaging, prefs, topic);
        });
    }

    private static boolean isCurrentlyDesired(
            SharedPreferences prefs, String topic) {
        Set<String> desired = new HashSet<>(
                prefs.getStringSet("desired_fcm_topics", new HashSet<>()));
        return desired.contains(topic);
    }

    private static boolean begin(String operation) {
        synchronized (LOCK) {
            return IN_FLIGHT.add(operation);
        }
    }

    private static void end(String operation) {
        synchronized (LOCK) {
            IN_FLIGHT.remove(operation);
        }
    }

    private static void updateKnown(
            SharedPreferences prefs, String topic, boolean subscribed, String state) {
        synchronized (LOCK) {
            Set<String> known = new HashSet<>(
                    prefs.getStringSet("known_fcm_topics", new HashSet<>()));
            if (subscribed) known.add(topic);
            else known.remove(topic);

            prefs.edit()
                    .putStringSet("known_fcm_topics", known)
                    .putString("topic_state_" + topic, state)
                    .apply();
        }
    }

    private static void setState(SharedPreferences prefs, String topic, String state) {
        prefs.edit().putString("topic_state_" + topic, state).apply();
    }

    static void saveTopicMap(Context context, AppConfig config) {
        if (context == null || config == null) return;

        JSONObject nameToTopic = new JSONObject();
        for (Map.Entry<String, String> entry : config.categoryTopicMap().entrySet()) {
            try {
                nameToTopic.put(entry.getKey(), entry.getValue());
            } catch (Exception ignored) {
                // Keep other mappings if one insertion fails.
            }
        }

        context.getSharedPreferences(DataRepository.PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString("category_topic_map", nameToTopic.toString())
                .apply();
    }
}

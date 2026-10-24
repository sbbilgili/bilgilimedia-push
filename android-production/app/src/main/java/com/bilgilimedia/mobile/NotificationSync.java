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
 * Keeps Firebase topic subscriptions aligned with the user's local notification preferences.
 *
 * known_fcm_topics contains only subscriptions confirmed by Firebase. Failed operations are
 * not recorded as successful, so a later sync can retry them safely.
 */
final class NotificationSync {
    private static final Object LOCK = new Object();
    private static final Set<String> IN_FLIGHT = new HashSet<>();

    private NotificationSync() {}

    static void sync(Context context, AppConfig config) {
        if (context == null || config == null) return;

        SharedPreferences prefs =
                context.getSharedPreferences(DataRepository.PREFS, Context.MODE_PRIVATE);

        boolean master = prefs.getBoolean("notify_master", true);
        Set<String> selected = new HashSet<>(
                prefs.getStringSet("notify_categories", new HashSet<>()));
        boolean initialized = prefs.getBoolean("notify_categories_initialized", false);

        if (!initialized) {
            for (Models.Category category : config.categories) {
                if (category.name != null && !category.name.isEmpty()) {
                    selected.add(category.name);
                }
            }
            prefs.edit()
                    .putStringSet("notify_categories", new HashSet<>(selected))
                    .putBoolean("notify_categories_initialized", true)
                    .apply();
        }

        Set<String> desired = new LinkedHashSet<>();
        if (master) {
            for (Models.Category category : config.categories) {
                if (category.topic == null || category.topic.isEmpty()) continue;
                if (selected.contains(category.name)) desired.add(category.topic);
            }
        }

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

    private static void subscribe(
            FirebaseMessaging messaging, SharedPreferences prefs, String topic) {
        String operation = "sub:" + topic;
        if (!begin(operation)) return;

        setState(prefs, topic, "bağlanıyor");
        messaging.subscribeToTopic(topic).addOnCompleteListener(task -> {
            try {
                if (task.isSuccessful()) {
                    updateKnown(prefs, topic, true, "bağlı");
                } else {
                    setState(prefs, topic, "hata");
                }
            } finally {
                end(operation);
            }
        });
    }

    private static void unsubscribe(
            FirebaseMessaging messaging, SharedPreferences prefs, String topic) {
        String operation = "unsub:" + topic;
        if (!begin(operation)) return;

        setState(prefs, topic, "ayrılıyor");
        messaging.unsubscribeFromTopic(topic).addOnCompleteListener(task -> {
            try {
                if (task.isSuccessful()) {
                    updateKnown(prefs, topic, false, "kapalı");
                } else {
                    setState(prefs, topic, "hata");
                }
            } finally {
                end(operation);
            }
        });
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
        JSONObject map = new JSONObject();
        for (Map.Entry<String, String> entry : config.categoryTopicMap().entrySet()) {
            try {
                map.put(entry.getKey(), entry.getValue());
            } catch (Exception ignored) {
                // Both values are strings; keep the remaining valid mappings if one fails.
            }
        }
        context.getSharedPreferences(DataRepository.PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString("category_topic_map", map.toString())
                .apply();
    }
}

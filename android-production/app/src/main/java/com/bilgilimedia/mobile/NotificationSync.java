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
 * Keeps Firebase topic subscriptions aligned with stable category IDs.
 *
 * Legacy 1.4.8 preferences stored visible category names. They are migrated once to
 * notify_category_ids while the legacy name set is kept in sync for backward compatibility.
 * known_fcm_topics contains only operations confirmed by Firebase.
 */
final class NotificationSync {
    private static final String KEY_SELECTED_IDS = "notify_category_ids";
    private static final String KEY_SELECTED_IDS_INITIALIZED =
            "notify_category_ids_initialized";
    private static final String KEY_LEGACY_NAMES = "notify_categories";
    private static final String KEY_LEGACY_INITIALIZED =
            "notify_categories_initialized";

    private static final Object LOCK = new Object();
    private static final Set<String> IN_FLIGHT = new HashSet<>();

    private NotificationSync() {}

    static void sync(Context context, AppConfig config) {
        if (context == null || config == null) return;

        SharedPreferences prefs =
                context.getSharedPreferences(DataRepository.PREFS, Context.MODE_PRIVATE);
        Set<String> selectedIds = selectedCategoryIds(context, config);
        boolean master = prefs.getBoolean("notify_master", true);

        Set<String> desired = new LinkedHashSet<>();
        if (master) {
            for (Models.Category category : config.categories) {
                if (category.topic == null || category.topic.isEmpty()) continue;
                if (selectedIds.contains(category.id)) desired.add(category.topic);
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

    static Set<String> selectedCategoryIds(Context context, AppConfig config) {
        SharedPreferences prefs =
                context.getSharedPreferences(DataRepository.PREFS, Context.MODE_PRIVATE);
        synchronized (LOCK) {
            ensureSelectionMigrated(prefs, config);
            return new LinkedHashSet<>(
                    prefs.getStringSet(KEY_SELECTED_IDS, new HashSet<>()));
        }
    }

    static void setCategorySelected(
            Context context, AppConfig config, String categoryId, boolean selected) {
        SharedPreferences prefs =
                context.getSharedPreferences(DataRepository.PREFS, Context.MODE_PRIVATE);
        synchronized (LOCK) {
            ensureSelectionMigrated(prefs, config);
            Set<String> ids = new LinkedHashSet<>(
                    prefs.getStringSet(KEY_SELECTED_IDS, new HashSet<>()));
            if (selected) ids.add(categoryId);
            else ids.remove(categoryId);
            sanitizeIds(ids, config);
            persistSelection(prefs, config, ids);
        }
    }

    static boolean isCategorySelected(
            Context context,
            AppConfig config,
            String categoryId,
            String categoryName) {
        Set<String> ids = selectedCategoryIds(context, config);
        if (ids.isEmpty()) return false;

        if (categoryId != null && !categoryId.isEmpty()) {
            return ids.contains(categoryId);
        }
        for (Models.Category category : config.categories) {
            if (category.name.equals(categoryName)) return ids.contains(category.id);
        }
        return false;
    }

    private static void ensureSelectionMigrated(
            SharedPreferences prefs, AppConfig config) {
        if (config == null) return;

        Set<String> ids = new LinkedHashSet<>();
        boolean idsInitialized = prefs.getBoolean(KEY_SELECTED_IDS_INITIALIZED, false);

        if (idsInitialized) {
            ids.addAll(prefs.getStringSet(KEY_SELECTED_IDS, new HashSet<>()));
            sanitizeIds(ids, config);
            persistSelection(prefs, config, ids);
            return;
        }

        boolean legacyInitialized = prefs.getBoolean(KEY_LEGACY_INITIALIZED, false);
        if (legacyInitialized) {
            Set<String> legacyNames = new HashSet<>(
                    prefs.getStringSet(KEY_LEGACY_NAMES, new HashSet<>()));
            for (Models.Category category : config.categories) {
                if (legacyNames.contains(category.name)) ids.add(category.id);
            }
        } else {
            for (Models.Category category : config.categories) {
                ids.add(category.id);
            }
        }
        persistSelection(prefs, config, ids);
    }

    private static void sanitizeIds(Set<String> ids, AppConfig config) {
        Set<String> valid = new HashSet<>();
        for (Models.Category category : config.categories) valid.add(category.id);
        ids.retainAll(valid);
    }

    private static void persistSelection(
            SharedPreferences prefs, AppConfig config, Set<String> ids) {
        Set<String> legacyNames = new LinkedHashSet<>();
        for (Models.Category category : config.categories) {
            if (ids.contains(category.id)) legacyNames.add(category.name);
        }

        prefs.edit()
                .putStringSet(KEY_SELECTED_IDS, new HashSet<>(ids))
                .putBoolean(KEY_SELECTED_IDS_INITIALIZED, true)
                .putStringSet(KEY_LEGACY_NAMES, new HashSet<>(legacyNames))
                .putBoolean(KEY_LEGACY_INITIALIZED, true)
                .apply();
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
        JSONObject idToTopic = new JSONObject();
        JSONObject nameToId = new JSONObject();
        for (Map.Entry<String, String> entry : config.categoryTopicMap().entrySet()) {
            try {
                nameToTopic.put(entry.getKey(), entry.getValue());
            } catch (Exception ignored) {
                // Keep other mappings if a single JSON insertion fails.
            }
        }
        for (Models.Category category : config.categories) {
            try {
                idToTopic.put(category.id, category.topic);
                nameToId.put(category.name, category.id);
            } catch (Exception ignored) {
                // Keep other mappings if a single JSON insertion fails.
            }
        }

        context.getSharedPreferences(DataRepository.PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString("category_topic_map", nameToTopic.toString())
                .putString("category_id_topic_map", idToTopic.toString())
                .putString("category_name_id_map", nameToId.toString())
                .apply();
    }
}

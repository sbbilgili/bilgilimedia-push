package com.bilgilimedia.mobile;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

final class LocalStore {
    private static final String PREFS = "bilgilimedia_prefs";
    private final SharedPreferences prefs;

    LocalStore(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    Set<String> savedArticles() {
        return new LinkedHashSet<>(prefs.getStringSet("saved_articles", new HashSet<>()));
    }

    void toggleArticle(String id) {
        Set<String> values = savedArticles();
        if (values.contains(id)) values.remove(id); else values.add(id);
        prefs.edit().putStringSet("saved_articles", new HashSet<>(values)).apply();
    }

    Set<String> savedAccounts() {
        return new LinkedHashSet<>(prefs.getStringSet("saved_accounts", new HashSet<>()));
    }

    void toggleAccount(String url) {
        Set<String> values = savedAccounts();
        if (values.contains(url)) values.remove(url); else values.add(url);
        prefs.edit().putStringSet("saved_accounts", new HashSet<>(values)).apply();
    }

    List<String> history() {
        List<String> result = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(prefs.getString("history_all", "[]"));
            for (int i = 0; i < array.length(); i++) {
                String id = array.optString(i, "");
                if (!id.isEmpty()) result.add(id);
            }
        } catch (Exception ignored) {}
        return result;
    }

    void addHistory(String id) {
        List<String> history = history();
        history.remove(id);
        history.add(0, id);
        while (history.size() > 80) history.remove(history.size() - 1);
        JSONArray array = new JSONArray();
        for (String value : history) array.put(value);
        prefs.edit()
                .putString("history_all", array.toString())
                .putLong("history_time_" + id, System.currentTimeMillis())
                .apply();
        incrementRead(id);
    }

    long historyTime(String id) {
        return prefs.getLong("history_time_" + id, 0L);
    }

    void clearHistory() {
        SharedPreferences.Editor editor = prefs.edit().remove("history_all");
        for (String id : history()) editor.remove("history_time_" + id);
        editor.apply();
    }

    int reads(String id) {
        return prefs.getInt("reads_" + id, 0);
    }

    private void incrementRead(String id) {
        prefs.edit().putInt("reads_" + id, reads(id) + 1).apply();
    }

    String theme() {
        return prefs.getString("theme", "system");
    }

    void setTheme(String value) {
        prefs.edit().putString("theme", value).apply();
    }

    boolean notificationMaster() {
        return prefs.getBoolean("notify_master", true);
    }

    void setNotificationMaster(boolean value) {
        prefs.edit().putBoolean("notify_master", value).apply();
    }

    boolean topicEnabled(String topic) {
        return prefs.getBoolean("topic_state_" + topic, true);
    }

    void setTopicEnabled(String topic, boolean value) {
        prefs.edit().putBoolean("topic_state_" + topic, value).apply();
    }

    void setFcmToken(String token) {
        prefs.edit().putString("fcm_token", token == null ? "" : token).apply();
    }

    String fcmToken() {
        return prefs.getString("fcm_token", "");
    }

    Set<String> knownFcmTopics() {
        return prefs.getStringSet("known_fcm_topics", new HashSet<>());
    }

    void setKnownFcmTopics(Set<String> values) {
        prefs.edit().putStringSet("known_fcm_topics", new HashSet<>(values)).apply();
    }

    void putConfigCache(String raw) {
        prefs.edit().putString("app_config_cache", raw).apply();
    }

    String configCache() {
        return prefs.getString("app_config_cache", "");
    }

    void putFeedVersion(int value) {
        prefs.edit().putInt("feed_version", value).apply();
    }

    int feedVersion() {
        return prefs.getInt("feed_version", 1);
    }
}

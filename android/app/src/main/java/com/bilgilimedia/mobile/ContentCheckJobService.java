package com.bilgilimedia.mobile;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

public class ContentCheckJobService extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        new Thread(() -> {
            try {
                check();
            } catch (Exception ignored) {
            } finally {
                jobFinished(params, false);
            }
        }).start();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    private void check() throws Exception {
        String raw = DataRepository.fetch(DataRepository.FEED_URL);
        JSONObject root = new JSONObject(raw);
        JSONArray articles = root.optJSONArray("articles");
        if (articles == null) return;

        SharedPreferences prefs = getSharedPreferences("bilgilimedia_background", MODE_PRIVATE);
        Set<String> known = new HashSet<>(prefs.getStringSet("known_content_ids", new HashSet<>()));
        Set<String> current = new HashSet<>();
        LocalStore store = new LocalStore(this);
        boolean initialized = prefs.getBoolean("known_content_initialized", false);

        for (int i = 0; i < articles.length(); i++) {
            JSONObject item = articles.optJSONObject(i);
            if (item == null) continue;
            String id = item.optString("id", "");
            if (id.isEmpty()) continue;
            current.add(id);
            if (initialized && !known.contains(id) && store.notificationMaster()) {
                NotificationUtil.show(
                        this,
                        id,
                        item.optString("title", "Yeni BilgiliMedia içeriği"),
                        item.optString("summary", "Yeni içerik yayımlandı."));
            }
        }

        prefs.edit()
                .putBoolean("known_content_initialized", true)
                .putStringSet("known_content_ids", current)
                .apply();
    }
}

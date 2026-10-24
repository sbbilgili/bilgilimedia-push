package com.bilgilimedia.mobile;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class ContentCheckJobService extends JobService {
    private static final int JOB_ID = 14801;
    private static final long PERIOD_MS = 12L * 60L * 60L * 1000L;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile Future<?> runningTask;

    public static void schedule(Context context) {
        JobScheduler scheduler =
                (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (scheduler == null) return;

        JobInfo info = new JobInfo.Builder(
                JOB_ID, new ComponentName(context, ContentCheckJobService.class))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .setPeriodic(PERIOD_MS)
                .build();
        scheduler.schedule(info);
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        runningTask = executor.submit(() -> {
            try {
                check();
            } catch (Exception ignored) {
                // Transient network/config failures are retried by the next scheduled run.
            } finally {
                if (!Thread.currentThread().isInterrupted()) {
                    jobFinished(params, false);
                }
            }
        });
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Future<?> task = runningTask;
        runningTask = null;
        if (task != null) task.cancel(true);
        return true;
    }

    @Override
    public void onDestroy() {
        Future<?> task = runningTask;
        if (task != null) task.cancel(true);
        executor.shutdownNow();
        super.onDestroy();
    }

    private void check() throws Exception {
        SharedPreferences prefs =
                getSharedPreferences(DataRepository.PREFS, MODE_PRIVATE);
        if (!prefs.getBoolean("notify_master", true)) return;
        if (!NotificationHelper.canNotify(this)) return;

        DataRepository.Snapshot snapshot = DataRepository.load(this, true);
        if (Thread.currentThread().isInterrupted()) return;

        NotificationSync.saveTopicMap(this, snapshot.config);
        NotificationSync.sync(this, snapshot.config);

        Set<String> now = new HashSet<>();
        for (Models.Article article : snapshot.articles) now.add(article.id);

        if (!prefs.getBoolean("known_content_initialized", false)) {
            DataRepository.setKnownIds(this, now);
            return;
        }

        Set<String> selectedIds =
                NotificationSync.selectedCategoryIds(this, snapshot.config);
        if (selectedIds.isEmpty()) {
            DataRepository.setKnownIds(this, now);
            return;
        }

        Set<String> known = DataRepository.getKnownIds(this);
        Set<String> notified = new HashSet<>(
                prefs.getStringSet("notified_content_ids", new HashSet<>()));

        for (Models.Article article : snapshot.articles) {
            if (Thread.currentThread().isInterrupted()) return;
            if (known.contains(article.id) || notified.contains(article.id)) continue;
            if (!NotificationSync.isCategorySelected(
                    this, snapshot.config, article.categoryId, article.category)) {
                continue;
            }

            if (NotificationHelper.show(
                    this, article.id, article.category, article.title, article.summary)) {
                notified.add(article.id);
            }
        }

        prefs.edit()
                .putStringSet("known_content_ids", now)
                .putStringSet("notified_content_ids", notified)
                .apply();
    }
}

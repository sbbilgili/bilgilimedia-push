package com.bilgilimedia.mobile;

import android.app.Application;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashSet;
import java.util.Set;

public class BilgiliMediaApp extends Application {
    private static volatile boolean firebaseReady = false;
    private static final int JOB_ID = 1408;

    @Override
    public void onCreate() {
        super.onCreate();
        initializeFirebase();
        scheduleContentCheck(this);
        syncTopicsFromCache(this);
    }

    private void initializeFirebase() {
        if (!BuildConfig.FIREBASE_ENABLED) return;
        if (BuildConfig.FIREBASE_API_KEY == null || BuildConfig.FIREBASE_API_KEY.trim().isEmpty()) return;
        if (BuildConfig.FIREBASE_APP_ID == null || BuildConfig.FIREBASE_APP_ID.trim().isEmpty()) return;
        try {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setApiKey(BuildConfig.FIREBASE_API_KEY)
                    .setApplicationId(BuildConfig.FIREBASE_APP_ID)
                    .setProjectId(BuildConfig.FIREBASE_PROJECT_ID)
                    .setGcmSenderId(BuildConfig.FIREBASE_SENDER_ID)
                    .setStorageBucket(BuildConfig.FIREBASE_PROJECT_ID + ".firebasestorage.app")
                    .build();
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this, options);
            }
            firebaseReady = !FirebaseApp.getApps(this).isEmpty();
        } catch (Exception ignored) {
            firebaseReady = false;
        }
    }

    static boolean isFirebaseReady() {
        return firebaseReady;
    }

    static void scheduleContentCheck(Context context) {
        try {
            JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            if (scheduler == null) return;
            ComponentName component = new ComponentName(context, ContentCheckJobService.class);
            JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, component)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setPersisted(true)
                    .setPeriodic(6L * 60L * 60L * 1000L);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.setRequiresBatteryNotLow(false);
            }
            scheduler.schedule(builder.build());
        } catch (Exception ignored) {}
    }

    static void syncTopicsFromCache(Context context) {
        if (!firebaseReady) return;
        try {
            LocalStore store = new LocalStore(context);
            String raw = store.configCache();
            if (raw == null || raw.trim().isEmpty()) return;
            Models.Config config = DataRepository.parseConfig(raw);
            syncTopics(context, config);
        } catch (Exception ignored) {}
    }

    static void syncTopics(Context context, Models.Config config) {
        if (!firebaseReady || config == null) return;
        LocalStore store = new LocalStore(context);
        Set<String> wanted = new HashSet<>();
        if (store.notificationMaster()) {
            for (Models.Category category : config.categories) {
                if (category.enabled && store.topicEnabled(category.topic)) {
                    wanted.add(category.topic);
                    FirebaseMessaging.getInstance().subscribeToTopic(category.topic);
                }
            }
        }

        for (String old : store.knownFcmTopics()) {
            if (!wanted.contains(old)) FirebaseMessaging.getInstance().unsubscribeFromTopic(old);
        }
        store.setKnownFcmTopics(wanted);

        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(store::setFcmToken);
    }
}

package com.bilgilimedia.mobile;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

public final class BilgiliMediaApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setApplicationId(BuildConfig.FIREBASE_APP_ID)
                    .setApiKey(BuildConfig.FIREBASE_API_KEY)
                    .setProjectId(BuildConfig.FIREBASE_PROJECT_ID)
                    .setGcmSenderId(BuildConfig.FIREBASE_SENDER_ID)
                    .setStorageBucket(BuildConfig.FIREBASE_STORAGE_BUCKET)
                    .build();
            FirebaseApp.initializeApp(this, options);
        }
        NotificationHelper.ensureChannel(this);
        ContentCheckJobService.schedule(this);
    }
}

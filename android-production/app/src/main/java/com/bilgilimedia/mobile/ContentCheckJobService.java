package com.bilgilimedia.mobile;

import android.Manifest;
import android.app.job.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.Build;
import java.util.*;

public final class ContentCheckJobService extends JobService {
    private static final int JOB_ID=14801;
    public static void schedule(Context context){JobScheduler s=(JobScheduler)context.getSystemService(Context.JOB_SCHEDULER_SERVICE);if(s==null)return;JobInfo info=new JobInfo.Builder(JOB_ID,new ComponentName(context,ContentCheckJobService.class)).setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY).setPersisted(true).setPeriodic(12*60*60*1000L).build();s.schedule(info);}
    @Override public boolean onStartJob(JobParameters params){new Thread(()->{try{check();}catch(Exception ignored){}jobFinished(params,false);},"BilgiliMediaFallbackCheck").start();return true;}
    @Override public boolean onStopJob(JobParameters params){return true;}
    private void check()throws Exception{
        SharedPreferences p=getSharedPreferences(DataRepository.PREFS,MODE_PRIVATE);if(!p.getBoolean("notify_master",false))return;if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)return;
        DataRepository.Snapshot snap=DataRepository.load(this,true);NotificationSync.saveTopicMap(this,snap.config);NotificationSync.sync(this,snap.config);Set<String>now=new HashSet<>();for(Models.Article a:snap.articles)now.add(a.id);
        if(!p.getBoolean("known_content_initialized",false)){DataRepository.setKnownIds(this,now);return;}
        Set<String>known=DataRepository.getKnownIds(this),selected=new HashSet<>(p.getStringSet("notify_categories",new HashSet<>())),notified=new HashSet<>(p.getStringSet("notified_content_ids",new HashSet<>()));if(selected.isEmpty())return;
        for(Models.Article a:snap.articles){if(known.contains(a.id)||notified.contains(a.id))continue;if(!selected.contains(a.category))continue;NotificationHelper.show(this,a.id,a.category,a.title,a.summary);notified.add(a.id);}
        p.edit().putStringSet("known_content_ids",now).putStringSet("notified_content_ids",notified).apply();
    }
}

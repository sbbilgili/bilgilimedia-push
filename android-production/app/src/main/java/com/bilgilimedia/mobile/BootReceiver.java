package com.bilgilimedia.mobile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public final class BootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context,Intent intent){
        ContentCheckJobService.schedule(context);
        try{
            String raw=context.getSharedPreferences(DataRepository.PREFS,Context.MODE_PRIVATE).getString("app_config_cache","");
            if(raw!=null&&!raw.isEmpty())NotificationSync.sync(context,AppConfig.parse(raw));
        }catch(Exception ignored){}
    }
}

package com.bilgilimedia.mobile;

import android.content.SharedPreferences;
import com.google.firebase.messaging.*;
import java.util.*;

public final class BilgiliFirebaseMessagingService extends FirebaseMessagingService {
    @Override public void onNewToken(String token){super.onNewToken(token);SharedPreferences p=getSharedPreferences(DataRepository.PREFS,MODE_PRIVATE);p.edit().putString("fcm_token",token).apply();try{String raw=p.getString("app_config_cache","");if(raw!=null&&!raw.isEmpty())NotificationSync.sync(this,AppConfig.parse(raw));}catch(Exception ignored){}}
    @Override public void onMessageReceived(RemoteMessage message){
        super.onMessageReceived(message);SharedPreferences p=getSharedPreferences(DataRepository.PREFS,MODE_PRIVATE);if(!p.getBoolean("notify_master",false))return;
        Map<String,String>d=message.getData();String id=v(d.get("article_id")),category=v(d.get("category")),title=v(d.get("title")),body=v(d.get("body"));if(category.isEmpty())category="Genel";
        Set<String>selected=new HashSet<>(p.getStringSet("notify_categories",new HashSet<>()));if(selected.isEmpty()||!selected.contains(category))return;
        Set<String>notified=new HashSet<>(p.getStringSet("notified_content_ids",new HashSet<>()));if(!id.isEmpty()&&notified.contains(id))return;
        if(!id.isEmpty()){notified.add(id);Set<String>known=new HashSet<>(p.getStringSet("known_content_ids",new HashSet<>()));known.add(id);p.edit().putStringSet("notified_content_ids",notified).putStringSet("known_content_ids",known).apply();}
        if(title.isEmpty())title="Yeni BilgiliMedia içeriği";if(body.isEmpty())body="Yeni içerik yayımlandı.";NotificationHelper.show(this,id,category,title,body);
    }
    private static String v(String s){return s==null?"":s;}
}

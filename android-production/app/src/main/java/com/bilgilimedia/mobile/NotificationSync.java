package com.bilgilimedia.mobile;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.firebase.messaging.FirebaseMessaging;
import java.util.*;

final class NotificationSync {
    private NotificationSync(){}
    static void sync(Context context,AppConfig config){
        SharedPreferences p=context.getSharedPreferences(DataRepository.PREFS,Context.MODE_PRIVATE);boolean master=p.getBoolean("notify_master",true);Set<String>selected=new HashSet<>(p.getStringSet("notify_categories",new HashSet<>()));boolean initialized=p.getBoolean("notify_categories_initialized",false);
        if(!initialized){for(Models.Category c:config.categories)selected.add(c.name);p.edit().putStringSet("notify_categories",selected).putBoolean("notify_categories_initialized",true).apply();}
        Set<String>desired=new LinkedHashSet<>();if(master)for(Models.Category c:config.categories)if(selected.contains(c.name))desired.add(c.topic);
        Set<String>known=new HashSet<>(p.getStringSet("known_fcm_topics",new HashSet<>()));for(String old:known)if(!desired.contains(old))FirebaseMessaging.getInstance().unsubscribeFromTopic(old);for(String topic:desired)if(!known.contains(topic))FirebaseMessaging.getInstance().subscribeToTopic(topic);
        p.edit().putStringSet("known_fcm_topics",new HashSet<>(desired)).apply();FirebaseMessaging.getInstance().getToken().addOnSuccessListener(token->p.edit().putString("fcm_token",token).apply());
    }
    static void saveTopicMap(Context context,AppConfig config){org.json.JSONObject map=new org.json.JSONObject();try{for(Map.Entry<String,String>e:config.categoryTopicMap().entrySet())map.put(e.getKey(),e.getValue());}catch(Exception ignored){}context.getSharedPreferences(DataRepository.PREFS,Context.MODE_PRIVATE).edit().putString("category_topic_map",map.toString()).apply();}
}

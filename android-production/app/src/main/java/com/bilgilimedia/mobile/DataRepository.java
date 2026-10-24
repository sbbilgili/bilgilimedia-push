package com.bilgilimedia.mobile;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

final class DataRepository {
    static final String FEED_URL="https://bilgilimedia-60d6f.web.app/feed.json";
    static final String CONFIG_URL="https://bilgilimedia-60d6f.web.app/app-config.json";
    static final String HOST_BASE="https://bilgilimedia-60d6f.web.app";
    static final String PREFS="preferences";
    static final class Snapshot {int version=1;String featuredId="";String feedRaw="",configRaw="";AppConfig config=new AppConfig();final List<Models.Article> articles=new ArrayList<>();}

    static Snapshot load(Context context,boolean forceNetwork)throws Exception{
        String feed=loadRemoteOrFallback(context,FEED_URL,"feed-cache.json","feed.json",forceNetwork);
        String config=loadRemoteOrFallback(context,CONFIG_URL,"config-cache.json","app-config.json",forceNetwork);
        Snapshot s=parseFeed(feed);s.feedRaw=feed;s.configRaw=config;s.config=AppConfig.parse(config);
        context.getSharedPreferences(PREFS,Context.MODE_PRIVATE).edit().putInt("feed_version",s.version).putString("content_cache",feed).putString("app_config_cache",config).putString("config_updated_at",s.config.updatedAt).putLong("content_updated_at",System.currentTimeMillis()).apply();
        return s;
    }
    static Snapshot parseFeed(String raw)throws Exception{
        JSONObject root=new JSONObject(raw);Snapshot s=new Snapshot();s.version=Math.max(1,root.optInt("version",1));s.featuredId=root.optString("featuredId","");
        JSONArray arr=root.optJSONArray("articles");if(arr==null)return s;
        for(int i=0;i<arr.length();i++){JSONObject o=arr.optJSONObject(i);if(o==null)continue;Models.Article a=new Models.Article();a.id=o.optString("id","");a.categoryId=o.optString("categoryId","");a.category=o.optString("category","Genel");a.title=o.optString("title","Yeni içerik");a.summary=o.optString("summary","");a.body=o.optString("body","");a.minutes=Math.max(1,o.optInt("minutes",1));a.imageUrl=o.optString("imageUrl","");a.imageCredit=o.optString("imageCredit","");a.imageSource=o.optString("imageSource","");a.videoUrl=o.optString("videoUrl","");a.videoCredit=o.optString("videoCredit","");a.videoSource=o.optString("videoSource","");JSONArray sources=o.optJSONArray("sources");if(sources!=null)for(int j=0;j<sources.length();j++){JSONObject so=sources.optJSONObject(j);if(so==null)continue;Models.Source src=new Models.Source();src.title=so.optString("title","Kaynak");src.url=so.optString("url","");if(src.url.startsWith("https://"))a.sources.add(src);}if(!a.id.isEmpty()&&!a.title.isEmpty())s.articles.add(a);}
        return s;
    }
    private static String loadRemoteOrFallback(Context context,String url,String cacheName,String assetName,boolean force)throws Exception{
        File cache=new File(context.getFilesDir(),cacheName);if(!force&&cache.exists()&&System.currentTimeMillis()-cache.lastModified()<300000L)return read(new FileInputStream(cache));
        try{String raw=fetch(url);try(FileOutputStream out=new FileOutputStream(cache)){out.write(raw.getBytes(StandardCharsets.UTF_8));}return raw;}catch(Exception e){if(cache.exists())return read(new FileInputStream(cache));SharedPreferences p=context.getSharedPreferences(PREFS,Context.MODE_PRIVATE);String key=assetName.equals("feed.json")?"content_cache":"app_config_cache";String pref=p.getString(key,"");if(pref!=null&&!pref.isEmpty())return pref;return read(context.getAssets().open(assetName));}
    }
    static String fetch(String value)throws Exception{URL u=new URL(value);if(!"https".equalsIgnoreCase(u.getProtocol()))throw new IllegalArgumentException("HTTPS required");HttpURLConnection c=(HttpURLConnection)u.openConnection();c.setConnectTimeout(8000);c.setReadTimeout(10000);c.setInstanceFollowRedirects(true);c.setRequestProperty("User-Agent","BilgiliMedia/1.4.9");int status=c.getResponseCode();if(status<200||status>=300)throw new IllegalStateException("HTTP "+status);if(c.getContentLength()>4000000)throw new IllegalStateException("Response too large");try(InputStream in=c.getInputStream()){return read(in);}finally{c.disconnect();}}
    static String read(InputStream in)throws Exception{BufferedReader r=new BufferedReader(new InputStreamReader(in,StandardCharsets.UTF_8));StringBuilder b=new StringBuilder();String line;while((line=r.readLine())!=null){b.append(line).append('\n');if(b.length()>4000000)throw new IllegalStateException("Input too large");}return b.toString();}
    static Set<String> getKnownIds(Context c){return new HashSet<>(c.getSharedPreferences(PREFS,Context.MODE_PRIVATE).getStringSet("known_content_ids",new HashSet<>()));}
    static void setKnownIds(Context c,Set<String> ids){c.getSharedPreferences(PREFS,Context.MODE_PRIVATE).edit().putBoolean("known_content_initialized",true).putStringSet("known_content_ids",new HashSet<>(ids)).apply();}
}

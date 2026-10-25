package com.bilgilimedia.mobile;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.*;
import java.util.regex.Pattern;

final class AppConfig {
    int schemaVersion=1; String updatedAt="";
    final List<Models.Section> sections=new ArrayList<>();
    final List<Models.Category> categories=new ArrayList<>();
    final List<Models.ExtraPage> extraPages=new ArrayList<>();
    final List<Models.Sponsor> sponsors=new ArrayList<>();
    boolean sponsorEnabled=false;

    static AppConfig parse(String raw)throws Exception{
        JSONObject root=new JSONObject(raw); AppConfig config=new AppConfig();
        config.schemaVersion=root.optInt("schemaVersion",1); config.updatedAt=root.optString("updatedAt","");
        JSONArray sectionArray=root.optJSONArray("sections");
        if(sectionArray!=null)for(int i=0;i<sectionArray.length();i++){JSONObject o=sectionArray.optJSONObject(i);if(o==null||!o.optBoolean("enabled",true))continue;Models.Section s=new Models.Section();s.id=o.optString("id","");s.title=o.optString("title","");s.order=o.optInt("order",i*10);if(!s.id.isEmpty()&&!s.title.isEmpty())config.sections.add(s);}
        JSONArray cats=root.optJSONArray("categories");
        if(cats!=null)for(int i=0;i<cats.length();i++){JSONObject o=cats.optJSONObject(i);if(o==null||!o.optBoolean("enabled",true))continue;Models.Category c=new Models.Category();c.id=o.optString("id",slug(o.optString("name","")));c.name=o.optString("name","");c.topic=o.optString("topic",slug(c.name));c.sectionId=o.optString("sectionId","");c.order=o.optInt("order",i*10);if(!c.name.isEmpty()&&!c.topic.isEmpty())config.categories.add(c);}
        JSONArray pages=root.optJSONArray("extraPages");
        if(pages!=null)for(int i=0;i<pages.length();i++){JSONObject o=pages.optJSONObject(i);if(o==null||!o.optBoolean("enabled",true))continue;Models.ExtraPage p=new Models.ExtraPage();p.id=o.optString("id","page-"+i);p.title=o.optString("title","BilgiliMedia");p.subtitle=o.optString("subtitle","");p.url=o.optString("url","");if(p.url.startsWith("https://bilgilimedia-60d6f.web.app/"))config.extraPages.add(p);}
        JSONObject sponsor=root.optJSONObject("sponsorAds");
        if(sponsor!=null){config.sponsorEnabled=sponsor.optBoolean("enabled",false);JSONArray items=sponsor.optJSONArray("items");if(items!=null)for(int i=0;i<items.length();i++){JSONObject o=items.optJSONObject(i);if(o==null||!o.optBoolean("enabled",true))continue;Models.Sponsor s=new Models.Sponsor();s.id=o.optString("id","sponsor-"+i);s.advertiser=o.optString("advertiser","");s.headline=o.optString("headline","");s.text=o.optString("text","");s.targetUrl=o.optString("targetUrl","");s.placement=o.optString("placement","");if(s.targetUrl.startsWith("https://"))config.sponsors.add(s);}}
        config.sections.sort(Comparator.comparingInt(a->a.order)); config.categories.sort(Comparator.comparingInt(a->a.order)); return config;
    }
    Map<String,String> categoryTopicMap(){Map<String,String>m=new LinkedHashMap<>();for(Models.Category c:categories)m.put(c.name,c.topic);return m;}
    static String slug(String input){if(input==null)return "";String v=input.replace('İ','I').replace('ı','i').replace('Ğ','G').replace('ğ','g').replace('Ü','U').replace('ü','u').replace('Ş','S').replace('ş','s').replace('Ö','O').replace('ö','o').replace('Ç','C').replace('ç','c');return java.text.Normalizer.normalize(v,java.text.Normalizer.Form.NFD).replaceAll("\\p{M}+","").toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_-]+","");}
}
